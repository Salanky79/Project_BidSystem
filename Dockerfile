# Stage 1: Build the Maven project
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy the source folder containing the project code
COPY source/ ./source/

# Ensure the Maven wrapper has execution permissions
RUN chmod +x ./source/mvnw

# Build the project (skipping tests for faster deployments on Railway)
RUN cd source && ./mvnw clean package -DskipTests

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
