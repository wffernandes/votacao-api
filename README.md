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