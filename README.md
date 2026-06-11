# Users Service — Facoffee

**Disciplina:** Técnicas Avançadas em Desenvolvimento de Software
**Estudantes:** Ana Beatriz Leite e Ana Clara Ottoni

---

Microsserviço responsável pelo gerenciamento de usuários da plataforma **Facoffee**. Desenvolvido com **Spring Boot 4**, integra autenticação via **Keycloak**, persistência com **PostgreSQL** e comunicação assíncrona com **RabbitMQ**.

---

## Pré-requisitos

- Java 21+
- Maven (ou use o wrapper `./mvnw`)
- Docker e Docker Compose v2+
- Repositório de infraestrutura `facoffee` clonado e rodando

---

## Como rodar

### 1. Suba a infraestrutura compartilhada

Este serviço depende da infraestrutura centralizada do projeto Facoffee. Antes de tudo, clone e suba o repositório de infraestrutura:

```bash
git clone <url-do-repositorio-facoffee>
cd facoffee
docker compose up -d
```

Isso disponibiliza os serviços de plataforma:

| Serviço         | Endereço               | Credenciais                                     |
|-----------------|------------------------|-------------------------------------------------|
| API Gateway     | http://localhost:8000  | —                                               |
| Keycloak        | http://localhost:8080  | user: `facoffee` / pass: `facoffee`             |
| RabbitMQ Painel | http://localhost:15672 | Definido em `rabbitmq/definitions.json`         |
| RabbitMQ AMQP   | `localhost:5672`       | —                                               |
| Mailpit         | http://localhost:8025  | —                                               |

> O realm `facoffee` já vem pré-configurado no Keycloak com o usuário inicial `facoffee@facom.ufms.br` (role `MANAGER`) e os clients `facoffee-public` e `facoffee-private` (secret: `facoffee-private-secret`).

### 2. Suba o banco de dados do serviço

O `docker-compose.yml` deste repositório sobe o PostgreSQL e o pgAdmin exclusivos do serviço de usuários:

```bash
docker compose up -d
```

| Serviço    | Endereço              | Credenciais                                      |
|------------|-----------------------|--------------------------------------------------|
| PostgreSQL | `localhost:5433`      | user: `facoffee_admin` / pass: `ufms_pass`       |
| pgAdmin    | http://localhost:5050 | email: `admin@facoffee.com` / pass: `admin_pass` |

### 3. Configure o `application.properties` (se necessário)

As configurações principais ficam em `src/main/resources/application.properties`. Os valores padrão já apontam para os serviços locais:

```properties
server.port=3001

# Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/facoffee
realm=facoffee

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/facoffee_users_db
spring.datasource.username=facoffee_admin
spring.datasource.password=ufms_pass

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=facoffee
spring.rabbitmq.password=facoffee
```

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:3001` e acessível via Gateway em `http://localhost:8000/api/users`.

---

## Autenticação

O serviço valida tokens JWT emitidos pelo Keycloak. Para obter um token de teste use o client público com o usuário MANAGER:

```bash
curl -X POST "http://localhost:8080/realms/facoffee/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=facoffee-public" \
  -d "username=facoffee@facom.ufms.br" \
  -d "password=facoffee"
```

Use o `access_token` retornado no cabeçalho das requisições:

```
Authorization: Bearer <access_token>
```

As roles de domínio (`MANAGER`, `PARTICIPANT`) são emitidas diretamente na claim `roles` do token.

---

## Endpoints

Todos os endpoints estão sob o prefixo `/users`.

| Método  | Rota                 | Descrição                                         | Autenticação       |
|---------|----------------------|---------------------------------------------------|--------------------|
| `POST`  | `/users`             | Cria um novo usuário                              | Pública            |
| `GET`   | `/users`             | Lista usuários com paginação e filtros opcionais  | Somente MANAGER    |
| `GET`   | `/users/{userId}`    | Busca um usuário pelo ID                          | Próprio ou MANAGER |
| `PATCH` | `/users/{id}`        | Atualiza o nome do usuário                        | Próprio ou MANAGER |
| `DELETE`| `/users/{id}`        | Desativa o usuário (soft delete)                  | Próprio ou MANAGER |
| `PUT`   | `/users/{id}/roles`  | Substitui os papéis do usuário                    | Somente MANAGER    |

### Detalhes dos endpoints de consulta

**`GET /users`** — Lista paginada de usuários. Parâmetros de query opcionais:

