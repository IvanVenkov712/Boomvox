# Boomvox API reference

This reference documents the implemented controllers and DTOs reviewed on 2026-10-29. The local development base URL is **`http://localhost:9090/api`**. There is no hosted API. Paths in the tables below are relative to `/api`.

Generated descriptions are available from a running backend at `/v3/api-docs` and `/swagger-ui/index.html`, outside the `/api` prefix. See the [backend README](../Backend/README.md) for installation and the [controller map](Controllers.md) for endpoint ownership.

## Conventions and authentication

- JSON field names use camelCase. IDs are numeric; timestamps are ISO-8601 strings. Entity DTOs generally use local timestamps without an offset; stream expiry is an `Instant` with a UTC offset.
- Send `Content-Type: application/json` for JSON requests. Song upload uses multipart parts named `audio` and `metadata`, with `metadata` typed as `application/json`.
- Send `Authorization: Bearer <token>` for authenticated requests. Register/login return `{ token, email, role }`; refresh returns `{ token }`.
- All catalogue, collection, feedback, recommendation, user, and streaming endpoints require authentication, including browsing. `GUEST` is a possible account role, not anonymous access.
- `/admin/**` is restricted to `ADMIN` by the HTTP security chain. Catalogue writes marked **Author intent** below declare `AUTHOR`/`ADMIN` method annotations, but `@EnableMethodSecurity` is missing, so that restriction is currently **not enforced** by those annotations.
- `/auth/**` is permitted by the HTTP chain. Refresh/logout still dereference an authenticated principal and therefore need a valid token in practice; without one they can fail with a server error.
- Lists are plain JSON arrays unless explicitly marked `Page<...>`. Only listening history accepts Spring pagination (`page`, `size`, `sort`). The custom `PageResponse` class is not used by these controllers.

The [known problems](known-problems.md) describe security, exception-mapping, and frontend contract gaps. An intended permission or error status should not be mistaken for currently enforced behaviour.

## Authentication

| Method | Path | Authentication | Body | Success |
| --- | --- | --- | --- | --- |
| POST | `/auth/register` | None | `RegisterRequest` | 201 `AuthResponse` |
| POST | `/auth/login` | None | `LoginRequest` | 200 `AuthResponse` |
| POST | `/auth/refresh` | Bearer principal required by controller | None | 200 `RefreshResponse` |
| POST | `/auth/logout` | Bearer principal required by controller | None | 204, empty |
| POST | `/auth/forgot-password` | None | `ForgotPasswordRequest` | 200 `PasswordResetTokenResponse`, or empty if email is unknown |
| POST | `/auth/reset-password` | None | `ResetPasswordRequest` | 200, empty |

Registration always creates `ORDINARY_USER`, regardless of any extra role field. Passwords require at least eight characters. Login takes email, not username. Logout performs no server-side revocation. Refresh reissues an access token; no separate refresh-token cookie or token store is implemented.

**The current password-reset endpoint returns the reset token directly to its unauthenticated caller.** It is stored on the user with a one-hour expiry; no email delivery is implemented. This flow must be fixed before public exposure.

## Users and administration

| Method | Path | Body / query | Success |
| --- | --- | --- | --- |
| GET | `/users/me` | None | 200 `UserResponse` |
| PUT | `/users/me` | `UpdateUserRequest` | 200 `UserResponse` |
| GET | `/users/{userId}` | None | 200 `UserResponse` |
| GET | `/users/me/listening-history` | `page`, `size`, `sort` | 200 `Page<ListeningHistoryResponse>` |
| GET | `/users/me/preferences` | None | 200 `UserPreferenceResponse` |
| GET | `/admin/users` | Optional `search`, `role` | 200 `UserResponse[]`; `ADMIN` only |
| PATCH | `/admin/users/{userId}/role` | `AdminPromoteRequest` | 200 `UserResponse`; `ADMIN` only |

