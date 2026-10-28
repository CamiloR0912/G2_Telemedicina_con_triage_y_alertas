FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
ARG SERVICE
COPY ${SERVICE}/pom.xml pom.xml
COPY ${SERVICE}/src src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
