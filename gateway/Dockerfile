FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=ion-gateway
ARG JAR_FILE
COPY ${JAR_FILE} /ion-gateway
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ion-gateway"]
