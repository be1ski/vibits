# Project Context

## Purpose

Build a Kotlin Multiplatform client for Memos with shared Compose UI.

## Current stack

- Kotlin Multiplatform + Compose Multiplatform
- Ktor for networking
- Koin for DI

## UI structure

- Profile tab: activity grid + weekly bar chart
- Posts tab: list of memos

## API behavior

- Paginated list requests using `pageSize` and `pageToken`
- Auth via Bearer token

## Conventions

- Prefer KDoc for public APIs
- Avoid `!!`
- Keep logs free of secrets
