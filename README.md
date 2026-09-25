# Voting API

API REST para gerenciamento de pautas, sessões de votação e votos de associados de uma cooperativa.

A aplicação permite criar pautas, abrir sessões de votação, registrar votos e consultar o resultado após o encerramento da sessão.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Docker
- Docker Compose
- Testcontainers
- WireMock
- JUnit 5
- Mockito
- AssertJ
- JaCoCo
- Spring Boot Actuator
- OpenAPI / Swagger

## Arquitetura

A aplicação foi organizada em camadas, separando as responsabilidades entre:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

A conversão de dados para DTOs é realizada por classes Mapper, mantendo essa responsabilidade separada das regras de negócio.

A estrutura do projeto é organizada por domínio, incluindo módulos relacionados a:

```text
pauta
sessao
voto
resultado
integracao
```

## Requisitos

### Execução local

Para executar a aplicação diretamente na máquina:

- Java 21
- PostgreSQL

O projeto contém Maven Wrapper, portanto não é necessário possuir Maven instalado globalmente.

### Testes automatizados

Para executar a suíte completa:

- Java 21
- Docker

Os testes de integração utilizam **Testcontainers**, que cria automaticamente uma instância PostgreSQL isolada em uma porta dinâmica.

Portanto, **não é necessário possuir PostgreSQL local em execução para executar os testes**.

Docker Compose não é necessário para a suíte de testes.

### Ambiente containerizado

Para executar a aplicação utilizando o `compose.yaml`:

- Docker
- Docker Compose

## Configuração

A aplicação utiliza variáveis de ambiente para permitir configuração externa e facilitar a execução em diferentes ambientes.

| Variável | Valor padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/votacao` | URL de conexão com PostgreSQL |
| `DB_USERNAME` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta HTTP da aplicação |
| `ELEGIBILIDADE_BASE_URL` | `https://user-info.herokuapp.com` | URL do serviço externo de elegibilidade |
| `ELEGIBILIDADE_TIMEOUT_SEGUNDOS` | `3` | Timeout da integração |

> O serviço original de elegibilidade utilizado no desafio (`https://user-info.herokuapp.com`) não está mais disponível. Para registrar votos manualmente, configure `ELEGIBILIDADE_BASE_URL` apontando para um mock ou serviço compatível.

## Banco de dados

A aplicação utiliza PostgreSQL como banco de dados relacional.

A criação e evolução do schema são controladas pelo Flyway.

O Hibernate está configurado com:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Dessa forma, o Hibernate apenas valida se o schema existente corresponde ao modelo da aplicação.

A responsabilidade pela criação e evolução das estruturas do banco pertence ao Flyway.

## Flyway

As migrations são executadas automaticamente durante a inicialização da aplicação.

As migrations do projeto estão em:

```text
src/main/resources/db/migration/
```

O histórico pode ser consultado no PostgreSQL através de:

```sql
SELECT *
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Decisões de domínio

### Sessão de votação

Cada pauta pode possuir apenas uma sessão de votação.

Quando a duração não é informada, a sessão permanece aberta por **1 minuto**.

Como o endpoint exige um corpo JSON, para utilizar a duração padrão deve ser enviado:

```json
{}
```

ou:

```json
{
  "duracaoMinutos": null
}
```

Os timestamps da aplicação são tratados em UTC.

A sessão utiliza o intervalo temporal:

```text
[inicio, fim)
```

Isso significa:

```text
inicio <= instante < fim
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

Além da verificação realizada pela aplicação, existe uma constraint no banco de dados para garantir a regra mesmo em cenários concorrentes.

### Identificação do associado

O identificador informado no campo `associadoId` é utilizado na integração com o serviço de elegibilidade.

A aplicação não realiza validação estrutural local de CPF.

O valor é validado quanto às restrições definidas no DTO e posteriormente enviado ao serviço externo responsável pela elegibilidade.

### Elegibilidade do associado

Antes de registrar um voto, a aplicação consulta o serviço externo de elegibilidade.

As respostas esperadas são:

