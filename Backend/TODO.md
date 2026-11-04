# Backend backlog

The previous notes about controllers being added later are obsolete: ten controllers now exist, including administration. The [known problems](../docs/known-problems.md) document is the source for current evidence, impact, and suggested repairs.

## Before public deployment

- Activate method security and implement catalogue/session ownership checks.
- Replace direct password-reset-token disclosure with a verified delivery flow.
- Supply a unique JWT signing secret and fix refresh/logout authentication requirements.
- Map custom exceptions to HTTP statuses and a stable JSON error response.
- Define complete production database/schema, upload-limit, origin, and credential configuration.

## Local build and integration

- Remove the unused navbar import and address the three component stylesheet budget warnings.
- Align frontend/backend session status, history duration field, song update/upload DTOs, and model enums/nullability.
- Implement missing song-tag listing/album deletion endpoints or remove frontend calls.
- Align albumless-upload persistence and define author/genre for songs without albums.
- Move audio-job submission after transaction commit; add retries/recovery and orphan cleanup.
- Align signed URL expiry reporting; improve player session renewal, unload reporting, and upload-poll cleanup.
- Clean dependent relational rows before parent deletion and coordinate S3 cleanup with committed DB changes.
- Define statistics and listening-duration semantics and persist the required updates.

## Verification and maintenance

- Make backend tests self-contained and fix the test profile/logger configuration.
- Repair the 14 obsolete service imports blocking `npm test -- --watch=false`, then update generated assertions/providers and add meaningful API/UI contract coverage.
- Add access-control, clean-migration, upload/processing, playback, and deletion checks.
- Remove or reconcile unused AI configuration/dependencies.
- Bound catalogue recommendation queries as data grows; define whether click analytics are required.
- Introduce deployment/health/backup/recovery procedures when hosting is added.

## Completed setup work (2026-11-04)

- Added production environment replacement, `/api` development/Nginx proxies, backend/frontend images, and local Compose orchestration with database persistence and readiness checks. Verified clean migrations, registration/login, API access, persistence after recreation, S3 upload, three processed variants, and signed audio/range downloads; validation and deletion problems remain open.
- Frontend npm manifest/lockfile, Angular workspace, and TypeScript configuration are present; dependency installation and production build passed with Node.js 24.22.1 and npm 11.19.1.
- Filled root/frontend ignore files for local configuration, dependencies, caches, and build output; added backend ignores for `aws.env`, environment variants, and logs while preserving example templates.
- Updated READMEs, architecture/deployment documentation, and known problems to distinguish the successful build from remaining warnings, test failures, and runtime integration gaps.

See [backend setup](README.md), [configuration](../docs/deployment.md), and the [API reference](../docs/project-endpoints.md) for current behaviour rather than planned features.
