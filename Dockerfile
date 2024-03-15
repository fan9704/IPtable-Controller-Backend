# Base Image
FROM openjdk:17

WORKDIR /app
COPY target/*.jar app.jar

# Environment Variables
ENV MONGO_DB_HOST=127.0.0.1
ENV MONGO_DB_USERNAME=test
ENV MONGO_DB_PASSWORD=123456
ENV MONGO_DB_DATABASE=network
ENV MONGO_DB_PORT=27017

EXPOSE 9990

CMD ["java", "-jar", "app.jar"]