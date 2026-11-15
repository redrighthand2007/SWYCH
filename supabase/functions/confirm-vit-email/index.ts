// Opened from the link in the VIT verification email (no login needed). Marks the account
// as VIT-verified and redirects to a small result page on GitHub Pages.
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const RESULT_PAGE = "https://redrighthand2007.github.io/SWYCH/vit-verified.html";

const redirect = (status: "ok" | "expired" | "taken") =>
  new Response(null, { status: 302, headers: { Location: `${RESULT_PAGE}?status=${status}` } });

async function sha256Hex(text: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
  return [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

Deno.serve(async (req) => {
  const token = new URL(req.url).searchParams.get("token") ?? "";
  if (!/^[0-9a-f]{64}$/.test(token)) return redirect("expired");

  const admin = createClient(SUPABASE_URL, SERVICE_KEY);
  const { data: pending } = await admin.from("vit_verifications")
    .select("user_id, vit_email, expires_at")
    .eq("token_hash", await sha256Hex(token))
    .maybeSingle();
  if (!pending || new Date(pending.expires_at).getTime() < Date.now()) return redirect("expired");

  // The unique index on vit_email rejects a VIT email that already verifies another account
  const { error } = await admin.from("users")
    .update({ vit_verified: true, vit_email: pending.vit_email })
    .eq("uid", pending.user_id);
  if (error) return redirect("taken");

  await admin.from("vit_verifications").delete().eq("user_id", pending.user_id);
  return redirect("ok");
});
