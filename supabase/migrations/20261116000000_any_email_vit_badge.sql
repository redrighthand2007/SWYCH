-- Swych: anyone can sign up with any email; VIT students get a verified badge.
-- A user is VIT-verified when they sign up with an @vitstudent.ac.in email, or later
-- confirm one from Profile (the vit-verification Edge Functions handle that link).

-- ---------------------------------------------------------------------------
-- Verified badge on profiles
-- ---------------------------------------------------------------------------

alter table public.users
    add column if not exists vit_verified boolean not null default false,
    add column if not exists vit_email text;

-- One VIT email can verify only one account
create unique index if not exists users_vit_email_unique
    on public.users (lower(vit_email)) where vit_email is not null;

-- Everyone who already signed up did so with a VIT email
update public.users
   set vit_verified = true, vit_email = lower(email)
 where lower(email) like '%@vitstudent.ac.in';

-- The badge is public; the VIT email itself stays private (own profile only)
grant select (vit_verified) on public.users to authenticated;

-- ---------------------------------------------------------------------------
-- Pending verification links (only the Edge Functions read or write these)
-- ---------------------------------------------------------------------------

create table if not exists public.vit_verifications (
    id         uuid primary key default gen_random_uuid(),
    user_id    text not null references public.users(uid) on delete cascade,
    vit_email  text not null,
    token_hash text not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null default now()
);
create index if not exists vit_verifications_user_idx on public.vit_verifications (user_id, created_at);
alter table public.vit_verifications enable row level security;
revoke all on public.vit_verifications from anon, authenticated;

-- ---------------------------------------------------------------------------
-- Sign-up: VIT emails are verified straight away
-- ---------------------------------------------------------------------------

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_is_vit boolean := lower(new.email) like '%@vitstudent.ac.in';
begin
    insert into public.users (uid, name, block, phone, email, vit_verified, vit_email)
    values (
        new.id::text,
        coalesce(new.raw_user_meta_data ->> 'name', ''),
        coalesce(new.raw_user_meta_data ->> 'block', ''),
        coalesce(new.raw_user_meta_data ->> 'phone', ''),
        lower(new.email),
        v_is_vit,
        case when v_is_vit then lower(new.email) end
    );
    return new;
end;
$$;
revoke all on function public.handle_new_user() from public, anon, authenticated;

-- ---------------------------------------------------------------------------
-- Allow any email again (removes the VIT-only rule on auth.users)
-- ---------------------------------------------------------------------------

drop trigger if exists enforce_college_email on auth.users;
drop function if exists public.enforce_college_email();
