# Первый этап: сборка с Maven
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Копируем pom.xml для кэширования зависимостей
COPY pom.xml .
# Скачиваем зависимости (кэшируемый слой)
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src src

# Собираем приложение
RUN mvn clean package -DskipTests

# Второй этап: финальный образ
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Копируем JAR из первого этапа
COPY --from=builder /app/target/*.jar app.jar

# Копируем SSL сертификаты (если нужно)
# COPY src/main/resources/ssl/ /app/ssl/

EXPOSE 8443
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
