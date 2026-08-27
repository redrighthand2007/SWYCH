# 🔒 Security Policy

## Supported Versions

| Version | Supported          |
|---------| ------------------ |
| 1.x.x   | ✅ Active support  |
| < 1.0   | ❌ Not supported   |

## Reporting a Vulnerability

We take the security of CacheDeal seriously. If you discover a security vulnerability, please report it responsibly.

### 📧 How to Report

1. **DO NOT** create a public GitHub issue for security vulnerabilities
2. Email us at: **security@cachedeal.com** (or open a private security advisory on GitHub)
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### ⏱️ Response Timeline

- **Acknowledgment:** Within 48 hours
- **Initial Assessment:** Within 1 week
- **Fix & Disclosure:** Coordinated with reporter

### 🛡️ Security Best Practices

This project follows these security practices:

- Firebase Security Rules for Firestore and Storage
- Phone number OTP verification for user authentication
- No sensitive data stored locally in plain text
- Input validation on all user-facing forms
- Dependency scanning via GitHub Dependabot

## 🙏 Acknowledgments

We appreciate responsible disclosure and will acknowledge security researchers who report valid vulnerabilities.
