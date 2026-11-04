# Storage documentation moved

The old phase-by-phase implementation tutorial has been replaced with documentation of the current code. Its sample DTOs, role names, single-file streaming path, and suggested code patches are no longer the current contract.

- [S3 storage and audio processing](s3_music_platform_guide.md): actual object keys, upload/processing lifecycle, permissions, signed playback, and troubleshooting.
- [Backend README](README.md): prerequisites, database/storage setup, and startup commands.
- [Architecture](../docs/architecture.md): component/data diagrams, upload/playback flows, and recommendation scoring.
- [API reference](../docs/project-endpoints.md): implemented routes, DTOs, enums, and examples.
- [Deployment and configuration](../docs/deployment.md): verified local Compose setup, native variables, profiles, backup/restore, and hosted preparation.
- [Known problems](../docs/known-problems.md): transaction timing, session handling, cleanup, and contract gaps.

This file remains as a navigation entry for existing links. Use the [root Compose quick start](../README.md#install-and-run-with-docker-compose) to install/run the current stack. Local migrations, authentication, persistence, upload/transcoding, and signed audio/range downloads passed checks on 2026-11-04. Boomvox still has no hosted public deployment.
