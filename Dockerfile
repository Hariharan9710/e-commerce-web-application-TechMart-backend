# Use an official Java runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy everything from your backend folder into the container
COPY backend ./backend

# Change to the backend directory
WORKDIR /app/backend

# Give permission to mvnw (for Linux builds)
RUN chmod +x mvnw

# Build the Spring Boot JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Expose port (Render will use PORT env variable automatically)
EXPOSE 8080

# Start the Spring Boot application
CMD ["java", "-jar", "target/*.jar"]
