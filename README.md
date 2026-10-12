# Projeto — Cidades ESG Inteligentes: Mobilidade Sustentável

API RESTful que incentiva o uso de transportes sustentáveis em cidades inteligentes. O usuário registra suas viagens, o sistema calcula quanto de CO₂ foi evitado em relação a um carro a combustão (referência de 0,21 kg CO₂/km) e converte esse impacto em pontos de recompensa, com ranking e resgate de benefícios.

Esta entrega aplica práticas completas de **DevOps** sobre esse projeto: pipeline de CI/CD com build, testes e deploy automatizados, containerização com Docker e orquestração dos serviços em dois ambientes — **staging** e **produção**.

**Integrante:** Francisco Andrey Leite Marques

---

## Índice

- [Como executar localmente com Docker](#como-executar-localmente-com-docker)
- [Pipeline CI/CD](#pipeline-cicd)
- [Containerização](#containerização)
- [Prints do funcionamento](#prints-do-funcionamento)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Checklist de entrega](#checklist-de-entrega)

---

## Como executar localmente com Docker

### Pré-requisitos

- Docker Desktop (ou Docker Engine + Compose v2)
- 4 GB de RAM livres — o Oracle XE é o serviço mais pesado da stack
- Opcional, para rodar fora do contêiner: JDK 17 e Maven 3.9+

### Passo 1 — Configurar as variáveis de ambiente

Nenhuma senha está escrita nos arquivos versionados. Copie o modelo e ajuste o que precisar:

```bash
cp .env.example .env
```

Para gerar um segredo de JWT forte (o algoritmo HS256 exige no mínimo 256 bits):

```bash
openssl rand -base64 48
```

### Passo 2 — Subir a stack de desenvolvimento

```bash
docker compose up --build
```

O Compose sobe dois contêineres: o banco Oracle XE e a API. A aplicação só inicia depois que o health check do banco passa, então na primeira execução o Oracle leva de 2 a 3 minutos para ficar pronto — é normal ver a API aguardando.

Quando terminar, a API responde em `http://localhost:8080`:

| Recurso | URL |
|---|---|
| Health check | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

### Passo 3 — Validar que está no ar

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

Criar um usuário e autenticar:

```bash
curl -X POST http://localhost:8080/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Andrey","email":"andrey@fiap.com.br","senha":"senha123","tipo":"PESSOA_FISICA"}'

curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"andrey@fiap.com.br","senha":"senha123"}'
```

O login devolve um token JWT que deve ser enviado nas demais chamadas no cabeçalho `Authorization: Bearer <token>`.

### Subir os dois ambientes lado a lado

Staging e produção são stacks independentes — cada uma com sua própria rede, seu próprio volume e seu próprio banco —, então podem rodar ao mesmo tempo na mesma máquina:

```bash
# Homologação: API em 8081, banco exposto em 1522
docker compose -f docker-compose.staging.yml up -d --build

# Produção: API em 8080, banco sem porta publicada no host
docker compose -f docker-compose.prod.yml up -d --build

# Conferir os dois de pé
docker ps
curl http://localhost:8081/actuator/health   # staging
curl http://localhost:8080/actuator/health   # produção
```

| | Staging | Produção |
|---|---|---|
| Porta da API | 8081 | 8080 |
| Porta do banco | 1522 (exposta) | não publicada |
| Perfil Spring | `staging` | `prod` |
| Swagger | habilitado | desabilitado |
| Log da aplicação | `DEBUG` | `INFO` (raiz em `WARN`) |
| Detalhe do health check | completo | apenas o status |
| Política de restart | `on-failure` | `unless-stopped` |

Para derrubar:

```bash
docker compose -f docker-compose.staging.yml down
docker compose -f docker-compose.prod.yml down          # mantém os volumes
docker compose -f docker-compose.prod.yml down -v       # apaga também os dados
```

### Rodar os testes sem Docker

```bash
mvn test
```

---

## Pipeline CI/CD

O pipeline está em [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) e roda no **GitHub Actions**.

### Ferramenta escolhida

GitHub Actions foi escolhido por três motivos práticos: já vive no mesmo lugar do código (sem serviço externo para configurar), o registro de imagens GitHub Container Registry (GHCR) é autenticado pelo próprio `GITHUB_TOKEN` do job — sem credencial manual — e os *Environments* do GitHub dão, de graça, o portão de aprovação manual que produção precisa.

### Gatilhos

| Evento | O que acontece |
|---|---|
| Pull request | Só build e testes. Nada é publicado nem implantado. |
| Push em `develop` | Build, testes, imagem publicada e deploy em **staging**. |
| Push em `main` | Tudo acima e, após aprovação manual, deploy em **produção**. |
| Execução manual | `workflow_dispatch` permite disparar o pipeline pela interface. |

### Etapas

```
┌──────────────┐   ┌───────────────┐   ┌─────────────────┐   ┌────────────────────┐
│  build-test  │──▶│ docker-image  │──▶│ deploy-staging  │──▶│ deploy-production  │
└──────────────┘   └───────────────┘   └─────────────────┘   └────────────────────┘
   compila           constrói a          sobe a imagem          repete em produção
   roda os testes    imagem e envia      publicada, espera      após aprovação e
   publica o JAR     para o GHCR         o health check e       promove a imagem
   e os relatórios   com cache de        roda o smoke test      para a tag
                     camadas                                    :production
```

**1. `build-test` — build e testes automatizados**
Configura o JDK 17 com cache das dependências Maven, compila (`mvn clean compile`), executa a suíte de testes (`mvn test`) e empacota o JAR. Publica como artefatos os relatórios do Surefire, o relatório de cobertura do JaCoCo e o próprio JAR, e escreve uma tabela com o resultado de cada suíte no resumo da execução. Se qualquer teste falhar, o pipeline para aqui e nenhuma imagem é construída.

**2. `docker-image` — build e publicação da imagem**
Constrói a imagem com Buildx usando cache de camadas do próprio Actions (`type=gha`), o que derruba bastante o tempo dos builds seguintes, e envia para o GHCR. As tags são decididas pela branch: `staging` a partir de `develop`, `production` e `latest` a partir de `main`, e sempre uma tag com o SHA curto do commit — é essa tag imutável que os deploys usam, garantindo que staging e produção recebam exatamente o mesmo binário.

**3. `deploy-staging` — deploy em homologação**
Sobe um Oracle XE como *service container* do job, baixa do GHCR a imagem daquele commit, inicia a aplicação com o perfil `staging`, aguarda o `/actuator/health` responder `UP` (até 5 minutos, com diagnóstico dos logs se estourar) e roda o smoke test: consulta o health check e confirma que uma tentativa de login com credencial inválida é rejeitada com 401/403 — ou seja, a camada de segurança subiu de fato.

**4. `deploy-production` — deploy em produção**
Só roda a partir da `main` e depende do ambiente `production` do GitHub, que exige aprovação manual. Repete o processo com o perfil `prod`, verifica o health check, confirma que o Swagger está desabilitado em produção e, no fim, promove a mesma imagem para a tag `:production` no GHCR.

### Configuração necessária no GitHub

Depois de criar o repositório e enviar o código:

1. **Settings → Environments → New environment → `staging`** (não precisa de proteção).
2. **Settings → Environments → New environment → `production`** e marque **Required reviewers**, adicionando você mesmo. É isso que cria o portão de aprovação antes da produção.
3. Opcionalmente, em cada ambiente, cadastre o secret `JWT_SECRET_STAGING` e `JWT_SECRET_PROD`. Se não existirem, o pipeline usa um valor padrão só para o ambiente efêmero de teste.
4. **Settings → Actions → General → Workflow permissions**: deixe em *Read and write permissions*, para o job conseguir publicar a imagem no GHCR.

---

## Containerização

### Dockerfile

Build **multi-stage** em dois estágios, que é o que mantém a imagem final enxuta:

- **Estágio de build** (`maven:3.9.6-eclipse-temurin-17`): copia primeiro só o `pom.xml` e roda `mvn dependency:go-offline`. Como essa camada só é invalidada quando o `pom.xml` muda, builds seguintes reaproveitam todas as dependências do cache. Depois copia o código e empacota o JAR.
- **Estágio de runtime** (`eclipse-temurin:17-jre-alpine`): leva apenas o JAR e o JRE — nem Maven, nem código-fonte, nem as dependências de compilação ficam na imagem entregue.

Decisões de segurança e operação embutidas na imagem:

| Decisão | Por quê |
|---|---|
| Usuário `appuser` não-root | Se a aplicação for comprometida, o atacante não tem root no contêiner |
| `COPY --chown=appuser:appgroup` | O JAR já nasce com o dono correto, sem precisar de `chmod` extra |
| `HEALTHCHECK` no `/actuator/health` | O orquestrador só direciona tráfego quando a aplicação está realmente pronta |
| `JAVA_OPTS` com `MaxRAMPercentage` | A JVM respeita o limite de memória do contêiner em vez do host |
| `SPRING_PROFILES_ACTIVE` como variável | A mesma imagem serve staging e produção — muda só a configuração |
| `.dockerignore` | Mantém `.env`, `.git` e `target/` fora do contexto de build |

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build --chown=appuser:appgroup /app/target/mobilidade-sustentavel-1.0.0.jar app.jar
USER appuser
ENV SPRING_PROFILES_ACTIVE=prod PORT=8080 JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

### Orquestração

Três arquivos de Compose, um por finalidade: `docker-compose.yml` (desenvolvimento), `docker-compose.staging.yml` e `docker-compose.prod.yml`. Os três exercem os elementos pedidos na atividade:

**Volumes** — cada ambiente tem um volume nomeado próprio (`oracle-data-staging`, `oracle-data-prod`) montado em `/opt/oracle/oradata`. Os dados do banco sobrevivem a `down`/`up` e ficam isolados entre ambientes: derrubar homologação não encosta nos dados de produção.

**Variáveis de ambiente** — nenhuma senha está escrita nos arquivos de produção. Tudo vem do `.env` com a sintaxe `${VAR:?mensagem}`, que faz o Compose **recusar subir** se a variável não estiver definida, em vez de silenciosamente usar um padrão inseguro. O que varia por ambiente (perfil Spring, porta, memória) também é injetado por variável, então a imagem nunca precisa ser reconstruída para mudar de ambiente.

**Redes** — cada stack cria sua própria rede bridge (`mobilidade-staging-net`, `mobilidade-prod-net`). A aplicação alcança o banco pelo nome do serviço (`oracle-db`), resolvido pelo DNS interno do Compose. Em produção, o banco **não publica porta no host**: ele só é acessível de dentro da rede, o que reduz a superfície exposta.

**Ordem de inicialização** — `depends_on` com `condition: service_healthy` faz a API esperar o Oracle passar no health check antes de subir, evitando o clássico erro de conexão recusada no primeiro boot.

Como alternativa ao Compose, o arquivo [`k8s/mobilidade.yaml`](k8s/mobilidade.yaml) traz a mesma stack em Kubernetes, com ConfigMap e Secret separados, PersistentVolumeClaim para o banco, `startupProbe`/`readinessProbe`/`livenessProbe` na aplicação e rolling update sem indisponibilidade.

### Comandos usados

```bash
# Build da imagem
docker build -t mobilidade-sustentavel:1.0.0 .

# Inspecionar o resultado
docker images mobilidade-sustentavel
docker history mobilidade-sustentavel:1.0.0

# Subir cada ambiente
docker compose -f docker-compose.staging.yml up -d --build
docker compose -f docker-compose.prod.yml up -d --build

# Operação
docker compose -f docker-compose.prod.yml logs -f mobilidade-api
docker compose -f docker-compose.prod.yml ps
docker exec -it mobilidade-api-prod sh

# Kubernetes (alternativa)
kubectl apply -f k8s/mobilidade.yaml
kubectl -n mobilidade get pods,svc
```

---

## Prints do funcionamento

As evidências de execução estão na pasta [`evidencias/`](evidencias/) e também na documentação técnica em PDF entregue junto com este projeto.

| # | Evidência | Arquivo |
|---|---|---|
| 1 | Pipeline completo verde no GitHub Actions | `evidencias/01-pipeline-completo.png` |
| 2 | Etapa de testes automatizados com a suíte passando | `evidencias/02-testes.png` |
| 3 | Build e publicação da imagem no GHCR | `evidencias/03-imagem-publicada.png` |
| 4 | Deploy e smoke test em **staging** | `evidencias/04-deploy-staging.png` |
| 5 | Aprovação manual e deploy em **produção** | `evidencias/05-deploy-producao.png` |
| 6 | Os dois ambientes rodando (`docker ps` + health checks) | `evidencias/06-ambientes-no-ar.png` |
| 7 | Aplicação respondendo em staging (Swagger) e em produção | `evidencias/07-aplicacao-funcionando.png` |

---

## Tecnologias utilizadas

| Camada | Tecnologia | Uso no projeto |
|---|---|---|
| Linguagem | Java 17 | Linguagem da aplicação |
| Framework | Spring Boot 3.2.5 | API REST, injeção de dependências, configuração por perfil |
| Segurança | Spring Security + JJWT 0.11.5 | Autenticação stateless por JWT e autorização por papel |
| Persistência | Spring Data JPA + Hibernate | Mapeamento objeto-relacional |
| Banco de dados | Oracle XE 21c | Base relacional da aplicação |
| Migrations | Flyway | Versionamento do schema |
| Observabilidade | Spring Boot Actuator | Health checks usados pelo Docker e pelo pipeline |
| Documentação da API | SpringDoc OpenAPI (Swagger UI) | Documentação interativa dos endpoints |
| Testes | JUnit 5, Mockito, AssertJ | Testes unitários executados no pipeline |
| Cobertura | JaCoCo | Relatório de cobertura publicado como artefato |
| Containerização | Docker (build multi-stage) | Empacotamento da aplicação |
| Orquestração | Docker Compose v2 e Kubernetes | Aplicação + banco, com volumes, redes e variáveis |
| CI/CD | GitHub Actions | Build, testes, publicação da imagem e deploy |
| Registro de imagens | GitHub Container Registry (GHCR) | Armazenamento das imagens versionadas |
| Produtividade | Lombok, Bean Validation | Redução de boilerplate e validação de entrada |

---

## Checklist de entrega

| Item | OK |
|---|:--:|
| Projeto compactado em `.ZIP` com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| `docker-compose.yml` ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| `README.md` com instruções e prints | ☑ |
| Documentação técnica com evidências (PDF ou PPT) | ☑ |
| Deploy realizado nos ambientes staging e produção | ☑ |

---

## Estrutura do projeto

```
mobilidade-sustentavel/
├── .github/workflows/ci-cd.yml      # Pipeline de CI/CD
├── k8s/mobilidade.yaml              # Orquestração alternativa em Kubernetes
├── src/
│   ├── main/java/...                # Código da aplicação
│   ├── main/resources/
│   │   ├── application.properties            # Configuração comum
│   │   ├── application-staging.properties    # Perfil de homologação
│   │   ├── application-prod.properties       # Perfil de produção
│   │   └── db/migration/                     # Migrations Flyway
│   └── test/java/...                # Testes automatizados
├── Dockerfile                       # Build multi-stage
├── docker-compose.yml               # Stack de desenvolvimento
├── docker-compose.staging.yml       # Ambiente de homologação
├── docker-compose.prod.yml          # Ambiente de produção
├── .env.example                     # Modelo das variáveis de ambiente
├── .dockerignore
└── README.md
```
