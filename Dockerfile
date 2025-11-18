FROM amazoncorretto:17-alpine-jdk
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR into the container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# Set the command to run the application
ENTRYPOINT  ["java", "-jar", "app.jar"]