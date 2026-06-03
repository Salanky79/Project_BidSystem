# Stage 1: Build the Maven project
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the source folder containing the project code
COPY source/ ./source/

# Build the project using the pre-installed Maven (skipping tests)
# Note: We use 'mvn' directly instead of './mvnw' to avoid CRLF (Windows line endings) issues on Linux
RUN cd source && mvn clean package -DskipTests

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the compiled fat jar from the builder stage
COPY --from=builder /app/source/server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar ./server.jar

# Railway automatically sets the PORT environment variable, our server reads it.
# Exposing 8080 as a fallback default.
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "server.jar"]