```text
ABLE_TO_VOTE
UNABLE_TO_VOTE
```

Caso o serviço esteja indisponível, ocorra timeout, seja retornada uma resposta inválida ou não seja possível determinar a elegibilidade, a API retorna erro de indisponibilidade da integração.

### Resultado da votação

O resultado final somente pode ser consultado após o encerramento da sessão.

A contabilização é executada diretamente no PostgreSQL através de agregação, evitando carregar todos os votos em memória.

Critérios:

```text
SIM > NAO  → APROVADA
NAO > SIM  → REJEITADA
SIM = NAO  → EMPATE
```

Uma votação encerrada sem votos também é considerada:

```text
EMPATE
```

pois o requisito não define um estado específico para ausência de votos.

A construção dos DTOs de resposta é delegada às classes Mapper, mantendo a regra de apuração no service e a transformação dos dados separada da regra de negócio.

## Índices

Foram adicionados índices específicos para os principais padrões de acesso aos votos.

Entre eles está o índice utilizado na apuração:

```text
(sessao_id, opcao)
```

Esse índice auxilia as consultas de agregação utilizadas para contabilizar votos `SIM` e `NAO` de uma sessão.

A existência dos índices também é validada através de teste de integração.

## Performance da apuração

Foi realizado um teste com:

```text
100.000 votos
```

A contabilização é executada diretamente no PostgreSQL.

Durante os testes realizados, a apuração pela aplicação ocorreu na ordem de dezenas de milissegundos.

Também foi realizada manualmente uma análise utilizando:

```sql
EXPLAIN ANALYZE
```

O PostgreSQL utilizou o índice:

```text
idx_voto_sessao_opcao
```

através de:

```text
Bitmap Index Scan
```

seguido de:

```text
Bitmap Heap Scan
```

Em uma das execuções observadas, o PostgreSQL apresentou aproximadamente:

```text
Execution Time: 13.520 ms
```

O `EXPLAIN ANALYZE` foi utilizado como validação manual da estratégia de acesso ao banco e não faz parte da suíte automatizada.

## Endpoints

A API está versionada através do prefixo:

```text
/api/v1
```

### Criar pauta

```http
POST /api/v1/pautas
```

Exemplo:

```json
{
  "titulo": "Nova proposta",
  "descricao": "Descrição da pauta"
}
```

### Abrir sessão de votação

```http
POST /api/v1/pautas/{pautaId}/sessoes
```

Exemplo:

```json
{
  "duracaoMinutos": 5
}
```

Para utilizar a duração padrão de 1 minuto:

```json
{}
```

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

### Consultar resultado

```http
GET /api/v1/pautas/{pautaId}/resultado
```

Exemplo de resposta:

```json
{
  "pautaId": 1,
  "totalVotos": 100,
  "totalSim": 60,
  "totalNao": 40,
  "resultado": "APROVADA"
}
```

## OpenAPI / Swagger

Os endpoints são documentados utilizando OpenAPI.

Os controllers utilizam anotações como:

```text
@Operation
@ApiResponse
```

Os DTOs utilizam:

```text
@Schema
```

Com a aplicação em execução, a interface Swagger UI pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI está disponível em:

```text
http://localhost:8080/v3/api-docs
```

## Coleção Bruno

O repositório contém uma coleção do **Bruno** para facilitar a execução e a validação manual dos endpoints da API.

A coleção está disponível em:

```text
collections/
```

Ela contempla os principais fluxos da aplicação:

- criação de pauta;
- abertura de sessão de votação;
- registro de voto;
- consulta do resultado da votação.

Para executar o fluxo de registro de votos, o serviço configurado em `ELEGIBILIDADE_BASE_URL` precisa estar disponível e retornar uma resposta compatível com a integração esperada pela aplicação.

## Tratamento de erros

A API possui tratamento centralizado de exceções.

Entre os cenários tratados estão:

- pauta inexistente;
- sessão inexistente;
- tentativa de abrir uma segunda sessão para a mesma pauta;
- sessão encerrada;
- tentativa de consultar resultado antes do encerramento;
- voto duplicado;
- associado não habilitado para votar;
- falha ou timeout no serviço de elegibilidade;
- requisição inválida.

