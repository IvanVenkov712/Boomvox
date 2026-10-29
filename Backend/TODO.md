# Backend backlog

The previous notes about controllers being added later are obsolete: ten controllers now exist, including administration. The [known problems](../docs/known-problems.md) document is the source for current evidence, impact, and suggested repairs.

## Before public deployment

- Activate method security and implement catalogue/session ownership checks.
- Replace direct password-reset-token disclosure with a verified delivery flow.
- Supply a unique JWT signing secret and fix refresh/logout authentication requirements.
- Map custom exceptions to HTTP statuses and a stable JSON error response.
- Define complete production database/schema, upload-limit, origin, and credential configuration.

## Local build and integration

- Recover frontend package, lock, Angular workspace, and TypeScript configuration files.
- Align frontend/backend session status, history duration field, song update/upload DTOs, and model enums/nullability.
- Implement missing song-tag listing/album deletion endpoints or remove frontend calls.
- Align albumless-upload persistence and define author/genre for songs without albums.
- Move audio-job submission after transaction commit; add retries/recovery and orphan cleanup.
- Align signed URL expiry reporting; improve player session renewal, unload reporting, and upload-poll cleanup.
- Clean dependent relational rows before parent deletion and coordinate S3 cleanup with committed DB changes.
- Define statistics and listening-duration semantics and persist the required updates.

## Verification and maintenance

- Make backend tests self-contained and fix the test profile/logger configuration.
- Repair outdated frontend specs and add meaningful API/UI contract coverage.
- Add access-control, clean-migration, upload/processing, playback, and deletion checks.
- Remove or reconcile unused AI configuration/dependencies.
- Bound catalogue recommendation queries as data grows; define whether click analytics are required.
- Introduce deployment/health/backup/recovery procedures when hosting is added.

See [backend setup](README.md), [configuration](../docs/deployment.md), and the [API reference](../docs/project-endpoints.md) for current behaviour rather than planned features.
