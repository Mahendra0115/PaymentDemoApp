FROM eclipse-temurin:21-jdk
COPY target/Payment-Integration-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
