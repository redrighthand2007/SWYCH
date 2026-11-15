-- Deleting a user from Authentication -> Users in the dashboard only removed the login.
-- Their profile (with its phone number), listings and deals stayed behind, so the phone
-- number could never be used again. Now the same cleanup runs however the user is deleted.

create or replace function public.purge_user_data(p_uid text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_others text[];
    v_other text;
begin
    -- Other people whose reputation counts include deals with this user
    v_others := array(
        select distinct case when buyer_id = p_uid then seller_id else buyer_id end
          from public.deals where p_uid in (buyer_id, seller_id)
    );

    -- Items of other sellers that were reserved for this user go back on sale
    update public.items i set status = 'OPEN'
      from public.deals d
     where d.item_id = i.id and d.buyer_id = p_uid
       and d.status = 'ACCEPTED' and i.status = 'LOCKED';

    update public.deals set cancelled_by = null where cancelled_by = p_uid;
    delete from public.deals where p_uid in (buyer_id, seller_id);
    delete from public.items where seller_id = p_uid;
    delete from public.users where uid = p_uid;

    foreach v_other in array v_others loop
        perform public.refresh_reputation(v_other);
    end loop;
end;
$$;

revoke all on function public.purge_user_data(text) from public, anon, authenticated;

create or replace function public.handle_deleted_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    perform public.purge_user_data(old.id::text);
    return old;
end;
$$;

revoke all on function public.handle_deleted_user() from public, anon, authenticated;

drop trigger if exists on_auth_user_deleted on auth.users;
create trigger on_auth_user_deleted
    after delete on auth.users
    for each row execute function public.handle_deleted_user();

-- Clean up profiles whose login was already deleted before this trigger existed
do $$
declare
    v_uid text;
begin
    for v_uid in
        select u.uid from public.users u
         where not exists (select 1 from auth.users a where a.id::text = u.uid)
    loop
        perform public.purge_user_data(v_uid);
    end loop;
end;
$$;
