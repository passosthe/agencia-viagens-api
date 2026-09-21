# API REST — Gerenciamento de Destinos de Viagem (v2)

API RESTful da agência de viagens, agora com **persistência em PostgreSQL**
(Spring Data JPA) e **autenticação/autorização** por perfil de acesso
(Spring Security).

---

## 1. Visão geral

A primeira versão da API entregou os endpoints de gerenciamento de destinos,
porém com duas limitações que impediam seu uso em um ambiente real:

- os dados eram mantidos **em memória**, e se perdiam a cada reinicialização;
- **não havia controle de acesso** — qualquer pessoa com a URL podia cadastrar
  ou excluir destinos do catálogo.

Esta versão resolve os dois pontos:

| Aspecto | Versão 1 | Versão 2 |
|---|---|---|
| Persistência | `ConcurrentHashMap` em memória | PostgreSQL + Spring Data JPA |
| Durabilidade | Dados perdidos ao reiniciar | Dados permanentes no banco |
| Repository | Classe implementada manualmente | Interface `JpaRepository` (implementação gerada pelo Spring) |
| Transações | Inexistentes | `@Transactional` com commit/rollback |
| Avaliações | Apenas a média armazenada | Entidade `Avaliacao` com histórico, autor e data |
| Valores monetários | `Double` | `BigDecimal` (precisão exata) |
| Segurança | Nenhuma | Spring Security, usuários no banco, perfis ADMIN/USER |
| Senhas | — | Hash BCrypt |

### Memória x banco de dados

A diferença não é apenas "onde o dado fica". Ao migrar para persistência real:

- os dados **sobrevivem** a reinicializações e deploys;
- várias instâncias da aplicação podem compartilhar o mesmo estado;
- ganhamos **transações** — uma operação que falha no meio é desfeita por
  completo, sem deixar dados pela metade;
- o banco passa a garantir **integridade** (chaves estrangeiras, campos
  obrigatórios, unicidade de login) independentemente do código da aplicação;
- consultas e filtros passam a ser executados pelo banco em SQL, muito mais
  eficiente do que percorrer coleções em memória.

---

## 2. Arquitetura

```
Cliente (app, parceiro, Postman)
        │  HTTP + credenciais (Basic Auth)
        ▼
┌───────────────────────┐
│  Spring Security      │  Autentica o usuário e verifica o perfil
│  (filtro de entrada)  │  → 401 se não autenticado, 403 se sem permissão
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│     Controller        │  Recebe a requisição, valida entrada, responde HTTP
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│      Service          │  Regra de negócio + controle transacional
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│  Repository (JPA)     │  Interface Spring Data → SQL gerado automaticamente
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│  Entities (@Entity)   │  Destino, Avaliacao, Usuario mapeados em tabelas
└───────────┬───────────┘
            ▼
       PostgreSQL
```

**Princípios mantidos da v1:**

- o **controller** não acessa o banco diretamente nem contém regra de negócio;
- o **service** concentra a lógica e define os limites das transações;
- o **repository** isola o acesso a dados;
- os **DTOs** continuam separando o que entra e o que sai do que é armazenado
  internamente — e agora também evitam o ciclo `Destino → Avaliacao → Destino`
  na serialização JSON.

A migração de memória para banco exigiu mudanças **apenas na camada de
repositório e nas entidades** — controller e DTOs permaneceram praticamente
intactos. Isso comprova, na prática, o valor da separação em camadas adotada
desde a primeira versão.

---

## 3. Modelo de dados

```
┌──────────────────┐         ┌────────────────────┐
│    destinos      │ 1     N │    avaliacoes      │
│──────────────────│◄────────│────────────────────│
│ id (PK)          │         │ id (PK)            │
│ nome             │         │ nota               │
│ localizacao      │         │ comentario         │
│ descricao        │         │ destino_id (FK)    │
│ preco_pacote     │         │ usuario_login      │
│ hoteis_disponivel│         │ data_avaliacao     │
│ media_avaliacao  │         └────────────────────┘
│ qtd_avaliacoes   │
└────────┬─────────┘
         │ 1
         │
         │ N
┌────────▼──────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ destino_atividades    │    │    usuarios      │ 1  │ usuario_perfis   │
│───────────────────────│    │──────────────────│───▶│──────────────────│
│ destino_id (FK)       │    │ id (PK)          │  N │ usuario_id (FK)  │
│ atividade             │    │ login (unique)   │    │ perfil           │
└───────────────────────┘    │ senha (hash)     │    └──────────────────┘
                             │ ativo            │
                             └──────────────────┘
```

