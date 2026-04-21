ARG DOCKERHUB_USERNAME
ARG API_NAME

FROM maven:3.8-openjdk-8 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM ${DOCKERHUB_USERNAME}/mule-runtime-base:4.9.0

ARG API_NAME

COPY --from=build /target/*.jar /opt/mule/apps/${API_NAME}.jar

RUN if [ ! -s /opt/mule/apps/${API_NAME}.jar ]; then \
    echo "ERRO: O ficheiro JAR não foi encontrado ou está vazio!" && exit 1; \
    fi