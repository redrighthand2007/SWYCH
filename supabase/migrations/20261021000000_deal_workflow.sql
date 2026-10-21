-- Swych: deal workflow, integrity checks and access rules.
--
-- Deal lifecycle:
--   PENDING   buyer made an offer, item still OPEN
--   ACCEPTED  seller accepted, item LOCKED, other offers REJECTED
--   COMPLETED both sides confirmed the hand-over, item SOLD
--   REJECTED  seller turned the offer down (or accepted someone else)
--   CANCELLED a side backed out after acceptance, item back to OPEN
--   EXPIRED   accepted deal not confirmed within 3 days, item back to OPEN
--
-- Every status change goes through the SECURITY DEFINER functions below,
-- so clients can no longer write deal or item status directly.

begin;

-- ---------------------------------------------------------------------------
-- Columns and data
-- ---------------------------------------------------------------------------

alter table public.items
    add column if not exists created_at timestamptz not null default now();

alter table public.deals
    add column if not exists accepted_at timestamptz,
    add column if not exists buyer_confirmed boolean not null default false,
    add column if not exists seller_confirmed boolean not null default false,
    add column if not exists cancelled_by text references public.users(uid);

alter table public.deals alter column status set default 'PENDING';
alter table public.deals alter column status set not null;
alter table public.items alter column status set not null;

-- Old flow marked accepted deals SOLD straight away; treat them as completed.
update public.deals
   set status = 'COMPLETED', buyer_confirmed = true, seller_confirmed = true
 where status = 'SOLD';
update public.deals set status = 'PENDING' where status in ('LOCKED', 'OPEN');

alter table public.deals
    add constraint deals_status_check
        check (status in ('PENDING', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'CANCELLED', 'EXPIRED')),
    add constraint deals_not_self check (buyer_id <> seller_id),
    add constraint deals_price_positive check (final_price > 0);

alter table public.items
    add constraint items_status_check check (status in ('OPEN', 'LOCKED', 'SOLD')),
    add constraint items_price_positive check (price > 0);

-- One live offer per buyer per item.
create unique index if not exists deals_one_live_offer
    on public.deals (item_id, buyer_id)
    where status in ('PENDING', 'ACCEPTED');

-- Only one accepted deal per item at a time.
create unique index if not exists deals_one_accepted_per_item
    on public.deals (item_id)
    where status = 'ACCEPTED';

-- Phone numbers and emails identify a single account.
create unique index if not exists users_phone_unique on public.users (phone);
create unique index if not exists users_email_unique on public.users (lower(email));

-- Removing a listing removes its offers with it.
alter table public.deals drop constraint deals_item_id_fkey;
alter table public.deals
    add constraint deals_item_id_fkey
        foreign key (item_id) references public.items(id) on delete cascade;

-- ---------------------------------------------------------------------------
-- Reputation (green = completed deals, red = deals you cancelled)
-- ---------------------------------------------------------------------------

create or replace function public.refresh_reputation(p_uid text)
returns void
language sql
security definer
set search_path = ''
as $$
    update public.users u
       set green_dots = (select count(*) from public.deals d
                          where d.status = 'COMPLETED'
                            and (d.buyer_id = p_uid or d.seller_id = p_uid)),
           red_dots   = (select count(*) from public.deals d
                          where d.status = 'CANCELLED' and d.cancelled_by = p_uid)
     where u.uid = p_uid;
$$;
revoke all on function public.refresh_reputation(text) from public, anon, authenticated;

select public.refresh_reputation(uid) from public.users;

-- ---------------------------------------------------------------------------
-- Deal actions
-- ---------------------------------------------------------------------------