As tabelas são criadas automaticamente pelo Hibernate a partir das entidades
(`spring.jpa.hibernate.ddl-auto=update`).

---

## 4. Tecnologias e justificativas

| Tecnologia | Justificativa |
|---|---|
| **Java 17** | LTS, fortemente tipada, padrão em aplicações corporativas. |
| **Spring Boot 3.3** | Framework de mercado para APIs REST em Java; auto-configuração reduz drasticamente o código de infraestrutura. |
| **Spring Data JPA** | Elimina o código repetitivo de acesso a dados: a interface do repositório declara o *quê*, e o framework gera o SQL. Queries derivadas do nome do método cobrem a maioria dos casos sem uma linha de SQL. |
| **PostgreSQL** | Banco relacional open source, maduro e gratuito, com forte suporte a integridade referencial e tipos precisos como `NUMERIC` — adequado a dados financeiros de pacotes de viagem. |
| **Spring Security** | Solução padrão de segurança do ecossistema Spring; integra-se de forma declarativa à aplicação e permite definir as regras de acesso em um único ponto. |
| **BCrypt** | Algoritmo de hash específico para senhas: lento por design (dificulta força bruta) e com *salt* automático por senha. |
| **HTTP Basic** | Mecanismo de autenticação simples, nativo do HTTP e adequado ao escopo desta etapa. Para produção com clientes públicos, o próximo passo natural seria JWT ou OAuth2. |
| **BigDecimal** | Representação exata de valores monetários, evitando os erros de arredondamento inerentes ao ponto flutuante. |
| **Bean Validation** | Rejeita dados inválidos na borda da aplicação, antes de chegarem à regra de negócio. |
| **Lombok** | Reduz código repetitivo (getters, setters, construtores). |

---

## 5. Estrutura do projeto

```
src/main/java/com/agenciaviagens/api/
├── AgenciaViagensApiApplication.java    # ponto de entrada
├── config/
│   └── CargaInicialUsuarios.java        # cria os usuários de teste
├── controller/
│   ├── DestinoController.java           # endpoints de destinos
│   └── AuthController.java              # dados do usuário autenticado
├── service/
│   └── DestinoService.java              # regra de negócio + transações
├── repository/
│   ├── DestinoRepository.java           # interface Spring Data JPA
│   └── UsuarioRepository.java           # busca de usuários para login
├── model/
│   ├── Destino.java                     # @Entity
│   ├── Avaliacao.java                   # @Entity (relacionamento N:1)
│   ├── Usuario.java                     # @Entity
│   └── Perfil.java                      # enum ADMIN / USER
├── dto/
│   ├── DestinoRequestDTO.java
│   ├── DestinoResponseDTO.java
│   └── AvaliacaoRequestDTO.java
├── security/
│   ├── SecurityConfig.java              # regras de acesso por endpoint
│   ├── UsuarioDetailsService.java       # carrega usuários do banco
│   └── UsuarioDetails.java              # adapta Usuario ao Spring Security
└── exception/
    ├── DestinoNaoEncontradoException.java
    └── GlobalExceptionHandler.java      # 404, 400 e 403 padronizados
```

---

## 6. Pré-requisitos

- **Java 17** ou superior
- **Maven 3.8+**
- **PostgreSQL 14+** em execução

---

## 7. Configuração do banco de dados

### 7.1 Criar o banco

Abra o terminal do PostgreSQL (`psql`) ou o pgAdmin e execute:

```sql
CREATE DATABASE agencia_viagens;
```

Pelo terminal, o comando completo seria:

```bash
psql -U postgres -c "CREATE DATABASE agencia_viagens;"
```

