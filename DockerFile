FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn dependency:go-offline

CMD ["mvn", "clean", "test"]