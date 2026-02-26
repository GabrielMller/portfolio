FROM eclipse-temurin:17-jdk-focal

ENV MULE_HOME=/opt/mule
ENV PATH=$MULE_HOME/bin:$PATH

RUN apt-get update && apt-get install -y wget unzip tzdata && \
    useradd -m mule

RUN wget https://repository.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/4.9.0/mule-standalone-4.9.0.zip -P /opt/ && \
    unzip /opt/mule-standalone-4.9.0.zip -d /opt/ && \
    mv /opt/mule-standalone-4.9.0 $MULE_HOME && \
    rm /opt/mule-standalone-4.9.0.zip

RUN ln -sf /dev/stdout $MULE_HOME/logs/mule.log && \
    ln -sf /dev/stdout $MULE_HOME/logs/mule_ee.log && \
    ln -sf /dev/stdout $MULE_HOME/logs/wrapper.log

RUN rm -rf $MULE_HOME/apps/* && \
    mkdir -p $MULE_HOME/logs $MULE_HOME/.mule && \
    chown -R mule:mule $MULE_HOME && \
    chmod -R 777 $MULE_HOME

WORKDIR $MULE_HOME
USER mule

ENV MULE_JDK_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Xmx1g -Xms1g -XX:MaxMetaspaceSize=256m -Dmule.deployment.force.parse.config=true"

EXPOSE 8081

CMD ["mule", "-M-Danypoint.platform.gatekeeper=disabled"]