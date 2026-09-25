# Voting API

API REST para gerenciamento de pautas e sessões de votação de uma cooperativa.

A aplicação permite cadastrar pautas, abrir sessões de votação, registrar votos de associados e consultar o resultado após o encerramento da sessão.

A solução também contempla validação externa da elegibilidade do associado, controle de concorrência, tratamento padronizado de erros, testes automatizados, documentação OpenAPI, persistência com PostgreSQL, versionamento do banco de dados com Flyway, análise de performance e execução containerizada com Docker.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven / Maven Wrapper
- JUnit 5
- Mockito
- AssertJ
- Testcontainers
- WireMock
- Springdoc OpenAPI / Swagger
- Spring Boot Actuator
- Docker
- Docker Compose

## Requisitos

### Execução local

- Java 21
- PostgreSQL

O projeto inclui o **Maven Wrapper**, portanto não é necessário possuir o Maven instalado globalmente.

Caso prefira, uma instalação local do Maven também pode ser utilizada.

### Testes de integração

- Docker ou outro runtime compatível com Testcontainers

Os testes de integração utilizam Testcontainers para iniciar automaticamente uma instância PostgreSQL durante a execução da suíte.

Docker Compose não é necessário para executar esses testes.

### Ambiente containerizado

Para executar a aplicação e o PostgreSQL através do arquivo `compose.yaml`:

- Docker
- Docker Compose

## Arquitetura

A aplicação utiliza uma arquitetura em camadas, separando as responsabilidades de entrada, regras de negócio, persistência e integrações externas.

```text
Cliente
   |
   v
Controller
   |
   v
Service
   |
   +----------> Serviço externo de elegibilidade
   |
   v
Repository
   |
   v
PostgreSQL
```

Os **Controllers** são responsáveis pelo contrato HTTP e pela entrada das requisições.

A camada de **Service** concentra as regras de negócio da aplicação.

Os **Repositories** realizam o acesso aos dados utilizando Spring Data JPA.

A conversão entre entidades e DTOs é realizada por **Mappers**, evitando atribuir essa responsabilidade à camada de serviço.

## Decisões de domínio

### Sessão de votação

Cada pauta pode possuir apenas uma sessão de votação.

Quando a duração não é informada na abertura, a sessão permanece aberta por **1 minuto**.

Os timestamps da aplicação são tratados em UTC.

A sessão utiliza o intervalo temporal:

```text
[inicio, fim)
```

Portanto, no instante exato definido por `fim`, a sessão já é considerada encerrada.

### Registro de votos

Os votos aceitos são:

```text
SIM
NAO
```

Cada voto pertence à sessão de votação de uma pauta.

Cada associado pode registrar apenas um voto por pauta.

Como cada pauta possui apenas uma sessão de votação, a unicidade é garantida no banco pela combinação:

```text
(sessao_id, associado_id)
```

### Resultado da votação

O resultado final somente pode ser consultado após o encerramento da sessão.

A contabilização dos votos é realizada diretamente no banco de dados, evitando carregar todos os votos em memória.

Critérios:

- `SIM > NAO`: `APROVADA`
- `NAO > SIM`: `REJEITADA`
- `SIM = NAO`: `EMPATE`

Uma votação encerrada sem votos também é considerada `EMPATE`, pois o requisito não define um estado específico para ausência de votos.

## Versionamento da API

A API utiliza versionamento por URI.

A versão atual é:

```text
/api/v1
```

