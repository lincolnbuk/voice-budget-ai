# Voice Budget AI

## Português

API de orçamento pessoal criada com Java 17, Spring Boot e Spring AI. Cada pessoa pode criar uma conta, autenticar-se com JWT, gerenciar suas próprias receitas e despesas e conversar com um assistente que usa ferramentas da aplicação para realizar consultas e registrar transações.

### Funcionalidades

- Cadastro e login com JWT; senhas armazenadas com BCrypt.
- Isolamento de transações por usuário autenticado.
- Criação, listagem, filtro por tipo, consulta por ID e exclusão de transações.
- Resumo financeiro com receitas, despesas, saldo e quantidade de transações.
- Assistente Spring AI com tool calling para consultas e ações reais.
- Transcrição de áudio e geração de MP3 pela API de áudio da OpenAI.
- PostgreSQL para execução normal e H2 nos testes.
- Docker Compose, health check e configuração opcional de deploy no Render.

### Tecnologias

Java 17 · Spring Boot · Spring AI · Spring Security · JWT · BCrypt · Spring Data JPA · PostgreSQL · H2 · Maven · Docker · OpenAI API.

### Executar com Docker

Requisitos: Docker Desktop com Docker Compose. Clone o repositório, configure `OPENAI_API_KEY` no `.env` se for testar IA/áudio e inicie os serviços:

```bash
git clone https://github.com/lincolnbuk/orcamento-ia-voz.git
cd orcamento-ia-voz
cp .env.example .env
# Edite .env para configurar OPENAI_API_KEY, se necessário
docker compose up --build
```

A API fica disponível em `http://localhost:8080`. O Compose sobe o PostgreSQL automaticamente; não é necessário instalar o banco na máquina. O banco local usa credenciais didáticas, que não devem ser reutilizadas em produção.

### Executar com Maven