As respostas de erro seguem um formato padronizado.

## Testes automatizados

O projeto possui testes unitários e testes de integração.

Os testes unitários utilizam:

- JUnit 5;
- Mockito;
- AssertJ.

Os testes de integração utilizam:

- Spring Boot Test;
- MockMvc;
- Testcontainers;
- PostgreSQL;
- WireMock.

O PostgreSQL dos testes é iniciado automaticamente pelo Testcontainers em uma porta dinâmica.

Isso permite executar a suíte sem depender de uma instância PostgreSQL previamente instalada ou disponível em `localhost:5432`.

O WireMock é utilizado para simular o serviço externo de elegibilidade.

Entre os cenários cobertos estão:

- criação de pauta;
- abertura de sessão;
- duração padrão da sessão;
- regras temporais da sessão;
- registro de votos `SIM` e `NAO`;
- prevenção de voto duplicado;
- concorrência no registro de votos;
- apuração aprovada;
- apuração rejeitada;
- empate;
- votação sem votos;
- comportamento da integração de elegibilidade;
- timeout da integração;
- resposta de elegibilidade sem status;
- resposta de elegibilidade sem corpo;
- JSON inválido;
- propagação de violações de integridade não relacionadas a voto duplicado;
- identificação da constraint de voto duplicado;
- existência dos índices utilizados pela aplicação;
- apuração de 100.000 votos.

## Executando os testes

### Windows / PowerShell

```powershell
.\mvnw.cmd clean verify
```

### Linux / macOS

Caso o `mvnw` esteja versionado com permissão de execução:

```bash
./mvnw clean verify
```

Caso contrário:

```bash
sh mvnw clean verify
```

### Maven instalado globalmente

```bash
mvn clean verify
```

Os testes de integração precisam de um runtime Docker disponível para que o Testcontainers possa iniciar o PostgreSQL.

Docker Compose não é necessário para executar a suíte.

## Cobertura de testes

A cobertura da suíte automatizada é monitorada utilizando **JaCoCo**.

O JaCoCo é executado durante o ciclo de build do Maven e gera métricas de cobertura do código, incluindo cobertura de linhas e branches.

Para executar toda a suíte e gerar o relatório:

### Windows / PowerShell

```powershell
.\mvnw.cmd clean verify
```

### Linux / macOS

```bash
./mvnw clean verify
```

ou, caso o wrapper não esteja executável:

```bash
sh mvnw clean verify
```

Após a execução, o relatório HTML estará disponível em:

```text
target/site/jacoco/index.html
```

O relatório apresenta métricas como:

- instructions;
- branches;
- lines;
- methods;
- classes.

### Cobertura atual

No levantamento realizado com a suíte atual, foram observados aproximadamente:

| Métrica | Cobertura |
|---|---:|
| Instructions | 92% |
| Branches | 97% |
| Lines | 89% |
| Classes | 100% |

A cobertura é utilizada como ferramenta para identificar cenários relevantes ainda não exercitados pelos testes, e não como objetivo isolado de atingir 100%.

Durante a análise de cobertura foram adicionados testes para cenários como:

- fronteiras temporais da sessão;
- propagação de violações de integridade não relacionadas a voto duplicado;
- resposta do serviço de elegibilidade sem corpo;
- percurso completo da cadeia de exceções na identificação de voto duplicado.

Não foram adicionados testes exclusivamente para aumentar artificialmente a cobertura de código estrutural, como getters e setters.

### Quality Gate

O build possui um **quality gate de cobertura** configurado através do JaCoCo.

Os limites mínimos são:

| Métrica | Mínimo |
|---|---:|
| Line Coverage | 85% |
| Branch Coverage | 90% |

Durante a fase Maven:

```text
verify
```

o JaCoCo verifica automaticamente esses limites.

Caso a cobertura fique abaixo de qualquer um deles, o build é interrompido com falha.

