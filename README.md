# Boomvox

Boomvox is a music platform for uploading and processing audio, listening to songs, organising personal collections, and discovering music through ratings and listening preferences. It combines an Angular browser application, a Java REST API, PostgreSQL, and private Amazon S3 audio storage.

The local Docker Compose stack is implemented and verified. There is no hosted public instance. Use the container quick start below, or the native setup when developing the Java and Angular source.

## Features

| Feature | What you can do |
| --- | --- |
| Accounts and profiles | Register, sign in with email/password, edit profile details, and access a personal favourites list. API requests use JWT authentication. |
| Music catalogue | Browse songs, albums, artists, and tags; search and filter by genre, original format, album, artist, or tag. Artists are author accounts; albums supply a song's artist and genre. |
| Audio publishing | Upload MP3/WAV audio with metadata, optionally attach it to an album, and monitor processing. FFmpeg generates 128 kbps MP3, 320 kbps MP3, and 256 kbps AAC variants. |
| Playback | Play active songs from temporary signed S3 URLs. The shared player supports queues, pause/resume, previous/next, and seeking while navigating between screens. |
| Personal collections | Create playlists, add/remove songs, and maintain favourites. Membership includes song position; services check collection ownership. |
| Ratings and feedback | Give a song an integer grade from 0 to 10 with a comment, update your rating, and view feedback and rating averages. |
| Discovery and history | View listening history/preferences and generate recommendations from listening, favourites, ratings, genres, artists, tags, and popularity. Recommendations use local scoring without an AI service call. |
| Administration | Find users and change account roles through administrator endpoints. New accounts start as `ORDINARY_USER`; author/admin roles are intended for publishing. |

The publishing flow is upload → `PROCESSING` → three `READY` variants → `ACTIVE` song → signed playback URL. The controller accepts files up to 100 MiB; servlet and Nginx request limits also apply, so multipart overhead reduces the largest usable upload.

These features have implementation code, but coverage differs. Local HTTP/storage checks verified authentication, upload/transcoding, and audio downloads. Some UI/API contracts, permissions, errors, and deletion flows still need work; see [known problems](docs/known-problems.md). Password reset must be repaired before public deployment.

## Advantages of the design

- **One command starts the local stack.** Docker supplies the application runtimes, PostgreSQL, Nginx, and FFmpeg. Readiness checks order startup, and a named volume preserves the database across container recreation.
- **Audio downloads go directly to S3.** The API handles authentication, metadata, and URL signing instead of forwarding every audio byte. The bucket stays private and playback access is temporary.
- **Audio processing runs in the background.** Upload acceptance is separate from FFmpeg work, and clients can poll status. The current worker runs inside the API JVM; durable queues and automatic recovery remain future work.
- **The browser uses one API path.** `/api` works through the Angular development proxy and container Nginx, avoiding a different browser API hostname for each same-origin deployment.
- **Recommendations are inspectable.** The scoring formula can be understood and adjusted in Java without operating an AI model server.
- **The code has clear boundaries.** Angular services, REST DTOs/controllers, business services, repositories, and versioned Liquibase migrations make workflows easier to locate and change.

These are implementation properties, not measured performance or production-readiness guarantees.

## Stack and repository

| Layer | Technology |
| --- | --- |
| Browser UI | Angular 21.2, TypeScript 5.9, RxJS, signals |
| API | Java 21, Spring Boot 3.4.5, Maven wrapper |
| Persistence | PostgreSQL 17 in Compose, Spring Data JPA, 21 Liquibase migrations |
| Authentication | Spring Security, encoded passwords, JJWT |
| Audio | FFmpeg/FFprobe, AWS SDK v2, private S3 objects and signed GET URLs |
| Containers | Java JDK/JRE stages; Node.js 24 build stage; Nginx static hosting and API proxy |

