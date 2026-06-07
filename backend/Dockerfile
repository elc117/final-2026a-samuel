FROM gradle:8.14-jdk21 AS build

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]
