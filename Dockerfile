# ---------- Stage 1: 用 Maven 建置 jar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先複製 pom.xml 單獨下載依賴，可以利用 Docker 的 layer cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 再複製原始碼並打包
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: 執行環境（只留 jar，不含 Maven，體積小很多）----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