> As **tabelas não precisam ser criadas manualmente** — o Hibernate as gera
> automaticamente na primeira execução, a partir das entidades anotadas.

### 7.2 Credenciais

O arquivo `src/main/resources/application.properties` usa variáveis de
ambiente com valores padrão:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Endereço do servidor PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `agencia_viagens` | Nome do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |

Se a sua instalação do PostgreSQL usa outra senha, você pode:

- definir a variável de ambiente `DB_PASSWORD` antes de rodar; ou
- ajustar diretamente a linha `spring.datasource.password` no
  `application.properties`.

---

## 8. Como executar

```bash
# 1. Clonar o repositório
git clone https://github.com/passosthe/agencia-viagens-api.git
cd agencia-viagens-api

# 2. Garantir que o PostgreSQL está rodando e o banco foi criado (seção 7)

# 3. Executar a aplicação
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

Na primeira execução, o log deve mostrar a criação das tabelas e as mensagens:

```
Usuario de teste criado: admin com perfis [ADMIN]
Usuario de teste criado: usuario com perfis [USER]
```

Para rodar os testes automatizados:

```bash
mvn test
```

---

## 9. Usuários e perfis de teste

Criados automaticamente na primeira execução:

| Login | Senha | Perfil | Pode fazer |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Tudo: cadastrar, atualizar, excluir, consultar e avaliar |
| `usuario` | `usuario123` | `USER` | Consultar destinos e registrar avaliações |

> Credenciais fixas existem apenas para permitir a avaliação desta atividade.
> Em um ambiente real, senhas iniciais devem vir de variáveis de ambiente e
> ser trocadas no primeiro acesso.

---

## 10. Regras de acesso por endpoint

| Método | Endpoint | ADMIN | USER | Sem autenticação |
|---|---|:---:|:---:|:---:|
| `GET` | `/api/auth/me` | ✅ | ✅ | ❌ 401 |
| `GET` | `/api/destinos` | ✅ | ✅ | ❌ 401 |
| `GET` | `/api/destinos/{id}` | ✅ | ✅ | ❌ 401 |
| `GET` | `/api/destinos/pesquisa` | ✅ | ✅ | ❌ 401 |
| `PATCH` | `/api/destinos/{id}/avaliacoes` | ✅ | ✅ | ❌ 401 |
| `POST` | `/api/destinos` | ✅ | ❌ 403 | ❌ 401 |
| `PUT` | `/api/destinos/{id}` | ✅ | ❌ 403 | ❌ 401 |
| `DELETE` | `/api/destinos/{id}` | ✅ | ❌ 403 | ❌ 401 |

**Lógica adotada:** consultar o catálogo e avaliar destinos são ações de
qualquer usuário autenticado (cliente ou parceiro). Alterar o catálogo —
cadastrar, atualizar e excluir — é uma operação sensível, restrita ao perfil
administrativo da agência.

**Códigos de resposta de segurança:**

- `401 Unauthorized` — credenciais ausentes ou inválidas;
- `403 Forbidden` — usuário autenticado, mas sem o perfil necessário.

---

## 11. Endpoints

Prefixo base: `/api/destinos`. Todas as requisições exigem autenticação
**HTTP Basic** (no Postman: aba *Authorization* → tipo *Basic Auth*).

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/destinos` | Cadastra um destino |
| `GET` | `/api/destinos` | Lista todos os destinos |
| `GET` | `/api/destinos/pesquisa?nome=&localizacao=` | Pesquisa por nome e/ou localização |
| `GET` | `/api/destinos/{id}` | Detalha um destino |
| `PUT` | `/api/destinos/{id}` | Atualiza um destino |
| `PATCH` | `/api/destinos/{id}/avaliacoes` | Registra avaliação e recalcula a média |
| `DELETE` | `/api/destinos/{id}` | Exclui um destino |
| `GET` | `/api/auth/me` | Retorna o login e os perfis do usuário autenticado |

### Exemplo — verificar autenticação

```
GET /api/auth/me
Authorization: Basic admin / admin123
```

