FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -pl server -am -DskipTests

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=build /app/server/target/*jar-with-dependencies.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
