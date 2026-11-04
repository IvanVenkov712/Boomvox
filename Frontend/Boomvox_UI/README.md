# Boomvox UI

Boomvox UI is an Angular application for discovering and listening to music, managing playlists and favourites, rating songs, editing a profile, and uploading audio. It uses standalone components, Angular Router, signals, `HttpClient`, RxJS, and a browser `Audio` element.

## Install and run

The workspace includes `package.json`, `package-lock.json`, `angular.json`, and `tsconfig*.json`. It declares Angular 21.2, TypeScript 5.9, RxJS 7.8, and a Vitest 4/jsdom test target. `package.json` declares `npm@11.14.1`; installation and production compilation were verified on 2026-11-04 with Node.js 24.22.1 and npm 11.19.1.

1. Install Node.js 24 and npm 11. Reopen the terminal and check `node --version` and `npm --version`.
2. Open a terminal in `Frontend/Boomvox_UI/` and install the locked dependencies:

   ```sh
   npm ci
   ```

3. Start the [backend](../../Backend/README.md#install-and-run) on port 9090. Both frontend environments use `/api`; `proxy.conf.json` forwards development API requests to `http://localhost:9090`.
4. Start the UI with the installed Angular CLI:

   ```sh
   npm start -- --host localhost --port 4200
   ```

5. Open `http://localhost:4200`. Register or log in at `/login`. There is no separate registration URL; the login component contains both forms.

### Build and test status

Run these commands from this directory:

| Command | Purpose and current status |
| --- | --- |
| `npm ci` | Install the locked dependency tree; verified with `--no-audit --no-fund` |
| `npm start -- --host localhost --port 4200` | Start the development server; browser/API flows have not been verified |
| `npm run build` | Default production build; passed, output in `dist/Boomvox_UI/browser/` |
| `npm run build -- --configuration development` | Build with source maps and without production optimisation |
| `npm test -- --watch=false` | Run the configured Angular/Vitest target once; currently fails during test compilation |

The production build completed with four non-blocking warnings: unused `RouterLinkActive` in `navbar.ts`, and component styles exceeding the 4 kB warning budget in `upload.css` (6.63 kB), `playlist-detail.css` (4.21 kB), and `profile.css` (4.65 kB). None exceeded the 8 kB error budget.

The test command failed with 14 `TS2305` errors because service specs import old names such as `Auth` instead of `AuthService`. Test cases did not execute. The app spec also still expects the generated welcome title. Production compilation excludes `*.spec.ts`, so this failure does not block `npm run build`. See [verification and test coverage](../../docs/known-problems.md#verification-and-test-coverage).

`.gitignore` excludes `node_modules/`, `.angular/`, `dist/`, `out-tsc/`, coverage, and package-manager logs. Keep the lockfile and workspace configuration under version control.

## Configuration

| File | Current values |
| --- | --- |
| `src/environments/environment.ts` | `production: false`, `apiUrl: '/api'` |
| `src/environments/environment.prod.ts` | `production: true`, `apiUrl: '/api'` |
| `proxy.conf.json` | Development `/api/**` requests forwarded to `http://localhost:9090` |
| `../nginx.conf` | Container `/api/` requests forwarded to `http://backend:9090` |

Services import `environment.ts`. The production target in `angular.json` replaces it with `environment.prod.ts`. Both configurations send API requests to the browser's current origin. During `npm start`, the configured Angular development proxy forwards `/api/**` to Spring Boot, preserving the path. Restart `npm start` after changing proxy configuration.

The frontend currently reads no OS environment variables or `.env` file. Configuration is compiled into the browser bundle. AWS keys, database passwords, and JWT signing secrets belong only in the backend environment. Direct cross-origin API access is still subject to the backend's CORS policy, which accepts only `http://localhost:4200`.

### Container image

Build from the repository root:

```sh
docker build -f Frontend/Dockerfile -t boomvox-frontend .
```

The image builds Angular with Node.js 24, then serves `dist/Boomvox_UI/browser/` with Nginx. The repository's Compose stack supplies the shared network, backend name `backend:9090`, and localhost port `4200:80`. Nginx starts after the backend health check passes. Follow the [Compose setup](../../docs/deployment.md#docker-compose-local-stack).

Nginx forwards `/api/` without removing the prefix, preserves the public Host header including its port, and accepts request bodies up to 100 MB. Client-side routes fall back to `index.html`; API errors remain backend responses. If the backend uses the `prod` profile, explicitly set `SERVER_PORT=9090` to match this upstream.

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
| Tests | The configured test target fails compilation on 14 obsolete service imports; the app spec also asserts an old generated title |

Playback unload delivery, URL-refresh session handling, poll cleanup, backend access control, and other limitations are detailed in [known problems](../../docs/known-problems.md). See the [API reference](../../docs/project-endpoints.md) for the actual contract when changing frontend interfaces.

## Development workflow

Keep component TypeScript, HTML, CSS, and specs together as in the existing tree. Add routes in `app.routes.ts`, put HTTP calls in a service, and keep shared contracts in `models/`. Cross-check DTO field names and enum values against the backend before adding new requests. Run the production build after changes and repair the test suite before relying on its results.

For a manual integration check, register/login, browse songs, play an `ACTIVE` song, pause/seek/skip, add it to a playlist/favourites, submit a rating, and inspect profile history. Use an authorised account for upload and confirm it progresses from `PROCESSING` to `ACTIVE`. Check browser Network requests against the [API reference](../../docs/project-endpoints.md); the known mismatches mean this is a diagnostic checklist, not a claim that every flow currently passes.

There is no hosted frontend. Hosting requirements, API URLs, CORS, and configuration ownership are covered in the [deployment guide](../../docs/deployment.md).
