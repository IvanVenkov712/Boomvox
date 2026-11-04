# Boomvox frontend

The browser application lives in [`Boomvox_UI/`](Boomvox_UI/). Start with its [README](Boomvox_UI/README.md) for installation status, routes, components, HTTP services, playback, and development instructions.

The checkout includes the Angular 21 workspace, npm manifest/lockfile, TypeScript configuration, development proxy, and a two-stage frontend Dockerfile. Native installation and production compilation passed on 2026-11-04 with Node.js 24.22.1 and npm 11.19.1; the container build also passed.

For the complete application, use the [Compose quick start](../README.md#install-and-run-with-docker-compose) from the repository root. It serves the built UI through Nginx at `http://localhost:4200`, forwards `/api/` to `backend:9090`, and waits for backend readiness before starting the frontend. No host Node.js installation is needed for this path.

From `Frontend/Boomvox_UI/`, run:

```sh
npm ci
npm start -- --host localhost --port 4200
```

Run `npm run build` for production output in `dist/Boomvox_UI/browser/`. The build passes with four warnings; `npm test -- --watch=false` currently fails to compile outdated service specs. See [known problems](../docs/known-problems.md) for details. Dependencies, Angular caches, and generated output are ignored by Git.

Both frontend environments use the relative API base `/api`. Native `npm start` automatically forwards `/api/**` to `http://localhost:9090`; the frontend image uses `nginx.conf` instead. Production builds select `environment.prod.ts`, and client routes fall back to `index.html` in Nginx. Swagger remains available on the separately published API port. There is no current hosted deployment.

Local checks verified Nginx routing, Angular route refresh, registration/login, upload/transcoding, and signed audio/range downloads. This does not establish visual browser playback or complete UI/API compatibility. See the [verification record](../docs/deployment.md#verification-on-2026-11-04).

See the [project README](../README.md), [configuration guide](../docs/deployment.md), and [known problems](../docs/known-problems.md) for shared setup and limitations.
