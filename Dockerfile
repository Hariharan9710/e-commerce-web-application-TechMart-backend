# Use an official Java runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy all files from current directory into container
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the Spring Boot JAR (skip tests for speed)
RUN ./mvnw clean package -DskipTests

# Expose port (Render automatically sets PORT env variable)
EXPOSE 8080

# Run the built jar file
CMD ["java", "-jar", "target/*.jar"]
