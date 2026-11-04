# Build from the repository root:
# docker build -f Backend/Dockerfile -t boomvox-backend .
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /build
COPY Backend/pom.xml Backend/mvnw ./
COPY Backend/.mvn/ .mvn/
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY Backend/src/ src/
# Package without running tests; runtime configuration is supplied separately.
RUN ./mvnw --batch-mode --no-transfer-progress package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/* \
    && command -v ffmpeg \
    && command -v ffprobe \
    && groupadd --gid 10001 boomvox \
    && useradd --uid 10001 --gid boomvox --home-dir /app --no-create-home boomvox \
    && mkdir -p /app/logs /app/tmp \
    && chown -R boomvox:boomvox /app

WORKDIR /app
COPY --from=build /build/target/boomvox-0.0.1-SNAPSHOT.jar app.jar

USER boomvox:boomvox
# The default dev profile listens on 9090.
EXPOSE 9090
ENTRYPOINT ["java", "-Djava.io.tmpdir=/app/tmp", "-jar", "app.jar"]
