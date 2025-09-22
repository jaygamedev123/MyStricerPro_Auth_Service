FROM openjdk:21-jdk-slim
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY  ./target/*.jar app.jar

EXPOSE 8080

# Set the command to run the application
CMD ["java", "-jar", "app.jar"]