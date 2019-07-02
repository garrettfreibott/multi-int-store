FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=cdr-spring-gateway
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]