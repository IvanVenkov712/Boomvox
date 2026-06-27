# BoomVox — Controllers Reference

> Reflects actual DTOs and enums in the project as of this writing.
> Enums available: `AudioVariantStatus`, `Genre`, `SessionStatus`, `SongFormat`, `SongProcessingStatus`, `StreamingEventType`, `UserRole`.
> No separate `Style` entity/DTOs — `Genre` is an enum field on songs. No separate `Artist` entity either — artists are `User` entities with `UserRole.ARTIST`, exposed via dedicated `/api/artists` endpoints on `LibraryController` (list, detail, songs, albums) backed by `UserResponse`/`SongResponse`/`AlbumResponse`.
> Comments, Reports, and Admin controllers are **not yet built** — pending DTOs.

---

## 1. AuthController

**Base path:** `/api/auth`
**Service:** `AuthService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| POST | `/register` | none | `RegisterRequest` | `AuthResponse` (201) |
| POST | `/login` | none | `LoginRequest` | `AuthResponse` |
| POST | `/refresh` | Bearer | — | `RefreshResponse` |
| POST | `/logout` | Bearer | — | — (204) |
| POST | `/forgot-password` | none | `ForgotPasswordRequest` | `PasswordResetTokenResponse` |
| POST | `/reset-password` | none | `ResetPasswordRequest` | — (200) |

Notes:
- `register`/`login` issue a JWT bundled in `AuthResponse`.
- `refresh` reads the current `UserPrincipal` and reissues a token.
- `forgot-password` returns a reset token directly in the response (no mail service).
- Reset token is stored on the `User` entity with a 1-hour expiry.

---

## 2. UserController

**Base path:** `/api/users`
**Services:** `UserService`, `UserPreferenceService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/me` | Bearer | — | `UserResponse` |
| PUT | `/me` | Bearer | `UpdateUserRequest` | `UserResponse` |
| GET | `/{userId}` | none | — | `UserResponse` |
| GET | `/me/listening-history` | Bearer | `Pageable` | `Page<ListeningHistoryResponse>` |
| GET | `/me/preferences` | Bearer | — | `UserPreferenceResponse` |

Notes:
- Public profile view (`/{userId}`) uses the same `UserResponse` DTO, restricted at the service layer.
- `UpdateUserRequest` only allows updating `username`, `firstName`, `lastName` — email, role, and password are not editable here.
- Listening history uses a dedicated `ListeningHistoryResponse` DTO.

---

## 3. SongController

**Base path:** `/api/songs`
**Service:** `SongService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| POST | `` (multipart: `audio`, `metadata`) | `ARTIST` | `SongUploadRequest` | `SongResponse` (201) |

Notes:
- Validates MIME type (`audio/mpeg`, `audio/wav`) and max size (100MB) before delegating to `SongService.uploadSong`.
- Upload triggers async processing (FFmpeg variant generation) — song starts in `SongProcessingStatus.PROCESSING`.

---

## 4. StreamingController

**Base path:** `/api/songs`
**Services:** `PlaybackService`, `FileStorageService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/{songId}/stream` | Bearer | — | `StreamUrlResponse` |

Notes:
- Gates on `SongProcessingStatus.ACTIVE` — returns 503 otherwise.
- Selects `AudioVariant` by `UserRole`: `GUEST` gets the default variant, all other roles get the highest-bitrate `READY` variant.
- Signs the variant's streaming key via `FileStorageService`.
- Starts a `StreamingSession` as a side effect of issuing the URL.

---

## 5. StreamingSessionController

