# ðŸ¤ Contributing to Swych

First off, thank you for considering contributing to Swych! It's people like you that make this app a great tool for the VIT campus community.

## ðŸ“‹ Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Convention](#commit-convention)
- [Pull Request Process](#pull-request-process)

## ðŸ“œ Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## ðŸš€ Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/swych.git`
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Commit your changes (see [Commit Convention](#commit-convention))
6. Push to the branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

## ðŸ› ï¸ Development Setup

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35
- A Supabase project with Phone Auth, Postgres, and Storage enabled

### Setup Steps

1. Clone the repository
2. Open the project in Android Studio
3. Copy your `google-services.json` to the `app/` directory
4. Sync Gradle and build the project
5. Run on an emulator or physical device

## ðŸ’¡ How to Contribute

### ðŸ› Reporting Bugs

- Use the [Bug Report](https://github.com/YOUR_USERNAME/swych/issues/new?template=bug_report.md) issue template
- Include detailed steps to reproduce
- Add screenshots if applicable
- Specify your device and Android version

### âœ¨ Suggesting Features

- Use the [Feature Request](https://github.com/YOUR_USERNAME/swych/issues/new?template=feature_request.md) issue template
- Explain the problem your feature solves
- Provide mockups if possible

### ðŸ”§ Submitting Code

1. Check existing issues and PRs to avoid duplicates
2. For major changes, open an issue first to discuss
3. Write clean, well-documented code
4. Add tests for new features
5. Update documentation as needed

## ðŸ“ Coding Standards

### Kotlin Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Use `sealed interface` for UI states
- Use `data class` for models

### Architecture

- Follow MVVM + Clean Architecture
- Keep UI logic in ViewModels
- Business logic goes in Use Cases
- Data access through Repositories
- Use Hilt for dependency injection

### Compose Guidelines

- Keep composables small and reusable
- Use `remember` and `derivedStateOf` appropriately
- Follow the slot API pattern for flexible components
- Use Material 3 components and theme tokens

## ðŸ“ Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation changes |
| `style` | Code style changes (formatting, no logic change) |
| `refactor` | Code refactoring |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks |
| `perf` | Performance improvements |
| `ci` | CI/CD changes |

### Examples

```
feat(auth): add phone OTP verification flow
fix(offers): resolve batch write race condition on accept
docs(readme): update setup instructions
style(theme): adjust color palette for dark mode
```

## ðŸ”„ Pull Request Process

1. Update the README.md with details of changes if applicable
2. Update the CHANGELOG.md with a note about your change
3. Ensure all tests pass
4. Request review from at least one maintainer
5. PRs require approval before merging
6. Squash and merge is preferred for feature branches

## ðŸ·ï¸ Branch Naming

```
feature/short-description    # New features
fix/short-description        # Bug fixes
docs/short-description       # Documentation
refactor/short-description   # Code refactoring
```

---

## â“ Questions?

Feel free to open an issue with the `question` label or reach out to the maintainers.

Thank you for contributing! ðŸŽ‰

