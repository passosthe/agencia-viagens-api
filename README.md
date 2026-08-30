# Gerenciamento de Destinos de Viagem

Projeto desenvolvido para a modernização dos serviços digitais de uma agência
de viagens, permitindo a integração com aplicativos de turismo, parceiros
comerciais e futuras plataformas digitais.

## 1. Visão geral do problema

A agência possui um site institucional e um sistema interno de reservas, mas
não possui uma forma padronizada de expor seus dados (destinos, pacotes,
disponibilidade de hotéis, avaliações) para sistemas externos. Este projeto
resolve isso construindo uma **API REST** para o recurso central da primeira
etapa: **Destinos de viagem**.

Nesta primeira versão, os dados são mantidos **em memória** (sem banco de
dados), com foco na definição da arquitetura, organização do projeto e
implementação dos endpoints principais.

## 2. Arquitetura proposta

O projeto segue a **arquitetura em camadas**, padrão consolidado para APIs
Spring Boot:

```
Cliente (app, site, parceiro)
        │  HTTP (JSON)
        ▼
 ┌─────────────┐
 │ Controller  │  Recebe requisições HTTP, valida entrada, devolve status/JSON
 └──────┬──────┘
        ▼
 ┌─────────────┐
 │  Service    │  Regra de negócio (ex: recalcular média de avaliação)
 └──────┬──────┘
        ▼
 ┌─────────────┐
 │ Repository  │  Acesso aos dados (versão inicial: Map em memória)
 └──────┬──────┘
        ▼
 ┌─────────────┐
 │ Model/Entity│  Representação do "Destino" em Java
 └─────────────┘
```

**Por que essa separação?**
- O **Controller** se comunica via HTTP — não conhece regra de negócio.
- O **Service** concentra toda a lógica de negócio, independente de como os dados chegam (HTTP) ou são guardados (memória, banco, etc.). Isso facilita testes automatizados e futuras mudanças.
- O **Repository** isola o armazenamento. Se a agência decidir migrar para um banco de dados relacional no futuro, apenas essa camada precisa mudar — Controller e Service permanecem intactos.
- Uso de **DTOs** (`DestinoRequestDTO`, `DestinoResponseDTO`, `AvaliacaoRequestDTO`) para nunca expor a Entity diretamente na API, controlando exatamente o que entra e o que sai.

Essa organização favorece manutenção, testabilidade e evolução da aplicação
— por exemplo, adicionar autenticação ou trocar o armazenamento em memória
por um banco de dados real não deve exigir reescrever a API inteira.

## 3. Justificativa das tecnologias

| Tecnologia | Motivo da escolha |
|---|---|
| **Java 17** | Linguagem robusta, fortemente tipada, madura para aplicações corporativas de longa duração — perfil comum em agências e empresas de médio/grande porte. |
| **Spring Boot 3** | Framework padrão de mercado para APIs REST em Java. Resolve configuração de servidor, roteamento HTTP e serialização JSON automaticamente, permitindo focar na regra de negócio. |
| **Maven** | Gerenciador de dependências e build, integrado nativamente ao ecossistema Spring. |
| **Bean Validation (`spring-boot-starter-validation`)** | Garante que dados inválidos (ex: preço negativo, nome vazio) sejam rejeitados antes de chegarem à regra de negócio. |
| **Lombok** | Reduz código repetitivo (getters/setters), deixando as classes mais legíveis. |
| **Armazenamento em memória (`ConcurrentHashMap`)** | Atende ao requisito desta etapa (sem necessidade de banco de dados) e já é seguro para múltiplas requisições simultâneas, evitando retrabalho ao evoluir para um banco real depois. |

## 4. Estrutura do projeto

