# Deployment and configuration

## Current deployment

**Boomvox is not currently deployed to a hosted environment**, confirmed by the maintainer on 2026-10-29. The repository contains local development settings, a partial production profile, backend/frontend Dockerfiles, and a local Docker Compose stack. The frontend image uses Nginx to serve Angular and proxy API requests. There are no hosted frontend/API URLs or CI/CD workflows.

| Component | Native development | Compose |
| --- | --- | --- |
| Frontend | Angular dev server at `http://localhost:4200` | Nginx at `http://localhost:4200`, container port 80 |
| Browser API path | `/api`, dev proxy to `localhost:9090` | `/api`, Nginx proxy to `backend:9090` |
| Direct/debug API | `http://localhost:9090/api` | `http://localhost:9090/api`, published on localhost |
| PostgreSQL | Host server at `localhost:5432` | PostgreSQL 17 at internal `db:5432`; no host port |
| Database/schema | Database `postgres`, schema `boomvox` | Same names; persistent `boomvox_postgres_data` volume |
| Audio storage | External private S3 bucket | External private S3 bucket |
| Audio processor | Host FFmpeg/FFprobe and writable temp storage | Executables and `/app/tmp` inside the backend container |

The base API port with `prod` or `test` alone is 8099 unless overridden. Compose explicitly sets port 9090 and selects `dev`; its frontend bundle is nevertheless an optimized Angular production build. Backend and frontend environment selections are separate.

Both frontend environment files use `/api` on the browser's current origin. The Angular development server proxies API requests to `localhost:9090`; the container's Nginx proxies them to `backend:9090` on its Docker network. Historical documentation's `https://api.Boomvox.com` was an example, not an active service address.

## Installing and starting locally

