-- Swych: scheduled deal expiry, photo storage rules and account deletion.

-- ---------------------------------------------------------------------------
-- Expire stale accepted deals every hour, not only when someone opens the app
-- ---------------------------------------------------------------------------

create extension if not exists pg_cron;

select cron.unschedule(jobid) from cron.job where jobname = 'expire-stale-deals';
select cron.schedule('expire-stale-deals', '0 * * * *', 'select public.expire_stale_deals()');

-- ---------------------------------------------------------------------------
-- Item photos: only signed-in users upload, into names prefixed with their id,
-- and only the uploader can replace or delete a photo. Reads stay public
-- through the bucket's public URLs.
-- ---------------------------------------------------------------------------

drop policy if exists "App Uploads" on storage.objects;
drop policy if exists "App Updates" on storage.objects;
drop policy if exists "Public Access" on storage.objects;

create policy "Signed-in users upload their own item photos" on storage.objects
    for insert to authenticated
    with check (bucket_id = 'items' and starts_with(name, auth.uid()::text || '_'));
create policy "Uploaders can read their item photos" on storage.objects
    for select to authenticated
    using (bucket_id = 'items' and owner = auth.uid());
create policy "Uploaders can replace their item photos" on storage.objects
    for update to authenticated
    using (bucket_id = 'items' and owner = auth.uid());
create policy "Uploaders can delete their item photos" on storage.objects
    for delete to authenticated
    using (bucket_id = 'items' and owner = auth.uid());

-- ---------------------------------------------------------------------------
-- In-app account deletion (required by Google Play for apps with sign-up)
-- ---------------------------------------------------------------------------

create or replace function public.delete_my_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_uid text := auth.uid()::text;
    v_other text;
    v_others text[];
begin
    if v_uid is null then raise exception 'Not signed in.'; end if;
    if exists (select 1 from public.deals
                where status = 'ACCEPTED' and v_uid in (buyer_id, seller_id)) then
        raise exception 'Finish or cancel your accepted deals before deleting your account.';
    end if;

    -- Other people whose reputation counts include deals with this user
    v_others := array(
        select distinct case when buyer_id = v_uid then seller_id else buyer_id end
          from public.deals where v_uid in (buyer_id, seller_id)
    );

    update public.deals set cancelled_by = null where cancelled_by = v_uid;
    delete from public.deals where v_uid in (buyer_id, seller_id);
    delete from public.items where seller_id = v_uid;
    delete from public.users where uid = v_uid;
    delete from auth.users where id = auth.uid();

    foreach v_other in array v_others loop
        perform public.refresh_reputation(v_other);
    end loop;
end;
$$;

revoke all on function public.delete_my_account() from public, anon;
grant execute on function public.delete_my_account() to authenticated;
