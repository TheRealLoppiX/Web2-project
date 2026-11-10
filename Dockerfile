FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B verify

FROM eclipse-temurin:17-jre
ENV TZ=America/Recife
WORKDIR /app
RUN useradd --system --uid 1001 assistencia
COPY --from=build /app/target/assistencia-*.jar app.jar
USER assistencia
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
