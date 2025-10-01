# smpCurrency

## CI / CD
A GitHub Actions workflow (`.github/workflows/build.yml`) builds the plugin on:
- Push to `main` or `master`
- Pull requests targeting `main` or `master`
- Tags starting with `v` (e.g. `v1.0.0`) – also creates a GitHub Release and attaches the JAR

### Snapshot Release (Continuous)
A second workflow (`.github/workflows/release-latest.yml`) automatically:
- Builds on every push to `main` / `master`
- Publishes / updates a GitHub Release with tag `snapshot`
- Replaces previous artifacts (ensuring only the latest build is present)
- Adds a copy of the jar suffixed with the short commit SHA (e.g. `smpCurrency-1.0-SNAPSHOT-abc1234.jar`)

Use cases:
- Always grab the latest development build: visit the `snapshot` release
- Stable server usage: prefer versioned tags like `v1.0.0`

To fetch via script (example):
```bash
curl -L -o smpCurrency-latest.jar \
  https://github.com/<your-user>/<your-repo>/releases/download/snapshot/$(curl -s https://api.github.com/repos/<your-user>/<your-repo>/releases/tags/snapshot | grep browser_download_url | grep '.jar"' | head -n1 | cut -d '"' -f4 | xargs basename)
```
Replace `<your-user>` and `<your-repo>` accordingly.

## Release Workflow
1. Update `version` in `build.gradle` or `gradle.properties` (if you externalize it later).
2. Commit changes to `main` / create PR.
3. Tag a release:
```bash
git tag v1.0.0
git push origin v1.0.0
```
4. GitHub Actions will build and publish a Release with attached JAR(s).

## Future Improvements (Ideas)
- Dynamic pack generation for every coin ID in config
- Adventure text everywhere instead of legacy color codes
- Add tests (e.g., config parsing, item creation integrity)
- Add checksum validation & `/currency packinfo`
- Automatic version incrementing via conventional commits / semantic-release style tooling
- Changelog generation (e.g., Keep a Changelog + release notes automation)