Essa estratégia permite a criação futura de novas versões do contrato sem quebrar os consumidores da versão atual.

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/pautas` | Cria uma nova pauta |
| `POST` | `/api/v1/pautas/{pautaId}/sessoes` | Abre uma sessão de votação |
| `POST` | `/api/v1/pautas/{pautaId}/votos` | Registra o voto de um associado |
| `GET` | `/api/v1/pautas/{pautaId}/resultado` | Consulta o resultado da votação |

### Criar pauta

```http
POST /api/v1/pautas
```

Exemplo:

```json
{
  "titulo": "Aprovação do orçamento anual",
  "descricao": "Votação para aprovação do orçamento do próximo exercício"
}
```

### Abrir sessão de votação

```http
POST /api/v1/pautas/{pautaId}/sessoes
```

Exemplo com duração de 5 minutos:

```json
{
  "duracaoMinutos": 5
}
```

Para utilizar a duração padrão de **1 minuto**, envie um objeto vazio:

```json
{}
```

Também é possível informar explicitamente:

```json
{
  "duracaoMinutos": null
}
```

O corpo da requisição não pode ser completamente omitido, pois o endpoint utiliza um `@RequestBody` obrigatório.

### Registrar voto

```http
POST /api/v1/pautas/{pautaId}/votos
```

Exemplo:

```json
{
  "associadoId": "69037798098",
  "opcao": "SIM"
}
```

As opções aceitas são:

```text
SIM
NAO
```

### Consultar resultado

```http
GET /api/v1/pautas/{pautaId}/resultado
```

O resultado somente pode ser consultado após o encerramento da sessão.

## Elegibilidade do associado

Antes de registrar o voto, a aplicação consulta um serviço externo para verificar se o associado está habilitado para votar.

Para implementação desse requisito, o campo `associadoId` é enviado ao serviço externo como CPF.

A aplicação **não realiza validação algorítmica de CPF localmente**. O DTO valida o preenchimento e o tamanho do identificador, enquanto a elegibilidade é determinada pelo serviço externo.

O contrato esperado do serviço possui os seguintes estados:

```text
ABLE_TO_VOTE
UNABLE_TO_VOTE
```

Os principais comportamentos tratados são:

- `ABLE_TO_VOTE`: associado habilitado para votar;
- `UNABLE_TO_VOTE`: associado não habilitado;
- CPF não encontrado;
- indisponibilidade do serviço externo;
- timeout;
- resposta inválida;
- resposta sem o status esperado.

A URL da integração é externalizada através da variável:

```text
ELEGIBILIDADE_BASE_URL
```

O serviço originalmente disponibilizado no desafio:

```text
https://user-info.herokuapp.com
```

não está mais disponível.

Por esse motivo, os testes automatizados utilizam **WireMock** para simular seu contrato de maneira determinística.

### Testando manualmente o registro de votos

Como o serviço original está indisponível, o fluxo manual de registro de votos requer que `ELEGIBILIDADE_BASE_URL` aponte para um mock ou serviço compatível com o contrato esperado.

Exemplo:

```text
ELEGIBILIDADE_BASE_URL=http://localhost:9999
```

O serviço configurado deve disponibilizar:

```http
GET /users/{cpf}
```

e retornar uma resposta compatível com o contrato de elegibilidade.

Nos testes automatizados essa dependência é substituída pelo WireMock.

## Tratamento de erros

A API possui tratamento centralizado de exceções através de um `GlobalExceptionHandler`.

Os principais códigos HTTP utilizados são:

| Status | Situação |
|---|---|
| `400 Bad Request` | Dados de entrada inválidos |
| `404 Not Found` | Recurso ou associado não encontrado |
| `409 Conflict` | Conflito de regra, como voto duplicado |
| `422 Unprocessable Content` | Regra de negócio impede a operação |
| `503 Service Unavailable` | Serviço externo indisponível |

As respostas são padronizadas e não expõem stack traces ou detalhes internos da implementação.

Exemplo:

```json
{
  "timestamp": "2026-09-24T03:00:00Z",
  "status": 422,
  "error": "Unprocessable Content",
  "message": "O associado não está habilitado para votar",
  "path": "/api/v1/pautas/1/votos"
}
```

Erros de Bean Validation também podem apresentar os campos que falharam na validação.

## Concorrência e consistência

A aplicação impede que um associado registre mais de um voto na mesma sessão.

A validação é realizada inicialmente pela camada de serviço.

Entretanto, apenas essa verificação não seria suficiente em um cenário concorrente, pois duas requisições poderiam passar pela validação antes da persistência.

Por esse motivo, a regra também é protegida no PostgreSQL através de uma constraint de unicidade:

```text
(sessao_id, associado_id)
```

Dessa forma, o próprio banco garante a consistência dos dados mesmo diante de requisições concorrentes.

A violação dessa regra é tratada pela aplicação e convertida para:

```text
409 Conflict
```

Foi criado um teste de integração concorrente para verificar que, diante de requisições simultâneas para o mesmo associado, apenas um voto é efetivamente persistido.

## Banco de dados e Flyway

A aplicação utiliza PostgreSQL como banco de dados.

O Hibernate está configurado para apenas validar o schema:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

A criação e evolução da estrutura do banco são responsabilidade do **Flyway**.

As migrations atuais são:

```text
V1 - criação da tabela de pauta
V2 - criação da tabela de sessão de votação
V3 - criação da estrutura de votos
V4 - criação dos índices de votação
```

Na inicialização da aplicação, o Flyway valida o histórico e executa automaticamente as migrations pendentes.

## Performance da apuração

A contabilização dos votos é realizada diretamente no PostgreSQL através de uma consulta de agregação, evitando carregar todos os votos para a memória da aplicação.

Foi criado um índice composto sobre:

```text
(sessao_id, opcao)
```

para auxiliar as consultas relacionadas à apuração.

### Validação automatizada

Foi implementado um teste de integração com **100.000 votos**, distribuídos da seguinte maneira:

```text
60.000 SIM
40.000 NAO
```

O teste verifica que a consulta retorna corretamente os totais esperados.

O tempo de execução é registrado apenas como informação diagnóstica e não é utilizado como critério de aprovação do teste, pois pode variar conforme hardware, Docker, banco de dados e ambiente de execução.

Existe também um teste de integração responsável por verificar a existência do índice utilizado pela consulta.

### Análise manual

Durante o desenvolvimento, a consulta de apuração também foi analisada manualmente utilizando `EXPLAIN ANALYZE`.

No cenário analisado, o PostgreSQL utilizou o índice `idx_voto_sessao_opcao` através de um `Bitmap Index Scan`.

Trecho observado no plano:

```text
Bitmap Index Scan on idx_voto_sessao_opcao
  Index Cond: (sessao_id = '1'::bigint)
