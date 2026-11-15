// Log in with phone number + password. The email behind the phone number is looked up and
// used here on the server only, so it never reaches the phone; the app just gets a session.
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;

const MAX_FAILURES = 5;
const WINDOW_MINUTES = 15;
const WRONG_LOGIN = "Wrong email/phone or password.";

const json = (status: number, body: Record<string, unknown>) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

Deno.serve(async (req) => {
  if (req.method !== "POST") return json(405, { error: "Use POST." });

  const body = await req.json().catch(() => ({}));
  const phone = String(body.phone ?? "").replace(/\D/g, "").slice(-10);
  const password = String(body.password ?? "");
  if (phone.length !== 10 || !password) return json(400, { error: "Please enter both phone number and password." });

  const admin = createClient(SUPABASE_URL, SERVICE_KEY, { auth: { persistSession: false } });

  // Slow down password guessing against one number
  const since = new Date(Date.now() - WINDOW_MINUTES * 60 * 1000).toISOString();
  const { count } = await admin.from("phone_login_failures")
    .select("phone", { count: "exact", head: true })
    .eq("phone", phone)
    .gte("created_at", since);
  if ((count ?? 0) >= MAX_FAILURES) {
    return json(429, { error: "Too many wrong attempts. Please wait 15 minutes and try again." });
  }

  const { data: user } = await admin.from("users").select("email").eq("phone", phone).maybeSingle();
  const email = user?.email as string | undefined;

  const failed = async () => {
    await admin.from("phone_login_failures").insert({ phone });
    // Same answer whether or not the number exists, so numbers can't be checked one by one
    return json(401, { error: WRONG_LOGIN });
  };
  if (!email) return await failed();

  const client = createClient(SUPABASE_URL, ANON_KEY, { auth: { persistSession: false } });
  const { data, error } = await client.auth.signInWithPassword({ email, password });
  if (error || !data.session) {
    const code = (error as { code?: string } | null)?.code;
    if (code === "email_not_confirmed") {
      return json(403, { error: "Your email isn't confirmed yet. Open the link we emailed you, then log in." });
    }
    if (code === "invalid_credentials" || !data?.session) return await failed();
    return json(502, { error: "Something went wrong on our side. Please try again in a minute." });
  }

  await admin.from("phone_login_failures").delete().eq("phone", phone);
  const s = data.session;
  return json(200, {
    access_token: s.access_token,
    refresh_token: s.refresh_token,
    expires_in: s.expires_in,
    token_type: s.token_type,
  });
});
