# Service guide

This guide describes the implemented service layer under [`src/main/java/bg/fmi/uni/boomvox/service/`](src/main/java/bg/fmi/uni/boomvox/service/). HTTP contracts live in the [API reference](../docs/project-endpoints.md); overall flows live in [architecture](../docs/architecture.md).

## Responsibilities

| Class | Implemented responsibilities | Important boundaries |
| --- | --- | --- |
| `AuthService` | Register ordinary users/favourites, authenticate email/password, issue/reissue JWTs, create/reset password tokens | Passwords are encoded; logout has no server-side effect; reset tokens are returned directly |
| `UserService` | User CRUD helpers, self-profile update, artist listing, listening history | Artist listing uses `AUTHOR`; history is paginated; other-user responses use the same DTO as self |
| `AdminService` | Search users and update roles | HTTP chain enforces admin access; role request validation is incomplete |
| `SongService` | Upload raw audio, save song/stats, launch processing, catalogue search/filter, update/delete, album/artist song lists | Authorisation/ownership is not checked inside the service; job is launched inside the upload transaction |
| `AlbumService` | Album create/read/update/delete helpers, filtering, author's albums | Create assigns the authenticated caller as author; not every helper has an HTTP endpoint |
| `TagService` | Browse/search and tag CRUD helpers | Controller exposes only browse/create |
| `SongTagService` | Attach/detach tags | Composite song/tag membership; duplicate attachment is rejected |
| `PlaylistService` | Create/delete/read owned playlists, add/remove songs, ordered song listing | Checks owner against current user; rejects duplicate song membership |
| `FavouritesService` | Current user's favourites metadata and song membership | Checks list ownership; rejects duplicate membership |
| `EngagementService` | Upsert/delete one rating per user/song, feedback/average queries, rating-stat refresh | Grade/comment validation; refreshes rating count/average, not every statistics counter |
| `PlaybackService` | Start sessions and `START` events, append events, end sessions and create history | Uses wall-clock elapsed duration; session ownership is not enforced here |
| `UserPreferenceService` | Aggregate listening/favourite/rating scores into genre, artist, and tag preferences | Computed on demand; artist/genre depend on the song's album |
| `RecommendationService` | Score active unseen songs, replace/read saved recommendations, click lookup | Limit 1–100, default 20 at HTTP layer; deterministic scoring, no AI invocation |
| `FileStorageService` | Upload S3 objects, generate signed GET URLs, delete objects and standard variants, build keys | Uses configured bucket; deletes log failures rather than throwing; actual expiry is configurable |
| `BaseService` | Shared query normalisation helper | Not a separate business workflow |

## Persistence and transactions

Business services generally use class-level `@Transactional`, with read-only methods annotated where appropriate. Spring Data repositories handle entity persistence and custom queries. `AdminService` instead relies on repository operations without a class-level service transaction; S3 operations are external to database transactions.

The asynchronous [`AudioProcessingJob`](src/main/java/bg/fmi/uni/boomvox/job/AudioProcessingJob.java) is a component outside the service package. It has its own transaction and executes through `audioProcessingExecutor`. It downloads the raw object, probes/transcodes audio, writes variants, updates song duration/status, and deletes the raw object after success. On failure it attempts to persist `FAILED` and cleans temporary paths.

The upload service currently starts that job before its own transaction commits, and neither a DB rollback nor an S3 failure automatically undoes the other system's changes. Deletion also lacks complete foreign-key cleanup. These are [known consistency problems](../docs/known-problems.md).

## Recommendation inputs

Preferences combine listening ratios, +4 for favourites, and `grade - 5` for ratings. Recommendation scoring weights genre by 3, artist by 2, tags by 1, and popularity by 0.25. It excludes listened-to/favourited songs and only considers `ACTIVE` songs. See the [architecture formula](../docs/architecture.md#recommendations) for the complete calculation and normalisation.

The click method returns an existing recommendation without recording a click. The POM's AI starter and AI configuration are unrelated to this implementation.

## Current gaps

Custom exceptions have no HTTP mapping. Catalogue services lack resource ownership checks, session mutations lack caller ownership checks, and parent deletion does not remove all dependent records. Song metadata update does not reassign album/format even though its request accepts them. Albumless upload conflicts with JPA nullability. Consult [known problems](../docs/known-problems.md) before extending these workflows.
