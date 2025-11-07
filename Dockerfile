# Use an official Java runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give permission to mvnw (especially important for Linux)
RUN chmod +x mvnw

# Download dependencies (for caching)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Expose port (Render uses PORT env variable)
EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "target/*.jar"]
