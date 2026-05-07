FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Add Aliyun Maven mirror for acceleration as per user_rule.md
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .
RUN mvn -s /usr/share/maven/ref/settings.xml -B -q dependency:go-offline
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings.xml -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