**Base path:** `/api/streaming`
**Service:** `PlaybackService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| PATCH | `/sessions/{id}` | Bearer | `UpdateSessionRequest` | — (204) |
| POST | `/events` | Bearer | `LogEventRequest` | — (204) |

Notes:
- `PATCH /sessions/{id}` closes a session (`SessionStatus`: `COMPLETED`/`SKIPPED`/`INTERRUPTED`).
- `POST /events` is fire-and-forget from the Angular client for `StreamingEventType` values (`PAUSE`, `RESUME`, `SEEK`, `REPLAY`, etc.) mid-session.

---

## 6. LibraryController

**Base path:** `/api`
**Services:** `SongService`, `SongTagService`, `TagService`, `AlbumService`, `UserService`

### Songs

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/songs` | none | query params (see below) | `List<SongResponse>` |
| GET | `/songs/{songId}` | none | — | `SongResponse` |
| PUT | `/songs/{songId}` | `ARTIST`/`ADMIN` | `SongRequest` | `SongResponse` |
| DELETE | `/songs/{songId}` | `ARTIST`/`ADMIN` | — | — (204) |

**Browse query params:** `search`, `genre` (`Genre` enum), `format` (`SongFormat` enum), `albumId`, `artistId`, `tagId`.

### Song ↔ Tag

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| POST | `/songs/{songId}/tags` | `ARTIST`/`ADMIN` | `SongTagRequest` | `SongTagResponse` (201) |
| DELETE | `/songs/{songId}/tags/{tagId}` | `ARTIST`/`ADMIN` | — | — (204) |

### Tags (entity)

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/tags` | none | `search` query param | `List<TagResponse>` |
| POST | `/tags` | `ARTIST`/`ADMIN` | `TagRequest` | `TagResponse` (201) |

### Albums

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/albums` | none | `search`, `artistId`, `genre` query params | `List<AlbumResponse>` |
| GET | `/albums/{albumId}` | none | — | `AlbumResponse` |
| POST | `/albums` | `ARTIST`/`ADMIN` | `AlbumRequest` | `AlbumResponse` (201) |
| PUT | `/albums/{albumId}` | `ARTIST`/`ADMIN` | `AlbumRequest` | `AlbumResponse` |

### Artists

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/artists` | none | — | `List<UserResponse>` |
| GET | `/artists/{artistId}` | none | — | `UserResponse` |
| GET | `/artists/{artistId}/songs` | none | — | `List<SongResponse>` |
| GET | `/artists/{artistId}/albums` | none | — | `List<AlbumResponse>` |

Notes:
- Ownership checks for update/delete live in the service layer.
- `Genre` filtering uses the enum directly; there is no `Style` concept in this project.
- There is no `Artist` entity — artists are `User` entities with `UserRole.ARTIST`. `GET /artists` is backed by `UserRepository.findByRole(UserRole)` and `UserService.getArtists()`, reusing the same `UserResponse` mapping as `/api/users/me` and `/api/users/{userId}`.
- No pagination or search/filter params on `/artists` — returns the full list, matching the spec's lack of query params for this endpoint.
- `GET /artists/{artistId}` reuses `UserService.getUserById`, the same lookup used by `/api/users/{userId}`.
- `GET /artists/{artistId}/songs` and `GET /artists/{artistId}/albums` are dedicated artist-page endpoints (`SongService.getSongsByArtist`, `AlbumService.getAlbumsByAuthor`) — together with the artist's `UserResponse`, these three calls back a full artist profile page on the frontend.

---

## 7. CollectionController

**Base path:** `/api`
**Services:** `PlaylistService`, `FavouritesService`

### Playlists

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/playlists` | Bearer | — | `List<PlaylistResponse>` |
| POST | `/playlists` | Bearer | `PlaylistRequest` | `PlaylistResponse` (201) |
| GET | `/playlists/{playlistId}` | Bearer | — | `PlaylistResponse` |
| DELETE | `/playlists/{playlistId}` | Bearer | — | — (204) |
| POST | `/playlists/{playlistId}/songs` | Bearer | `PlaylistSongRequest` | `PlaylistSongResponse` (201) |
| DELETE | `/playlists/{playlistId}/songs/{songId}` | Bearer | — | — (204) |

### Favourites

