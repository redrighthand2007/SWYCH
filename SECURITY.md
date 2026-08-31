# Security Policy

## Supported Versions

| Version | Status |
|---------|--------|
| 1.x | ✅ Actively supported |
| < 1.0 | ❌ No longer supported |

---

## Reporting a Vulnerability

If you find a security issue in Swych, please report it responsibly.

**Do not** create a public GitHub issue for security vulnerabilities. This puts other users at risk before a fix is available.

### How to Report

**Option 1 — GitHub Private Advisory (preferred)**
Go to the [Security tab](https://github.com/redrighthand2007/Swych-App/security/advisories/new) of this repo and open a private security advisory.

**Option 2 — Direct contact**
Reach out to the maintainer directly via GitHub.

### What to include
- A clear description of the vulnerability
- Steps to reproduce it
- What data or users could be affected
- A suggested fix, if you have one

---

## Response Timeline

| Step | Time |
|------|------|
| Acknowledgment | Within 48 hours |
| Initial assessment | Within 7 days |
| Fix & coordinated disclosure | As soon as possible |

---

## Current Security Practices

- All data is stored in **Supabase PostgreSQL** — no sensitive data is stored locally on the device
- User sessions are stored as a UID in SharedPreferences — no passwords or tokens saved in plain text
- All network calls go through **HTTPS only**
- Input validation on all user-facing forms before any data reaches the database
- Photo uploads go through **Cloudinary** — no raw files stored on our servers

---

## Responsible Disclosure

We deeply appreciate researchers who report vulnerabilities privately. Valid reports will be acknowledged and credited (with your permission).

Thank you for helping keep Swych safe for students. 🔒
