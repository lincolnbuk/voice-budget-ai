# Orçamento IA por voz

API didática de orçamento pessoal construída com Java, Spring Boot e Spring AI. Ela recebe comandos em texto ou áudio, usa um modelo OpenAI para conversar e chamar ferramentas da aplicação, salva transações no H2 e permite consultar o saldo.

> A transcrição e síntese de voz usam a API de áudio da OpenAI. Para chamadas de IA e áudio, configure uma chave válida. Não envie chaves reais para o GitHub.

## O que foi implementado

- **Tool calling real:** o assistente recebe ferramentas para criar transações, listar transações e consultar o resumo financeiro. As ferramentas usam os mesmos serviços e validações da API REST.
- **Validação:** valor precisa ser positivo; descrição e categoria são obrigatórias e têm limites de tamanho; tipo é `INCOME` ou `EXPENSE`.
- **API melhorada:** listar por tipo, buscar por ID, excluir por ID e obter resumo com receitas, despesas, saldo e quantidade.
- **Autenticação:** todas as rotas `/api/**` exigem o cabeçalho `X-API-Key`; comparação da chave em tempo constante.
- **Áudio real:** upload de áudio é transcrito com `whisper-1`, e texto pode ser convertido em MP3 pelo endpoint de fala.
- **Erros consistentes:** validação, transações ausentes e configuração de IA retornam códigos HTTP apropriados.

## Tecnologias

- Java 17
- Spring Boot 3.4
- Spring AI 1.0 e OpenAI Chat Model (`gpt-4o-mini` por padrão)
- Spring Web, Validation e Data JPA
- H2 em memória
- Maven
- OpenAI Audio API (`whisper-1` e `tts-1`)

## Pré-requisitos

- JDK 17+
- Maven 3.9+
- Chave de API OpenAI para comandos do assistente, transcrição e geração de áudio

## Executar localmente

```bash
git clone https://github.com/lincolnbuk/orcamento-ia-voz.git
cd orcamento-ia-voz
export OPENAI_API_KEY="sua-chave"
export APP_API_KEY="uma-chave-local-segura"
mvn spring-boot:run
```

A API inicia em `http://localhost:8080`. Para escolher outro modelo de chat, defina `OPENAI_MODEL`. A aplicação tem valores locais padrão para iniciar, mas o assistente e os endpoints de áudio necessitam de uma chave OpenAI válida. O banco H2 é em memória e os dados são apagados ao reiniciar.

No Windows PowerShell, configure as variáveis assim:

```powershell
$env:OPENAI_API_KEY = "sua-chave"
$env:APP_API_KEY = "uma-chave-local-segura"
mvn spring-boot:run
```

## Endpoints

Todas as rotas abaixo exigem `X-API-Key: <APP_API_KEY>`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/transactions` | Cria uma transação validada |
| `GET` | `/api/transactions` | Lista; aceita `?type=INCOME` ou `?type=EXPENSE` |
| `GET` | `/api/transactions/{id}` | Busca por ID |
| `DELETE` | `/api/transactions/{id}` | Exclui por ID |
| `GET` | `/api/transactions/summary` | Retorna receitas, despesas, saldo e contagem |
| `POST` | `/api/assistant/command` | Interpreta um comando de texto e pode executar ferramentas |
| `POST` | `/api/assistant/audio` | Transcreve áudio e processa o texto como comando |
| `POST` | `/api/assistant/speak` | Converte texto em MP3 |

## Testar o fluxo principal

Defina a variável para chamadas HTTP:

```bash
export API_KEY="uma-chave-local-segura"
```

Crie uma transação diretamente:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","amount":42.50,"type":"EXPENSE","category":"Alimentação"}'
```

Peça ao assistente para registrar uma transação. O modelo deve decidir chamar a ferramenta `createTransaction`; não passe valores que não queira gravar:

```bash
curl -X POST http://localhost:8080/api/assistant/command \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"text":"Registre uma despesa de 18 reais com café na categoria alimentação"}'
```

Consulte somente despesas e o resumo:

```bash
curl "http://localhost:8080/api/transactions?type=EXPENSE" -H "X-API-Key: $API_KEY"
curl http://localhost:8080/api/transactions/summary -H "X-API-Key: $API_KEY"
```

Envie um áudio (formatos aceitos dependem da API OpenAI; limite configurado: 20 MB):

```bash
curl -X POST http://localhost:8080/api/assistant/audio \
  -H "X-API-Key: $API_KEY" \
  -F "audio=@comando.wav"
```

Gere uma resposta de voz; a resposta é um arquivo MP3:

```bash
curl -X POST http://localhost:8080/api/assistant/speak \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"text":"Seu saldo foi consultado com sucesso."}' \
  --output resposta.mp3
```

Sem chave ou com chave incorreta, a API retorna `401`. Valores inválidos retornam `400`; transações inexistentes retornam `404`.

## Executar os testes

```bash
mvn test
```

Os testes cobrem autenticação, criação, resumo e validação de valor sem precisar chamar a API OpenAI.

## Organização

```text
src/main/java/com/example/orcamentoia/
  controller/  # endpoints REST e tratamento de erros
  dto/         # contratos de entrada e saída
  model/       # entidade e tipo de transação
  repository/  # acesso JPA
  security/    # filtro de API key
  service/     # regras financeiras, assistente, ferramentas e áudio
```

## Aprendizados e limites

O desafio evidencia que IA é uma camada de interpretação, não substitui regras de negócio: o modelo recebe ferramentas limitadas, mas a aplicação valida os dados antes de persistir. O filtro por chave simples é adequado para demonstração local, não substitui OAuth/JWT, rotação de credenciais, rate limiting ou isolamento de usuários em produção. Para persistência durável, troque H2 em memória por PostgreSQL e configure migrações.
