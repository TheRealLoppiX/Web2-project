# API de Pedidos – Assistência Técnica

API REST para gerenciamento de pedidos (ordens de serviço) de uma assistência técnica de informática, desenvolvida
para a disciplina de JEE do IFSertão-PE – Campus Salgueiro. A especificação completa está em
[docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_0.pdf](docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_0.pdf).

A API controla o ciclo de vida das OS, da entrada do equipamento até a entrega ao cliente: cadastro de clientes,
equipamentos e técnicos; abertura da OS; laudo e orçamento; atribuição de técnico; mudanças de status com
transições validadas e histórico de cada mudança.

## Tecnologias

- Java 17 e Spring Boot 3.5 (Web, Data JPA, Validation)
- Hibernate e PostgreSQL 16
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc e H2 nos testes
- Maven (via Maven Wrapper) e Docker Compose

## Pré-requisitos

- JDK 17 ou superior (`java -version`)
- Docker e Docker Compose, para subir o PostgreSQL sem instalá-lo
  (ou um PostgreSQL 16 local com um banco chamado `assistencia`)
- Não é preciso instalar o Maven: use `./mvnw` (Linux/macOS/Git Bash) ou `mvnw.cmd` (Windows)

## Como rodar

### 1. Configurar as variáveis de ambiente

As credenciais do banco são lidas das variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD` e nunca ficam no código.
Copie o arquivo de exemplo e defina uma senha:

```bash
cp .env.example .env
```

O arquivo `.env` está no `.gitignore`. O Docker Compose lê esse arquivo automaticamente; para a aplicação, exporte as
variáveis no terminal.

Linux, macOS ou Git Bash:

```bash
set -a; . ./.env; set +a
```

Windows (PowerShell):

```powershell
Get-Content .env | ForEach-Object { $k, $v = $_ -split '=', 2; if ($k) { Set-Item "env:$k" $v } }
```

Se `DB_URL` não for definida, a aplicação usa `jdbc:postgresql://localhost:5432/assistencia`.

### 2. Subir o banco de dados

```bash
docker compose up -d --wait
```

### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`. As tabelas são criadas automaticamente pelo Hibernate na primeira
execução. O fuso horário padrão é `America/Recife`.

### 4. Documentação e testes manuais

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificação OpenAPI: <http://localhost:8080/v3/api-docs>
- Coleção do Postman: [docs/API_Pedidos_Assistencia_Tecnica.postman_collection.json](docs/API_Pedidos_Assistencia_Tecnica.postman_collection.json).
  Importe no Postman e execute em sequência (Collection Runner); as requisições seguem o fluxo de atendimento e os ids
  criados são guardados nas variáveis da coleção.

### Testes automatizados

```bash
./mvnw verify
```

Executa os testes unitários (JUnit 5 e Mockito) do serviço de ordens de serviço e do enum de transições de status, e o
teste de integração com H2 em memória e MockMvc, que percorre o fluxo cliente → equipamento → OS até `ENTREGUE` e
verifica uma transição inválida retornando 422. Os testes não precisam do PostgreSQL nem das variáveis de ambiente.

## Endpoints

