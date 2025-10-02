# -------- build --------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# -------- run --------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/generator.jar /app/app.jar
CMD ["java","-jar","/app/app.jar"]