```json
{
  "login": "admin",
  "perfis": ["ADMIN"],
  "autenticado": true
}
```

### Exemplo — cadastrar destino (ADMIN)

```
POST /api/destinos
Authorization: Basic admin / admin123
Content-Type: application/json

{
  "nome": "Chapada Diamantina",
  "localizacao": "Bahia, Brasil",
  "descricao": "Trilhas, cachoeiras e grutas no interior baiano",
  "precoPacote": 1250.00,
  "hoteisDisponiveis": true,
  "atividadesTuristicas": ["Trilha do Vale do Pati", "Poço Encantado"]
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "nome": "Chapada Diamantina",
  "localizacao": "Bahia, Brasil",
  "descricao": "Trilhas, cachoeiras e grutas no interior baiano",
  "precoPacote": 1250.00,
  "hoteisDisponiveis": true,
  "atividadesTuristicas": ["Trilha do Vale do Pati", "Poço Encantado"],
  "mediaAvaliacao": 0.00,
  "quantidadeAvaliacoes": 0
}
```

### Exemplo — registrar avaliação (USER ou ADMIN)

```
PATCH /api/destinos/1/avaliacoes
Authorization: Basic usuario / usuario123
Content-Type: application/json

{
  "nota": 4.5,
  "comentario": "Passeio excelente, guias muito bem preparados"
}
```

A resposta traz a `mediaAvaliacao` recalculada e a `quantidadeAvaliacoes`
atualizada. O autor da avaliação é obtido do usuário autenticado, não do
corpo da requisição.

### Exemplo — tentativa sem permissão

```
DELETE /api/destinos/1
Authorization: Basic usuario / usuario123
```

Resposta `403 Forbidden`:

```json
{
  "timestamp": "2026-08-30T21:15:42.123",
  "status": 403,
  "erro": "Acesso negado",
  "mensagem": "Seu perfil nao possui permissao para executar esta operacao"
}
```

### Exemplo com `curl`

```bash
# Consulta como usuário comum
curl -u usuario:usuario123 http://localhost:8080/api/destinos

# Cadastro como administrador
curl -u admin:admin123 -X POST http://localhost:8080/api/destinos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fernando de Noronha","localizacao":"Pernambuco, Brasil","precoPacote":3500.00}'
```

---

## 12. Como testar no Postman

1. Crie a requisição normalmente (método + URL).
2. Vá na aba **Authorization** e escolha o tipo **Basic Auth**.
3. Preencha *Username* e *Password* com um dos usuários da seção 9.
4. Para requisições com corpo, use **Body → raw → JSON**.

**Roteiro sugerido de validação:**

| # | Ação | Usuário | Resultado esperado |
|---|---|---|---|
| 1 | `GET /api/destinos` sem autenticação | — | `401` |
| 2 | `GET /api/auth/me` | `admin` | `200` com perfil ADMIN |
| 3 | `POST /api/destinos` | `admin` | `201` |
| 4 | `POST /api/destinos` | `usuario` | `403` |
| 5 | `GET /api/destinos` | `usuario` | `200` com a lista |
| 6 | `PATCH /api/destinos/1/avaliacoes` nota 4 | `usuario` | `200`, média 4.00 |
| 7 | `PATCH /api/destinos/1/avaliacoes` nota 5 | `usuario` | `200`, média 4.50 |
| 8 | Reiniciar a aplicação e repetir o passo 5 | `usuario` | Dados **continuam lá** (persistência) |
| 9 | `DELETE /api/destinos/1` | `usuario` | `403` |
| 10 | `DELETE /api/destinos/1` | `admin` | `204` |

O passo 8 é o que demonstra na prática a diferença entre armazenamento em
memória e persistência em banco de dados.

---

## 13. Próximos passos

- Autenticação via **JWT**, eliminando o envio de credenciais a cada requisição.
- Endpoint de **cadastro de novos usuários** com validação de força de senha.
- **Paginação** nas listagens (`Pageable` do Spring Data).
- Migrações de schema versionadas com **Flyway**, substituindo o `ddl-auto=update`.
- Documentação interativa com **Swagger/OpenAPI**.
