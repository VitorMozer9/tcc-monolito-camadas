# Produtos, estoque e pedidos

API REST do experimento de evolução arquitetural. Um único executável Spring Boot atende os três conjuntos de operações. O saldo muda exclusivamente por movimentações manuais.

## Ambiente e execução

Executar Maven e a aplicação com **JDK 23.0.2**. O POM usa Spring Boot **4.1.2** e compila com **release 21**. O gerenciamento de dependências do Spring Boot fornece JUnit Jupiter, Mockito e MockMvc. ArchUnit é uma dependência de teste.

Na raiz do projeto, em PowerShell:

```powershell
$env:MAVEN_USER_HOME = Join-Path $PWD '.maven-local'
$env:TEMP = (New-Item -Force -ItemType Directory .maven-local/tmp).FullName
$env:TMP = $env:TEMP
$repoMaven = "-Dmaven.repo.local=$(Join-Path $env:MAVEN_USER_HOME 'repository')"
java -version
.\mvnw.cmd $repoMaven -version
.\mvnw.cmd $repoMaven compile
.\mvnw.cmd $repoMaven test
.\mvnw.cmd $repoMaven verify
.\mvnw.cmd $repoMaven spring-boot:run
```

Com Maven instalado, os mesmos objetivos podem ser executados com `mvn`. A aplicação atende em `http://localhost:8080`. Em desenvolvimento, o H2 grava em `./data/pedidosestoque` e o Hibernate atualiza o esquema. Os testes de integração ativam o perfil `test`, usam H2 em memória e recriam o esquema. Os relatórios do Surefire ficam em `target/surefire-reports/`.

## Arquitetura

Pacote-base `com.github.vitormozer9.pedidosestoque`.

| Pacote | Responsabilidade |
|---|---|
| `controller` | Receber JSON, validar dados de entrada, delegar aos serviços e definir status de sucesso. |
| `service` | Coordenar casos de uso e transações. Resolver referências, carregar agregados e produzir DTOs antes do fechamento da transação. |
| `repository` | Persistir Produto, Estoque e Pedido com Spring Data JPA. As consultas de pedidos carregam os itens para a resposta. |
| `entity` | Representar Produto, Estoque, Pedido e ItemPedido. Proteger o saldo e as transições do pedido, calcular subtotal e total. |
| `dto` | Definir os contratos de entrada e saída em records. |
| `exception` | Representar falhas de recurso e conflitos de negócio e convertê-las em respostas HTTP. |

Cada produto tem no máximo um estoque, identificado externamente por `produtoId`. A unicidade é garantida no banco e o saldo usa versão otimista. Um pedido possui vários itens, persistidos pelo pedido com cascata e remoção de órfãos. A chave composta única de pedido e produto protege a unicidade de cada item. Alterações de itens e cancelamento adquirem um bloqueio de escrita na linha do pedido para preservar suas regras diante de transações simultâneas.

## Contrato HTTP

Prefixo comum `/api/v1`. Requisições com corpo usam `Content-Type: application/json`. Os nomes entre chaves na coluna de entrada identificam os campos do objeto JSON.

| Método | Caminho | Entrada | Sucesso |
|---|---|---|---|
| POST | `/produtos` | `{nome, descricao, preco}` | 201, produto |
| GET | `/produtos` | — | 200, lista de ativos |
| GET | `/produtos/{id}` | — | 200, produto ativo |
| PUT | `/produtos/{id}` | `{nome, descricao, preco}` | 200, produto atualizado |
| DELETE | `/produtos/{id}` | — | 204, corpo vazio |
| POST | `/estoques` | `{produtoId, quantidadeInicial}` | 201, estoque |
| GET | `/estoques/{produtoId}` | — | 200, estoque |
| POST | `/estoques/{produtoId}/movimentacoes` | `{tipo, quantidade}` | 200, estoque atualizado |
| POST | `/pedidos` | `{"itens":[{"produtoId":1,"quantidade":2}]}` | 201, pedido |
| GET | `/pedidos` | — | 200, lista de pedidos |
| GET | `/pedidos/{pedidoId}` | — | 200, pedido |
| POST | `/pedidos/{pedidoId}/itens` | `{produtoId, quantidade}` | 200, pedido atualizado |
| PUT | `/pedidos/{pedidoId}/itens/{produtoId}` | `{quantidade}` | 200, pedido atualizado |
| DELETE | `/pedidos/{pedidoId}/itens/{produtoId}` | — | 200, pedido atualizado |
| POST | `/pedidos/{pedidoId}/cancelamento` | — | 200, pedido cancelado |

**Produto** expõe `id`, `nome`, `descricao`, `preco` e `ativo`. O nome é obrigatório, com até 120 caracteres. A descrição é opcional, com até 500. O preço é positivo e representado por `BigDecimal` com até duas casas decimais, armazenado como `DECIMAL(19,2)`. O produto nasce ativo e a exclusão lógica o retira da consulta e da listagem.

**Estoque** expõe somente `produtoId` e `quantidadeDisponivel`. O cadastro exige produto ativo e quantidade inicial inteira a partir de zero. Movimentações `ENTRADA` e `SAIDA` exigem quantidade inteira positiva. Uma saída pode zerar o saldo. O estoque continua consultável e movimentável após desativação do produto. O banco armazena o saldo atual.

**Pedido** expõe `id`, `status`, `criadoEm`, `itens` e `total`. Cada item contém exclusivamente `produtoId`, `nomeProduto`, `quantidade`, `precoUnitario` e `subtotal`. Um pedido nasce `ABERTO`, com ao menos um item, quantidades inteiras positivas e produtos distintos. A inclusão exige produto ativo e captura nome e preço. A alteração de quantidade conserva esses dados. O subtotal multiplica quantidade por preço unitário e o total soma os subtotais. O cancelamento mantém o pedido e seus itens consultáveis. O status `CANCELADO` impede edições e novo cancelamento.

Pedidos operam independentemente da existência e do saldo de estoque. Criar, editar ou cancelar um pedido preserva o saldo. Um pedido com qualquer item inválido é rejeitado integralmente.

## Erros e verificação

| Status | Situações |
|---|---|
| 400 | JSON ou parâmetro inválido, campo obrigatório ausente, itens vazios, quantidade inválida, preço inválido ou tamanho excedido. |
| 404 | Produto ativo, estoque, pedido ou item solicitado não encontrado. |
| 409 | Estoque duplicado ou insuficiente, conflito de concorrência, produto duplicado no pedido, remoção do último item, alteração de pedido cancelado ou novo cancelamento. |

Falhas de negócio retornam um objeto como `{"erro":"Estoque insuficiente."}`. A validação da entrada utiliza Bean Validation e Spring MVC.

A suíte inclui testes unitários de domínio e serviço, testes HTTP com MockMvc e H2 e testes ArchUnit. A verificação de concorrência usa duas transações independentes sobre a mesma versão de estoque. Cenários de integração verificam a preservação do saldo, a independência entre pedido e estoque e a captura do nome e preço na inclusão.