| Parâmetro | Tipo     | Padrão | Descrição                          |
|-----------|----------|--------|------------------------------------|
| `status`  | `string` | —      | Filtra por status (`ACTIVE`, `INACTIVE`) |
| `role`    | `string` | —      | Filtra por papel (`MANAGER`, `PARTICIPANT`) |
| `page`    | `integer`| `0`    | Número da página                   |
| `size`    | `integer`| `20`   | Quantidade de itens por página     |

Exemplo de requisição:
```bash
curl -X GET "http://localhost:8000/api/users?status=ACTIVE&role=PARTICIPANT&page=0&size=10" \
  -H "Authorization: Bearer <access_token>"
```

**`GET /users/{userId}`** — Retorna os dados de um usuário específico. Um MANAGER pode consultar qualquer usuário; um PARTICIPANT só pode consultar a si mesmo.

```bash
curl -X GET "http://localhost:8000/api/users/<userId>" \
  -H "Authorization: Bearer <access_token>"
```

---

## Arquitetura

```
src/main/java/facoffe/users/
├── config/
│   ├── CustomAccessDeniedHandler.java    # Resposta HTTP 403 customizada
│   ├── CustomAuthenticationEntryPoint.java # Resposta HTTP 401 customizada
│   ├── GlobalExceptionHandler.java       # Tratamento centralizado de erros
│   ├── JwtAuthorityConverter.java        # Mapeia roles do JWT para authorities do Spring
│   ├── KeycloakConfig.java               # Configura o cliente admin do Keycloak
│   ├── RabbitMQConfig.java               # Declara a fila users.deactivated
│   └── SecurityConfig.java              # Configura o OAuth2 Resource Server
├── controller/
│   └── UserController.java              # Camada HTTP (REST)
├── DTO/
│   ├── CreateUserRequestDTO.java
│   ├── UpdateUserRequestDTO.java
│   ├── DeleteUserRequestDTO.java
│   ├── ReplaceUserRolesRequestDTO.java
│   ├── UserResponseDTO.java
│   ├── UserListResponseDTO.java         # Resposta paginada de listagem
│   ├── PageDTO.java                     # Metadados de paginação
│   └── ErrorResponseDTO.java
├── event/
│   ├── EventEnvelope.java               # Envelope genérico para eventos publicados
│   └── UserDeactivatedPayload.java      # Payload do evento de desativação
├── exception/
│   ├── CustomAccessDeniedException.java
│   ├── EmailAlreadyExistsException.java
│   └── UserNotFoundException.java
├── model/
│   ├── User.java                        # Entidade JPA (tabela users)
│   └── Role.java                        # Entidade JPA (tabela roles)
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── service/
│   └── UserService.java                 # Lógica de negócio
└── UsersApplication.java                # Ponto de entrada
```

### Fluxo geral

1. **Criação de usuário:** o `UserController` recebe a requisição e delega ao `UserService`, que cria o registro no PostgreSQL e replica o usuário no Keycloak (com papel atribuído), disparando o e-mail de verificação/definição de senha.

2. **Listagem e busca:** `GET /users` retorna uma lista paginada com filtros opcionais de `status` e `role`, acessível apenas por MANAGER. `GET /users/{userId}` permite que o próprio usuário ou um MANAGER consulte os dados de um usuário específico.

3. **Autenticação e autorização:** a aplicação funciona como um **OAuth2 Resource Server**. O `JwtAuthorityConverter` extrai as roles do token JWT emitido pelo Keycloak e as converte para `GrantedAuthority` do Spring Security. Operações sensíveis verificam se o usuário autenticado é o próprio dono do recurso ou possui a role `MANAGER`.

4. **Desativação (soft delete):** ao "deletar" um usuário, o status é alterado para `INACTIVE` no banco e a conta é desabilitada no Keycloak. Em seguida, um evento `UserDeactivated` é publicado na fila RabbitMQ `users.deactivated` para que outros serviços possam reagir.

5. **Sincronização de dados:** o `id` do usuário no banco local é o mesmo gerado pelo Keycloak, garantindo consistência entre os sistemas.

### Integrações externas

| Sistema    | Papel                                                          |
|------------|----------------------------------------------------------------|
| Keycloak   | Gerenciamento de identidade, emissão e validação de tokens JWT |
| PostgreSQL | Persistência dos dados de usuários e papéis                    |
| RabbitMQ   | Publicação de eventos assíncronos (ex: desativação de usuário) |
