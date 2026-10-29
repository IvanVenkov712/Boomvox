# Boomvox

Boomvox is a music platform for uploading, processing, streaming, organising, rating, and discovering songs. It contains an Angular browser application and a Java REST API. PostgreSQL stores the catalogue and user activity; Amazon S3 stores audio files.

## Project status

There is **no hosted deployment** at present (confirmed by the maintainer on 2026-10-29). The checked-in configuration targets local development: frontend `http://localhost:4200`, backend `http://localhost:9090`, and PostgreSQL on port `5432`.

The backend has a Maven build. The frontend currently contains source files without `package.json`, a dependency lockfile, `angular.json`, or TypeScript build configuration, so a fresh checkout cannot install or run the UI until those files are restored. See [known problems](docs/known-problems.md) before relying on the application for a public deployment.

## Features in the source

- Registration, email/password login, JWT authentication, and profile editing.
- Song, album, artist, and tag browsing; catalogue search and filters.
- MP3/WAV upload, asynchronous FFmpeg processing, and three audio variants.
- Direct browser playback from temporary S3 URLs, a playback queue, and playback activity tracking.
- Personal playlists, favourites, ratings from 0 to 10 with comments, and listening history.
- Recommendations based on listening history, favourites, ratings, genre, artist, tags, and popularity.
- Administrator endpoints for finding users and changing roles.

These features have implementation code; their limitations and integration gaps are documented separately. Recommendations use an in-process scoring algorithm, despite the unused AI integration configuration in the backend.

## Repository

```text
Backend/               Spring Boot application, Maven wrapper, SQL migrations
Frontend/Boomvox_UI/   Angular application source
docs/                  Architecture, API, deployment, and known problems
```

## Install and run locally

1. Clone the repository and open its directory:

   ```sh
   git clone https://github.com/IvanVenkov712/Boomvox.git
   cd Boomvox
   ```

2. Install JDK 21, PostgreSQL, and FFmpeg (including `ffprobe`). Make the binaries available on `PATH`. Configure a private S3 bucket and credentials with access to it. The Maven wrapper supplies Maven; a separate Maven installation is unnecessary.
3. Follow the [backend installation instructions](Backend/README.md#install-and-run) to create the local database role, copy and fill in `Backend/.env`, and start the API:

   ```powershell
   cd Backend
   .\mvnw.cmd spring-boot:run
   ```

   On macOS/Linux, use `./mvnw spring-boot:run` from `Backend/` instead. The `dev` profile is active by default. The API base is `http://localhost:9090/api`; Swagger UI is `http://localhost:9090/swagger-ui/index.html`.

4. Follow the [frontend setup instructions](Frontend/Boomvox_UI/README.md#install-and-run). Restore the missing workspace files before installing dependencies or starting Angular. Once restored, run the frontend on `localhost:4200` in a second terminal.
5. Register through the login screen or `POST /api/auth/register`. New accounts have the `ORDINARY_USER` role. There is no seeded administrator or automatic role bootstrap; uploads are intended for `AUTHOR`/`ADMIN` accounts. See [account setup](Backend/README.md#accounts-and-roles).

AWS credentials, bucket configuration, and a JWT signing secret are described in the [configuration reference](docs/deployment.md#configuration-reference). A populated local `.env` must remain untracked.

## Documentation

| Document | Contents |
| --- | --- |
| [Backend README](Backend/README.md) | Installation, database setup, configuration, commands, and backend layout |
| [Frontend README](Frontend/Boomvox_UI/README.md) | Setup status, routes, components, services, state, and development workflow |
| [Architecture](docs/architecture.md) | Components, data model, upload/playback flows, and recommendations |
| [API reference](docs/project-endpoints.md) | Implemented endpoints, request/response fields, authentication, and examples |
| [Controllers](docs/Controllers.md) | Endpoint ownership and controller responsibilities |
| [Services](Backend/Services.md) | Service responsibilities and transaction boundaries |
| [Deployment and configuration](docs/deployment.md) | Current local topology, variables, profiles, and production preparation |
| [Storage and audio processing](Backend/s3_music_platform_guide.md) | S3 integration, object layout, processing, and troubleshooting |
| [Known problems](docs/known-problems.md) | Verified source-level issues, impact, and suggested follow-up |
| [Backend backlog](Backend/TODO.md) | Prioritised work linked to the known problems |

## Verification

The backend test command is `./mvnw test` (`.\mvnw.cmd test` on Windows), run from `Backend/`. The only backend test checks application context startup; its Testcontainers configuration is empty, so it still needs external configuration/services. Frontend tests are blocked by the missing workspace files and include outdated generated specs. See [verification limits](docs/known-problems.md#verification-and-test-coverage).

Documentation describes the source reviewed on 2026-10-29, starting from commit `b600507`. It does not claim successful end-to-end execution or a production deployment.