Todas as listagens são paginadas com os parâmetros `page`, `size` (padrão 20) e `sort` (ex.: `sort=nome,asc`).

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/clientes` | Cadastra cliente |
| GET | `/api/clientes?nome=` | Lista clientes (filtro opcional por nome parcial) |
| GET | `/api/clientes/{id}` | Consulta cliente |
| PUT | `/api/clientes/{id}` | Atualiza cliente |
| DELETE | `/api/clientes/{id}` | Remove cliente (409 se tiver equipamentos) |
| GET | `/api/clientes/{id}/equipamentos` | Lista os equipamentos do cliente |
| POST | `/api/equipamentos` | Cadastra equipamento vinculado a um cliente |
| GET | `/api/equipamentos/{id}` | Consulta equipamento |
| PUT | `/api/equipamentos/{id}` | Atualiza equipamento |
| DELETE | `/api/equipamentos/{id}` | Remove equipamento (409 se tiver OS) |
| POST | `/api/tecnicos` | Cadastra técnico (ativo) |
| GET | `/api/tecnicos?ativos=` | Lista técnicos (filtro opcional por ativos) |
| GET | `/api/tecnicos/{id}` | Consulta técnico |
| PUT | `/api/tecnicos/{id}` | Atualiza técnico |
| DELETE | `/api/tecnicos/{id}` | Desativa técnico (exclusão lógica) |
| POST | `/api/ordens-servico` | Abre OS em `AGUARDANDO_ANALISE` |
| GET | `/api/ordens-servico?status=&clienteId=&tecnicoId=` | Lista OS com filtros |
| GET | `/api/ordens-servico/{id}` | Consulta OS |
| PUT | `/api/ordens-servico/{id}` | Registra laudo técnico, serviços e valor total |
| PATCH | `/api/ordens-servico/{id}/tecnico` | Atribui ou troca o técnico responsável |
| PATCH | `/api/ordens-servico/{id}/status` | Altera o status da OS |
| GET | `/api/ordens-servico/{id}/historico` | Histórico de status em ordem cronológica |

### Exemplo de fluxo

```http
POST /api/ordens-servico
{ "equipamentoId": 1, "descricaoDefeito": "Notebook não liga." }

PUT /api/ordens-servico/1
{ "laudoTecnico": "Cooler obstruído.", "valorTotal": 150.00 }

PATCH /api/ordens-servico/1/status
{ "status": "ORCAMENTO_APROVADO", "observacao": "Aprovado por telefone." }
```

No `PUT /api/ordens-servico/{id}`, campos não informados mantêm o valor atual da OS.

## Ciclo de vida da OS

```
AGUARDANDO_ANALISE ──► ORCAMENTO_APROVADO ──► EM_REPARO ──► CONCLUIDO ──► ENTREGUE
        │                      │
        └──────────────────────┴──► CANCELADO
```

As transições permitidas ficam no próprio enum `StatusOrdemServico` (padrão State). As regras de cada etapa são
validadas na camada de serviço:

| Regra | Descrição |
|-------|-----------|
| RN-01 | Toda OS nasce em `AGUARDANDO_ANALISE` e gera a primeira entrada no histórico |
| RN-02 | Transições fora do diagrama retornam 422 |
| RN-03 | `ORCAMENTO_APROVADO` exige laudo técnico e valor total |
| RN-04 | `EM_REPARO` exige técnico ativo atribuído |
| RN-05 | `CONCLUIDO` exige descrição dos serviços |
| RN-06 | E-mail e CPF/CNPJ de cliente e e-mail de técnico são únicos (409) |
| RN-07 | Cliente com equipamentos e equipamento com OS não podem ser removidos (409) |
| RN-08 | OS em `ENTREGUE` ou `CANCELADO` não aceita alterações |
| RN-09 | Técnico inativo não pode receber OS |
| RN-10 | O valor total da OS não pode ser negativo |

Ao entrar em `CONCLUIDO` e `ENTREGUE`, as datas de conclusão e de entrega são preenchidas automaticamente.

## Formato de erro

Toda falha devolve o mesmo formato; `campos` aparece apenas em erros de validação.

```json
{
  "timestamp": "2026-11-09T14:30:00",
  "status": 422,
  "erro": "Regra de negócio violada",
  "mensagem": "Transição de EM_REPARO para ENTREGUE não permitida.",
  "caminho": "/api/ordens-servico/1/status"
}
```

| Código | Situação |
|--------|----------|
| 400 | Dados inválidos (Bean Validation, JSON mal formatado, parâmetro inválido) |
| 404 | Recurso não encontrado |
| 409 | Duplicidade ou registro com dependentes |
| 422 | Regra de negócio violada |

## Estrutura do projeto

```
src/main/java/br/edu/ifsertaope/assistencia/
├── Application.java
├── controller/   endpoints REST
├── service/      regras de negócio
├── repository/   interfaces Spring Data JPA
├── model/        entidades JPA e enums
├── dto/          records de requisição e resposta
├── mapper/       conversão entre entidades e DTOs
├── exception/    exceções e handler global de erros
└── config/       Clock (America/Recife) e OpenAPI
```

## Autores

Arthur Vasconcelos de Oliveira e Bruno Alencar – IFSertão-PE
