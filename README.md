# Voice Budget AI

A bilingual personal budgeting API built with Java 17, Spring Boot, and Spring AI. The project uses the reverse-domain Java namespace `com.lincolnbuk.voicebudgetai`.

## Português

API de orçamento pessoal com cadastro e autenticação JWT, gerenciamento de receitas e despesas e assistente financeiro por texto e voz.

### Funcionalidades

- Cadastro e login com JWT; senhas protegidas com BCrypt.
- Isolamento de transações por usuário autenticado.
- Criação, listagem, filtro por tipo, consulta e exclusão de transações.
- Resumo financeiro com receitas, despesas, saldo e quantidade de transações.
- Assistente Spring AI com chamadas de ferramentas para consultar e registrar transações.
- Transcrição de áudio e geração de fala em MP3 pela API de áudio da OpenAI.
- PostgreSQL para execução normal e H2 para testes.
- Docker Compose, health check e configuração opcional de deploy no Render.

### Tecnologias

Java 17 · Spring Boot · Spring AI · Spring Security · JWT · BCrypt · Spring Data JPA · PostgreSQL · H2 · Maven · Docker · OpenAI API.

### Executar com Docker

Requisitos: Docker Desktop com Docker Compose. Clone o repositório, configure `OPENAI_API_KEY` no `.env` se quiser testar os recursos de IA/áudio e inicie os serviços:

```bash
git clone https://github.com/lincolnbuk/orcamento-ia-voz.git
cd orcamento-ia-voz
cp .env.example .env
# Edite .env para configurar OPENAI_API_KEY, se necessário
docker compose up --build
```

A API fica disponível em `http://localhost:8080`. O Docker Compose inicia o PostgreSQL automaticamente. As credenciais locais são apenas para desenvolvimento; configure segredos fortes em ambientes compartilhados ou de produção.

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

O segredo JWT deve ter ao menos 32 bytes. Nunca o publique no GitHub. O modelo padrão é `gpt-4o-mini`; ele pode ser alterado por meio de `OPENAI_MODEL`.

### Autenticação e exemplos

Registro e login são públicos. As demais rotas `/api/**` exigem `Authorization: Bearer <token>`.

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

### Testes e deploy

Execute `mvn test`. Os testes de integração usam H2 e não fazem chamadas externas à OpenAI. Para deploy no Render, conecte o repositório usando o Blueprint de `render.yaml`, revise planos e custos e configure `OPENAI_API_KEY` como segredo.

---

## English

A personal budgeting API built with Java 17, Spring Boot, and Spring AI. Users can register, authenticate with JWT, manage their own income and expenses, and use a text-and-voice financial assistant.

### Features

- JWT registration and login; passwords are hashed with BCrypt.
- Transaction isolation by authenticated user.
- Create, list, filter, retrieve, and delete transactions.
- Financial summary with income, expenses, balance, and transaction count.
- Spring AI assistant with tool calling for transaction queries and creation.
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

The API is available at `http://localhost:8080`. Compose starts PostgreSQL automatically. Local database credentials are for development only; use strong secrets in shared or production environments.

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

### Authentication and example

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

### Tests and deployment

Run `mvn test`. Integration tests use H2 and do not call OpenAI. For Render deployment, connect the repository using the `render.yaml` Blueprint, review plans and costs, and set `OPENAI_API_KEY` as a secret.
