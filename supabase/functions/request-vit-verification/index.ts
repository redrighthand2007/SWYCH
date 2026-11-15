// Sends a one-time link to a student's @vitstudent.ac.in address. Opening it (handled by
// confirm-vit-email) marks the signed-in account as VIT-verified. The login email is unchanged.
import { createClient } from "jsr:@supabase/supabase-js@2";
import nodemailer from "npm:nodemailer@6.9.16";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
// Gmail address and app password, set as Edge Function secrets
const SMTP_USER = Deno.env.get("SMTP_USER");
const SMTP_PASS = Deno.env.get("SMTP_PASS");

const VIT_EMAIL = /^[a-z0-9._%+-]+@vitstudent\.ac\.in$/;
const LINK_MINUTES = 30;
const MAX_REQUESTS_PER_HOUR = 3;

const json = (status: number, body: Record<string, unknown>) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

async function sha256Hex(text: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
  return [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return json(405, { error: "Use POST." });

  const admin = createClient(SUPABASE_URL, SERVICE_KEY);
  const jwt = (req.headers.get("Authorization") ?? "").replace(/^Bearer\s+/i, "");
  const { data: auth } = await admin.auth.getUser(jwt);
  const uid = auth?.user?.id;
  if (!uid) return json(401, { error: "Please log in again." });

  const body = await req.json().catch(() => ({}));
  const email = String(body.email ?? "").trim().toLowerCase();
  if (!VIT_EMAIL.test(email)) return json(400, { error: "Enter your @vitstudent.ac.in email." });
  if (!SMTP_USER || !SMTP_PASS) {
    return json(503, { error: "VIT verification isn't available yet. Please try again later." });
  }

  const { data: me } = await admin.from("users").select("vit_verified").eq("uid", uid).maybeSingle();
  if (!me) return json(404, { error: "Profile not found." });
  if (me.vit_verified) return json(409, { error: "Your account is already VIT-verified." });

  const { data: taken } = await admin.from("users").select("uid").eq("vit_email", email).neq("uid", uid).maybeSingle();
  if (taken) return json(409, { error: "This VIT email already verifies another account." });

  const since = new Date(Date.now() - 60 * 60 * 1000).toISOString();
  const { count } = await admin.from("vit_verifications")
    .select("id", { count: "exact", head: true })
    .eq("user_id", uid)
    .gte("created_at", since);
  if ((count ?? 0) >= MAX_REQUESTS_PER_HOUR) {
    return json(429, { error: "Too many attempts. Please try again in an hour." });
  }

  const token = [...crypto.getRandomValues(new Uint8Array(32))].map((b) => b.toString(16).padStart(2, "0")).join("");
  const { data: row, error: insertError } = await admin.from("vit_verifications").insert({
    user_id: uid,
    vit_email: email,
    token_hash: await sha256Hex(token),
    expires_at: new Date(Date.now() + LINK_MINUTES * 60 * 1000).toISOString(),
  }).select("id").single();
  if (insertError || !row) return json(500, { error: "Something went wrong. Please try again." });

  const link = `${SUPABASE_URL}/functions/v1/confirm-vit-email?token=${token}`;
  try {
    const mailer = nodemailer.createTransport({
      host: "smtp.gmail.com",
      port: 465, // Edge Functions can't use port 587
      secure: true,
      auth: { user: SMTP_USER, pass: SMTP_PASS },
    });
    await mailer.sendMail({
      from: `"Swych" <${SMTP_USER}>`,
      to: email,
      subject: "Verify you're a VIT student on Swych",
      text: `Open this link to get the verified VIT badge on Swych:\n\n${link}\n\nThe link expires in ${LINK_MINUTES} minutes. If you didn't ask for this, ignore this email.`,
      html: `<h2>Get your VIT badge on Swych</h2>
<p>Tap the button to confirm this is your VIT student email. A golden <b>V</b> will appear next to your name.</p>
<p><a href="${link}" style="background:#ea580c;color:#fff;padding:12px 20px;border-radius:10px;text-decoration:none;font-weight:600">Verify my VIT email</a></p>
<p style="color:#777">The link expires in ${LINK_MINUTES} minutes. If you didn't ask for this, ignore this email.</p>`,
    });
  } catch (_) {
    await admin.from("vit_verifications").delete().eq("id", row.id);
    return json(502, { error: "Couldn't send the email. Please try again." });
  }

  return json(200, { sentTo: email });
});
