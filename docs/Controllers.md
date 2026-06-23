# BoomVox — Controllers Reference

> Reflects actual DTOs and enums in the project as of this writing.
> Enums available: `AudioVariantStatus`, `Genre`, `SessionStatus`, `SongFormat`, `SongProcessingStatus`, `StreamingEventType`, `UserRole`.
> No separate `Style` or `Artist` entities/DTOs — `Genre` is an enum field on songs; artist info is embedded in `SongResponse`/`UserResponse` where relevant.
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
**Services:** `SongService`, `SongTagService`, `TagService`, `AlbumService`

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

Notes:
- Ownership checks for update/delete live in the service layer.
- `Genre` filtering uses the enum directly; there is no `Style` concept in this project.

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

## 8. Pending — Not Yet Built

| Controller | Blocked on | Notes |
|------------|------------|-------|
| `EngagementController` | none — `RatingRequest`/`RatingResponse` exist | Ratings only. Maps to ratings CRUD + average-rating recompute side effect. |
| `RecommendationController` | none — `RecommendationRequest`/`RecommendationResponse` exist | Generate/fetch/save recommendations; uses `RecommendationService`. |
| `CommentController` | no Comment DTOs | Skipped per current decision. |
| `ReportController` | no Report DTOs | Skipped per current decision. |
| `AdminController` | no admin-user/report DTOs | Skipped; would cover user status changes and song visibility once decided. |

---

## Cross-Cutting Conventions

- All controllers use constructor injection, `ResponseEntity<T>` return types, and `@AuthenticationPrincipal UserPrincipal` for the current user.
- Ownership/ACL checks live in the **service layer**, not the controller — controllers only declare role-level `@PreAuthorize` where the check is role-based.
- 201 Created for all resource-creation endpoints; 204 No Content for deletions and fire-and-forget updates.
- `@Valid` on all `@RequestBody` and `@RequestPart` parameters.
