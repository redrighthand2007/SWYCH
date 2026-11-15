-- Swych: base tables, photo bucket and row level security switched on.
-- Later migrations add the deal workflow, constraints, functions and access rules.

create table if not exists public.users (
    uid        text primary key,
    name       text not null,
    block      text not null,
    phone      text not null,
    email      text not null,
    green_dots integer default 0,
    red_dots   integer default 0
);

create table if not exists public.items (
    id          text primary key,
    seller_id   text not null references public.users(uid),
    category    text not null,
    title       text not null,
    description text,
    price       real not null,
    photo_url   text,
    status      text default 'OPEN'
);

create table if not exists public.deals (
    id             text primary key,
    item_id        text not null references public.items(id),
    buyer_id       text not null references public.users(uid),
    seller_id      text not null references public.users(uid),
    item_title     text not null,
    item_photo_url text,
    final_price    real not null,
    status         text default 'LOCKED',
    "timestamp"    bigint not null
);

alter table public.users enable row level security;
alter table public.items enable row level security;
alter table public.deals enable row level security;

-- Public bucket: listing photos are shown to every signed-in student by URL
insert into storage.buckets (id, name, public)
values ('items', 'items', true)
on conflict (id) do nothing;
