# =============================================================================
# Cidades ESG Inteligentes - Dockerfile multi-stage
#
#   Etapa 1 (build):   compila com Maven + JDK 21 e separa o JAR em camadas
#   Etapa 2 (runtime): imagem final enxuta, só com o JRE 21 e usuário sem root
#
# Build:  docker build -t cidades-esg-inteligentes .
# Run:    docker compose up -d   (veja o docker-compose.yml e o README)
# =============================================================================

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Primeiro só o Maven Wrapper e o pom.xml: a camada com as dependências fica em
# cache e só é refeita quando o pom.xml muda (builds seguintes ficam bem mais rápidos).
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -B -q dependency:go-offline

# Depois o código-fonte. Os testes já rodaram na etapa "Testes" do pipeline,
# por isso aqui o pacote é gerado sem executá-los.
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests \
    && cp target/cidades-esg.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="cidades-esg-inteligentes" \
      org.opencontainers.image.description="API de indicadores ESG para cidades inteligentes (Spring Boot 4, Java 21)"

# Usuário sem privilégios: a aplicação nunca roda como root
RUN addgroup -S app && adduser -S -G app -h /app app \
    && mkdir -p /app/logs && chown -R app:app /app
WORKDIR /app

# Camadas da aplicação, da que muda menos (dependências) para a que muda mais (código)
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./

# Commit gravado na imagem e exibido em /api/info (o pipeline usa para confirmar cada deploy):
# - no Render, chega pelo build arg RENDER_GIT_COMMIT (o Render só repassa variáveis aos ARGs declarados);
# - no GitHub Actions, chega pelo build arg GIT_COMMIT.
ARG GIT_COMMIT=local
ARG RENDER_GIT_COMMIT
# JVM ajustada para containers pequenos (o plano gratuito do Render tem 512 MB e 0,1 CPU):
# heap limitada a 60% da memória do container, GC serial e só o compilador C1 (inicialização mais rápida).
ENV APP_COMMIT=${RENDER_GIT_COMMIT:-${GIT_COMMIT}} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:+ExitOnOutOfMemoryError"

USER app
EXPOSE 8080

# Liveness: o processo responde (a conexão com o banco é verificada no readiness)
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -q -O /dev/null "http://127.0.0.1:${PORT}/actuator/health/liveness" || exit 1

# "exec" faz o Java receber o SIGTERM direto, permitindo o desligamento gracioso
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar application.jar"]
