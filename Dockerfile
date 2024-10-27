FROM openjdk:23-slim-bullseye

WORKDIR /app

COPY build/libs/*.jar /app/verveapp.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/verveapp.jar"]
