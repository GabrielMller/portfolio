ARG DOCKERHUB_USERNAME
ARG API_NAME
ARG MULE_ENV
ARG SECURE_KEY

FROM ${DOCKERHUB_USERNAME}/mule-runtime-base:4.9.0

ARG API_NAME
ARG MULE_ENV
ARG SECURE_KEY

COPY ./target/*.jar /opt/mule/apps/${API_NAME}.jar

RUN if [ ! -s /opt/mule/apps/${API_NAME}.jar ]; then \
    echo "ERRO: O ficheiro JAR não foi encontrado ou está vazio!" && exit 1; \
    fi

ARG MULE_ENV
ARG SECURE_KEY
    
CMD ["sh", "-c", "/opt/mule/bin/mule -M-Danypoint.platform.gatekeeper=disabled -M-Dmule.env=${MULE_ENV} -M-Dsecure.key=${SECURE_KEY}"]