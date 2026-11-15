-- Upvotes and downvotes replace the green / red dots on cards. Anyone signed in can vote on a
-- seller from any of their listings: one vote per person per listing, so a new listing can be
-- voted on again. Votes stay when the listing is removed or sold, so removing a listing can't
-- wipe its downvotes.

create table if not exists public.votes (
    voter_id   text not null references public.users(uid) on delete cascade,
    item_id    text not null,
    seller_id  text not null references public.users(uid) on delete cascade,
    value      smallint not null check (value in (-1, 1)),
    created_at timestamptz not null default now(),
    primary key (voter_id, item_id)
);
create index if not exists votes_seller_idx on public.votes (seller_id);

alter table public.votes enable row level security;
revoke all on public.votes from anon, authenticated;
grant select on public.votes to authenticated;
-- People only see their own votes (to show which arrow they picked); totals live on users
create policy "Voters can see their own votes" on public.votes
    for select to authenticated using ((select auth.uid())::text = voter_id);

alter table public.users
    add column if not exists upvotes integer not null default 0,
    add column if not exists downvotes integer not null default 0;
grant select (upvotes, downvotes) on public.users to authenticated;

create or replace function public.refresh_votes(p_uid text)
returns void
language sql
security definer
set search_path = ''
as $$
    update public.users u
       set upvotes   = (select count(*) from public.votes v where v.seller_id = p_uid and v.value = 1),
           downvotes = (select count(*) from public.votes v where v.seller_id = p_uid and v.value = -1)
     where u.uid = p_uid;
$$;
revoke all on function public.refresh_votes(text) from public, anon, authenticated;

-- p_value: 1 = upvote, -1 = downvote, 0 = take the vote back
create or replace function public.vote_item(p_item_id text, p_value integer)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid text := auth.uid()::text;
    v_seller text;
begin
    if v_uid is null then raise exception 'Not signed in.'; end if;
    if p_value not in (-1, 0, 1) then raise exception 'Invalid vote.'; end if;

    select seller_id into v_seller from public.items where id = p_item_id;
    if v_seller is null then
        -- Listing gone: an existing vote can still be taken back, nothing new can be cast
        select seller_id into v_seller from public.votes where voter_id = v_uid and item_id = p_item_id;
        if v_seller is null or p_value <> 0 then
            raise exception 'This item is no longer available.';
        end if;
    end if;
    if v_seller = v_uid then raise exception 'You can''t vote on your own listing.'; end if;

    if p_value = 0 then
        delete from public.votes where voter_id = v_uid and item_id = p_item_id;
    else
        insert into public.votes (voter_id, item_id, seller_id, value)
        values (v_uid, p_item_id, v_seller, p_value)
        on conflict (voter_id, item_id) do update set value = excluded.value, created_at = now();
    end if;

    perform public.refresh_votes(v_seller);
end;
$$;
revoke all on function public.vote_item(text, integer) from public, anon;
grant execute on function public.vote_item(text, integer) to authenticated;

-- Deleting an account also takes back that person's votes on others
create or replace function public.purge_user_data(p_uid text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_others text[];
    v_voted text[];
    v_other text;
begin
    v_others := array(
        select distinct case when buyer_id = p_uid then seller_id else buyer_id end
          from public.deals where p_uid in (buyer_id, seller_id)
    );
    v_voted := array(select distinct seller_id from public.votes where voter_id = p_uid);

    update public.items i set status = 'OPEN'
      from public.deals d
     where d.item_id = i.id and d.buyer_id = p_uid
       and d.status = 'ACCEPTED' and i.status = 'LOCKED';

    update public.deals set cancelled_by = null where cancelled_by = p_uid;
    delete from public.deals where p_uid in (buyer_id, seller_id);
    delete from public.items where seller_id = p_uid;
    delete from public.votes where p_uid in (voter_id, seller_id);
    delete from public.users where uid = p_uid;

    foreach v_other in array v_others loop
        perform public.refresh_reputation(v_other);
    end loop;
    foreach v_other in array v_voted loop
        perform public.refresh_votes(v_other);
    end loop;
end;
$$;
revoke all on function public.purge_user_data(text) from public, anon, authenticated;

-- In-app account deletion uses the same cleanup
create or replace function public.delete_my_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid text := auth.uid()::text;
begin
    if v_uid is null then raise exception 'Not signed in.'; end if;
    if exists (select 1 from public.deals
                where status = 'ACCEPTED' and v_uid in (buyer_id, seller_id)) then
        raise exception 'Finish or cancel your accepted deals before deleting your account.';
    end if;
    perform public.purge_user_data(v_uid);
    delete from auth.users where id = auth.uid();
end;
$$;
revoke all on function public.delete_my_account() from public, anon;
grant execute on function public.delete_my_account() to authenticated;
