# Build from the repository root:
# docker build -f Frontend/Dockerfile -t boomvox-frontend .
FROM node:24-alpine AS build

WORKDIR /build
COPY Frontend/Boomvox_UI/package.json Frontend/Boomvox_UI/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY Frontend/Boomvox_UI/ ./
RUN npm run build

FROM nginx:stable-alpine AS runtime

COPY Frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/Boomvox_UI/browser/ /usr/share/nginx/html/

EXPOSE 80