O fluxo é:

```text
Testes
   ↓
JaCoCo
   ↓
Relatório de cobertura
   ↓
Quality Gate
   ↓
BUILD SUCCESS / BUILD FAILURE
```

Quando os limites são atendidos, o JaCoCo informa:

```text
All coverage checks have been met.
```

Os limites foram definidos abaixo da cobertura atual para permitir a evolução normal do código sem tornar o build excessivamente frágil, ao mesmo tempo em que protegem o projeto contra regressões significativas na cobertura.

## Executando localmente

É necessário possuir PostgreSQL disponível e criar o banco:

```text
votacao
```

As configurações padrão são:

```text
URL: jdbc:postgresql://localhost:5432/votacao
Usuário: postgres
Senha: postgres
```

As configurações podem ser sobrescritas através das variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

### Windows / PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

ou:

```bash
sh mvnw spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080
```

## Docker

A aplicação possui `Dockerfile` para geração da imagem.

Para gerar o pacote:

### Windows / PowerShell

```powershell
.\mvnw.cmd clean package
```

### Linux / macOS

```bash
./mvnw clean package
```

Depois, a imagem pode ser criada com:

```bash
docker build -t votacao-api .
```

## Docker Compose

O projeto contém um arquivo:

```text
compose.yaml
```

que permite iniciar a aplicação juntamente com PostgreSQL.

Execute:

```bash
docker compose up --build
```

Para executar em segundo plano:

```bash
docker compose up -d --build
```

Para encerrar:

```bash
docker compose down
```

A utilização do Docker Compose é destinada ao ambiente containerizado e não é requisito para a execução dos testes automatizados.

## Health Check

A aplicação utiliza Spring Boot Actuator.

O endpoint principal de health check está disponível em:

```text
GET /actuator/health
```

As probes de liveness e readiness podem ser utilizadas através de:

```text
GET /actuator/health/liveness
GET /actuator/health/readiness
```

A aplicação deve possuir as probes habilitadas através da configuração:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
```

A exposição dos endpoints permanece restrita a:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Isso permite utilização em ambientes como Kubernetes e plataformas de cloud sem expor desnecessariamente outros endpoints do Actuator.

## Cloud readiness

A aplicação foi preparada para execução em ambientes containerizados e cloud.

As principais configurações operacionais podem ser fornecidas externamente por variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SERVER_PORT
ELEGIBILIDADE_BASE_URL
ELEGIBILIDADE_TIMEOUT_SEGUNDOS
```

A aplicação também possui:

- imagem Docker;
- Docker Compose para execução local containerizada;
- migrations Flyway;
- health check via Actuator;
- probes de liveness e readiness;
- configuração externa do banco;
- configuração externa da integração de elegibilidade;
- porta HTTP configurável;
- testes de integração isolados com Testcontainers;
- quality gate de cobertura com JaCoCo.

Essas características permitem que a mesma imagem da aplicação seja configurada para diferentes ambientes sem necessidade de alteração no código.

## Observações sobre o serviço de elegibilidade

O desafio original utiliza o serviço:

```text
https://user-info.herokuapp.com
```

Esse serviço não está mais disponível.

A aplicação mantém essa URL como valor padrão por compatibilidade com a especificação original, mas permite substituí-la através de:

```text
ELEGIBILIDADE_BASE_URL
```

Para testes automatizados, a integração é simulada com WireMock.

Para testes manuais de registro de voto, configure essa variável apontando para um mock ou serviço compatível.

## Build

Para realizar uma validação completa do projeto:

### Windows / PowerShell

```powershell
.\mvnw.cmd clean verify
```

### Linux / macOS

```bash
./mvnw clean verify
```

Esse comando:

1. compila a aplicação;
2. executa os testes unitários;
3. inicia a infraestrutura necessária aos testes de integração através do Testcontainers;
4. executa os testes de integração;
5. gera o relatório JaCoCo;
6. valida o quality gate de cobertura.

Um build válido deve terminar com:

```text
All coverage checks have been met.

BUILD SUCCESS
```