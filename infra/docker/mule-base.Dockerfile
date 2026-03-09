FROM eclipse-temurin:17-jdk-focal

ENV MULE_HOME=/opt/mule
ENV PATH=$MULE_HOME/bin:$PATH

RUN apt-get update && apt-get install -y wget unzip tzdata && \
    useradd -m mule

RUN wget https://repository.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/4.9.0/mule-standalone-4.9.0.zip -P /opt/ && \
    unzip /opt/mule-standalone-4.9.0.zip -d /opt/ && \
    mv /opt/mule-standalone-4.9.0 $MULE_HOME && \
    rm /opt/mule-standalone-4.9.0.zip

RUN sed -i 's/wrapper.logfile=.*/wrapper.logfile=\/dev\/null/' $MULE_HOME/conf/wrapper.conf && \
    sed -i 's/wrapper.console.loglevel=.*/wrapper.console.loglevel=INFO/' $MULE_HOME/conf/wrapper.conf && \
    # COMENTA AS LINHAS DE MEMÓRIA PADRÃO
    sed -i 's/^wrapper.java.initmemory=/#wrapper.java.initmemory=/' $MULE_HOME/conf/wrapper.conf && \
    sed -i 's/^wrapper.java.maxmemory=/#wrapper.java.maxmemory=/' $MULE_HOME/conf/wrapper.conf

RUN rm -rf $MULE_HOME/apps/* && \
    mkdir -p $MULE_HOME/logs $MULE_HOME/.mule && \
    chown -R mule:mule $MULE_HOME && \
    chmod -R 777 $MULE_HOME

WORKDIR $MULE_HOME
USER mule

ENV MULE_JDK_OPTIONS="-Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

CMD ["mule", "-M-Danypoint.platform.gatekeeper=disabled -Xms512m -Xmx768m -XX:MaxMetaspaceSize=256m"]