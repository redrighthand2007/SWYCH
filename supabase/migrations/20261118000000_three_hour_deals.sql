-- Accepted deals now expire after 3 hours instead of 3 days, and phone numbers are only
-- shared while a deal is being arranged (ACCEPTED), not after it is completed.

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
         where status = 'ACCEPTED' and accepted_at < now() - interval '3 hours'
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
       and d.status = 'ACCEPTED';
$$;

-- With a 3-hour window an hourly check is too coarse; run it every 10 minutes
select cron.unschedule('expire-stale-deals')
 where exists (select 1 from cron.job where jobname = 'expire-stale-deals');
select cron.schedule('expire-stale-deals', '*/10 * * * *', 'select public.expire_stale_deals()');
