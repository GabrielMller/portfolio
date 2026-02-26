# 1. Imagem de SO + Java 17
FROM eclipse-temurin:17-jdk-focal

ENV MULE_HOME=/opt/mule
ENV PATH=$MULE_HOME/bin:/usr/local/bin:$PATH

# 3. Instalação do Runtime Mule 4.9.0
RUN wget https://repository.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/4.9.0/mule-standalone-4.9.0.zip -P /opt/ && \
    unzip /opt/mule-standalone-4.9.0.zip -d /opt/ && \
    mv /opt/mule-standalone-4.9.0 $MULE_HOME && \
    rm /opt/mule-standalone-4.9.0.zip

# --- CORREÇÃO DE LOGS ---
# Redirecionamos os logs do wrapper e da aplicação para o stdout/stderr do Docker
RUN ln -sf /dev/stdout $MULE_HOME/logs/mule.log && \
    ln -sf /dev/stdout $MULE_HOME/logs/mule_ee.log && \
    ln -sf /dev/stdout $MULE_HOME/logs/wrapper.log

RUN rm -rf $MULE_HOME/apps/* && \
    mkdir -p $MULE_HOME/logs $MULE_HOME/.mule && \
    chown -R mule:mule $MULE_HOME && \
    chmod -R 777 $MULE_HOME

WORKDIR $MULE_HOME
USER mule

# Ajuste de memória: Mule 4.9 + JDK 17 precisa de espaço para Metaspace
ENV MULE_JDK_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Xmx1g -Xms1g -XX:MaxMetaspaceSize=256m -Dmule.deployment.force.parse.config=true"

EXPOSE 8081
# Usamos o modo console para garantir que o log flua para o kubectl logs
CMD ["mule", "-M-Danypoint.platform.gatekeeper=disabled"]