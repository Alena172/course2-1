FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY src ./src

RUN chmod +x gradlew

RUN ./gradlew build --no-daemon

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/build/libs/course2-0.0.1-SNAPSHOT.jar"]
