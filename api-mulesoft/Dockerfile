# 1. Usamos Java 17 para evitar o erro de "class file version 61.0"
FROM eclipse-temurin:17-jdk-focal

# Definição de variáveis de ambiente
ENV MULE_HOME=/opt/mule
ENV PATH=$MULE_HOME/bin:$PATH

# Instalação de dependências básicas e criação do usuário mule
RUN apt-get update && apt-get install -y unzip && \
    useradd -m mule

# 2. Instalação do Runtime Mule Kernel 4.9.0
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://repository.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/4.9.0/mule-standalone-4.9.0.zip -P /opt/ && \
    unzip /opt/mule-standalone-4.9.0.zip -d /opt/ && \
    mv /opt/mule-standalone-4.9.0 $MULE_HOME && \
    rm /opt/mule-standalone-4.9.0.zip

# 3. Preparação do ambiente de aplicações
# Limpamos qualquer app padrão e garantimos pastas de logs e cache
RUN rm -rf $MULE_HOME/apps/* && \
    mkdir -p $MULE_HOME/logs $MULE_HOME/.mule

# 4. Cópia da sua aplicação
# O uso do asterisco '*' garante que pegamos o JAR completo gerado pelo Maven
# Renomeamos para um nome fixo para facilitar o monitoramento

COPY ./target/teste-mule-application.jar $MULE_HOME/apps/teste-mule-application.jar

# 5. Validação anti-erro (Verifica se o arquivo não está vazio/0 bytes)
RUN if [ ! -s $MULE_HOME/apps/teste-mule-application.jar ]; then \
    echo "ERRO CRÍTICO: O arquivo JAR copiado está vazio ou não foi encontrado!" && exit 1; \
    fi

# 6. Ajuste de Permissões (Crucial para rodar no Docker/WSL2)
# Tornamos o usuário mule dono de tudo e damos permissão de escrita
RUN chown -R mule:mule $MULE_HOME && \
    chmod -R 777 $MULE_HOME

# Configuração de execução
USER mule
WORKDIR $MULE_HOME

# Opções de inicialização para evitar travamentos no carregamento de serviços
ENV MULE_JDK_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Xmx1536m -Xms512m -Dmule.deployment.force.parse.config=true -Dmule.system.healtcheck.disable=true"

# Porta padrão do HTTP Listener
EXPOSE 8081

# Inicia o Mule
CMD ["mule"]