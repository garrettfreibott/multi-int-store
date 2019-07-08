FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=cdr-gateway
ARG JAR_FILE
COPY ${JAR_FILE} /cdr-gateway
ENTRYPOINT ["/cdr-gateway"]