```

Essa análise é diagnóstica e **não faz parte da suíte automatizada**.

O plano escolhido pelo PostgreSQL pode variar conforme volume, distribuição dos dados e estatísticas disponíveis no banco.

## Testes automatizados

O projeto possui testes em diferentes níveis.

### Testes unitários

Os testes unitários utilizam:

- JUnit 5;
- Mockito;
- AssertJ.

Eles validam as regras de negócio de forma isolada.

### Testes de integração

Os testes de integração utilizam:

- Spring Boot Test;
- MockMvc;
- Testcontainers;
- PostgreSQL;
- Flyway.

O Testcontainers inicia uma instância real do PostgreSQL durante os testes.

Isso permite validar o comportamento real de:

- migrations;
- queries;
- constraints;
- persistência;
- transações;
- concorrência.

Essa abordagem reduz diferenças entre o ambiente utilizado nos testes e o banco adotado pela aplicação.

### Testes da integração externa

O serviço de elegibilidade é simulado com **WireMock**.

São testados cenários como:

- associado habilitado;
- associado não habilitado;
- CPF inexistente;
- erro HTTP;
- indisponibilidade;
- timeout;
- JSON inválido;
- resposta sem status.

Assim, a suíte não depende da disponibilidade do serviço externo.

### Controle do tempo nos testes

As regras temporais utilizam um `Clock` injetável.

Nos testes, o relógio pode ser controlado para validar deterministicamente cenários como:

```text
antes do encerramento
no instante exato do encerramento
```

Isso evita o uso de `Thread.sleep()` e torna os testes mais rápidos e previsíveis.

### Executando os testes

Linux/macOS:

```bash
./mvnw clean verify
```

Windows:

```powershell
mvnw.cmd clean verify
```

Alternativamente, caso o Maven esteja instalado globalmente:

```bash
mvn clean verify
```

Os testes que utilizam Testcontainers requerem Docker ou outro runtime compatível disponível no ambiente.

Docker Compose não é necessário para executar a suíte de testes.

## OpenAPI / Swagger

A API possui documentação OpenAPI utilizando **Springdoc**.

Com a aplicação em execução, a interface Swagger pode ser acessada em:

```text
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI está disponível em:

```text
http://localhost:8080/v3/api-docs
```

Os endpoints são documentados com anotações como:

```text
@Operation
@ApiResponse
```

Os DTOs utilizam:

```text
@Schema
```

para descrever campos, exemplos e valores permitidos.

## Configuração

A aplicação utiliza variáveis de ambiente para permitir sua execução em diferentes ambientes sem necessidade de alterar ou recompilar o código.

| Variável | Descrição | Valor padrão |
|---|---|---|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/votacao` |
| `DB_USERNAME` | Usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha do PostgreSQL | `postgres` |
| `SERVER_PORT` | Porta HTTP da aplicação | `8080` |
| `ELEGIBILIDADE_BASE_URL` | URL do serviço de elegibilidade | `https://user-info.herokuapp.com` (indisponível atualmente) |
| `ELEGIBILIDADE_TIMEOUT_SEGUNDOS` | Timeout da integração em segundos | `3` |

> **Atenção:** o serviço de elegibilidade utilizado originalmente pelo desafio não está mais disponível. Para executar manualmente o fluxo de votação, configure `ELEGIBILIDADE_BASE_URL` apontando para um mock ou serviço compatível.

Em ambientes produtivos, credenciais não devem ser armazenadas no código-fonte ou no repositório.

Elas devem ser fornecidas pelo mecanismo de configuração ou gerenciamento de secrets da plataforma utilizada.

## Executando localmente

### 1. Criar o banco de dados

