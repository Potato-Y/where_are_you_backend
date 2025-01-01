# Build Image
FROM amazoncorretto:21-alpine AS TEMP_BUILD_IMAGE
ENV APP_HOME=/app
WORKDIR $APP_HOME

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

COPY src ./src

#RUN chmod +x ./gradlew
RUN ./gradlew build --stacktrace

# App 구동
FROM amazoncorretto:21-alpine

ENV ARTIFACT_NAME=where-are-you-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/app
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME $APP_HOME/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
