# ==============================================================================
# Dockerfile — Mobilidade Sustentável API
# Build multi-stage: compila com Maven e entrega apenas o JRE + o JAR.
# ==============================================================================

# ------------------------------------------------------------------ Estágio 1
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa as dependências primeiro para aproveitar o cache de camadas do Docker:
# enquanto o pom.xml não mudar, esta camada é reaproveitada entre builds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compila o JAR. Os testes rodam na etapa dedicada do pipeline de CI, não aqui,
# para que a imagem só seja construída depois de o código já ter passado.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------ Estágio 2
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="Mobilidade Sustentável API" \
      org.opencontainers.image.description="API ESG de monitoramento de mobilidade sustentável" \
      org.opencontainers.image.source="https://github.com/OWNER/mobilidade-sustentavel" \
      org.opencontainers.image.licenses="MIT"

WORKDIR /app

# wget é usado pelo HEALTHCHECK; curl fica disponível para depuração manual
RUN apk add --no-cache curl && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup

# Copia o JAR já com o dono correto, para rodar sem privilégios de root
COPY --from=build --chown=appuser:appgroup /app/target/mobilidade-sustentavel-1.0.0.jar app.jar

USER appuser

# Perfil e memória ajustáveis por ambiente sem reconstruir a imagem
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

EXPOSE 8080

# O orquestrador (Compose/Kubernetes) usa este health check para saber
# quando o contêiner realmente está pronto para receber tráfego.
# Usa $PORT para continuar válido quando o ambiente muda a porta da aplicação.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
