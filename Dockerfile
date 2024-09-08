# Use an official Maven image as the base image
FROM maven:3.9-eclipse-temurin-17 AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
RUN mvn verify --fail-never
COPY src ./src
# Build the application using Maven
RUN mvn package assembly:single -DskipTests
#RUN mvn package -DskipTests

# Use an official OpenJDK image as the base image
FROM eclipse-temurin:17-jre-alpine
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/egcbot-1.0-SNAPSHOT-jar-with-dependencies.jar egcbot.jar
#COPY --from=build /app/target/egcbot-1.0-SNAPSHOT.jar egcbot.jar
#COPY --from=build /app/lib lib


# Set the command to run the application
#CMD ["java", "-Djava.library.path=/app/classes/", "-jar", "egcbot.jar"]
CMD ["java", "-cp", "egcbot.jar", "com.egc.Main"]