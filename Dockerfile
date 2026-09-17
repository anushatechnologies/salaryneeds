# ====================================================================
# Build Stage: Compile and package application
# ====================================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Extract Spring Boot layers for fast image rebuilds and docker caching
WORKDIR /workspace/target
RUN java -Djarmode=layertools -jar *.jar extract

# ====================================================================
# Runtime Stage: Lightweight, hardened, non-root container
# ====================================================================
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Create a secure non-root user and group
RUN groupadd -r salarygroup && useradd -r -g salarygroup -u 1001 salaryuser

# Copy extracted layers from build stage
COPY --from=build --chown=salaryuser:salarygroup /workspace/target/dependencies/ ./
COPY --from=build --chown=salaryuser:salarygroup /workspace/target/spring-boot-loader/ ./
COPY --from=build --chown=salaryuser:salarygroup /workspace/target/snapshot-dependencies/ ./
COPY --from=build --chown=salaryuser:salarygroup /workspace/target/application/ ./

# Expose default application port
EXPOSE 8080

# Switch to non-root user
USER salaryuser:salarygroup

# JVM options optimized for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Container healthcheck using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch application using Spring Boot JarLauncher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]