create or replace function public.accept_deal(p_deal_id text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid  text := auth.uid()::text;
    v_deal public.deals;
    v_item public.items;
begin
    select * into v_deal from public.deals where id = p_deal_id for update;
    if not found then raise exception 'This offer no longer exists.'; end if;
    if v_deal.seller_id is distinct from v_uid then raise exception 'Only the seller can accept this offer.'; end if;
    if v_deal.status <> 'PENDING' then raise exception 'This offer is no longer pending.'; end if;

    select * into v_item from public.items where id = v_deal.item_id for update;
    if v_item.status <> 'OPEN' then raise exception 'You have already accepted another offer for this item.'; end if;

    update public.deals set status = 'ACCEPTED', accepted_at = now() where id = p_deal_id;
    update public.items set status = 'LOCKED' where id = v_deal.item_id;
    update public.deals set status = 'REJECTED'
     where item_id = v_deal.item_id and id <> p_deal_id and status = 'PENDING';
end;
$$;

create or replace function public.reject_deal(p_deal_id text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_deal public.deals;
begin
    select * into v_deal from public.deals where id = p_deal_id for update;
    if not found then raise exception 'This offer no longer exists.'; end if;
    if v_deal.seller_id is distinct from auth.uid()::text then raise exception 'Only the seller can reject this offer.'; end if;
    if v_deal.status <> 'PENDING' then raise exception 'This offer is no longer pending.'; end if;

    update public.deals set status = 'REJECTED' where id = p_deal_id;
end;
$$;

-- Buyer withdrawing a pending offer deletes it; either side cancelling an
-- accepted deal reopens the item and counts against whoever cancelled.
create or replace function public.cancel_deal(p_deal_id text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid  text := auth.uid()::text;
    v_deal public.deals;
begin
    select * into v_deal from public.deals where id = p_deal_id for update;
    if not found then raise exception 'This deal no longer exists.'; end if;
    if v_uid is null or v_uid not in (v_deal.buyer_id, v_deal.seller_id) then
        raise exception 'You are not part of this deal.';
    end if;

    if v_deal.status = 'PENDING' then
        if v_uid <> v_deal.buyer_id then raise exception 'Use Reject to turn down an offer.'; end if;
        delete from public.deals where id = p_deal_id;
    elsif v_deal.status = 'ACCEPTED' then
        update public.deals set status = 'CANCELLED', cancelled_by = v_uid where id = p_deal_id;
        update public.items set status = 'OPEN' where id = v_deal.item_id and status = 'LOCKED';
        perform public.refresh_reputation(v_uid);
    else
        raise exception 'This deal can no longer be cancelled.';
    end if;
end;
$$;

-- Each side confirms the hand-over; the deal completes once both have.
create or replace function public.confirm_deal(p_deal_id text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid  text := auth.uid()::text;
    v_deal public.deals;
begin
    select * into v_deal from public.deals where id = p_deal_id for update;
    if not found then raise exception 'This deal no longer exists.'; end if;
    if v_deal.status <> 'ACCEPTED' then raise exception 'Only accepted deals can be completed.'; end if;

    if v_uid = v_deal.buyer_id then
        update public.deals set buyer_confirmed = true where id = p_deal_id returning * into v_deal;
    elsif v_uid = v_deal.seller_id then
        update public.deals set seller_confirmed = true where id = p_deal_id returning * into v_deal;
    else
        raise exception 'You are not part of this deal.';
    end if;

    if v_deal.buyer_confirmed and v_deal.seller_confirmed then
        update public.deals set status = 'COMPLETED' where id = p_deal_id;
        update public.items set status = 'SOLD' where id = v_deal.item_id;
        perform public.refresh_reputation(v_deal.buyer_id);
        perform public.refresh_reputation(v_deal.seller_id);
    end if;
end;
$$;

-- Accepted deals older than 3 days: complete them if either side confirmed,
-- otherwise expire them and reopen the item. Called by the app on refresh.
create or replace function public.expire_stale_deals()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_deal public.deals;
begin
    for v_deal in
        select * from public.deals
         where status = 'ACCEPTED' and accepted_at < now() - interval '3 days'
         for update skip locked
    loop
        if v_deal.buyer_confirmed or v_deal.seller_confirmed then
            update public.deals set status = 'COMPLETED' where id = v_deal.id;
            update public.items set status = 'SOLD' where id = v_deal.item_id;
            perform public.refresh_reputation(v_deal.buyer_id);
            perform public.refresh_reputation(v_deal.seller_id);
        else
            update public.deals set status = 'EXPIRED' where id = v_deal.id;
            update public.items set status = 'OPEN' where id = v_deal.item_id and status = 'LOCKED';
        end if;
    end loop;
end;
$$;

-- Phone number of the other side, only once a deal is accepted or completed.
create or replace function public.get_deal_contact(p_deal_id text)
returns text
language sql
stable
security definer
set search_path = ''
as $$
    select u.phone
      from public.deals d
      join public.users u
        on u.uid = case when d.buyer_id = auth.uid()::text then d.seller_id else d.buyer_id end
     where d.id = p_deal_id
       and auth.uid()::text in (d.buyer_id, d.seller_id)
       and d.status in ('ACCEPTED', 'COMPLETED');
$$;

-- Own full profile (phone and email are hidden from other users).
create or replace function public.get_my_profile()
returns setof public.users
language sql
stable
security definer
set search_path = ''
as $$
    select * from public.users where uid = auth.uid()::text;
$$;

-- Login by phone: returns only the matching email, nothing else.
create or replace function public.get_login_email(p_phone text)
returns text
language sql
stable
security definer
set search_path = ''
as $$
    select email from public.users where phone = p_phone;
$$;

revoke all on function public.accept_deal(text), public.reject_deal(text), public.cancel_deal(text),
    public.confirm_deal(text), public.expire_stale_deals(), public.get_deal_contact(text),
    public.get_my_profile(), public.get_login_email(text) from public, anon;
grant execute on function public.accept_deal(text), public.reject_deal(text), public.cancel_deal(text),
    public.confirm_deal(text), public.expire_stale_deals(), public.get_deal_contact(text),
    public.get_my_profile() to authenticated;
grant execute on function public.get_login_email(text) to anon, authenticated;

-- ---------------------------------------------------------------------------
-- Profile is created from sign-up metadata, in the same transaction as the
-- auth account, so a failed profile insert also fails the sign-up.
-- ---------------------------------------------------------------------------

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.users (uid, name, block, phone, email)
    values (
        new.id::text,
        coalesce(new.raw_user_meta_data ->> 'name', ''),
        coalesce(new.raw_user_meta_data ->> 'block', ''),
        coalesce(new.raw_user_meta_data ->> 'phone', ''),
        lower(new.email)
    );
    return new;
end;
$$;

-- Trigger-only function; not part of the API.
revoke all on function public.handle_new_user() from public, anon, authenticated;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------------
-- Row level security
-- ---------------------------------------------------------------------------

-- users: signed-in users can see names, blocks and reputation; phone and
-- email only through the functions above.
drop policy if exists "Public profiles are viewable by everyone" on public.users;
drop policy if exists "Users can insert their own profile" on public.users;
drop policy if exists "Users can update own profile" on public.users;
create policy "Signed-in users can view profiles" on public.users
    for select to authenticated using (true);
revoke select, insert, update, delete on public.users from anon, authenticated;
grant select (uid, name, block, green_dots, red_dots) on public.users to authenticated;

-- items: anyone signed in can browse; sellers manage their own OPEN listings.
drop policy if exists "Items are viewable by everyone" on public.items;
drop policy if exists "Users can insert own items" on public.items;
drop policy if exists "Users can update own items" on public.items;
drop policy if exists "Users can delete own items" on public.items;
create policy "Signed-in users can view items" on public.items
    for select to authenticated using (true);
create policy "Sellers can list items" on public.items
    for insert to authenticated
    with check (auth.uid()::text = seller_id and status = 'OPEN');
create policy "Sellers can edit open listings" on public.items
    for update to authenticated
    using (auth.uid()::text = seller_id and status = 'OPEN')
    with check (auth.uid()::text = seller_id and status = 'OPEN');
create policy "Sellers can remove open listings" on public.items
    for delete to authenticated
    using (auth.uid()::text = seller_id and status = 'OPEN');

-- deals: only the two sides see a deal; buyers create offers on OPEN items;
-- status changes only through the functions; finished deals can be dismissed.
drop policy if exists "Deals are viewable by everyone" on public.deals;
drop policy if exists "Buyers can insert deals" on public.deals;
drop policy if exists "Buyers and sellers can update deals" on public.deals;
drop policy if exists "Buyers and sellers can delete deals" on public.deals;
create policy "Participants can view deals" on public.deals
    for select to authenticated
    using (auth.uid()::text in (buyer_id, seller_id));
create policy "Buyers can make offers on open items" on public.deals
    for insert to authenticated
    with check (
        auth.uid()::text = buyer_id
        and status = 'PENDING'
        and not buyer_confirmed and not seller_confirmed
        and exists (
            select 1 from public.items i
             where i.id = item_id
               and i.status = 'OPEN'
               and i.seller_id = deals.seller_id
               and i.seller_id <> auth.uid()::text
        )
    );
create policy "Participants can dismiss finished deals" on public.deals
    for delete to authenticated
    using (
        auth.uid()::text in (buyer_id, seller_id)
        and status in ('REJECTED', 'CANCELLED', 'EXPIRED')
    );
revoke update on public.deals from anon, authenticated;

commit;
