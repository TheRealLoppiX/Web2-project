# API de Pedidos – Assistência Técnica

API REST para gerenciamento de pedidos (ordens de serviço) de uma assistência técnica de informática, desenvolvida
para a disciplina de JEE do IFSertão-PE – Campus Salgueiro. A especificação completa está em
[docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_1.docx](docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_1.docx)
(versão 1.1, com autenticação e perfis de acesso); a versão 1.0 original continua em
[docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_0.pdf](docs/SRS_Gerenciamento_Pedidos_Assistencia_Tecnica_v1_0.pdf).

A API controla o ciclo de vida das OS, da entrada do equipamento até a entrega ao cliente: cadastro de clientes,
equipamentos e técnicos; abertura da OS; laudo e orçamento; atribuição de técnico; mudanças de status com
transições validadas e histórico de cada mudança. O acesso é protegido com Spring Security e token JWT, com perfis
de atendente, técnico e gerente. A visão do produto está em
[docs/Documento_de_Visao_API_Pedidos.docx](docs/Documento_de_Visao_API_Pedidos.docx).

## Tecnologias

- Java 17 e Spring Boot 3.5 (Web, Data JPA, Validation, Security, OAuth2 Resource Server)
- JWT assinado com HS256 e senhas com BCrypt
- Hibernate e PostgreSQL 16
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc e H2 nos testes
- Docker e Docker Compose para compilar, testar e executar

## Pré-requisitos

- Docker Desktop (Windows/macOS) ou Docker Engine com o plugin Compose (Linux)

Não é preciso instalar Java, Maven nem PostgreSQL: a compilação, os testes e a execução acontecem dentro dos
contêineres.

## Como rodar

```bash
cp .env.example .env
```

```bash
docker compose up -d --build
```

Pronto: o primeiro comando cria o arquivo de configuração e o segundo sobe o PostgreSQL e a API. Na primeira vez, a
imagem da API é construída: o Docker baixa as dependências, compila o projeto e executa os 90 testes automatizados
(alguns minutos). Se algum teste falhar, a imagem não é criada. Nas próximas vezes, `docker compose up -d` sobe tudo em
segundos.

A API fica disponível em `http://localhost:8080` assim que o log mostrar `Started Application`:

```bash
docker compose logs -f api
```

Para parar, use `docker compose down` (os dados continuam no volume `postgres-data`); para apagar também os dados, use
`docker compose down -v`.

As tabelas são criadas automaticamente pelo Hibernate na primeira execução, junto com o usuário gerente definido em
`ADMIN_LOGIN`/`ADMIN_PASSWORD` (com o `.env.example`, login `gerente` e senha `troque-esta-senha-do-gerente`). O fuso
horário padrão é `America/Recife`.

### Variáveis de ambiente

Credenciais e segredos são lidos de variáveis de ambiente e nunca ficam no código. O Docker Compose lê o arquivo `.env`,
que está no `.gitignore`; troque os valores de exemplo antes de usar fora da sala de aula:

| Variável | Uso |
|----------|-----|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `JWT_SECRET` | Segredo de assinatura dos tokens; obrigatório, com pelo menos 32 caracteres |
| `JWT_EXPIRACAO` | Validade do token (padrão `8h`) |
| `ADMIN_LOGIN`, `ADMIN_PASSWORD` | Gerente criado na primeira execução, quando ainda não há usuários |

Dentro do Docker Compose, a API se conecta ao banco pelo nome do serviço (`postgres`), então `DB_URL` do `.env` não é
usada por ela.

### Documentação e testes manuais

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificação OpenAPI: <http://localhost:8080/v3/api-docs>
- Cópia estática da documentação, que abre sem a API rodando:
  [docs/documentacao-api.html](docs/documentacao-api.html) (Swagger UI no navegador) e
  [docs/openapi.json](docs/openapi.json) (especificação OpenAPI 3.1)