É necessário ter PostgreSQL disponível. Configure as variáveis de ambiente:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/orcamento
export DB_USERNAME=orcamento
export DB_PASSWORD=orcamento
export JWT_SECRET='use-um-segredo-aleatorio-com-pelo-menos-32-bytes'
export OPENAI_API_KEY='sua-chave-openai' # necessário para IA e áudio
mvn spring-boot:run
```

O segredo JWT deve ter ao menos 32 bytes. Nunca o publique no GitHub. O modelo padrão é `gpt-4o-mini`; ele pode ser trocado por meio de `OPENAI_MODEL`.

### Autenticação e exemplos

Registro e login são públicos. As demais rotas `/api/**` precisam de `Authorization: Bearer <token>`.

Registrar uma conta (senha de 12 a 72 caracteres):

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"uma-senha-segura-123"}'
```

A resposta contém `accessToken`. Use-o nos exemplos seguintes:

```bash
export TOKEN='cole-o-accessToken-aqui'
```

Criar uma despesa e consultar o resumo:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"Mercado","amount":42.50,"type":"EXPENSE","category":"Alimentação"}'

curl http://localhost:8080/api/transactions/summary \
  -H "Authorization: Bearer $TOKEN"
```

Pedir ao assistente que registre uma despesa:

```bash
curl -X POST http://localhost:8080/api/assistant/command \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Registre uma despesa de 18 reais com café na categoria alimentação"}'
```

Enviar áudio e gerar fala em MP3:

```bash
curl -X POST http://localhost:8080/api/assistant/audio \
  -H "Authorization: Bearer $TOKEN" \
  -F 'audio=@comando.wav'

curl -X POST http://localhost:8080/api/assistant/speak \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Sua despesa foi registrada."}' \
  --output resposta.mp3
```

Os recursos de IA, transcrição e síntese precisam de uma chave OpenAI válida. O upload de áudio tem limite de 20 MB.

### Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Cadastra usuário e retorna JWT |
| `POST` | `/api/auth/login` | Público | Autentica e retorna JWT |
| `POST` | `/api/transactions` | JWT | Cria uma transação |
| `GET` | `/api/transactions?type=EXPENSE` | JWT | Lista transações próprias; filtro opcional `INCOME`/`EXPENSE` |
| `GET` | `/api/transactions/{id}` | JWT | Consulta uma transação própria |
| `DELETE` | `/api/transactions/{id}` | JWT | Exclui uma transação própria |
| `GET` | `/api/transactions/summary` | JWT | Consulta o resumo financeiro próprio |
| `POST` | `/api/assistant/command` | JWT | Envia comando de texto ao assistente |
| `POST` | `/api/assistant/audio` | JWT | Transcreve áudio e processa o comando |
| `POST` | `/api/assistant/speak` | JWT | Gera resposta de voz em MP3 |
| `GET` | `/actuator/health` | Público | Health check |

### Testes

```bash
mvn test
```

Os testes de integração usam H2 e não fazem chamadas externas à OpenAI. Cobrem cadastro, autenticação JWT, validação, criação de transações e isolamento entre usuários.

### Deploy no Render

O repositório inclui `render.yaml` e `Dockerfile`. Conecte o repositório ao Render usando o Blueprint, revise os planos e custos do serviço e do PostgreSQL, e configure `OPENAI_API_KEY` como secret. O Blueprint gera o `JWT_SECRET`; mantenha segredos fora do código. O health check é `/actuator/health`.

### Aprendizados e limites

A IA interpreta pedidos, mas as regras e a persistência continuam sob controle da aplicação. As ferramentas respeitam o usuário autenticado e as transações são isoladas por conta. Para produção, considere migrações Flyway/Liquibase, rate limiting, gestão/rotação de segredos, recuperação de senha e um provedor OIDC quando houver mais serviços.

---

## English

A personal budgeting API built with Java 17, Spring Boot, and Spring AI. Users can register, authenticate with JWT, manage their own income and expenses, and interact with an assistant that uses application tools to query and create financial records.

### Features

- JWT registration and login; passwords are hashed with BCrypt.
- Transaction isolation by authenticated user.
- Create, list, filter by type, retrieve, and delete transactions.
- Financial summary with income, expenses, balance, and transaction count.
- Spring AI assistant with tool calling for real application actions and queries.
- Audio transcription and MP3 speech generation through the OpenAI Audio API.
- PostgreSQL for normal execution and H2 for tests.
- Docker Compose, health check, and optional Render deployment configuration.

### Tech stack

Java 17 · Spring Boot · Spring AI · Spring Security · JWT · BCrypt · Spring Data JPA · PostgreSQL · H2 · Maven · Docker · OpenAI API.

### Run with Docker

Requirements: Docker Desktop with Docker Compose. Clone the repository, set `OPENAI_API_KEY` in `.env` if you want to test AI/audio features, and start the services:

```bash
git clone https://github.com/lincolnbuk/orcamento-ia-voz.git
cd orcamento-ia-voz
cp .env.example .env
# Edit .env to set OPENAI_API_KEY if needed
docker compose up --build
```

The API is available at `http://localhost:8080`. Compose starts PostgreSQL automatically, so there is no need to install the database on your machine. The local database credentials are for development only and must not be reused in production.

### Run with Maven

A PostgreSQL instance must be available. Configure the environment variables:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/orcamento
export DB_USERNAME=orcamento
export DB_PASSWORD=orcamento
export JWT_SECRET='use-a-random-secret-at-least-32-bytes-long'
export OPENAI_API_KEY='your-openai-key' # required for AI and audio
mvn spring-boot:run
```

The JWT secret must be at least 32 bytes. Never commit it to GitHub. The default model is `gpt-4o-mini`; set `OPENAI_MODEL` to use another model.

### Authentication and examples

Registration and login are public. All other `/api/**` routes require `Authorization: Bearer <token>`.

Register an account (password length: 12–72 characters):

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alex","password":"a-secure-password-123"}'
```

The response contains `accessToken`. Use it in subsequent requests:

```bash
export TOKEN='paste-accessToken-here'
```

Create an expense and retrieve the summary:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"Groceries","amount":42.50,"type":"EXPENSE","category":"Food"}'

curl http://localhost:8080/api/transactions/summary \
  -H "Authorization: Bearer $TOKEN"
```

Ask the assistant to record an expense:

```bash
curl -X POST http://localhost:8080/api/assistant/command \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Record an 18-real expense for coffee in the food category"}'
```

Upload audio and generate speech as MP3:

```bash
curl -X POST http://localhost:8080/api/assistant/audio \
  -H "Authorization: Bearer $TOKEN" \
  -F 'audio=@command.wav'

curl -X POST http://localhost:8080/api/assistant/speak \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Your expense was recorded."}' \
  --output response.mp3
```

AI, transcription, and speech features require a valid OpenAI API key. Audio uploads are limited to 20 MB.

### Endpoints

| Method | Route | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register and receive a JWT |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `POST` | `/api/transactions` | JWT | Create a transaction |
| `GET` | `/api/transactions?type=EXPENSE` | JWT | List own transactions; optional `INCOME`/`EXPENSE` filter |
| `GET` | `/api/transactions/{id}` | JWT | Retrieve an owned transaction |
| `DELETE` | `/api/transactions/{id}` | JWT | Delete an owned transaction |
| `GET` | `/api/transactions/summary` | JWT | Retrieve the user's financial summary |
| `POST` | `/api/assistant/command` | JWT | Send a text command to the assistant |
| `POST` | `/api/assistant/audio` | JWT | Transcribe audio and process the command |
| `POST` | `/api/assistant/speak` | JWT | Generate an MP3 voice response |
| `GET` | `/actuator/health` | Public | Health check |

### Tests

```bash
mvn test
```

Integration tests use H2 and do not call OpenAI. They cover registration, JWT authentication, validation, transaction creation, and per-user data isolation.

### Deploy to Render

The repository includes `render.yaml` and a `Dockerfile`. Connect the repository to Render using the Blueprint, review service/database plans and costs, and set `OPENAI_API_KEY` as a secret. The Blueprint generates `JWT_SECRET`; keep all secrets out of source control. The health check endpoint is `/actuator/health`.

### Learning outcomes and limitations

AI interprets requests, while business rules and persistence remain controlled by the application. Tools operate as the authenticated user, and transactions are isolated by account. For production, consider Flyway/Liquibase migrations, rate limiting, secret management and rotation, password recovery, and an OIDC provider when adding more services.
