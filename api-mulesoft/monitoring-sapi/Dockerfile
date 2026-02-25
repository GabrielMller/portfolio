ARG DOCKERHUB_USERNAME
FROM ${DOCKERHUB_USERNAME}/mule-runtime-base:4.9.0

COPY ./target/*.jar /opt/mule/apps/${API_NAME}.jar

RUN if [ ! -s /opt/mule/apps/${API_NAME}.jar ]; then \
    echo "ERRO: O ficheiro JAR não foi encontrado ou está vazio!" && exit 1; \
    fi