Thank you for considering contributing to smpCurrency!

This project is small and reviewable — small, focused PRs are easiest to land. The sections below explain the recommended workflow, development setup, testing, and what we expect in a pull request.

Getting started

1. Fork the repository and create a feature branch from `main`:

   git checkout -b feat/my-feature

2. Make changes on your branch. Keep each branch focused on a single logical change.

Development environment

- Use the included Gradle wrapper to build and run tasks: `./gradlew <task>`.
- Java: Tested with JDK 17+. Use the JDK that matches your local Minecraft server runtime.
- Build: `./gradlew build` — this compiles the plugin and runs tests (if any).
- Quick check: `./gradlew :compileJava` to typecheck/compile only.

Code style and guidelines

- Follow existing project conventions (package layout, naming, and simple formatting).
- Keep public APIs stable — if you must change a public API, document the rationale in the PR.
- Prefer clear, well-named methods over clever one-liners.
- Add JavaDoc for new public classes/methods where it helps clarification.

Tests

- Add unit tests for new behavior where feasible. The project uses Gradle's `test` task: `./gradlew test`.
- If a change requires manual testing (for example, resource-pack hosting behavior), include clear reproduction steps in the PR description.

Committing & PRs

- Rebase or merge `main` before opening a PR so CI runs cleanly.
- Use descriptive commit messages. A short summary line plus one or two sentences in the body is ideal.
- Pull request checklist (please include):
  - What the change does and why (short description).
  - Any configuration changes required (e.g., `config.yml` updates).
  - How the change was tested (commands, server version, screenshots or logs if relevant).
  - Mention related issues or PRs (if any).

Issue reporting

- When opening an issue, include:
  - Minecraft server type/version (Paper/Spigot) and Java version.
  - Plugin version or commit SHA you tested against.
  - Steps to reproduce the problem, expected vs actual behavior, and any relevant console logs.

CI and releases

- The repository includes GitHub Actions workflows for building and publishing snapshot and tagged releases. CI will run automatically on PRs and pushes.

Maintainers & contact

- If you're unsure about an approach, open an issue with your idea first or ping a maintainer on the PR.

License

- By contributing, you agree that your contributions will be licensed under the project's MIT License.

Thank you again — small improvements and documentation fixes are especially welcome!
