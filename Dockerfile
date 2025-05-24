# Giai đoạn build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

# Giai đoạn chạy
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy WAR file từ giai đoạn build
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.war backend.war

# Copy file Firebase config (bạn phải đặt sẵn ở thư mục gốc)
COPY config.json /app/config.json

EXPOSE 8888

# Chạy file .war đúng tên
ENTRYPOINT ["java", "-jar", "backend.war"]
