# Testes automatizados de API e BDD — Mobilidade Sustentável

Suíte de testes de caixa-preta da API **Mobilidade Sustentável** (Spring Boot + Oracle), feita para o
*Desafio de Testes Automatizados* da Fase 7 (FIAP). Os testes conversam com a aplicação real por HTTP,
como um cliente faria: sem mocks e sem acesso direto ao banco.

- **Comportamento (BDD):** cenários em Gherkin, em português, que descrevem as regras de negócio:
  crédito de CO2, pontos, resgate de recompensas e controle de acesso.
- **API:** os 23 endpoints da aplicação e o health check, cada um com status code, corpo JSON e
  contrato validado por JSON Schema, em casos positivos e negativos.
- **Execução simples:** um script, uma imagem Docker ou o pipeline do GitHub Actions. O código é o
  mesmo nos três caminhos.

| Execução padrão | Resultado |
|---|---|
| Cenários | **63** (15 de negócio + 48 de API), todos aprovados |
| Passos | 348 |
| Tempo | cerca de 12 s contra o staging local |
| Fora da execução padrão | 2 cenários `@defeito-conhecido`, que reproduzem defeitos reais da API (ver [Achados](#achados)) |

## Sumário

1. [Ferramentas](#ferramentas)
2. [Estrutura](#estrutura)
3. [Como executar](#como-executar)
4. [Configuração](#configuração)
5. [Tags e filtros](#tags-e-filtros)
6. [Relatórios](#relatórios)
7. [Cenários de negócio](#cenários-de-negócio)
8. [Cobertura da API](#cobertura-da-api)
9. [Contratos (JSON Schema)](#contratos-json-schema)
10. [Como os testes são escritos](#como-os-testes-são-escritos)
11. [Achados](#achados)
12. [Como adicionar um cenário](#como-adicionar-um-cenário)

## Ferramentas

| Ferramenta | Versão | Papel |
|---|---|---|
| Java (JDK) | 17 | linguagem dos passos |
| Cucumber (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`) | 7.20.1 | BDD: Gherkin em português e um contexto novo por cenário |
| JUnit Platform Suite | 1.11.3 | roda o Cucumber pelo Maven Surefire |
| REST Assured | 5.5.0 | chamadas HTTP e leitura do corpo das respostas |
| `json-schema-validator` (REST Assured) | 5.5.0 | testes de contrato com JSON Schema (draft-04) |
| Maven / Surefire | 3.9 / 3.5.2 | build e execução |
| Docker e Docker Compose | — | ambiente de staging e imagem que roda a suíte |
| GitHub Actions | — | execução no pipeline, resumo e relatório publicados |

## Estrutura

```
api-tests/
├── Dockerfile, .dockerignore       imagem que roda a suíte sem Java nem Maven na máquina
├── run-tests.ps1, run-tests.sh     sobem o staging, esperam o health check e rodam a suíte
├── pom.xml                         dependências de teste
└── src/test
    ├── java/br/com/fiap/mobilidade/apitests
    │   ├── RunCucumberTest.java     ponto de entrada (JUnit Platform Suite)
    │   ├── steps/
    │   │   ├── NegocioSteps.java    passos declarativos, na língua do produto
    │   │   ├── ApiSteps.java        passos técnicos reutilizáveis (requisição, status, contrato, campos)
    │   │   ├── PreparacaoSteps.java passos "Dado": quem está autenticado e quais dados existem
    │   │   ├── Hooks.java           evidência anexada ao relatório e limpeza de dados
    │   │   └── Tipos.java           tipo {decimal}, que aceita vírgula ou ponto
    │   └── support/
    │       ├── ApiClient.java       cliente HTTP sobre o REST Assured
    │       ├── Config.java          endereço da API e credenciais do administrador
    │       └── Contexto.java        estado de um cenário (token, última resposta, variáveis)
    └── resources
        ├── junit-platform.properties  plugins de relatório e filtro de tags
        ├── features/negocio/        3 funcionalidades de negócio
        ├── features/api/            5 funcionalidades técnicas, uma por recurso da API
        └── schemas/                 14 contratos JSON Schema
```

## Como executar

Todos os caminhos precisam da API no ar. O staging local é o `docker-compose.staging.yml` da raiz
do projeto (API em `localhost:8081`, Oracle em `localhost:1522`). Na primeira subida o Oracle leva
uns 2 minutos para inicializar; os scripts esperam até 5 minutos pelo health check.

### 1. Script (recomendado)

Sobe o staging, espera `GET /actuator/health` responder `UP` e roda a suíte. O código de saída é
o do Maven: `0` quando todos os cenários passam.

Windows (PowerShell):

```powershell
cd api-tests
.\run-tests.ps1                  # sobe o staging e roda tudo
.\run-tests.ps1 -SemSubir        # usa uma API que já está no ar
.\run-tests.ps1 -BaseUrl http://localhost:8080
.\run-tests.ps1 -JavaHome "C:\caminho\jdk-17" -Mvn "C:\caminho\maven\bin\mvn.cmd"   # sem java/mvn no PATH
```

Linux ou macOS:

```bash
cd api-tests
./run-tests.sh                   # sobe o staging e roda tudo
./run-tests.sh --sem-subir       # usa uma API que já está no ar
```

Requisitos: Docker, JDK 17 e Maven 3.9 ou mais novo.

### 2. Docker (sem Java nem Maven na máquina)

```bash
cd api-tests
docker build -t mobilidade-api-tests .

# staging no ar
docker compose -f ../docker-compose.staging.yml up -d

# roda na mesma rede do staging e grava o relatório em ./relatorio
docker run --rm --network mobilidade-staging_mobilidade-staging-net \
  -e API_BASE_URL=http://mobilidade-api:8080 \
  -v "$PWD/relatorio:/tests/target/cucumber-report" \
  mobilidade-api-tests
```

No PowerShell, troque o volume por `-v "${PWD}\relatorio:/tests/target/cucumber-report"`.
Com o Docker Desktop também dá para apontar para a porta publicada:
`-e API_BASE_URL=http://host.docker.internal:8081`. Argumentos depois do nome da imagem vão para
o Maven, por exemplo `mobilidade-api-tests -Dcucumber.filter.tags=@negocio`.

### 3. Maven direto

```bash
cd api-tests
mvn test                                       # API em http://localhost:8081
mvn test -Dapi.baseUrl=http://localhost:8080
mvn clean test                                 # recompila tudo do zero
```

### 4. Pipeline (GitHub Actions)

O workflow `.github/workflows/ci-cd.yml` roda a suíte no job **Deploy em staging**, logo depois do
smoke test:

1. a imagem da aplicação publicada no GHCR sobe junto com um Oracle de serviço (banco descartável);
2. `mvn -B -f api-tests/pom.xml test` roda contra `http://localhost:8081`;
3. uma tabela por funcionalidade aparece no resumo da execução (aba *Summary*);
4. o relatório completo fica no artefato `relatorio-bdd-staging` por 14 dias;
5. se algum cenário falhar, o job falha e o deploy em produção não acontece.

Gatilhos: push e pull request nas branches `main` e `develop`. O deploy em staging, e com ele a
suíte, roda em todo push; produção só a partir da `main` e com aprovação manual.

## Configuração

| Propriedade (`-D`) | Variável de ambiente | Padrão | Para quê |
|---|---|---|---|
| `api.baseUrl` | `API_BASE_URL` | `http://localhost:8081` | endereço da API testada |
| `api.adminEmail` | `API_ADMIN_EMAIL` | `admin@mobilidade.com` | administrador criado pela aplicação na subida |
| `api.adminSenha` | `API_ADMIN_SENHA` | `admin123` | senha desse administrador |

A propriedade tem prioridade sobre a variável de ambiente, que tem prioridade sobre o padrão.

## Tags e filtros

| Tag | Marca |
|---|---|
| `@negocio` | cenários de comportamento (`features/negocio`) |
| `@api` | testes por endpoint (`features/api`) |
| `@esg-ambiental`, `@esg-social`, `@esg-governanca` | o pilar ESG de cada funcionalidade de negócio |
| `@defeito-conhecido` | cenário que reproduz um defeito ainda aberto; fica fora da execução padrão |
| `@desativa-usuario-ao-final` | o usuário do cenário é desativado no fim, para não deixar dado inválido no banco |

O filtro padrão está em `junit-platform.properties` (`cucumber.filter.tags=not @defeito-conhecido`)
e pode ser trocado na linha de comando:

```bash
mvn test -Dcucumber.filter.tags="@negocio"
mvn test -Dcucumber.filter.tags="@esg-governanca"
mvn test -Dcucumber.filter.tags="@defeito-conhecido"   # reproduz os defeitos: falha de propósito
```

## Relatórios

Gerados em `target/cucumber-report/`:

| Arquivo | Conteúdo |
|---|---|
| `index.html` | relatório navegável. Cada cenário mostra os passos e, anexada, a última chamada feita à API: método, rota, corpo enviado, status e corpo da resposta |
| `cucumber.json` | resultado estruturado, usado para montar o resumo no pipeline |
| `cucumber-junit.xml` | formato JUnit, para ferramentas de CI |

## Cenários de negócio

Ficam em `features/negocio` e usam passos declarativos: falam a língua do produto ("registro uma
viagem", "tento resgatar a recompensa") sem verbos HTTP nem JSON. A tradução para chamadas à API
fica em `NegocioSteps.java`.

### Crédito ambiental por viagens sustentáveis (`@esg-ambiental`)

Regra: CO2 economizado = (0,21 kg/km do carro a combustão − emissão do modal) × distância, nunca
negativo. Pontos = distância × pontos por km do modal.

| Cenário | Tipo | O que verifica |
|---|---|---|
| Viagem de bicicleta gera crédito de CO2 e pontos | positivo | 12,5 km → 2,625 kg, 125 pontos, saldo 125 |
| O crédito depende da emissão de cada modal (esquema com 6 exemplos) | positivo | 10 km em cada modal, de Bicicleta (2,10 kg, 100 pontos) a Carro Híbrido (0,90 kg, 20 pontos) |
| O painel de impacto soma todas as viagens do usuário | positivo | 5 km de bicicleta + 10 km de ônibus → 2 viagens, 2,5 kg, árvores equivalentes, saldo 80 |
| Corrigir o modal de uma viagem refaz o crédito e o saldo | positivo | 10 km de bicicleta corrigidos para carro híbrido → 0,90 kg, 20 pontos, saldo 20 |

### Resgate de recompensas (`@esg-social`)

| Cenário | Tipo | O que verifica |
|---|---|---|
| Resgate recusado quando o saldo de pontos não é suficiente | negativo | 422 "Pontos insuficientes…", saldo continua 0 |
| Resgate confirmado depois de acumular pontos pedalando | positivo | 15 km de bicicleta (150 pontos), resgate de 100 → saldo 50 |

### Controle de acesso e integridade (`@esg-governanca`)

| Cenário | Tipo | O que verifica |
|---|---|---|
| Usuário comum não pode cadastrar tipos de transporte | negativo | 403 |
| Cadastro com e-mail já existente é recusado | negativo | 422 com o e-mail na mensagem |
| Login inválido não revela se o e-mail existe | negativo | a mesma mensagem "Credenciais inválidas." para senha errada e para e-mail inexistente |
| Conta desativada não consegue mais entrar | negativo | 422 "Credenciais inválidas." depois de desativar a conta |
| Usuário não pode excluir a viagem de outro usuário | `@defeito-conhecido` | espera 403; hoje a API devolve 204 |
| Corrigir uma viagem cujos pontos já foram gastos não deixa o saldo negativo | `@defeito-conhecido` | espera saldo ≥ 0; hoje fica em −70 |

Na execução padrão são 15 cenários de negócio: 10 positivos e 5 negativos.

## Cobertura da API

Os 48 cenários técnicos ficam em `features/api`, um arquivo por recurso. Para cada endpoint são
verificados o status code, campos e valores do corpo e o contrato JSON Schema da resposta.

| Método | Endpoint | Acesso | Status testados | Contratos |
|---|---|---|---|---|
| POST | `/auth/login` | público | 200, 400, 422 | `login-response`, `erro-validacao`, `erro-padrao` |
| GET | `/actuator/health` | público | 200 | `health` |
| POST | `/usuarios` | público | 201, 400, 422 | `usuario-response`, `erro-validacao`, `erro-padrao` |
| GET | `/usuarios` | ADMIN | 200, 403 | `usuario-lista` |
| GET | `/usuarios/{id}` | autenticado | 200, 403 (sem token), 404 | `usuario-response`, `erro-padrao` |
| DELETE | `/usuarios/{id}` | autenticado | 204, 404 | `erro-padrao` |
| GET | `/usuarios/{id}/impacto` | autenticado | 200, 404 | `impacto-response`, `erro-padrao` |
| GET | `/usuarios/ranking` | autenticado | 200 | `ranking-lista` |
| POST | `/usuarios/{id}/recompensas/{recompensaId}/resgatar` | autenticado | 200, 422 | `erro-padrao` (o 200 é texto) |
| GET | `/transportes` | autenticado | 200 | `transporte-lista` |
| GET | `/transportes/{id}` | autenticado | 200, 404 | `transporte-response`, `erro-padrao` |
| POST | `/transportes` | ADMIN | 201, 400, 403, 422 | `transporte-response`, `erro-validacao`, `erro-padrao` |
| PUT | `/transportes/{id}` | ADMIN | 200, 403, 404 | `transporte-response`, `erro-padrao` |
| DELETE | `/transportes/{id}` | ADMIN | 204, 403 | — (sem corpo) |
| POST | `/viagens/usuario/{idUsuario}` | autenticado | 201, 400, 404 | `viagem-response`, `erro-validacao`, `erro-padrao` |
| GET | `/viagens/{id}` | autenticado | 200, 404 | `viagem-response`, `erro-padrao` |
| GET | `/viagens/usuario/{idUsuario}` | autenticado | 200, 404 | `viagem-lista`, `erro-padrao` |
| PUT | `/viagens/{id}` | autenticado | 200, 404 | `viagem-response`, `erro-padrao` |
| DELETE | `/viagens/{id}` | autenticado | 204, 404 | `erro-padrao` |
| GET | `/recompensas` | autenticado | 200 | `recompensa-lista` |
| GET | `/recompensas/{id}` | autenticado | 200, 404 | `recompensa-response`, `erro-padrao` |
| POST | `/recompensas` | ADMIN | 201, 400, 403 | `recompensa-response`, `erro-validacao` |
| DELETE | `/recompensas/{id}` | ADMIN | 204, 403 | — (sem corpo) |
| POST | `/recompensas/usuario/{idUsuario}/resgatar/{idRecompensa}` | autenticado | 200, 422 | `erro-padrao` (o 200 é texto) |

Além do status, os cenários conferem o que a regra de negócio promete: o cabeçalho `Location`
aponta para o recurso criado, listas vêm ordenadas (ranking por pontos, recompensas por custo),
cada erro de validação aponta o campo errado e a desativação é lógica (o recurso continua lá com
`ativo: false`).

## Contratos (JSON Schema)

Ficam em `src/test/resources/schemas` e seguem três regras:

- todos os campos obrigatórios declarados, com tipo, formato e limites (ids ≥ 1, pontos ≥ 0, datas
  ISO-8601, token no formato JWT, enums para tipo de usuário e perfil);
- `additionalProperties: false`: um campo a mais quebra o contrato. É o que garante, por exemplo,
  que a resposta de usuário nunca traga a senha;
- as listas validam cada item com as mesmas regras do recurso individual.

| Contrato | Usado em |
|---|---|
| `login-response.json` | `POST /auth/login` |
| `usuario-response.json`, `usuario-lista.json` | `/usuarios` |
| `impacto-response.json` | `GET /usuarios/{id}/impacto` |
| `ranking-lista.json` | `GET /usuarios/ranking` |
| `transporte-response.json`, `transporte-lista.json` | `/transportes` |
| `viagem-response.json`, `viagem-lista.json` | `/viagens` |
| `recompensa-response.json`, `recompensa-lista.json` | `/recompensas` |
| `erro-padrao.json` | respostas 404 e 422 |
| `erro-validacao.json` | respostas 400, com o mapa de campos inválidos |
| `health.json` | `GET /actuator/health` |

Os contratos foram testados contra respostas erradas de propósito: validar a resposta do login
com o contrato de usuário falha apontando o campo `token` como não permitido e os campos
obrigatórios `ativo`, `dataCadastro` e `pontosRecompensa` como ausentes.

## Como os testes são escritos

- **Duas camadas de passos.** Os cenários de negócio usam frases do produto (`NegocioSteps`); os
  de API usam passos técnicos reutilizáveis (`ApiSteps`), como
  `Quando faço uma requisição POST em "/usuarios" com o corpo:`,
  `Então o status da resposta deve ser 201` e
  `E a resposta deve seguir o contrato "usuario-response.json"`.
- **Preparação separada da verificação.** Os passos `Dado` montam os dados com
  `ApiClient.preparar()`, que não substitui a resposta avaliada pelos passos `Então`.
- **Cenários independentes.** Cada cenário cria o próprio usuário, com e-mail de sufixo aleatório.
  Nenhum depende de dado criado por outro, então rodam em qualquer ordem, quantas vezes for preciso
  e contra qualquer ambiente.
- **Variáveis de cenário.** Valores guardados durante o cenário (ids, sufixo) entram nas rotas e
  nos corpos como `{{nome}}`, por exemplo `"/viagens/{{idViagem}}"`.
- **Números em português.** O tipo `{decimal}` aceita `2,625` ou `2.625`.
- **Dados de teste.** No pipeline o banco é descartado no fim do job. No staging local os
  registros criados pelos testes se acumulam; para zerar, rode
  `docker compose -f docker-compose.staging.yml down -v` na raiz (apaga o volume do Oracle de
  staging, que é recriado na próxima subida).

## Achados

A suíte encontrou defeitos reais. Os dois primeiros estão reproduzidos em cenários
`@defeito-conhecido`; quando forem corrigidos na aplicação, basta tirar a tag para que entrem na
execução padrão.

1. **BOLA: qualquer usuário age sobre recursos de outro (OWASP API1:2023).** A aplicação confere
   se há um token válido, mas nunca confere se o recurso pertence a quem chama. Trocando o id na
   rota, um usuário consegue consultar, corrigir e excluir viagens alheias (e a exclusão tira os
   pontos da vítima), registrar viagens em nome de outro, consultar e desativar outra conta, ver o
   impacto de outro usuário e resgatar recompensas com os pontos dele. O cenário reproduz a
   exclusão: a API devolve 204 onde deveria devolver 403. Correção sugerida: nos serviços de
   viagens, usuários e resgates, comparar o dono do recurso com o usuário do token (ou exigir
   ADMIN) e responder 403.
2. **Saldo de pontos negativo.** Corrigir uma viagem recalcula o saldo como
   `saldo − pontos antigos + pontos novos`, sem considerar que os pontos já podem ter sido gastos.
   No cenário: 15 km de bicicleta (150 pontos), resgate de 100 (saldo 50) e correção para carro
   híbrido (30 pontos) deixam o saldo em −70. A exclusão faz o contrário e trava o saldo em zero,
   então o usuário fica com a recompensa mesmo sem a viagem que a pagou. Os contratos de
   `GET /usuarios` e do ranking exigem pontos ≥ 0 e passam a falhar quando existe um usuário assim;
   por isso o cenário desativa o próprio usuário no fim (`@desativa-usuario-ao-final`). Correção
   sugerida: recusar com 422 a correção ou exclusão que deixaria o saldo abaixo de zero.
3. **403 em vez de 401.** Requisições sem token recebem 403, porque a configuração de segurança
   não define um `AuthenticationEntryPoint`. Os testes registram o comportamento atual. Sugestão:
   responder 401 com `WWW-Authenticate: Bearer`.
4. **Resgate responde texto, não JSON.** As duas rotas de resgate devolvem `text/plain` no sucesso
   ("… resgatada com sucesso! Pontos restantes: N"), então não há contrato JSON possível; o teste
   confere status e texto. Sugestão: devolver JSON com a recompensa, os pontos debitados e o saldo.
5. **Duas rotas para o mesmo resgate.** `POST /recompensas/usuario/{idUsuario}/resgatar/{idRecompensa}`
   e `POST /usuarios/{id}/recompensas/{recompensaId}/resgatar` fazem a mesma coisa. As duas estão
   cobertas; sugestão: manter só uma.
6. **Emissão do modal sem teto.** O cadastro de modal aceita emissão acima de 0,21 kg/km (a do
   carro de referência). Viagens nesse modal creditam 0 kg sem aviso. Sugestão: validar o limite
   no cadastro.

Ponto positivo confirmado: o login responde "Credenciais inválidas." tanto para senha errada quanto
para e-mail inexistente ou conta desativada, então não dá para descobrir quais e-mails estão
cadastrados.

## Como adicionar um cenário

1. Escreva o cenário no arquivo certo: `features/negocio` para regra de negócio, `features/api`
   para o contrato de um endpoint.
2. Reaproveite os passos existentes (`ApiSteps`, `PreparacaoSteps`, `NegocioSteps`).
3. Se precisar de um passo novo, crie o método com a anotação em português (`@Dado`, `@Quando`,
   `@Entao`, `@E`) na classe da camada certa.
4. Resposta JSON nova pede contrato novo em `schemas/`, com `additionalProperties: false`.
5. Rode só o que mudou (`mvn test -Dcucumber.filter.tags="@sua-tag"`) e depois a suíte inteira.
