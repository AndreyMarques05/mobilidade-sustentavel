# ==============================================================================
# Imagem que roda a suite de testes de API e BDD sem precisar de Java ou
# Maven instalados na maquina. A API alvo vem pela variavel API_BASE_URL.
#
#   docker build -t mobilidade-api-tests .
#
#   # API publicada na propria maquina (Docker Desktop):
#   docker run --rm -e API_BASE_URL=http://host.docker.internal:8081 mobilidade-api-tests
#
#   # Na mesma rede do docker-compose de staging, levando o relatorio para fora:
#   docker run --rm --network mobilidade-staging_mobilidade-staging-net \
#     -e API_BASE_URL=http://mobilidade-api:8080 \
#     -v "$PWD/relatorio:/tests/target/cucumber-report" mobilidade-api-tests
#
#   # Argumentos extras vao para o Maven, por exemplo so os cenarios de negocio:
#   docker run --rm mobilidade-api-tests -Dcucumber.filter.tags=@negocio
# ==============================================================================

FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /tests

# Dependencias primeiro: a camada so e refeita quando o pom.xml muda
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

ENV API_BASE_URL=http://host.docker.internal:8081

# O relatorio fica em /tests/target/cucumber-report; monte um volume nessa
# pasta para leva-lo para fora do conteiner.
ENTRYPOINT ["mvn", "-B", "test"]