One implicit list per user — no list ID in the path.

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/favourites` | Bearer | — | `FavouritesListResponse` |
| POST | `/favourites/songs` | Bearer | `FavouritesListSongRequest` | `FavouritesListSongResponse` (201) |
| DELETE | `/favourites/songs/{songId}` | Bearer | — | — (204) |

Notes:
- `PlaylistRequest` only contains `name` — owner is always taken from the authenticated user, never from the request body.
- Favourites assume **one list per user** by design.
- Listen Later feature removed — no backing entities exist.

---

## 8. EngagementController

**Base path:** `/api`
**Service:** `EngagementService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| POST | `/songs/{songId}/ratings` | Bearer | `RatingRequest` | `RatingResponse` (201) |
| GET | `/songs/{songId}/ratings/me` | Bearer | — | `RatingResponse` |
| DELETE | `/songs/{songId}/ratings/me` | Bearer | — | — (204) |
| GET | `/songs/{songId}/ratings` | none | — | `List<RatingResponse>` |
| GET | `/songs/{songId}/ratings/average` | none | — | `Double` |
| GET | `/users/me/ratings` | Bearer | — | `List<RatingResponse>` |
| GET | `/songs/{songId}/stats` | none | — | `SongStatsResponse` |

Notes:
- `RatingRequest` no longer contains `userId` — user is always taken from the authenticated principal.
- Rating is upserted (create or update) — `POST` returns 201 on both create and update.
- `DELETE /songs/{songId}/ratings/me` deletes the current user's rating and recomputes song stats.
- `GET /songs/{songId}/stats` triggers a live recompute of rating stats before returning.

---

## 9. RecommendationController

**Base path:** `/api/recommendations`
**Service:** `RecommendationService`

| Method | Path | Auth | Request DTO | Response DTO |
|--------|------|------|-------------|---------------|
| GET | `/me` | Bearer | query params (see below) | `List<RecommendationResponse>` |
| GET | `/similar/{songId}` | none | `limit` query param | `List<RecommendationResponse>` |
| POST | `/{recommendationId}/click` | Bearer | — | `RecommendationResponse` |

**`/me` query params:** `limit` (default 20), `sourceType` (`RecommendationSource`-style filter — generated client-side from `CONTENT_BASED`, `COLLABORATIVE`, `ML_MODEL`, `GENRE_BASED`, `MANUAL`).

Notes:
- `/me` generates/fetches personalized recommendations for the authenticated user based on ratings, listening history, favourites, and preferred genres; backed by `RecommendationService` and `RecommendationRequest`/`RecommendationResponse`.
- `/similar/{songId}` is public and returns songs similar to a given song — no personalization, no auth required.
- `POST /{recommendationId}/click` logs that the user engaged with a recommendation (improves future ranking) and returns the updated `RecommendationResponse`.
- User identity for `/me` and `/{recommendationId}/click` is always taken from `UserPrincipal`, never from the request body.

---

## 10. Pending — Not Yet Built

| Controller | Blocked on | Notes |
|------------|------------|-------|
| `CommentController` | no Comment DTOs | Skipped per current decision. |
| `ReportController` | no Report DTOs | Skipped per current decision. |
| `AdminController` | no admin-user/report DTOs | Skipped; would cover user status changes and song visibility once decided. |

---

## Cross-Cutting Conventions

- All controllers use constructor injection, `ResponseEntity<T>` return types, and `@AuthenticationPrincipal UserPrincipal` for the current user.
- Ownership/ACL checks live in the **service layer**, not the controller — controllers only declare role-level `@PreAuthorize` where the check is role-based.
- 201 Created for all resource-creation endpoints; 204 No Content for deletions and fire-and-forget updates.
- `@Valid` on all `@RequestBody` and `@RequestPart` parameters.
- User identity is never trusted from the request body — always taken from `UserPrincipal` (applies to `RatingRequest`, `PlaylistRequest`).