The profile update accepts only username, first name, and last name. `UserResponse` includes email for both self and other-user requests; there is no separate public-profile DTO. Administrator role changes accept backend `UserRole` values. There is no seeded admin; see [account setup](../Backend/README.md#accounts-and-roles).

## Songs, tags, albums, and artists

| Method | Path | Body / query | Success | Access |
| --- | --- | --- | --- | --- |
| GET | `/songs` | Optional `search`, `genre`, `format`, `albumId`, `artistId`, `tagId` | 200 `SongResponse[]` | Bearer |
| POST | `/songs` | Multipart `audio` + `metadata` (`SongUploadRequest`) | 201 `SongResponse` | Author intent |
| GET | `/songs/{songId}` | None | 200 `SongResponse` | Bearer |
| PUT | `/songs/{songId}` | `SongRequest` | 200 `SongResponse` | Author intent |
| DELETE | `/songs/{songId}` | None | 204, empty | Author intent |
| POST | `/songs/{songId}/tags` | `SongTagRequest` | 201 `SongTagResponse` | Author intent |
| DELETE | `/songs/{songId}/tags/{tagId}` | None | 204, empty | Author intent |
| GET | `/tags` | Optional `search` | 200 `TagResponse[]` | Bearer |
| POST | `/tags` | `TagRequest` | 201 `TagResponse` | Author intent |
| GET | `/albums` | Optional `search`, `artistId`, `genre` | 200 `AlbumResponse[]` | Bearer |
| GET | `/albums/{albumId}` | None | 200 `AlbumResponse` | Bearer |
| POST | `/albums` | `AlbumRequest` | 201 `AlbumResponse` | Author intent |
| PUT | `/albums/{albumId}` | `AlbumRequest` | 200 `AlbumResponse` | Author intent |
| GET | `/albums/{albumId}/songs` | None | 200 `SongResponse[]` | Bearer |
| GET | `/artists` | None | 200 `UserResponse[]` | Bearer |
| GET | `/artists/{artistId}` | None | 200 `UserResponse` | Bearer |
| GET | `/artists/{artistId}/songs` | None | 200 `SongResponse[]` | Bearer |
| GET | `/artists/{artistId}/albums` | None | 200 `AlbumResponse[]` | Bearer |

Catalogue browsing/search returns `ACTIVE` songs. Direct song lookup and the album/artist song lists do not apply that same status filter. `genre` and `artistId` are resolved through a song's album. `format` filters the original song format, rather than the available encoded variants. Artist listing selects `AUTHOR` accounts; artist detail retrieves a user by ID without checking that role.

Upload accepts only the exact MIME types `audio/mpeg` and `audio/wav`. The controller rejects files larger than 100 MiB; the dev servlet request/file limits are also `100MB`. Upload metadata permits a null/omitted `albumId`, though the JPA mapping still conflicts with that behaviour. The service derives format from MIME type, and the job later replaces duration using FFprobe. A 201 response indicates upload acceptance, not completed transcoding; poll the returned song until `processingStatus` is `ACTIVE` or `FAILED`.

`SongRequest` is a different contract from upload metadata: it requires format, duration, file size, and storage key. The update implementation changes name, duration, file size, and storage key; it looks up the submitted album but does not reassign album or format. It is not an audio replacement endpoint.

For tag attachment, include `songId` in the body and keep it equal to the path ID: the current implementation uses the body ID without checking the path ID. There is no `GET /songs/{songId}/tags` or `DELETE /albums/{albumId}` mapping, despite frontend calls to those paths.

## Playlists and favourites

Every operation below uses the authenticated user's identity. Playlist and favourites service methods check collection ownership.

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| GET | `/playlists` | None | 200 `PlaylistResponse[]` for current user |
| POST | `/playlists` | `PlaylistRequest` | 201 `PlaylistResponse` |
| GET | `/playlists/{playlistId}` | None | 200 `PlaylistResponse` |
| DELETE | `/playlists/{playlistId}` | None | 204, empty |
| GET | `/playlists/{playlistId}/songs` | None | 200 `PlaylistSongResponse[]`, ordered by position |
| POST | `/playlists/{playlistId}/songs` | `PlaylistSongRequest` | 201 `PlaylistSongResponse` |
| DELETE | `/playlists/{playlistId}/songs/{songId}` | None | 204, empty |
| GET | `/favourites` | None | 200 `FavouritesListResponse` |
| GET | `/favourites/songs` | None | 200 `FavouritesListSongResponse[]` |
| POST | `/favourites/songs` | `FavouritesListSongRequest` | 201 `FavouritesListSongResponse` |
| DELETE | `/favourites/songs/{songId}` | None | 204, empty |

Membership responses contain IDs, position, and time added; they do not embed song details. Fetch song details separately when needed. Favourites addition requires the current user's `favouritesListId`, available from `/favourites` or `/users/me`. Adding the same song twice raises a custom validation exception. There is no playlist rename, sharing, public discovery, reorder endpoint, or arbitrary favourites-list CRUD controller.

## Ratings and statistics

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| POST | `/songs/{songId}/ratings` | `RatingRequest` | 201 `RatingResponse`, including when replacing an existing rating |
| GET | `/songs/{songId}/ratings/me` | None | 200 `RatingResponse` |
| DELETE | `/songs/{songId}/ratings/me` | None | 204, empty |
| GET | `/songs/{songId}/ratings` | None | 200 `RatingResponse[]` |
| GET | `/songs/{songId}/ratings/average` | None | 200 JSON number; 0 when no ratings exist |
| GET | `/users/me/ratings` | None | 200 `RatingResponse[]` |
| GET | `/songs/{songId}/stats` | None | 200 `SongStatsResponse` |

Grades are integers from 0 to 10, and comments are required. A user has at most one rating per song; POST updates it if present. Keep body `songId` equal to the URL ID: the controller currently delegates the body without validating the path/body match. The stats GET recalculates and persists the rating count/average; other counters are not maintained by all relevant operations.

There is no separate comments or reports API. A rating's `comment` field is the implemented textual feedback.

## Streaming and sessions

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| GET | `/songs/{songId}/stream` | None | 200 `StreamUrlResponse` |
| PATCH | `/streaming/sessions/{id}` | `UpdateSessionRequest` | 204, empty |
| POST | `/streaming/events` | `LogEventRequest` | 204, empty |

A stream request returns 503 with an empty body if the song is not `ACTIVE`. An active song still needs a `READY` variant; absence raises a custom `NotFoundException`. `GUEST` selects the default variant; `ORDINARY_USER`, `AUTHOR`, and `ADMIN` select the highest bitrate. The signed URL is for direct browser-to-S3 playback.

Every stream request starts a session and logs `START`. Backend terminal statuses are `COMPLETED`, `SKIPPED`, and `ABANDONED`; `ACTIVE` is also an enum value, but the update DTO does not restrict final transitions. The close operation creates one history row per session, with elapsed wall-clock time capped by song duration. The service does not record an `END` event automatically.

The event DTO actually bound by the controller is **`LogEventRequest`**, not the stricter unused `StreamingEventRequest`. Its fields are not annotated with validation constraints. The session/event endpoints require authentication but do not check whether the session belongs to the caller.

The response's `expiresAt` always reports 15 minutes ahead, while the signer uses `aws.s3.signedUrlExpiryMinutes`. Keep the default until these are aligned. The signer also hardcodes response content type `audio/mpeg`.

## Recommendations

| Method | Path | Query / body | Success |
| --- | --- | --- | --- |
| GET | `/recommendations` | Optional `limit`, default 20 | 200 `RecommendationResponse[]` |
| POST | `/recommendations/generate` | Optional `limit`, default 20; no body | 200 `RecommendationResponse[]` |
| POST | `/recommendations/{songId}/click` | No body | 200 `RecommendationResponse` |

Limits must be 1–100. GET returns saved recommendations without automatically generating them. Generate replaces the current user's saved list using the [scoring algorithm](architecture.md#recommendations), excluding listened-to and favourited songs. A percent is a relative score, not a probability. Click currently only looks up and returns the saved row; no click count or event is persisted.

## Request contracts

`?` denotes nullable or optional fields. Runtime Bean Validation constraints are noted where present; service/database constraints may add restrictions.

| DTO | JSON fields and constraints |
| --- | --- |
| `RegisterRequest` | `username`, `email` (valid email), `password` (min 8), `firstName`, `lastName`; all nonblank |
| `LoginRequest` | `email` (valid, nonblank), `password` (nonblank) |
| `ForgotPasswordRequest` | `email` (valid, nonblank) |
| `ResetPasswordRequest` | `token` (nonblank), `newPassword` (nonblank, min 8) |
| `UpdateUserRequest` | `username`, `firstName`, `lastName` (all nonblank, max 20 each) |
| `AdminPromoteRequest` | `role: UserRole`; no Bean Validation constraints |
| `AlbumRequest` | `name` (nonblank), `genre: Genre` (nonnull) |
| `SongUploadRequest` | `albumId?: number`, `name` (nonblank), `format?: SongFormat`, `duration: number` (seconds, no validation annotation) |
| `SongRequest` | `albumId` (positive), `name` (nonblank), `format` (nonnull), `duration` (positive seconds), `fileSize` (nonnegative bytes), `storageKey` (nonblank) |
| `TagRequest` | `word` (nonblank) |
| `SongTagRequest` | `songId`, `tagId` (positive) |
| `PlaylistRequest` | `name` (nonblank) |
| `PlaylistSongRequest` | `songId` (positive), `position` (nonnegative) |
| `FavouritesListSongRequest` | `favouritesListId`, `songId` (positive), `position` (nonnegative) |
| `RatingRequest` | `songId` (positive), `grade` (integer 0–10), `comment` (nonblank) |
| `UpdateSessionRequest` | `status: SessionStatus`; no Bean Validation constraints |
| `LogEventRequest` | `sessionId`, `type: StreamingEventType`, `positionSec`, `seekFromSec?`, `seekToSec?`; no Bean Validation constraints |

The upload duration is provisional; FFprobe supplies the processed duration. The frontend currently sends `genre` instead of upload `format`/`duration`; genre is not a field in the backend upload DTO. Registration DTOs lack the 20/50-character limits present on several database columns, so database failures can occur for oversized fields.

## Response contracts

All properties below are returned at the top level of the named DTO. `?` marks fields that can be null. Integer counts and sizes are JSON numbers.

| DTO | Fields |
| --- | --- |
| `AuthResponse` | `token`, `email`, `role` |
| `RefreshResponse` | `token` |
| `PasswordResetTokenResponse` | `resetToken` |
| `UserResponse` | `id`, `username`, `email`, `firstName`, `lastName`, `role`, `favouritesListId?` |
| `AlbumResponse` | `id`, `name`, `createdAt`, `authorId`, `genre` |
| `SongResponse` | `id`, `albumId?`, `name`, `uploadedAt`, `format`, `duration` (seconds), `fileSize` (bytes), `storageKey`, `statsId?`, `processingStatus` |
| `TagResponse` | `id`, `word` |
| `SongTagResponse` | `songId`, `tagId`, `addedAt` |
| `PlaylistResponse` | `id`, `name`, `createdAt`, `ownerId` |
| `PlaylistSongResponse` | `playlistId`, `songId`, `position`, `addedAt` |
| `FavouritesListResponse` | `id`, `name`, `createdAt` |
| `FavouritesListSongResponse` | `favouritesListId`, `songId`, `position`, `addedAt` |
| `RatingResponse` | `userId`, `songId`, `grade`, `comment`, `lastUpdatedAt` |
| `SongStatsResponse` | `id`, `ratingsCount`, `avgRating`, `playsCount`, `playlistsCount`, `recommendationsCount` |
| `StreamUrlResponse` | `streamUrl`, `expiresAt`, `sessionId`, `durationSec`, `bitrateKbps`, `format` |
| `ListeningHistoryResponse` | `id`, `songId`, `songTitle`, `listenedAt`, `listenedDurationSec` |
| `UserPreferenceResponse` | `userId`, `genreScores`, `artistScores`, `tagScores` (JSON maps of scores) |
| `RecommendationResponse` | `userId`, `songId`, `percent` |

History returns Spring's `Page` representation: use `content`, `number` (zero-based page), `size`, `totalElements`, and `totalPages`. Do not expect the unused custom `{ page, ... }` wrapper. Artist IDs in preference maps are JSON object keys and therefore appear as strings.

## Enums

| Enum | Backend values |
| --- | --- |
| `UserRole` | `ADMIN`, `AUTHOR`, `ORDINARY_USER`, `GUEST` |
| `Genre` | `METAL`, `POP_FOLK`, `POP`, `ROCK`, `COUNTRY`, `TECHNO` |
| `SongFormat` | `MP3`, `WAV`, `FLAC`, `AAC` |
| `SongProcessingStatus` | `PROCESSING`, `ACTIVE`, `FAILED` |
| `AudioVariantStatus` | `PROCESSING`, `READY`, `FAILED` |
| `SessionStatus` | `ACTIVE`, `COMPLETED`, `SKIPPED`, `ABANDONED` |
| `StreamingEventType` | `START`, `PAUSE`, `RESUME`, `SEEK`, `REPLAY`, `END` |

An enum value existing does not imply an upload MIME type or a UI control supports it. In particular, FLAC/AAC are not accepted upload types, and frontend `INTERRUPTED` is not a backend session status.

## Errors and limitations

Success statuses in the endpoint tables are explicit controller responses. Invalid JSON, invalid enum values, and failures of annotated Bean Validation normally produce HTTP 400. Spring Security handles access failures, but no custom authentication entry point or common JSON error response is configured; do not assume every authentication failure is HTTP 401.

`NotFoundException`, `ValidationException`, `StorageException`, and `AudioProcessingException` have no HTTP status mapping or controller advice. Missing resources, incorrect credentials, duplicate membership, and service-level validation can therefore surface as HTTP 500 rather than the intended 404/400. `ErrorResponse` exists but is not wired into exception handling. There is no stable application-specific error envelope to depend on yet.

Song and playlist deletion can fail on dependent foreign keys; S3 deletes are not transactional with PostgreSQL. Method annotations and session ownership are also incomplete. See [known problems](known-problems.md) for evidence and follow-up.

## Examples

These examples use a POSIX shell and `curl`. On Windows, use an equivalent JSON-capable REST client or adjust quoting for `curl.exe`/PowerShell. Replace IDs and `<token>` with values from your running local application. No example domain or credential represents a deployed service.

Register (creates an ordinary user):

```sh
curl -X POST http://localhost:9090/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"listener","email":"listener@example.test","password":"local-example-password","firstName":"Local","lastName":"Listener"}'
```

Log in and copy `token` from the response:

```sh
curl -X POST http://localhost:9090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"listener@example.test","password":"local-example-password"}'
```

Browse, filter, and inspect history:

```sh
curl 'http://localhost:9090/api/songs?search=demo&genre=ROCK' \
  -H 'Authorization: Bearer <token>'

curl 'http://localhost:9090/api/users/me/listening-history?page=0&size=20&sort=listenedAt,desc' \
  -H 'Authorization: Bearer <token>'
```

With an author/admin account, put the following in a local `metadata.json` (use a real album ID, or null for an albumless upload subject to the known mapping issue):

```json
{"albumId":1,"name":"Local demo","format":"MP3","duration":180}
```

Upload and poll:

```sh
curl -X POST http://localhost:9090/api/songs \
  -H 'Authorization: Bearer <token>' \
  -F 'audio=@demo.mp3;type=audio/mpeg' \
  -F 'metadata=@metadata.json;type=application/json'

curl http://localhost:9090/api/songs/1 \
  -H 'Authorization: Bearer <token>'
```

Request playback after the song is `ACTIVE`:

```sh
curl http://localhost:9090/api/songs/1/stream \
  -H 'Authorization: Bearer <token>'
```

Use `streamUrl` only for playback and use the returned `sessionId` for activity requests. For an example session ID of 7:

```sh
curl -X POST http://localhost:9090/api/streaming/events \
  -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' \
  -d '{"sessionId":7,"type":"SEEK","positionSec":45,"seekFromSec":12,"seekToSec":45}'

curl -X PATCH http://localhost:9090/api/streaming/sessions/7 \
  -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' \
  -d '{"status":"COMPLETED"}'

curl -X POST http://localhost:9090/api/songs/1/ratings \
  -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' \
  -d '{"songId":1,"grade":8,"comment":"Enjoyed this track"}'

curl -X POST 'http://localhost:9090/api/recommendations/generate?limit=20' \
  -H 'Authorization: Bearer <token>'
```
