# Boomvox frontend

The browser application lives in [`Boomvox_UI/`](Boomvox_UI/). Start with its [README](Boomvox_UI/README.md) for installation status, routes, components, HTTP services, playback, and development instructions.

The checkout includes the Angular 21 workspace, `package.json`, `package-lock.json`, and TypeScript configuration. Dependency installation and the production build passed on 2026-11-04 with Node.js 24.22.1 and npm 11.19.1.

From `Frontend/Boomvox_UI/`, run:

```sh
npm ci
npm start -- --host localhost --port 4200
```

Run `npm run build` for production output in `dist/Boomvox_UI/browser/`. The build passes with four warnings; `npm test -- --watch=false` currently fails to compile outdated service specs. See [known problems](../docs/known-problems.md) for details. Dependencies, Angular caches, and generated output are ignored by Git.

The intended local addresses are `http://localhost:4200` for the UI and `http://localhost:9090/api` for the API. There is no current hosted deployment.

See the [project README](../README.md), [configuration guide](../docs/deployment.md), and [known problems](../docs/known-problems.md) for shared setup and limitations.
