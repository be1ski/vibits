# AGENTS (shared)

## Scope

This module is shared across all platforms. Any changes here affect Android, Desktop, and iOS.

## Required change flow

Follow the root `AGENTS.md` flow. Additionally:

- Run `:desktopApp:run` after UI changes.
- If Android-specific behavior is touched, run `:androidApp:installDebug`.

## Notes

- Avoid platform-specific dependencies in `commonMain`.
- Never log auth tokens.
