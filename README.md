# Orçamento IA por voz

API de orçamento pessoal em Java 17, Spring Boot e Spring AI. Usuários podem criar uma conta, obter um JWT e enviar comandos por texto ou áudio. O assistente pode chamar ferramentas financeiras reais; cada usuário só consulta e modifica as próprias transações.

## Evolução desta rodada

- **JWT e usuários:** registro/login com senhas BCrypt e tokens Bearer assinados; as transações são isoladas por usuário.
- **PostgreSQL:** banco padrão da aplicação; H2 fica apenas nos testes.
- **Fluxo integrado de ponta a ponta:** testes exercitam registro, autenticação, autorização, criação de transação, resumo e isolamento entre usuários usando o pipeline HTTP do Spring.
- **Docker:** Dockerfile multi-stage, Compose com PostgreSQL e blueprint opcional de deploy Render.
- **Assistente mais natural e seguro:** prompt em português do Brasil, pede dados ausentes e evita afirmar que salvou algo sem retorno positivo da ferramenta.

## Tecnologias

Java 17 · Spring Boot 3.4 · Spring AI 1.0 · OpenAI · Spring Security OAuth2 Resource Server · JWT HMAC SHA-256 · BCrypt · Spring Data JPA · PostgreSQL · H2 para testes · Maven · Docker.

## Executar com Docker Compose

Pré-requisitos: Docker e Docker Compose. O modelo OpenAI é necessário para comandos de IA e recursos de áudio; as rotas convencionais de transações e autenticação funcionam sem ele.

```bash
cp .env.example .env
# Edite .env e informe OPENAI_API_KEY se for usar IA/áudio
docker compose up --build
```

A API fica em `http://localhost:8080`, o PostgreSQL persiste no volume `postgres_data`. O Compose usa credenciais exclusivamente locais de desenvolvimento; não reutilize esses valores em produção.

## Executar com Maven

É necessário ter PostgreSQL disponível. Defina as variáveis de ambiente:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/orcamento
export DB_USERNAME=orcamento
export DB_PASSWORD=orcamento
export JWT_SECRET='substitua-por-uma-chave-secreta-com-pelo-menos-32-bytes'
export OPENAI_API_KEY='sua-chave-openai' # opcional para executar API sem IA
mvn spring-boot:run
```

No PowerShell:

```powershell
$env:JDBC_DATABASE_URL = "jdbc:postgresql://localhost:5432/orcamento"
$env:DB_USERNAME = "orcamento"
$env:DB_PASSWORD = "orcamento"
$env:JWT_SECRET = "substitua-por-uma-chave-secreta-com-pelo-menos-32-bytes"
$env:OPENAI_API_KEY = "sua-chave-openai"
mvn spring-boot:run
```

O secret JWT precisa ter pelo menos 32 bytes. Configure um valor aleatório diferente para cada ambiente e mantenha-o fora do código. Para escolher o modelo do chat, use `OPENAI_MODEL` (padrão `gpt-4o-mini`).

## Autenticação e exemplos

### Registrar usuário

A senha deve ter entre 12 e 72 caracteres. A resposta contém `accessToken` e validade em segundos.

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"uma-senha-segura-123"}'
```

No login, use o mesmo formato e guarde o `accessToken` da resposta:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"uma-senha-segura-123"}'
export TOKEN='cole-o-accessToken-aqui'
```

Envie o token nas rotas protegidas usando `Authorization: Bearer <token>`.

### Criar transação e consultar resumo

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"Mercado","amount":42.50,"type":"EXPENSE","category":"Alimentação"}'

curl 'http://localhost:8080/api/transactions?type=EXPENSE' \
  -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/transactions/summary \
  -H "Authorization: Bearer $TOKEN"
```

### Pedir ao assistente para registrar ou consultar

```bash
curl -X POST http://localhost:8080/api/assistant/command \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Registre uma despesa de 18 reais com café na categoria alimentação"}'
```

O modelo escolhe se precisa chamar as ferramentas. Se faltar uma informação essencial, deve pedir esclarecimento sem gravar uma transação incompleta.

### Comando e resposta por áudio

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

A transcrição usa `whisper-1` e a síntese usa `tts-1` da API OpenAI. Limite de upload configurado: 20 MB.

## Endpoints

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/register` | Pública | Cria conta e retorna JWT |
| `POST` | `/api/auth/login` | Pública | Autentica e retorna JWT |
| `POST` | `/api/transactions` | Bearer JWT | Cria transação |
| `GET` | `/api/transactions?type=...` | Bearer JWT | Lista próprias transações; filtro opcional |
| `GET` | `/api/transactions/{id}` | Bearer JWT | Busca uma transação própria |
| `DELETE` | `/api/transactions/{id}` | Bearer JWT | Exclui uma transação própria |
| `GET` | `/api/transactions/summary` | Bearer JWT | Resumo financeiro próprio |
| `POST` | `/api/assistant/command` | Bearer JWT | Comando de texto e tool calling |
| `POST` | `/api/assistant/audio` | Bearer JWT | Transcrição e processamento do comando |
| `POST` | `/api/assistant/speak` | Bearer JWT | Gera resposta MP3 |
| `GET` | `/actuator/health` | Pública | Health check para deploy |

## Testes

```bash
mvn test
```

Os testes de integração executam o fluxo HTTP completo com H2 isolado e chave OpenAI fictícia: cadastro, login, token inválido, validação, transação e separação dos dados entre usuários. Não é feita chamada externa à OpenAI nos testes.

## Deploy

### Render

O arquivo `render.yaml` declara o serviço web Docker e um PostgreSQL. No Render, revise o plano disponível para o banco e configure `OPENAI_API_KEY` no painel (marcada como secret/sync false). O `JWT_SECRET` é gerado pelo blueprint. O health check usa `/actuator/health`.

### Qualquer plataforma Docker

Construa e execute a imagem com as variáveis `JDBC_DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e, para IA/áudio, `OPENAI_API_KEY`. Use uma instância PostgreSQL gerenciada e TLS em produção. Não exponha a porta do banco publicamente.

## Limites e próximos passos

JWT HS256 usa uma chave compartilhada; para sistemas com múltiplos serviços, considere um provedor OAuth2/OIDC e rotação/gestão de chaves. A API ainda não implementa recuperação de senha, verificação de email, rate limiting nem histórico de conversas. `ddl-auto=update` serve para o exercício; uma evolução de produção deve adicionar migrações versionadas com Flyway ou Liquibase.
