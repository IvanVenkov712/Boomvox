# Deployment and configuration

## Current deployment

**Boomvox is not currently deployed to a hosted environment**, confirmed by the maintainer on 2026-10-29. The repository contains local development settings and a partial production profile. There are no checked-in Dockerfiles, Compose files, infrastructure definitions, CI/CD workflows, hosted frontend/API URLs, or deployment scripts.

| Component | Current local configuration | Source |
| --- | --- | --- |
| Frontend | Intended origin `http://localhost:4200`; workspace files must be restored to run it | Frontend source and `SecurityConfig` |
| API with default `dev` profile | `http://localhost:9090/api` | `application-dev.yml` |
| Base API port with `prod`/`test` alone | `8099`, unless overridden | `application.yml` |
| PostgreSQL | `localhost:5432`, database `postgres`, schema `boomvox` | `application-dev.yml` |
| Audio storage | An externally configured private S3 bucket; no bucket identity is tracked in the example | `S3Config`, `FileStorageService` |
| Audio processor | FFmpeg/FFprobe in the backend JVM's host environment, using local temporary files | `AudioProcessingJob` |

Both frontend environment files currently point to the local API on port 9090. Historical documentation's `https://api.Boomvox.com` was an example, not an active service address.

## Installing and starting locally

Use the [backend README](../Backend/README.md#install-and-run) for JDK/PostgreSQL/FFmpeg installation requirements, database role creation, `.env` setup, and startup commands. Use the [frontend README](../Frontend/Boomvox_UI/README.md#install-and-run) for the missing build-file prerequisite and the dependency installation/dev-server commands once those files are restored.

Start the backend from `Backend/`, since configuration imports and log paths are relative to the current working directory. Start the frontend separately on port 4200. An audio-capable backend needs the PostgreSQL connection, AWS settings, FFmpeg binaries, and writable temporary storage.

## How configuration is loaded

The backend loads shared `application.yml`, `application.properties`, and the active profile's YAML. `application.properties` imports these optional files relative to the process working directory:

```properties
spring.config.import=optional:file:.env[.properties],optional:file:aws.env[.properties]
```

Use ignored `Backend/.env` for local values. It is parsed as a **Java properties file**, not as a shell script: write `aws.region=your-region` without `export` or quotes. The `.env.example` uses actual property names so values work directly in this imported file. OS environment variables can be used instead and override file configuration.

`aws.env` is supported by the import but is not currently ignored by `Backend/.gitignore`; avoid storing secrets there. The actual `.env`, AWS keys, database passwords, and JWT signing secret must stay outside version control.

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

The frontend currently supports **no runtime environment variables**. Its `apiUrl` constants are browser-visible build inputs; the missing Angular workspace configuration must be recovered before defining build replacements. Never put server secrets in those files.

## Profile behaviour

| Profile | Port | Database/schema setup | Custom logger |
| --- | --- | --- | --- |
| `dev` (default) | 9090 | Local PostgreSQL, schema init SQL, Liquibase schema, Hibernate validation | `ConsoleAppLogger`; SQL output enabled |
| `prod` | 8099 unless overridden | Not supplied by profile; operator must configure it | `FileAppLogger`, `logs/boomvox-prod.log` |
| `test` alone | 8099 unless overridden | Not supplied by profile; Testcontainers class is empty | No `AppLogger` bean matches this profile; startup needs correction |

`application-test.yml` also uses a stale `theatre` prefix instead of `boomvox`, so its custom logger properties do not bind. Switching to `prod` does not activate the database or multipart settings in `dev`.

## Preparing a future hosted deployment

This is a preparation path, not an existing deployment procedure:

1. Resolve the access-control, password-reset, frontend build, and integration issues in [known problems](known-problems.md).
2. Provision PostgreSQL with a `boomvox` schema. Configure credentials, the JDBC URL, Liquibase schema/changelog, and Hibernate validation explicitly for `prod`. Create the schema beforehand with the application role as owner, or provide the connection-init SQL with appropriate privileges.
3. Provision a private S3 bucket and inject the required storage properties. See the [S3 guide](../Backend/s3_music_platform_guide.md) for the current client behaviour and object layout.
4. Install a Java runtime compatible with the Java 21 build, FFmpeg/FFprobe, and sufficient writable temporary space on the backend host. Set `SPRING_PROFILES_ACTIVE=prod`, a unique `JWT_SECRET`, database/storage configuration, and suitable multipart limits. Give the configured log directory write access.
5. Build from `Backend/` with `./mvnw package` after making test prerequisites available. If deliberately packaging with `-DskipTests`, record that tests were skipped. Launch from the configured working directory:

   ```sh
   java -jar target/boomvox-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

6. Restore the frontend workspace, configure its production API URL and environment replacement, and create a production build. Serve the output with a static host that falls back to `index.html` for client routes. No output directory can be verified until the Angular workspace is restored.
7. Set the API's permitted frontend origin in `SecurityConfig`; it is currently hardcoded to localhost. Configure HTTPS termination and matching public URLs. Apply S3 browser CORS settings if required by the selected browser access mode.
8. Verify registration/login, authenticated catalogue requests, upload/transcoding, S3 playback and seek, collections, ratings, and history. Record public URLs, hosting provider, release identifier, secrets ownership, restart/recovery steps, and database backup/restore procedures when a real deployment is introduced.

The backend serves only the API; there is no checked-in integration that packages the Angular build inside the Spring application. There is also no health/metrics endpoint, durable audio-job retry mechanism, or automated backup/deploy workflow at present.
