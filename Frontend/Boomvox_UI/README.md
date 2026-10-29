# Boomvox UI

Boomvox UI is an Angular application for discovering and listening to music, managing playlists and favourites, rating songs, editing a profile, and uploading audio. It uses standalone components, Angular Router, signals, `HttpClient`, RxJS, and a browser `Audio` element.

## Install and run

### Current setup blocker

This directory contains `src/`, `.editorconfig`, and `.prettierrc`, but no `package.json`, lockfile, `angular.json`, or `tsconfig*.json`. The Angular, TypeScript, Node.js, and test-runner versions cannot be established from the checked-in source. **`npm install`, `npm ci`, and `ng serve` cannot set up this checkout as it stands.**

Restore the original workspace/build files from the working development copy or project history before running it. Preserve the current source. If the original files are unavailable, create an Angular workspace in a separate directory, integrate this source, and review dependencies and bootstrap configuration before checking the recovered workspace files into this directory. This is a recovery task; there is no verified frontend build configuration in this repository yet.

### Once the workspace files are restored

1. Install Node.js and npm versions compatible with the restored `package.json` and Angular version, using the [Node.js downloads page](https://nodejs.org/en/download). Use the project's pinned versions if the recovered files specify them. Reopen the terminal and check `node --version` and `npm --version`.
2. Open a terminal in `Frontend/Boomvox_UI/` and install the locked dependencies:

   ```sh
   npm ci
   ```

   If only `package.json` was recovered, run `npm install` once to create a lockfile, review it, and commit it with the restored configuration.

3. Start the [backend](../../Backend/README.md#install-and-run). Check that `src/environments/environment.ts` points to `http://localhost:9090/api`.
4. With the local Angular CLI installed by the restored dependencies, start the UI:

   ```sh
   npx ng serve --host localhost --port 4200
   ```

5. Open `http://localhost:4200`. Register or log in at `/login`. There is no separate registration URL; the login component contains both forms.

These commands depend on the restored Angular workspace and have not been validated against the current incomplete checkout. Frontend unit tests and a production build should be run with that workspace's configured targets (normally `npx ng test` and `npx ng build --configuration production`). Existing specs also require repair.

## Configuration

| File | Current values |
| --- | --- |
| `src/environments/environment.ts` | `production: false`, `apiUrl: 'http://localhost:9090/api'` |
| `src/environments/environment.prod.ts` | `production: true`, the same localhost API URL |

Services import `environment.ts`. A production file is present, but no `angular.json` exists to establish file replacement. A production build must explicitly wire that replacement and use the intended API address when hosting is introduced.

The frontend currently reads no OS environment variables or `.env` file. Configuration is compiled into the browser bundle. AWS keys, database passwords, and JWT signing secrets belong only in the backend environment. The backend's API CORS policy currently accepts only `http://localhost:4200`; `127.0.0.1` or another port is a different origin.

## Application layout

```text
src/
  main.ts                 Bootstraps App using appConfig
  styles.css              Shared styles
  environments/           Development and production API constants
  app/
    app.ts, app.html       Shell: navbar, sidebar, router outlet, player bar
    app.config.ts         Router and HttpClient/interceptor providers
    app.routes.ts         Route definitions
    components/           Navbar, sidebar browser, player bar, rating widget
    pages/                Routed screens
    services/             API clients and shared state
    models/               TypeScript request/response interfaces and enums
    guards/               Login-presence route guard
    interceptors/         Bearer token and HTTP error handling
```

The shell hides its navigation and player on `/login`. `PlayerService` is application-scoped, so playback and the queue can continue while navigating between pages.

## Routes and screens

All routes except `/login` use `authGuard`. `/` redirects to `/home`.

| Route | Component | Purpose |
| --- | --- | --- |
| `/login` | `Login` | Login and registration tabs |
| `/home` | `Home` | Saved recommendations; generates them if the saved list is empty |
| `/profile` | `Profile` | Account details, profile editing, preferences, paginated listening history |
| `/songs` | `Songs` | Catalogue search/filtering and playback |
| `/songs/:id` | `SongDetail` | Song details, playback, collections, tags, and ratings |
| `/artists` | `Artists` | Browse artist accounts |
| `/artists/:id` | `ArtistDetail` | Artist's albums and songs |
| `/albums/:id` | `AlbumDetail` | Album details and tracks |
| `/playlists` | `Playlists` | Current user's playlists and playlist creation |
| `/playlists/:id` | `PlaylistDetail` | Songs in an owned playlist and collection actions |
| `/favourites` | `Favourites` | Current user's favourite songs |
| `/users/:id` | `UserDetail` | User details and administrator role action |
| `/upload` | `Upload` | Audio selection, optional album selection/creation, upload status |
| `/admin/users` | `AdminUsers` | Search/filter users; opens user details |

The guard checks whether a token exists, without validating its expiry or role. The upload component redirects users whose cached role is not `AUTHOR` or `ADMIN`. Admin navigation is hidden for other roles, but the admin route has no role guard. Backend authorisation must enforce access independently.

## HTTP services and state

`app.config.ts` registers `provideRouter(routes)` and `provideHttpClient(withInterceptors([authInterceptor]))`. API clients build their URLs from `environment.apiUrl`; model names generally mirror backend DTOs, with the mismatches below.

| Service file | Responsibility |
| --- | --- |
| `auth.ts` | Register/login, store token/email/role, local logout |
| `user.ts` | Current user state, profile update, history, preferences |
| `song.ts`, `album.ts`, `artist.ts`, `tag.ts` | Catalogue requests and related cached signals |
| `playlist.ts`, `favourites.ts` | Personal collections and membership actions |
| `rating.ts` | Rating submission/removal, feedback, averages, statistics |
| `recommendation.ts` | Read/generate recommendations and report a click |
| `streaming.ts` | Get stream URL, log playback events, close sessions |
| `player.ts` | Shared audio element, signals, queue, controls, URL refresh |
| `admin.ts` | User search and role changes |
| `layout.ts` | Shared layout state |

Services and pages use RxJS subscriptions for HTTP operations and Angular signals for local/shared state. There is no separate global state store.

## Authentication flow

1. Login or registration returns `{ token, email, role }` from the backend.
2. `AuthService` stores those three fields in `localStorage`.
3. The interceptor adds `Authorization: Bearer <token>` to `HttpClient` requests when a token is present.
4. On HTTP 401, it clears local storage and routes to `/login`. There is no automatic refresh-token flow, and HTTP 403 does not trigger that cleanup.
5. Logout resets the player, removes the stored session fields, and returns to `/login`; it does not call the backend logout endpoint.

The browser's S3 audio request is made by the audio element using a signed URL, separately from Angular `HttpClient`. It does not need the application's Bearer token.

## Upload and playback

The upload page sends `FormData` containing `audio` and a `metadata` JSON blob. Do not manually set the multipart `Content-Type`, since the browser supplies its boundary. After a 201 response, the page polls `GET /songs/{id}` every three seconds until status is `ACTIVE` or `FAILED`, or a request fails. An album can be selected or created during upload.

`PlayerService` requests `GET /songs/{id}/stream`, uses the returned `streamUrl` as `Audio.src`, and retains the session ID and expiry in memory. It exposes signals for current song, playback state, elapsed time, and duration. Controls support pause/resume, seeking, previous/next, and queue advancement after a track ends. Resume fetches another URL if the saved URL has expired; that request also starts a new backend session.

The backend generates 128 kbps MP3, 320 kbps MP3, and 256 kbps AAC variants. It selects the highest bitrate for `ORDINARY_USER`, `AUTHOR`, and `ADMIN`, and the default variant for an authenticated `GUEST`. There is no UI quality selector.

## Current integration problems

| Area | Source-level mismatch and effect |
| --- | --- |
| Session close | UI sends `INTERRUPTED` on unload/logout; backend expects `ABANDONED`, so these requests fail deserialisation |
| Listening history | UI reads `listenedDurationSeconds`; backend returns `listenedDurationSec`, so the duration is missing in the profile |
| Song tags | UI calls `GET /songs/{id}/tags`, which has no backend mapping; attachment also declares `TagResponse` instead of `SongTagResponse` |
| Album deletion | UI service calls `DELETE /albums/{id}`, which has no backend mapping |
| Song update | UI sends `name`, `genre`, and `albumId`; backend also requires `format`, `duration`, `fileSize`, and `storageKey` |
| Upload genre | UI sends a song `genre`; backend upload DTO has no such field, and genre is obtained through an album |
| Models | UI omits backend `GUEST`/`FLAC` enum values and stream quality/duration fields; backend allows nullable IDs that UI types declare as required |
| Tests | Specs import obsolete service class names and assert an old generated title; build/test metadata is missing |

Playback unload delivery, URL-refresh session handling, poll cleanup, backend access control, and other limitations are detailed in [known problems](../../docs/known-problems.md). See the [API reference](../../docs/project-endpoints.md) for the actual contract when changing frontend interfaces.

## Development workflow

After recovering the workspace, keep component TypeScript, HTML, CSS, and specs together as in the existing tree. Add routes in `app.routes.ts`, put HTTP calls in a service, and keep shared contracts in `models/`. Cross-check DTO field names and enum values against the backend before adding new requests.

For a manual integration check, register/login, browse songs, play an `ACTIVE` song, pause/seek/skip, add it to a playlist/favourites, submit a rating, and inspect profile history. Use an authorised account for upload and confirm it progresses from `PROCESSING` to `ACTIVE`. Check browser Network requests against the [API reference](../../docs/project-endpoints.md); the known mismatches mean this is a diagnostic checklist, not a claim that every flow currently passes.

There is no hosted frontend. Hosting requirements, API URLs, CORS, and configuration ownership are covered in the [deployment guide](../../docs/deployment.md).
