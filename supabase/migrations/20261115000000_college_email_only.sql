-- Swych: only VIT student emails can hold an account.
-- The app already checks this on the sign-up screen; this enforces it on the server too,
-- so accounts can't be created (or emails changed) by calling Supabase directly.

create or replace function public.enforce_college_email()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    if new.email is null or lower(new.email) not like '%@vitstudent.ac.in' then
        raise exception 'Only @vitstudent.ac.in email addresses can use Swych.';
    end if;
    return new;
end;
$$;

-- Trigger-only function; not part of the API.
revoke all on function public.enforce_college_email() from public, anon, authenticated;

drop trigger if exists enforce_college_email on auth.users;
create trigger enforce_college_email
    before insert or update of email on auth.users
    for each row execute function public.enforce_college_email();
