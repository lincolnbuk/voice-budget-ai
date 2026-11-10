# Orçamento IA por voz

API de orçamento pessoal desenvolvida com Java 17, Spring Boot e Spring AI. A aplicação permite que cada usuário crie uma conta, faça login com JWT, registre transações financeiras e use um assistente de IA para interpretar comandos por texto ou áudio.

O objetivo principal do projeto é demonstrar como conectar uma API real com um modelo de linguagem, respeitando responsabilidades do código, regras de negócio e segurança. Em vez de deixar tudo no controller, a aplicação separa autenticação, domínio, serviços e integração com IA.

## O que o projeto faz

- Cria usuários e autentica com JWT
- Permite registrar receitas e despesas
- Calcula resumo financeiro por usuário
- Permite consultar transações por tipo
- Recebe comandos em texto e interpreta intenção do usuário
- Aceita áudio para transcrição e processa comando
- Gera resposta em áudio a partir de texto
- Usa tool calling para executar ações reais da aplicação
- Mantém cada usuário isolado das transações de outros usuários

## Melhorias implementadas nesta versão

- Autenticação JWT com Spring Security e BCrypt
- Isolamento por usuário: cada pessoa vê apenas suas próprias transações
- PostgreSQL como banco principal em ambiente real
- Docker e Docker Compose para subir a aplicação com banco e ambiente local
- Testes E2E cobrindo cadastro, login, token, validação e separação por usuário
- Prompt do assistente mais natural e mais seguro
- Endpoints REST mais completos para gestão de transações e integração com IA

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.4
- Spring AI 1.0
- OpenAI API
- Spring Security
- OAuth2 Resource Server
- JWT (HMAC SHA-256)
- BCrypt
- Spring Data JPA
- PostgreSQL
- H2 para testes
- Maven
- Docker / Docker Compose

## Estrutura do projeto

```text
src/main/java/com/example/orcamentoia/
├── controller/
│   ├── AuthController.java
│   ├── AssistantController.java
│   ├── TransactionController.java
│   └── ApiExceptionHandler.java
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── CreateTransactionRequest.java
│   ├── AssistantCommandRequest.java
│   ├── AssistantResponse.java
│   └── FinancialSummary.java
├── model/
│   ├── AppUser.java
│   ├── Transaction.java
│   └── TransactionType.java
├── repository/
│   ├── AppUserRepository.java
│   └── TransactionRepository.java
├── security/
│   ├── SecurityConfig.java
│   ├── AppUserDetailsService.java
│   └── ApiKeyFilter.java
├── service/
│   ├── AuthService.java
│   ├── BudgetTools.java
│   ├── OpenAiAudioService.java
│   ├── TransactionService.java
│   ├── VoiceAssistantService.java
│   ├── TransactionNotFoundException.java
│   └── UsernameAlreadyExistsException.java
├── BudgetAiApplication.java
└──
```

## Pré-requisitos

- JDK 17+
- Maven 3.9+
- Docker e Docker Compose (opcional, para execução local com banco e app)
- PostgreSQL local ou via Docker
- Chave OpenAI para usar IA e áudio (`OPENAI_API_KEY`)

## Executar com Docker Compose

A maneira mais simples é usar o ambiente pronto com banco e aplicação em um único comando.

```bash
cp .env.example .env
# edite o valor de OPENAI_API_KEY no arquivo .env

docker compose up --build
```

A aplicação fica em:
- http://localhost:8080

O PostgreSQL fica em:
- host: localhost
- porta: 5432
- banco: orcamento
- usuário: orcamento
- senha: orcamento

## Executar com Maven

Se quiser rodar localmente sem Docker, primeiro configure as variáveis de ambiente.

