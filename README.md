# Voting API

API REST para gerenciamento de pautas e sessões de votação
de uma cooperativa.

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit 5

## Requisitos

- Java 21
- Maven
- PostgreSQL

## Decisões de domínio

### Sessão de votação

Cada pauta pode possuir apenas uma sessão de votação.

Quando a duração não é informada na abertura, a sessão
permanece aberta por 1 minuto.

Os timestamps da aplicação são tratados em UTC.

### Registro de votos

Os votos aceitos são `SIM` e `NAO`.

Cada voto pertence à sessão de votação de uma pauta.

Cada associado pode registrar apenas um voto por pauta.

Como cada pauta possui apenas uma sessão de votação, a
unicidade é garantida no banco pela combinação:

`(sessao_id, associado_id)`.

A sessão utiliza o intervalo temporal `[inicio, fim)`.
Portanto, no instante exato definido por `fim`, a sessão
já é considerada encerrada.

### Resultado da votação

O resultado final somente pode ser consultado após o
encerramento da sessão.

A contabilização dos votos é realizada diretamente no banco
de dados, evitando carregar todos os votos em memória.

Critérios:

- SIM > NAO: APROVADA
- NAO > SIM: REJEITADA
- SIM = NAO: EMPATE

Uma votação encerrada sem votos também é considerada EMPATE,
pois o requisito não define um estado específico para ausência
de votos.