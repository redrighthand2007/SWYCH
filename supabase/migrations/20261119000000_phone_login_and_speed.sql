-- 1. Phone login moves to the phone-login Edge Function. get_login_email let anyone, even
--    without an account, turn a phone number into that student's email; it is removed.
drop function if exists public.get_login_email(text);

create table if not exists public.phone_login_failures (
    phone      text not null,
    created_at timestamptz not null default now()
);
create index if not exists phone_login_failures_phone_idx on public.phone_login_failures (phone, created_at);
alter table public.phone_login_failures enable row level security;
revoke all on public.phone_login_failures from anon, authenticated;

-- 2. Indexes for the columns deals and items are looked up by
create index if not exists deals_buyer_idx on public.deals (buyer_id);
create index if not exists deals_seller_idx on public.deals (seller_id);
create index if not exists deals_cancelled_by_idx on public.deals (cancelled_by);
create index if not exists items_seller_idx on public.items (seller_id);

-- 3. Same access rules, but "who is logged in" is worked out once per query, not per row
alter policy "Buyers can make offers on open items" on public.deals
    with check (
        (select auth.uid())::text = buyer_id
        and status = 'PENDING' and not buyer_confirmed and not seller_confirmed
        and exists (
            select 1 from public.items i
             where i.id = deals.item_id and i.status = 'OPEN'
               and i.seller_id = deals.seller_id and i.seller_id <> (select auth.uid())::text
        )
    );
alter policy "Participants can dismiss finished deals" on public.deals
    using (
        (select auth.uid())::text in (buyer_id, seller_id)
        and status in ('REJECTED', 'CANCELLED', 'EXPIRED')
    );
alter policy "Participants can view deals" on public.deals
    using ((select auth.uid())::text in (buyer_id, seller_id));
alter policy "Sellers can edit open listings" on public.items
    using ((select auth.uid())::text = seller_id and status = 'OPEN')
    with check ((select auth.uid())::text = seller_id and status = 'OPEN');
alter policy "Sellers can list items" on public.items
    with check ((select auth.uid())::text = seller_id and status = 'OPEN');
alter policy "Sellers can remove open listings" on public.items
    using ((select auth.uid())::text = seller_id and status = 'OPEN');
