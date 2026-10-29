# Controller guide

The backend currently contains ten REST controllers under [`Backend/src/main/java/bg/fmi/uni/boomvox/controller/`](../Backend/src/main/java/bg/fmi/uni/boomvox/controller/). This is a code-navigation guide; the complete endpoint tables, DTO fields, examples, and current access behaviour are in the [API reference](project-endpoints.md).

| Controller | Base mapping | Responsibility | Main collaborators |
| --- | --- | --- | --- |
| `AuthController` | `/api/auth` | Register/login, access-token reissue, stateless logout, password reset | `AuthService` |
| `UserController` | `/api/users` | Self/other-user details, profile update, listening history, preferences | `UserService`, `UserPreferenceService` |
| `AdminController` | `/api/admin` | Search/filter users and change roles | `AdminService` |
| `LibraryController` | `/api` | Song browsing/update/delete, tags, albums, and artist catalogue views | `SongService`, `SongTagService`, `TagService`, `AlbumService`, `UserService` |
| `SongController` | `/api/songs` | Multipart upload, MIME and file-size checks | `SongService` |
| `StreamingController` | `/api/songs` | `GET /{songId}/stream`: variant selection, signed URL, session creation | Song/variant repositories, `FileStorageService`, `PlaybackService` |
| `StreamingSessionController` | `/api/streaming` | Patch final session status and post playback events | Session repository, `PlaybackService` |
| `CollectionController` | `/api` | Current user's playlists/favourites and song memberships | `PlaylistService`, `FavouritesService` |
| `EngagementController` | `/api` | Rating upsert/removal, rating lists, averages, song statistics | `EngagementService` |
| `RecommendationController` | `/api/recommendations` | Saved recommendations, generation, click lookup | `RecommendationService` |

## Contract boundaries

Controllers bind DTOs from `dto/`, usually apply `@Valid` to request bodies, extract `UserPrincipal` for current-user operations, and return `ResponseEntity` with explicit success statuses. Returned records use camelCase fields. Catalogue and collection lists are arrays; user history is a Spring `Page`.

Artists are users with role `AUTHOR`, not a separate entity or `ARTIST` enum value. Genres belong to albums. There are no comments or reports controllers; textual feedback is part of a rating. Administrator functionality is implemented, unlike the earlier design notes.

## Current implementation gaps

- HTTP security requires authentication outside `/api/auth/**` and Swagger paths and enforces `ADMIN` on `/api/admin/**`. Catalogue `@PreAuthorize` checks lack method-security activation.
- Auth refresh/logout permit anonymous requests through the security matcher but assume a non-null principal.
- Streaming-session mutation does not verify session ownership.
- Rating submission and song-tag attachment accept a path song ID but use the body song ID without comparing them.
- `StreamingSessionController` binds the unannotated `LogEventRequest` and `UpdateSessionRequest`; the stricter alternate streaming DTOs are not used there.
- Custom exceptions have no HTTP mapping; `ErrorResponse` is not connected to controller advice.
- Frontend calls to `GET /api/songs/{songId}/tags` and `DELETE /api/albums/{albumId}` have no matching controller methods.

See [known problems](known-problems.md) for source evidence and [service responsibilities](../Backend/Services.md) for the next layer.