No PostgreSQL:

```sql
CREATE DATABASE votacao;
```

### 2. Configurar a integração de elegibilidade

Para testar apenas funcionalidades que não dependem do registro de votos, nenhuma configuração adicional é necessária.

Para testar manualmente o registro de votos, configure `ELEGIBILIDADE_BASE_URL` apontando para um serviço ou mock compatível.

Exemplo:

```text
ELEGIBILIDADE_BASE_URL=http://localhost:9999
```

### 3. Executar a aplicação

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

Caso o Maven esteja instalado globalmente:

```bash
mvn spring-boot:run
```

Durante a inicialização, o Flyway valida e aplica as migrations necessárias.

A aplicação ficará disponível em:

```text
http://localhost:8080
```

O Swagger poderá ser acessado em:

```text
http://localhost:8080/swagger-ui.html
```

> Para que `./mvnw` funcione diretamente após uma clonagem em Linux/macOS, o arquivo `mvnw` deve estar versionado no Git com permissão de execução (`100755`).

## Docker

A aplicação possui um `Dockerfile` multi-stage utilizando Java 21.

A primeira etapa contém as ferramentas necessárias para realizar o build da aplicação.

A imagem final contém apenas o ambiente necessário para executar o artefato.

Para construir a imagem:

```bash
docker build -t votacao-api:1.0 .
```

## Docker Compose

A aplicação e o PostgreSQL podem ser executados em conjunto utilizando Docker Compose:

```bash
docker compose up --build
```

No ambiente Docker, a API acessa o PostgreSQL através do nome do serviço na rede interna, sem depender de `localhost`.

Para utilizar o fluxo de registro de votos, `ELEGIBILIDADE_BASE_URL` também deve apontar para um serviço de elegibilidade acessível a partir do container da API.

> Dentro de um container, `localhost` representa o próprio container. Portanto, um mock executado diretamente na máquina host pode exigir um endereço diferente, dependendo do ambiente Docker utilizado.

Para encerrar os containers:

```bash
docker compose down
```

Para encerrar e remover também os volumes:

```bash
docker compose down -v
```

## Health Check

O projeto utiliza **Spring Boot Actuator** para disponibilizar informações sobre a saúde da aplicação.

Health check:

```text
GET /actuator/health
```

Exemplo:

```json
{
  "status": "UP"
}
```

As probes de liveness e readiness são habilitadas explicitamente na configuração da aplicação.

Liveness:

```text
GET /actuator/health/liveness
```

Readiness:

```text
GET /actuator/health/readiness
```

O **liveness** indica se a aplicação está em execução.

O **readiness** indica se a aplicação está pronta para receber tráfego.

Esses endpoints podem ser utilizados por plataformas de containers e orquestradores para monitorar o estado da aplicação.

## Cloud Readiness

A aplicação foi preparada para execução em ambientes containerizados e futura implantação em nuvem.

As principais decisões adotadas foram:

- containerização com Docker;
- Dockerfile multi-stage;
- aplicação stateless;
- configuração por variáveis de ambiente;
- PostgreSQL externo ao container da aplicação;
- versionamento do schema com Flyway;
- health check com Spring Boot Actuator;
- probes de liveness e readiness;
- serviço externo configurável;
- credenciais externas ao código.

A aplicação não depende de configurações específicas da máquina de desenvolvimento para ser executada.

Uma plataforma de containers pode fornecer as configurações necessárias através de variáveis de ambiente e conectar a aplicação a uma instância PostgreSQL gerenciada.

A infraestrutura específica de cloud não faz parte desta versão do projeto e poderá ser tratada separadamente.

## Build

### Executar toda a suíte de testes

Linux/macOS:

```bash
./mvnw clean verify
```

Windows:

```powershell
mvnw.cmd clean verify
```

### Gerar o artefato

Linux/macOS:

```bash
./mvnw clean package
```

Windows:

```powershell
mvnw.cmd clean package
```

### Construir a imagem Docker

```bash
docker build -t votacao-api:1.0 .
```

Caso o Maven esteja instalado globalmente, os comandos `mvn` também podem ser utilizados no lugar do Maven Wrapper.

## Fluxo para validação manual

Uma sequência possível para validar a aplicação é:

1. configurar um serviço de elegibilidade compatível;
2. criar uma pauta;
3. abrir uma sessão de votação;
4. registrar votos `SIM` e `NAO`;
5. tentar registrar um segundo voto para o mesmo associado;
6. tentar consultar o resultado enquanto a sessão ainda estiver aberta;
7. aguardar o encerramento da sessão;
8. consultar o resultado final.

Os principais cenários também são cobertos pelos testes automatizados.
