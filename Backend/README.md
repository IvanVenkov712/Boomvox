# Boomvox backend

The backend is a Spring Boot REST application for accounts, the music catalogue, personal collections, ratings, playback tracking, and recommendations. It accepts audio uploads, stores files in S3, and runs FFmpeg in background threads. Audio playback goes directly from S3 to the browser.

## Stack

| Component | Repository configuration |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.4.5 |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL, Liquibase |
| Authentication | Spring Security, JJWT 0.12.6, encoded passwords |
| API documentation | springdoc 2.8.4 |
| Storage | AWS SDK for Java v2, S3 client and presigner |
| Audio | FFmpeg/FFprobe executables through `net.bramp.ffmpeg` 0.9.1 |
| Build | Maven wrapper; artifact `bg.uni.fmi:boomvox:0.0.1-SNAPSHOT` |

The POM includes a Spring AI Ollama starter, while configuration also mentions OpenAI. Neither is used by `RecommendationService`; see [configuration issues](../docs/known-problems.md#configuration-and-deployment).

## Install and run

### 1. Install prerequisites

On Windows:

1. Download a JDK 21 installer from the [Temurin releases page](https://adoptium.net/temurin/releases/?version=21), select the JDK package for your architecture, and install it. Set `JAVA_HOME` to the JDK directory and add its `bin` directory to `PATH` if the installer has not done so.
2. Use an installer linked by the [PostgreSQL Windows downloads page](https://www.postgresql.org/download/windows/). Install the server and command-line tools, retain port `5432`, and remember the administrator password you choose. Add the PostgreSQL `bin` directory to `PATH` to use `psql`. The repository does not pin a PostgreSQL major version.
3. Get a Windows executable build linked from the [FFmpeg downloads page](https://ffmpeg.org/download.html), extract it to a stable directory, and add the directory containing `ffmpeg.exe` and `ffprobe.exe` to `PATH`. The build needs MP3 (`libmp3lame`) and AAC encoders.

On macOS/Linux, install the same JDK 21, PostgreSQL, and FFmpeg/FFprobe prerequisites using the downloads for your OS or its package manager. Start PostgreSQL and ensure the required binaries are on `PATH`. Set `JAVA_HOME` to the JDK installation directory.

Check the tools in a new terminal after changing `PATH`:

```sh
java -version
psql --version
ffmpeg -version
ffprobe -version
```

The Maven wrapper downloads its Maven distribution and dependencies on first use, so that step needs internet access. FFmpeg is a native prerequisite; the Maven dependency does not install its executables.

### 2. Prepare PostgreSQL

The default `dev` profile connects to database `postgres`, as role `boomvox`, and uses schema `boomvox`. It does **not** connect to a database named `boomvox`.

For a fresh local database installation, connect as the PostgreSQL administrator:

```sh
psql -U postgres -d postgres
```

Run once, if the role does not already exist:

```sql
CREATE ROLE boomvox WITH LOGIN PASSWORD 'boomvox_pass';
GRANT CONNECT, CREATE ON DATABASE postgres TO boomvox;
```

`boomvox_pass` is the checked-in development password. If you choose other credentials or a different PostgreSQL host/database, override `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` in your local `.env` or use their environment variables from the [configuration reference](../docs/deployment.md#configuration-reference).

For development, Hikari executes `CREATE SCHEMA IF NOT EXISTS boomvox` before Liquibase starts. The role therefore needs permission to create that schema or must own an existing schema. Liquibase applies the 21 included SQL changelog files; Hibernate validates the resulting mappings. Keep the `boomvox` schema: entity mappings, native SQL, and migration 021 explicitly reference it.

### 3. Configure storage and local properties

Create a private S3 bucket and credentials that can put, get, and delete objects in it. Follow the [storage guide](s3_music_platform_guide.md) for the object layout and browser integration.

From `Backend/`, copy the example **if you do not already have `.env`**:

```powershell
Copy-Item .env.example .env
```

On macOS/Linux, use `cp -n .env.example .env`. Edit the copied file, replace all required placeholders, and set a unique JWT signing secret of at least 32 bytes. The file uses Java properties syntax (`aws.region=...`, without `export` or shell quotes); Spring imports it relative to the process working directory.

The example covers the required AWS and JWT properties plus optional database overrides. The [full reference](../docs/deployment.md#configuration-reference) explains every setting and its OS environment-variable equivalent. Do not commit credentials or reuse the repository's built-in JWT secret outside disposable local development.

### 4. Start the API

Run from `Backend/` so the relative `.env` import resolves:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```sh
./mvnw spring-boot:run
```

If the wrapper is not executable on macOS/Linux, run `chmod +x mvnw` first. The default `dev` profile listens on **9090**, overriding the base configuration's 8099.

| Local URL | Purpose |
| --- | --- |
| `http://localhost:9090/api` | REST API base |
| `http://localhost:9090/swagger-ui/index.html` | Swagger UI |
| `http://localhost:9090/v3/api-docs` | Generated OpenAPI JSON |

There is no configured Actuator health endpoint. Confirm startup using the application logs and the OpenAPI endpoint. Most API requests require a JWT; see [API examples](../docs/project-endpoints.md#examples).

## Build and test commands

Run commands from this directory. Replace `./mvnw` with `.\mvnw.cmd` on Windows.

| Command | Purpose and prerequisites |
| --- | --- |
| `./mvnw spring-boot:run` | Start the application with the default `dev` profile |
| `./mvnw test` | Run the context startup test; database and application configuration are still required |
| `./mvnw package` | Run tests and create the executable JAR under `target/` |
| `./mvnw package -DskipTests` | Create the JAR without running tests; this does not validate runtime behaviour |
| `java -jar target/boomvox-0.0.1-SNAPSHOT.jar` | Run the packaged application with the default profile |

`TestcontainersConfiguration` currently defines no containers. Selecting `test` alone also lacks an `AppLogger` bean and database setup. The test suite is not self-contained; see [known test issues](../docs/known-problems.md#verification-and-test-coverage).

## Accounts and roles

Registration creates an `ORDINARY_USER` and a personal favourites list. Login uses email and password. The backend roles are `GUEST`, `ORDINARY_USER`, `AUTHOR`, and `ADMIN`; the artist endpoints list users with role `AUTHOR`.

No administrator is seeded. For a fresh local installation, register an account, then use a trusted database administrator connection to assign its initial role:

```sql
UPDATE boomvox."user" SET role = 'ADMIN' WHERE email = 'your-local-admin@example.test';
```

Use the email of the account you registered. Sign out and back in so the UI's cached role is updated. Existing administrators can use `PATCH /api/admin/users/{userId}/role` for subsequent role changes. Role annotations on catalogue writes currently lack method-security activation; this is a [known access-control problem](../docs/known-problems.md#authentication-and-access-control).

## Code layout

All application Java code is under `src/main/java/bg/fmi/uni/boomvox/`.

| Package/path | Responsibility |
| --- | --- |
| `controller/` | HTTP mappings, request validation, principal extraction, and responses |
| `service/` | Business logic, ownership checks for personal collections, recommendation scoring |
| `repository/` | JPA repositories and custom catalogue queries |
| `domain/`, `ids/`, `enums/` | Persistent entities, composite IDs, and domain values |
| `dto/` | Request and response contracts |
| `security/` | JWT creation/validation and user principal loading |
| `job/AudioProcessingJob.java` | Background audio processing and variant persistence |
| `config/` | Security, CORS, S3, background executor, and profile-specific logging |
| `src/main/resources/db/changelog/` | Ordered Liquibase migrations |

See [architecture](../docs/architecture.md), [controllers](../docs/Controllers.md), [services](Services.md), and the [API reference](../docs/project-endpoints.md) for more detail.

## Deployment and troubleshooting

No instance is currently hosted. The `prod` profile changes logging, but still needs explicit database, schema, storage, JWT, and multipart configuration. Follow the [deployment guide](../docs/deployment.md) before using it.

| Symptom | Check |
| --- | --- |
| Database connection or schema error | PostgreSQL process, role/password, `currentSchema=boomvox`, and schema ownership/creation privileges |
| AWS property resolution or credential error | `.env` location, required values, AWS region, and bucket permissions |
| Upload remains `PROCESSING` or becomes `FAILED` | Backend logs, FFmpeg/FFprobe on `PATH`, temp disk space, S3 permissions, and the async transaction issue in known problems |
| Browser cannot call the API | API at port 9090 and frontend origin exactly `http://localhost:4200`; CORS is hardcoded |
| Audio does not play | Song is `ACTIVE`, a `READY` variant exists, signed URL has not expired, and S3 is reachable from the browser |
| Missing resource or invalid login yields an unexpected server error | Custom exceptions do not yet have HTTP exception mapping |

The [known problems](../docs/known-problems.md) distinguish implementation defects from missing operational configuration.
