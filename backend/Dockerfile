FROM gradle:8.14-jdk21 AS build

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean test installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/install/gym-social-api/ /app/

EXPOSE 7000

ENTRYPOINT ["/app/bin/gym-social-api"]
