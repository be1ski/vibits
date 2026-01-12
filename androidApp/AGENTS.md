# AGENTS (androidApp)

## Scope

Android wrapper for shared UI.

## Required change flow

Follow the root `AGENTS.md` flow. Always verify Android builds with:

```bash
./gradlew :androidApp:installDebug
```

## Notes

- Keep Android-specific code minimal.
- Initialize shared DI and context in `MemosApplication`.