Linux/macOS:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/orcamento
export DB_USERNAME=orcamento
export DB_PASSWORD=orcamento
export JWT_SECRET='substitua-por-uma-chave-secreta-com-pelo-menos-32-bytes'
export OPENAI_API_KEY='sua-chave-openai'
mvn spring-boot:run
```

Windows PowerShell:

```powershell
$env:JDBC_DATABASE_URL = "jdbc:postgresql://localhost:5432/orcamento"
$env:DB_USERNAME = "orcamento"
$env:DB_PASSWORD = "orcamento"
$env:JWT_SECRET = "substitua-por-uma-chave-secreta-com-pelo-menos-32-bytes"
$env:OPENAI_API_KEY = "sua-chave-openai"
mvn spring-boot:run
```

Observações importantes:
- `JWT_SECRET` precisa ter pelo menos 32 bytes
- `OPENAI_API_KEY` é obrigatório para uso de IA e áudio
- `OPENAI_MODEL` pode ser configurado se quiser trocar o modelo padrão (`gpt-4o-mini`)

## Fluxo principal da API

1. Usuário cria conta ou faz login
2. Recebe um JWT Bearer
3. Usa esse token nas rotas protegidas
4. Envia comando por texto ou áudio
5. O assistente interpreta a intenção
6. O Spring AI chama as ferramentas da aplicação
7. A aplicação valida e persiste a transação
8. A API responde com texto, resumo ou geração de áudio

## Endpoints

Todos os endpoints abaixo, exceto registro e login, exigem token JWT no cabeçalho:

```http
Authorization: Bearer <token>
```

### Autenticação

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Cria usuário e retorna JWT |
| POST | `/api/auth/login` | Faz login e retorna JWT |

### Transações

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/transactions` | Cria uma transação |
| GET | `/api/transactions` | Lista transações do usuário |
| GET | `/api/transactions?type=EXPENSE` | Lista por tipo |
| GET | `/api/transactions/{id}` | Busca uma transação |
| DELETE | `/api/transactions/{id}` | Remove uma transação |
| GET | `/api/transactions/summary` | Resumo financeiro do usuário |

### Assistente IA

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/assistant/command` | Recebe texto e responde com IA |
| POST | `/api/assistant/audio` | Recebe áudio, transcreve e processa comando |
| POST | `/api/assistant/speak` | Gera áudio a partir de texto |

## Exemplos de uso

### 1) Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"uma-senha-segura-123"}'
```

Resposta esperada:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 2) Fazer login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"uma-senha-segura-123"}'
```

### 3) Criar transação

```bash
export TOKEN='cole_o_token_aqui'

curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "Mercado",
    "amount": 42.50,
    "type": "EXPENSE",
    "category": "Alimentação"
  }'
```

### 4) Consultar resumo financeiro

```bash
curl http://localhost:8080/api/transactions/summary \
  -H "Authorization: Bearer $TOKEN"
```

### 5) Enviar comando textual para o assistente

```bash
curl -X POST http://localhost:8080/api/assistant/command \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"registre uma despesa de 18 reais com café na categoria alimentação"}'
```

### 6) Enviar áudio

```bash
curl -X POST http://localhost:8080/api/assistant/audio \
  -H "Authorization: Bearer $TOKEN" \
  -F 'audio=@comando.wav'
```

### 7) Gerar resposta em voz

```bash
curl -X POST http://localhost:8080/api/assistant/speak \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Sua despesa foi registrada com sucesso."}' \
  --output resposta.mp3
```

## Testes

Execute os testes de integração com Maven:

```bash
mvn test
```

Os testes cobrem:
- cadastro de usuário
- login com JWT
- rejeição de token ausente ou inválido
- criação de transação
- resumo financeiro
- validação de valores inválidos
- isolamento entre usuários

## Deploy

### Render

O repositório inclui o arquivo `render.yaml` para um deploy simples em Render usando Docker. O arquivo define:
- serviço web da aplicação
- banco PostgreSQL
- health check em `/actuator/health`

No painel do Render, configure a variável `OPENAI_API_KEY` como secret e revise os valores do banco. O `JWT_SECRET` também deve ser configurado como variável de ambiente segura.

### Qualquer plataforma Docker

O projeto também contém um `Dockerfile` e um `docker-compose.yml` prontos para subir a aplicação e o banco.

```bash
docker compose up --build
```

## Observações importantes

- A IA não substitui as regras de negócio: a aplicação continua validando valores, categorias e tipo de transação antes de persisti-los
- Cada usuário tem dados isolados por autenticação
- O H2 é usado apenas para testes; o PostgreSQL é a base para execução real
- Em produção, use segredos reais, rotação de chaves e ambiente seguro

## O que aprendi durante o desafio

- A parte mais importante de um projeto com IA não é somente “chamar o modelo”, mas conectar a IA com regras realmente úteis da aplicação
- Tool calling é a ponte entre a inteligência artificial e ações reais do sistema
- Separar autenticação, domínio e serviços deixa o código mais fácil de evoluir
- Validação e segurança são fundamentais em qualquer integração que manipule dados financeiros
- Fluxos de áudio e IA exigem atenção adicional para erro, configuração e UX

## Próximos passos possíveis

- dashboard web para resumir despesas por categoria
- exportação de transações em CSV
- auditoria e logs de ações do assistente
- suporte a múltiplos perfis e permissões
- relatórios mensais e filtros por data
- melhor UX com IA conversacional mais contextualizada

## Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.