- Coleção do Postman: [docs/API_Pedidos_Assistencia_Tecnica.postman_collection.json](docs/API_Pedidos_Assistencia_Tecnica.postman_collection.json).
  Importe no Postman, preencha a variável `senhaGerente` com o valor de `ADMIN_PASSWORD` e execute em sequência
  (Collection Runner). O gerente cadastra um atendente e um técnico, cada etapa da OS é feita pelo perfil responsável e
  os tokens e ids são guardados nas variáveis da coleção.

## Autenticação e perfis

Faça login e envie o token no cabeçalho `Authorization` das demais requisições:

```http
POST /api/auth/login
{ "login": "gerente", "senha": "..." }

GET /api/ordens-servico
Authorization: Bearer <token>
```

No Swagger, use o botão **Authorize** com o token. A API não guarda sessão: cada requisição é validada pelo token.

| Operação | Atendente | Técnico | Gerente |
|----------|:---------:|:-------:|:-------:|
| Consultas (`GET`) | ✔ | ✔ | ✔ |
| Cadastrar, atualizar e remover clientes e equipamentos | ✔ | | ✔ |
| Cadastrar, atualizar e desativar técnicos | | | ✔ |
| Abrir OS | ✔ | | ✔ |
| Registrar laudo, serviços e valor (`PUT` da OS) | | ✔ | ✔ |
| Atribuir técnico à OS | | | ✔ |
| Alterar status da OS | ✔ | ✔ | ✔ |
| Gerenciar usuários (`/api/usuarios`) | | | ✔ |

### Testes automatizados

Os testes rodam automaticamente na construção da imagem (`docker compose up -d --build`). Para executá-los de novo e
ver o resultado completo:

```bash
docker compose build --no-cache --progress=plain api
```

São os testes unitários (JUnit 5 e Mockito) dos services e do enum de transições de status, cobrindo as regras
RN-01 a RN-10, do login JWT e do Singleton, e os testes de integração com H2 em memória e MockMvc, que percorrem o
fluxo cliente → equipamento → OS até `ENTREGUE`, verificam uma transição inválida retornando 422 e as respostas 401 e
403 por perfil. Os testes não precisam do PostgreSQL nem das variáveis de ambiente.

## Endpoints

Todas as listagens são paginadas com os parâmetros `page`, `size` (padrão 20) e `sort` (ex.: `sort=nome,asc`).

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Autentica e devolve o token JWT (público) |
| POST | `/api/usuarios` | Cadastra usuário da equipe (gerente) |
| GET | `/api/usuarios` | Lista usuários (gerente) |
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
| POST | `/api/tecnicos` | Cadastra técnico ativo (nome, e-mail e especialidade) |
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

No `PUT /api/ordens-servico/{id}`, campos não informados mantêm o valor atual da OS. Datas trafegam em ISO 8601
(`2026-11-09T14:30:00`) no fuso `America/Recife` e valores monetários em reais, com ponto decimal.

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

## Padrões de projeto

| Padrão | Onde |
|--------|------|
| State | `StatusOrdemServico`: cada status define as transições permitidas e o efeito ao entrar nele |
| Singleton | `RelogioAssistencia`: instância única (holder idiom, thread-safe) que fornece data e hora no fuso `America/Recife` |
| Repository | Interfaces `JpaRepository` do Spring Data |
| DTO | Records de requisição e resposta, separados das entidades JPA |

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
| 400 | Dados inválidos (Bean Validation, JSON mal formatado, parâmetro inválido, método ou tipo de conteúdo não suportado) |
| 401 | Token ausente, inválido ou expirado, ou login e senha incorretos |
| 403 | Perfil sem permissão para a operação |
| 404 | Recurso não encontrado |
| 409 | Duplicidade ou registro com dependentes |
| 422 | Regra de negócio violada |

Erros inesperados retornam 500 no mesmo formato e são registrados no console do servidor.

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
└── config/       segurança (JWT e perfis), Singleton RelogioAssistencia e OpenAPI
```

## Autores

Arthur Vasconcelos de Oliveira e Bruno Alencar – IFSertão-PE