Use the [backend README](../Backend/README.md#install-and-run) for JDK/PostgreSQL/FFmpeg installation requirements, database role creation, `.env` setup, and startup commands. For the frontend, use Node.js 24 and npm 11, run `npm ci` in `Frontend/Boomvox_UI/`, then `npm start -- --host localhost --port 4200`. See the [frontend README](../Frontend/Boomvox_UI/README.md#build-and-test-status) for the verified build and current test failures.

Start the backend from `Backend/`, since configuration imports and log paths are relative to the current working directory. Start the frontend separately on port 4200. An audio-capable backend needs the PostgreSQL connection, AWS settings, FFmpeg binaries, and writable temporary storage.

## Docker Compose local stack

Install [Docker Desktop with Linux containers and Compose](https://docs.docker.com/compose/install/) on Windows/macOS, or Docker Engine and the Compose plugin on Linux. Clone the repository and run commands from its root. Host installations of Java, Node.js, PostgreSQL, and FFmpeg are unnecessary for this path.

1. If a root `.env` does not already exist, copy `.compose.env.example` to `.env`:

   ```powershell
   if (-not (Test-Path .env)) { Copy-Item .compose.env.example .env }
   ```

2. Replace all placeholders. `DB_PASSWORD` and `JWT_SECRET` should be unique random values; use at least 32 bytes for the JWT secret. Supply the existing private S3 bucket's region and basic access credentials. This file uses Compose dotenv syntax with uppercase variable names. It is separate from `Backend/.env`, which uses Java property names. Compose explicitly passes the required values to the backend; neither local secrets file is baked into an image. Both `.env` files are ignored by Git and Docker.
3. Validate and start the stack:

   ```sh
   docker compose config --quiet
   docker compose up --build -d --wait --wait-timeout 180
   docker compose ps
   ```

4. Open `http://localhost:4200`. Register and log in through the UI. Swagger is available at `http://localhost:9090/swagger-ui/index.html`. The frontend and API are bound to localhost; PostgreSQL has no host port. For uploads, assign an account `AUTHOR` or `ADMIN` as described in the [backend account setup](../Backend/README.md#accounts-and-roles).
5. Inspect logs or stop the stack:

   ```sh
   docker compose logs --tail 100 backend
   docker compose down
   ```

The stack uses PostgreSQL 17 with database `postgres`, role `boomvox`, and the `boomvox` schema. The backend uses `dev` so existing schema initialization, Liquibase migrations, Hibernate validation, console logging, and 100 MB upload limits apply. The PostgreSQL image creates the local role as a superuser; this setup is for local development. Database files are stored in named volume `boomvox_postgres_data`; `docker compose down` preserves it. Adding `--volumes` would delete it. Changing `DB_PASSWORD` after initialization does not change the password inside an existing database; update the role's password to match before recreating the backend.

Startup order is database readiness (`pg_isready` over TCP), backend readiness (HTTP `/v3/api-docs`), then Nginx. Nginx serves static files and forwards `/api/` to `backend:9090`. Its own health check checks HTTP `/`. These checks establish service readiness, not S3 access or successful audio processing. Backend packaging skips tests as specified in its Dockerfile.

To check persistence, register a temporary account, run `docker compose down`, then `docker compose up -d --wait --wait-timeout 180`, and log in again. Verify upload, three audio variants, signed-URL playback, and seeking separately with a valid S3 configuration. No hosted production deployment is created by these commands.

### Generating local secrets

Generate a separate random value for `DB_PASSWORD` and `JWT_SECRET`. For example, this PowerShell snippet prints two values to paste into the ignored root `.env`:

```powershell
$secretGenerator = [Security.Cryptography.RandomNumberGenerator]::Create()
foreach ($setting in @('DB_PASSWORD', 'JWT_SECRET')) {
    $secretBytes = New-Object byte[] 48
    $secretGenerator.GetBytes($secretBytes)
    '{0}={1}' -f $setting, [Convert]::ToBase64String($secretBytes)
}
$secretGenerator.Dispose()
```

On macOS/Linux with OpenSSL, run `openssl rand -hex 32` separately for each value. The JWT implementation uses the resulting text directly; it does not Base64-decode it. Keep values stable across restarts. Compose may interpolate `$` in unquoted/double-quoted dotenv values; single-quote a literal value that contains it. Do not use the shared signing secret from `application.yml` for a non-disposable environment.

Only variables explicitly listed under a service's `environment` are passed by this Compose file. Adding `SPRING_PROFILES_ACTIVE=prod` to root `.env` alone does not override its literal `dev` setting; change/override the service configuration as part of hosted preparation.

### Routine operations and troubleshooting

| Task or symptom | Command/check |
| --- | --- |
| Readiness and ports | `docker compose ps` |
| Startup failure | `docker compose logs --tail 100 db backend frontend` |
| Pause and resume | `docker compose stop`, then `docker compose up -d --wait --wait-timeout 180` |
| Remove containers, retain data | `docker compose down` |
| Open the internal database | `docker compose exec db psql -U boomvox -d postgres` |
| Host port already in use | Stop the competing native/Compose process; the configured defaults are 4200 and 9090 |
| Backend unhealthy | Inspect migration/startup logs and test `http://localhost:9090/v3/api-docs`; no Actuator endpoint exists |
| UI loads but API returns 502 | Check backend health and Nginx upstream name/port; keep `backend:9090` reachable on the same network |
| Database rejects credentials after editing `.env` | Restore the matching password or update the existing PostgreSQL role before recreating the backend; retain the volume |
| Unknown AWS property or rejected object access | Check root Compose variable names, bucket region, static credentials, and object permissions; see the S3 guide |

### Backup, restore, and release operations

PostgreSQL volumes survive container replacement, but are not backups. Store database archives outside the repository, together with the release identifier and a matching S3 recovery strategy. The following uses PostgreSQL's archive format and `docker compose cp`, avoiding binary output redirection differences between PowerShell and POSIX shells.

Create an external backup directory, for example `../boomvox-backups` (`New-Item -ItemType Directory -Force ../boomvox-backups` in PowerShell, or `mkdir -p ../boomvox-backups` in a POSIX shell). Use a unique archive filename for each backup:

```sh
docker compose exec -T db pg_dump -U boomvox -d postgres --format=custom --file=/tmp/boomvox-backup.dump
docker compose cp db:/tmp/boomvox-backup.dump ../boomvox-backups/boomvox-backup.dump
```

Before restoring, choose the intended local database, stop application writes, and retain a current backup. The following restore replaces the archived objects in the target `postgres` database; it is not a merge. Use only your trusted archive. See [PostgreSQL restore options](https://www.postgresql.org/docs/17/app-pgrestore.html).

```sh
docker compose stop frontend backend
docker compose cp ../boomvox-backups/boomvox-backup.dump db:/tmp/boomvox-restore.dump
docker compose exec -T db pg_restore -U boomvox -d postgres --clean --if-exists --exit-on-error --single-transaction --no-owner --no-privileges /tmp/boomvox-restore.dump
docker compose up -d --wait --wait-timeout 180
```

A DB archive does not include S3 objects, role passwords, the JWT secret, logs, or queued/running audio jobs. Preserve compatible bucket objects and keep secrets in the operator's secret store. Variant keys contain numeric song IDs, so unrelated databases should not share the same raw/variant namespace. Enable and test the bucket recovery/versioning policy appropriate to the deployment; none is managed by this repository.

For an update, retain the current release/images and a restorable database/S3 state, check out the intended source release, then run `docker compose up --build -d --wait --wait-timeout 180`. Check logs and repeat the smoke workflow below. Liquibase applies pending migrations at startup. Running an older image does not automatically undo database migrations; recovery may require the matching archive and objects. Backup/restore commands are documented procedures, not an automated or already-verified recovery pipeline.

### Verification on 2026-11-04

The local Compose stack was built and started with the existing private S3 configuration and newly generated database/JWT secrets in an ignored root `.env`.

| Check | Result |
| --- | --- |
| Compose validation and both image builds | Passed |
| Database, backend, and frontend health checks | All healthy |
| Clean PostgreSQL initialization | All 21 Liquibase migrations applied; Hibernate validation and application startup passed |
| Frontend and `/login` refresh | Both served the Angular entry page |
| Registration/login through Nginx | Passed with the browser's localhost Origin header |
| JWT profile and catalogue requests | Passed; unauthenticated catalogue access rejected |
| Persistence across `down` and `up` | Account ID, favourites list, login, and migration state survived complete container recreation |
| One-second WAV upload to S3 | Passed through Nginx and the API |
| FFmpeg processing | Three variants became READY and the song became ACTIVE |
| Signed audio URL | Download returned HTTP 200 with audio data |
| Seeking transport | `Range: bytes=0-99` returned HTTP 206 with 100 bytes |

These are HTTP/database/storage checks, not visual browser automation or a passing unit-test suite. The four known frontend build warnings remain. Invalid registration with an empty body returned an empty 403 instead of the intended validation response. Deleting the played test song removed its S3 objects but failed on the existing streaming-session foreign key; see [known problems](known-problems.md). Its exact temporary database records were then removed in a guarded transaction. All three services were left running and healthy.

## How configuration is loaded

The backend loads shared `application.yml`, `application.properties`, and the active profile's YAML. `application.properties` imports these optional files relative to the process working directory:

```properties
spring.config.import=optional:file:.env[.properties],optional:file:aws.env[.properties]
```

Use ignored `Backend/.env` for local values. It is parsed as a **Java properties file**, not as a shell script: write `aws.region=your-region` without `export` or quotes. The `.env.example` uses actual property names so values work directly in this imported file. OS environment variables can be used instead and override file configuration.

Both `.env` and the optional `aws.env` are ignored by Git, as are `.env.*` variants; `.env.example` and `.env.*.example` templates remain trackable. The actual local files, AWS keys, database passwords, and JWT signing secret must stay outside version control. Ignore rules do not untrack previously committed files.

## Configuration reference

The property column is suitable for the imported `.env` file. The environment-variable column is for values supplied to the backend process by the shell, service manager, or future hosting provider.

### Storage and authentication

| Property | OS environment variable | Required/default | Purpose |
| --- | --- | --- | --- |
| `aws.region` | `AWS_REGION` | Required; no repository default | AWS region of the configured S3 bucket and signer |
| `aws.accessKeyId` | `AWS_ACCESS_KEY_ID` | Required by the current S3 configuration | AWS access key used by both S3 client and presigner |
| `aws.secretAccessKey` | `AWS_SECRET_ACCESS_KEY` | Required by the current S3 configuration | Secret paired with that access key |
| `aws.s3.bucket` | `AWS_S3_BUCKET` | Required; no repository default | Bucket for raw audio and generated variants |
| `aws.s3.signedUrlExpiryMinutes` | `AWS_S3_SIGNED_URL_EXPIRY_MINUTES` | Default `15` | Actual lifetime used when signing S3 GET URLs |
| `jwt.secret` | `JWT_SECRET` | A shared default is tracked; override with a unique secret | JWT signing/validation key; provide at least 32 bytes for HMAC signing |
| `jwt.expiration-ms` | `JWT_EXPIRATIONMS` | Default `3600000` | JWT lifetime in milliseconds, one hour by default |

`AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_S3_BUCKET`, and `AWS_S3_SIGNED_URL_EXPIRY_MINUTES` are explicitly referenced by `application.properties`. `S3Config` constructs a static basic-credentials provider from the properties. It does not use the AWS default credentials chain, instance roles, or an AWS profile; it also does not read `AWS_SESSION_TOKEN`. Temporary session credentials need a code change.

`jwt.secret` is converted directly to bytes, not Base64-decoded. Generate a random secret locally, put its text in `.env` or a process variable, and retain it securely; changing it invalidates existing tokens. The stream response still reports a fixed 15-minute `expiresAt`, so keep signing expiry at 15 until that mismatch is fixed.

### Database and profiles

| Property | OS environment variable | Current/default | Purpose |
| --- | --- | --- | --- |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | `dev` | Select `dev`, `prod`, or `test` configuration |
| `server.port` | `SERVER_PORT` | `9090` in `dev`; shared default `8099` | HTTP listen port |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | Dev: `jdbc:postgresql://localhost:5432/postgres?currentSchema=boomvox` | JDBC database URL and schema search path |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | Dev: `boomvox` | PostgreSQL login role |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | Dev: `boomvox_pass` | Password for the PostgreSQL role |
| `spring.datasource.hikari.connection-init-sql` | `SPRING_DATASOURCE_HIKARI_CONNECTIONINITSQL` | Dev: `CREATE SCHEMA IF NOT EXISTS boomvox` | Ensure schema exists before migrations; needs suitable DB privileges |
| `spring.jpa.hibernate.ddl-auto` | `SPRING_JPA_HIBERNATE_DDLAUTO` | Dev: `validate` | Validate mappings instead of generating/updating tables |
| `spring.jpa.properties.hibernate.default_schema` | Prefer exact key in `.env` or `SPRING_APPLICATION_JSON` | Dev: `boomvox` | Pass through Hibernate's default database schema, preserving its underscore |
| `spring.liquibase.default-schema` | `SPRING_LIQUIBASE_DEFAULTSCHEMA` | Dev: `boomvox` | Locate Liquibase migrations/history in the application schema |
| `spring.liquibase.change-log` | `SPRING_LIQUIBASE_CHANGELOG` | Dev explicitly sets `classpath:db/changelog/db.changelog-master.yaml` | Entry point for ordered SQL migrations; also matches the conventional default path |

Hyphens are removed when expressing canonical Spring properties as environment-variable names (for example `jwt.expiration-ms` becomes `JWT_EXPIRATIONMS`); see [Spring Boot's environment binding rules](https://docs.spring.io/spring-boot/3.4/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables). Dotted keys can be set directly in the imported properties file. Hibernate's pass-through map property `hibernate.default_schema` needs its underscore preserved; set the exact key in `.env`, or set `SPRING_APPLICATION_JSON` to `{"spring.jpa.properties.hibernate.default_schema":"boomvox"}`. These instructions assume the existing `boomvox` schema name because the source also contains explicit schema references.

### Upload limits, logging, and other settings

| Property/setting | OS environment variable | Current/default | Purpose |
| --- | --- | --- | --- |
| `spring.servlet.multipart.max-file-size` | `SPRING_SERVLET_MULTIPART_MAXFILESIZE` | Dev: `100MB`; prod/test do not set it | Servlet file-size ceiling; controller also checks 100 MiB |
| `spring.servlet.multipart.max-request-size` | `SPRING_SERVLET_MULTIPART_MAXREQUESTSIZE` | Dev: `100MB`; prod/test do not set it | Entire multipart request ceiling, including metadata and overhead |
| `boomvox.log-level` | `BOOMVOX_LOGLEVEL` | Shared/prod: `INFO`; dev: `DEBUG` | Threshold for the custom `AppLogger` |
| `boomvox.log-file` | `BOOMVOX_LOGFILE` | Prod: `logs/boomvox-prod.log` | File used by the production custom logger; directory must be writable |
| `boomvox.default-page-size` | `BOOMVOX_DEFAULTPAGESIZE` | `10` | Custom application property logged at startup; history uses Spring `Pageable` instead |
| `PATH` | `PATH` | Must include `ffmpeg` and `ffprobe` | Allows the Java FFmpeg wrapper to find native executables |
| `JAVA_HOME` | `JAVA_HOME` | JDK installation used for the build | Select the Java toolchain, targeting Java 21 |
| `spring.ai.openai.api-key` | `OPENAI_API_KEY` via the existing placeholder | No configured value | Leftover AI configuration; not used by the current recommendation algorithm |

The POM includes the Ollama starter while the dev YAML has its Ollama section commented out and contains OpenAI settings. Supplying `OPENAI_API_KEY` does not switch starters or make recommendations AI-powered. The checked-in business services do not call either provider. Reconcile/remove that configuration before treating either as an operational dependency.

The frontend currently supports **no runtime environment variables**. Both environment files use the browser-visible API base `/api`. The production target in `angular.json` replaces `environment.ts` with `environment.prod.ts`, selecting `production: true`. `npm start` loads `proxy.conf.json` automatically and forwards `/api/**` to `http://localhost:9090`; the frontend image uses `Frontend/nginx.conf` to forward `/api/` to `http://backend:9090`. Both proxies preserve the API path. See the [frontend container instructions](../Frontend/Boomvox_UI/README.md#container-image). Server secrets belong in backend configuration.

## Profile behaviour

| Profile | Port | Database/schema setup | Custom logger |
| --- | --- | --- | --- |
| `dev` (default) | 9090 | Local PostgreSQL, schema init SQL, Liquibase schema, Hibernate validation | `ConsoleAppLogger`; SQL output enabled |
| `prod` | 8099 unless overridden | Not supplied by profile; operator must configure it | `FileAppLogger`, `logs/boomvox-prod.log` |
| `test` alone | 8099 unless overridden | Not supplied by profile; Testcontainers class is empty | No `AppLogger` bean matches this profile; startup needs correction |

`application-test.yml` also uses a stale `theatre` prefix instead of `boomvox`, so its custom logger properties do not bind. Switching to `prod` does not activate the database or multipart settings in `dev`.

## Preparing a future hosted deployment

This is the preparation sequence for a future release, not a claim that the current local configuration is ready for public use. There is no provider-specific infrastructure or hosted deployment to update.

1. Resolve the access-control, password-reset, error handling, deletion consistency, test, and integration issues in [known problems](known-problems.md). The successful local smoke run does not establish complete browser or authorization correctness.
2. Provision PostgreSQL, a dedicated application role, and a `boomvox` schema owned by that role. Retain the schema name because mappings and SQL explicitly reference it. Configure database TLS/network access for the provider. Separate the migration role from a restricted runtime role if required by the deployment; the local Compose superuser is not that setup.
3. Provision a private S3 bucket/namespace compatible with the database and inject the required `AWS_*` variables and a unique `JWT_SECRET`. The current Java client requires static basic credentials; managed-role/session-token support needs a code change. Configure the public frontend's S3 CORS origin if the chosen audio request mode needs it. See the [storage guide](../Backend/s3_music_platform_guide.md).
4. Build release images from the repository root and tag them with your release identifier:

   ```sh
   docker build -f Backend/Dockerfile -t boomvox-backend:release .
   docker build -f Frontend/Dockerfile -t boomvox-frontend:release .
   ```

   Replace `release` with the version/commit you will retain. The backend image includes Java 21 and native audio tools; a separate host FFmpeg installation is unnecessary. Allow writable temporary space and preserve prod logs outside the replaceable container when needed. The backend image build skips tests; make the required release tests an explicit separate check.
5. Select `SPRING_PROFILES_ACTIVE=prod` and provide database credentials plus the settings below. For example, put non-secret settings in an external `application-prod.properties` and mount it read-only at `/app/config/application-prod.properties` for the backend image. Supply `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` as process variables. Point the JDBC URL at your database with `currentSchema=boomvox` and the provider's TLS options.

   ```properties
   server.port=9090
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.show-sql=false
   spring.jpa.properties.hibernate.default_schema=boomvox
   spring.liquibase.default-schema=boomvox
   spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
   spring.servlet.multipart.max-file-size=100MB
   spring.servlet.multipart.max-request-size=100MB
   ```

   The schema must already exist before Liquibase writes its history tables. Spring loads external configuration and environment overrides using its [configuration precedence](https://docs.spring.io/spring-boot/3.4/reference/features/external-config.html). A native packaged deployment needs the same settings and host Java 21/FFmpeg; run `java -jar target/boomvox-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod` from `Backend/` with external configuration supplied. Do not assume root Compose dotenv variables are loaded by a native JVM.
6. Run the frontend and backend on the same private network with upstream `backend:9090`, or adjust Nginx for the chosen routing. Keep the browser API base `/api`. Put a public HTTPS entry point in front of the frontend and configure trusted forwarding of the public host, scheme, and port through the complete proxy chain. The checked-in Nginx uses its own `$scheme`; if another proxy terminates TLS, adapt forwarding and Spring's trusted forwarded-header handling. Configure the actual allowed origin in `SecurityConfig` when Spring treats requests as cross-origin; there is no configurable origin environment variable today. Keep the database private and avoid publishing the API debug port publicly.
7. Back up, run migrations, check readiness, and repeat registration/login, upload/transcoding, signed playback and seek, collections, ratings, and history in the target browser. Test role/ownership rejection and recovery as well. Retain a release-specific rollback/recovery procedure, database/object archives, and the previous images. Record the actual URLs, provider, release identifier, and operational owner when hosting is introduced.

The API and frontend remain separate images. Compose currently offers readiness checks using OpenAPI/static HTTP, not a dedicated health/metrics endpoint. Durable worker retries/recovery, scheduled tested backups, and automated hosted deployment are still unimplemented.
