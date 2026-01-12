# Contributing

## Workflow

Follow the AGENTS flow defined in `AGENTS.md`:

1. Understand scope
2. Inspect files
3. Implement
4. Run/verify
5. Commit
6. Push

## Code style

- Kotlin style: official
- Avoid `!!`
- Prefer small composables and clear naming
- Add KDoc to public types and functions

## Testing

- Desktop: `./gradlew :desktopApp:run`
- Android: `./gradlew :androidApp:installDebug`

## Commits

- Use imperative, descriptive messages
- One topic per commit
