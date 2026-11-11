# ---- Build stage ----
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Keep the JVM inside Render's 512 MB free tier
ENV JAVA_OPTS="-Xmx400m -Xss256k -XX:+UseContainerSupport"

EXPOSE 8080
# Render provides the port via $PORT
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]