```
src/main/java/com/agenciaviagens/api/
├── AgenciaViagensApiApplication.java   # ponto de entrada da aplicação
├── controller/
│   └── DestinoController.java          # endpoints REST
├── service/
│   └── DestinoService.java             # regras de negócio
├── repository/
│   └── DestinoRepository.java          # armazenamento em memória
├── model/
│   └── Destino.java                    # entidade de domínio
├── dto/
│   ├── DestinoRequestDTO.java
│   ├── DestinoResponseDTO.java
│   └── AvaliacaoRequestDTO.java
└── exception/
    ├── DestinoNaoEncontradoException.java
    └── GlobalExceptionHandler.java     # tratamento centralizado de erros
```

## 5. Endpoints da API

Prefixo base: `/api/destinos`

| Método | Endpoint | Descrição | Corpo da requisição |
|---|---|---|---|
| `POST` | `/api/destinos` | Cadastra um novo destino | JSON com `nome`, `localizacao`, `descricao`, `precoPacote`, `hoteisDisponiveis`, `atividadesTuristicas` |
| `GET` | `/api/destinos` | Lista todos os destinos | — |
| `GET` | `/api/destinos/pesquisa?nome=&localizacao=` | Pesquisa destinos por nome e/ou localização | — |
| `GET` | `/api/destinos/{id}` | Detalha um destino específico | — |
| `PUT` | `/api/destinos/{id}` | Atualiza um destino existente | Mesmo formato do cadastro |
| `PATCH` | `/api/destinos/{id}/avaliacoes` | Registra uma nova avaliação e recalcula a média | JSON com `nota` (0 a 5) |
| `DELETE` | `/api/destinos/{id}` | Exclui um destino | — |

### Exemplo — cadastrar um destino

```
POST /api/destinos
Content-Type: application/json

{
  "nome": "Chapada Diamantina",
  "localizacao": "Bahia, Brasil",
  "descricao": "Trilhas, cachoeiras e grutas no interior baiano",
  "precoPacote": 1250.00,
  "hoteisDisponiveis": true,
  "atividadesTuristicas": ["Trilha do Vale do Pati", "Poço Encantado", "Cachoeira da Fumaça"]
}
```

Resposta (`201 Created`):

```json
{
  "id": 1,
  "nome": "Chapada Diamantina",
  "localizacao": "Bahia, Brasil",
  "descricao": "Trilhas, cachoeiras e grutas no interior baiano",
  "precoPacote": 1250.0,
  "hoteisDisponiveis": true,
  "atividadesTuristicas": ["Trilha do Vale do Pati", "Poço Encantado", "Cachoeira da Fumaça"],
  "mediaAvaliacao": 0.0,
  "quantidadeAvaliacoes": 0
}
```

### Exemplo — registrar avaliação

```
PATCH /api/destinos/1/avaliacoes
Content-Type: application/json

{ "nota": 4.5 }
```

### Exemplo — pesquisar

```
GET /api/destinos/pesquisa?localizacao=bahia
```

## 6. Como executar o projeto

**Pré-requisitos:** Java 17+ e Maven instalados.

```bash
# 1. Clonar o repositório
git clone <URL_DO_REPOSITORIO>
cd agencia-viagens-api

# 2. Rodar a aplicação
mvn spring-boot:run
```

Dica: se preferir não instalar o Maven localmente, rode `mvn -N io.takari:maven:wrapper`
uma vez no projeto para gerar o Maven Wrapper (`mvnw`/`mvnw.cmd`) e versioná-lo no repositório.

A API sobe em `http://localhost:8080`.

## 7. Como testar

- Rodar os testes automatizados: `mvn test`
- Testar manualmente com **Postman**, **Insomnia** ou `curl`, usando os exemplos
  de endpoints da seção 5.
- Exemplo rápido com `curl`:
  ```bash
  curl -X POST http://localhost:8080/api/destinos \
    -H "Content-Type: application/json" \
    -d '{"nome":"Fernando de Noronha","localizacao":"Pernambuco, Brasil","precoPacote":3500.0}'
  ```


