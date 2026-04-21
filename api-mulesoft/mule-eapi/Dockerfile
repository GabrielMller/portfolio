# Estágio 1: Build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (Sua imagem base do Mule)
FROM gabrielmllr/mule-runtime-base:latest

# Argumentos (se quiser passar via --build-arg)
ARG API_NAME=mule-eapi

# Copia o JAR do estágio de build para a pasta de apps do Mule
# Note que usamos o nome fixo ou a variável para o destino
COPY --from=build /app/target/*.jar /opt/mule/apps/${API_NAME}.jar

# Validação simples
RUN if [ ! -f /opt/mule/apps/${API_NAME}.jar ]; then \
    echo "ERRO: O ficheiro JAR não foi encontrado!" && exit 1; \
    fi

# O CMD já deve estar definido na sua imagem base, 
# mas você pode sobrescrever aqui se necessário.