```text
Backend/                 Java application, Dockerfile, migrations, backend guides
Frontend/Boomvox_UI/     Angular workspace, lockfile, development proxy
Frontend/Dockerfile      Angular build and Nginx runtime
Frontend/nginx.conf      API forwarding, upload limit, SPA route fallback
docker-compose.yml       Database/API/UI services, ports, volume, readiness checks
.compose.env.example     Template for the ignored root Compose .env
docs/                    Architecture, API, deployment, verification, known problems
```

## Install and run with Docker Compose

### 1. Install prerequisites and clone

Install Git and [Docker Desktop with Compose](https://docs.docker.com/compose/install/) on Windows/macOS, or Docker Engine and the Compose plugin on Linux. Start Docker and use Linux containers. This path needs no host Java, Node.js, PostgreSQL, or FFmpeg installation.

```sh
git clone https://github.com/IvanVenkov712/Boomvox.git
cd Boomvox
docker version
docker compose version
```

You also need a private S3 bucket, its region, and basic AWS credentials with permission to put/get/delete the application's audio objects. Follow the [S3 setup guide](Backend/s3_music_platform_guide.md#configuration-and-permissions). The current client does not support session-token credentials, IAM role discovery, or a local S3 emulator endpoint.

### 2. Configure secrets

From the repository root, copy the template only if `.env` does not already exist.

PowerShell:

```powershell
if (-not (Test-Path .env)) { Copy-Item .compose.env.example .env }
```

macOS/Linux:

```sh
test -e .env || cp .compose.env.example .env
```

Edit `.env` and replace every placeholder:

| Variable | Value to supply |
| --- | --- |
| `DB_PASSWORD` | A unique random password for the container database role |
| `AWS_REGION` | The bucket's AWS region |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Basic credentials with the required object permissions |
| `AWS_S3_BUCKET` | Your private bucket name |
| `JWT_SECRET` | A unique random signing secret of at least 32 bytes |

Root `.env` uses uppercase Compose variables. `Backend/.env` is a separate Java-properties file for native development; do not copy it directly into the root. Both files are ignored by Git and Docker. See [secret generation and configuration](docs/deployment.md#generating-local-secrets).

### 3. Build and start

```sh
docker compose config --quiet
docker compose up --build -d --wait --wait-timeout 180
docker compose ps
```

The first build downloads images, packages, Maven, and dependencies. PostgreSQL starts first, then the API and migrations, then Nginx after readiness checks pass. Compose uses the backend's `dev` profile with database hostname `db`.

| Address | Purpose |
| --- | --- |
| `http://localhost:4200` | Browser application |
| `http://localhost:4200/api` | API through Nginx, used by the browser |
| `http://localhost:9090/swagger-ui/index.html` | Swagger UI on the API's debug port |
| `http://localhost:9090/v3/api-docs` | OpenAPI JSON and API readiness check |

Published ports are bound to localhost. PostgreSQL is internal at `db:5432`, database `postgres`, schema `boomvox`; it has no published host port.

### 4. Create accounts and use the platform

Open `http://localhost:4200` and register on the login screen. A new account can browse, listen, maintain collections, and rate songs. An empty database has no songs or seeded administrator.

For your first author/admin account, register it and open a trusted database session from the repository root:

```sh
docker compose exec db psql -U boomvox -d postgres
```

Run this using the registered account's email:

```sql
UPDATE boomvox."user" SET role = 'ADMIN' WHERE email = 'your-account@example.test';
```

Exit `psql` with `\q`, then sign out and back in to update the UI's cached role. Use `AUTHOR` instead if you only need publishing. Existing administrators can manage later role changes through the application. See [role limitations](docs/known-problems.md#authentication-and-access-control).

Upload an MP3/WAV from `/upload`, wait for `ACTIVE`, then play it from the catalogue. Audio comes directly from S3; check bucket CORS and browser codec support if playback differs from the verified HTTP download.

### 5. Stop, resume, and update

```sh
docker compose logs --tail 100 backend
docker compose stop
docker compose up -d --wait --wait-timeout 180
```

`stop` retains containers and data. `docker compose down` removes containers/network but keeps the named database volume `boomvox_postgres_data`. Adding `--volumes` deletes that data. Keep `.env` stable: changing its database password does not update an initialized PostgreSQL role.

After source changes, rebuild with `docker compose up --build -d --wait --wait-timeout 180`. Back up before schema-changing updates; see [backup, restore, and release operations](docs/deployment.md#backup-restore-and-release-operations).

## Run natively for development

Install JDK 21, PostgreSQL 17, FFmpeg/FFprobe, Node.js 24, and npm 11. The Maven wrapper supplies Maven. Follow the [backend setup](Backend/README.md#install-and-run) to create the database role and fill `Backend/.env` from its example.

In one PowerShell terminal:

```powershell
cd Backend
.\mvnw.cmd spring-boot:run
```

On macOS/Linux, use `./mvnw spring-boot:run`; if needed, normalize wrapper line endings and make it executable as described in the backend README.

In another terminal, starting from the repository root:

```sh
cd Frontend/Boomvox_UI
npm ci
npm start -- --host localhost --port 4200
```

The Angular server forwards `/api/**` to `http://localhost:9090`; open `http://localhost:4200`. Stop Compose services before starting native processes on the same ports. Native PostgreSQL uses `localhost:5432`, unlike Compose's internal `db:5432`.

## Deploy

The commands above deploy the complete application locally. The checked-in Compose configuration selects `dev`, publishes localhost ports, and creates a local PostgreSQL superuser. A hosted release needs explicit `prod` database/schema/upload settings, a public HTTPS entry point, appropriate origin handling, backups, and fixes for the documented access-control and password-reset issues.

Follow [hosted deployment preparation](docs/deployment.md#preparing-a-future-hosted-deployment) for settings, image commands, and release checks. No hosting provider, public domain, automated production release, or hosted instance is configured.

## Verification and current limitations

Local Compose checks on 2026-11-04 passed both image builds, all three readiness checks, all 21 migrations, registration/login, authenticated profile/catalogue requests, persistence after full container recreation, S3 upload, three encoded variants, signed audio download, and byte-range seeking. Temporary data/objects were removed. These were HTTP/database/storage checks; visual browser playback and every feature were not tested.

The frontend production build passes with four warnings. Frontend tests fail to compile obsolete service imports. The backend context test needs external configuration/services, and image packaging skips tests. Run `npm run build` / `npm test -- --watch=false` in the Angular workspace, and `./mvnw test` (`.\mvnw.cmd test` on Windows) in `Backend/` with dependencies configured.

Invalid registration can return an empty 403. Deleting a song referenced by a playback session can fail after deleting its S3 objects. Other contract, security, statistics, and worker-recovery gaps are tracked in [known problems](docs/known-problems.md) and the [backend backlog](Backend/TODO.md).

## Documentation

| Document | Contents |
| --- | --- |
| [Backend README](Backend/README.md) | Native/container setup, database, roles, build, troubleshooting |
| [Frontend README](Frontend/Boomvox_UI/README.md) | Development proxy, container image, routes, services, playback |
| [Deployment and configuration](docs/deployment.md) | Compose, secrets, profiles, backups, hosted preparation, verification |
| [Architecture](docs/architecture.md) | Components, data model, audio flows, recommendation formula |
| [API reference](docs/project-endpoints.md) | Endpoints, DTOs, authentication, errors, request examples |
| [Controller guide](docs/Controllers.md) / [Service guide](Backend/Services.md) | Code responsibilities and boundaries |
| [S3 and audio guide](Backend/s3_music_platform_guide.md) | Bucket permissions, objects, CORS, troubleshooting |
| [Known problems](docs/known-problems.md) | Verified failures and remaining source-level findings |
