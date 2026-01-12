# AGENTS

This file describes the working agreement for automated or human changes.

## Required change flow

All changes must follow a transparent, repeatable flow:

1. Understand the request and scope.
2. Inspect the relevant files.
3. Implement the change.
4. Run or update the appropriate app/tests (at least `:desktopApp:run` or `:androidApp:installDebug` when relevant).
5. Verify the result.
6. Commit with a clear message.
7. Push only after verification.

If a step cannot be completed, document why and what is needed to proceed.

## Repository structure

- `shared/` - Kotlin Multiplatform shared code
- `androidApp/` - Android app wrapper
- `desktopApp/` - Desktop app wrapper
- `app/` - legacy Android-only app (not in the current build)

## Documentation expectations

- Keep `README.md` in each module up to date.
- Add KDoc/Javadoc to new public types and functions.
- Update `ARCHITECTURE.md` for major changes.

## Security

- Never commit tokens or secrets.
- Logs must never include auth tokens.
