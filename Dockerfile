
# This is a tag. If you see this, the owner of this code is https://github.com/wenjielee11
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/fetchinterview-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
