# API de Chamados de Suporte Técnico

Desenvolvimento de uma API REST para Gerenciamento de Chamados
Disciplina: Linguagens para Aplicação Comercial
Professor: Joselito Júnior
Tecnologias: Java, Spring Boot e JSON

**Equipe:** Letícia Carneiro de Lemos

## Como executar

```bash
./mvnw spring-boot:run
# API em http://localhost:8080 
./testes.sh        # executa todas as requisições de demonstração
```

## Modelo

`Chamado`: `id`, `titulo`, `solicitante`, `setor`, `prioridade` (BAIXA | MEDIA | ALTA),
`status` (ABERTO | EM_ANDAMENTO | RESOLVIDO). A aplicação começa com 12 chamados fictícios.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/chamados` | Lista todos |
| GET | `/chamados/{id}` | Consulta por ID (404 se não existir) |
| GET | `/chamados/setor?nome=financeiro` | Filtra por setor (ignora maiúsculas) |
| GET | `/chamados/prioridade?nivel=alta` | Filtra por prioridade (400 se inválida) |
| GET | `/chamados/busca?texto=impressora` | Busca no título (ignora maiúsculas) |
| GET | `/chamados/criticos` | Prioridade ALTA e status diferente de RESOLVIDO |
| GET | `/chamados/resumo` | Quantidades por status e total |
| POST | `/chamados` | Cadastra; gera ID; responde **201** |


```json
{ "titulo": "Teclado sem resposta", "solicitante": "Maria Silva", "setor": "Juridico", "prioridade": "MEDIA" }
```

## Evidências dos testes

Resultados esperados (com os dados iniciais):

| # | Requisição | Esperado |
|---|---|---|
| 1 | `GET /chamados` | 200, 12 chamados |
| 2 | `GET /chamados/1` | 200, chamado da impressora do financeiro |
| 3 | `GET /chamados/999` | 404 (sem correspondência) |
| 4 | `GET /chamados/setor?nome=financeiro` | 200, 2 chamados (ids 1 e 5) |
| 5 | `GET /chamados/prioridade?nivel=alta` | 200, 5 chamados (1, 2, 6, 9, 11) |
| 6 | `GET /chamados/busca?texto=IMPRESSORA` | 200, 3 chamados (1, 7, 11) |
| 7 | `GET /chamados/busca?texto=satelite` | 200, `[]` (sem correspondência) |
| 8 | `GET /chamados/criticos` | 200, 4 chamados (1, 2, 6, 11); o 9 é ALTA mas RESOLVIDO |
| 9 | `GET /chamados/resumo` | `{"abertos":5,"emAndamento":3,"resolvidos":4,"total":12}` |
| 10 | `POST /chamados` | 201, chamado com `id` 13 |
| 11 | `GET /chamados/13` | 200, o chamado cadastrado |
| 12 | `GET /chamados/resumo` | abertos 6, total 13 |


Todos os testes foram executados com a API recém-iniciada (`./mvnw spring-boot:run`), usando o script `bash testes.sh > evidencias.txt`, que dispara as requisições com `curl`. O resultado ficou salvo no arquivo `evidencias.txt`.

### Teste 1 – Listar todos os chamados

**Requisição**
```bash
curl -i http://localhost:8080/chamados
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},{"id":2,"titulo":"Computador nao liga apos queda de energia","solicitante":"Bruno Lima","setor":"RH","prioridade":"ALTA","status":"EM_ANDAMENTO"},{"id":3,"titulo":"Solicitar acesso ao sistema de vendas","solicitante":"Carla Dias","setor":"Comercial","prioridade":"MEDIA","status":"ABERTO"},{"id":4,"titulo":"Wi-Fi instavel na sala de reunioes","solicitante":"Daniel Rocha","setor":"Marketing","prioridade":"MEDIA","status":"EM_ANDAMENTO"},{"id":5,"titulo":"Redefinir senha da conta de e-mail","solicitante":"Elisa Prado","setor":"Financeiro","prioridade":"BAIXA","status":"RESOLVIDO"},{"id":6,"titulo":"Servidor de arquivos fora do ar","solicitante":"Fabio Nunes","setor":"TI","prioridade":"ALTA","status":"ABERTO"},{"id":7,"titulo":"Trocar toner da impressora do RH","solicitante":"Gabriela Melo","setor":"RH","prioridade":"BAIXA","status":"RESOLVIDO"},{"id":8,"titulo":"Instalar planilhas no notebook novo","solicitante":"Heitor Alves","setor":"Logistica","prioridade":"BAIXA","status":"ABERTO"},{"id":9,"titulo":"VPN nao conecta para trabalho remoto","solicitante":"Isabela Costa","setor":"Comercial","prioridade":"ALTA","status":"RESOLVIDO"},{"id":10,"titulo":"Monitor com tela piscando","solicitante":"Joao Pereira","setor":"Logistica","prioridade":"MEDIA","status":"RESOLVIDO"},{"id":11,"titulo":"Impressora de etiquetas sem conexao de rede","solicitante":"Karen Martins","setor":"Logistica","prioridade":"ALTA","status":"EM_ANDAMENTO"},{"id":12,"titulo":"Criar conta de acesso para estagiario","solicitante":"Lucas Barros","setor":"Marketing","prioridade":"BAIXA","status":"ABERTO"}]
```

**Explicação:** a API iniciou com 12 chamados em memória e devolveu todos em um array JSON, com status 200 OK.

---

### Teste 2 – Consultar chamado por ID existente

**Requisição**
```bash
curl -i http://localhost:8080/chamados/1
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"}
```

**Explicação:** o valor `1` da URL foi capturado por `@PathVariable` e a API devolveu apenas o chamado de id 1.

---

### Teste 3 – Consultar chamado por ID inexistente

**Requisição**
```bash
curl -i http://localhost:8080/chamados/999
```

**Resposta**
```http
HTTP/1.1 404
Content-Type: application/json

{"timestamp":"2026-11-10T20:35:42.680Z","status":404,"error":"Not Found","path":"/chamados/999"}
```

**Explicação:** não existe chamado com id 999. A API lança `ResponseStatusException` e responde 404 Not Found, indicando que o recurso não existe.

---

### Teste 4 – Filtrar por setor

**Requisição**
```bash
curl -i "http://localhost:8080/chamados/setor?nome=financeiro"
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},{"id":5,"titulo":"Redefinir senha da conta de e-mail","solicitante":"Elisa Prado","setor":"Financeiro","prioridade":"BAIXA","status":"RESOLVIDO"}]
```

**Explicação:** o parâmetro foi enviado em minúsculas (`financeiro`), mas o setor está cadastrado como "Financeiro". Os chamados 1 e 5 foram retornados porque a comparação ignora maiúsculas e minúsculas.

---

### Teste 5 – Filtrar por prioridade

**Requisição**
```bash
curl -i "http://localhost:8080/chamados/prioridade?nivel=alta"
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},{"id":2,"titulo":"Computador nao liga apos queda de energia","solicitante":"Bruno Lima","setor":"RH","prioridade":"ALTA","status":"EM_ANDAMENTO"},{"id":6,"titulo":"Servidor de arquivos fora do ar","solicitante":"Fabio Nunes","setor":"TI","prioridade":"ALTA","status":"ABERTO"},{"id":9,"titulo":"VPN nao conecta para trabalho remoto","solicitante":"Isabela Costa","setor":"Comercial","prioridade":"ALTA","status":"RESOLVIDO"},{"id":11,"titulo":"Impressora de etiquetas sem conexao de rede","solicitante":"Karen Martins","setor":"Logistica","prioridade":"ALTA","status":"EM_ANDAMENTO"}]
```

**Explicação:** o texto `alta` foi convertido para o enum `ALTA`. A API retornou os 5 chamados de alta prioridade (ids: 1, 2, 6, 9 e 11), independentemente do status.

---

### Teste 6 – Buscar pelo título sem diferenciar maiúsculas

**Requisição**
```bash
curl -i "http://localhost:8080/chamados/busca?texto=IMPRESSORA"
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},{"id":7,"titulo":"Trocar toner da impressora do RH","solicitante":"Gabriela Melo","setor":"RH","prioridade":"BAIXA","status":"RESOLVIDO"},{"id":11,"titulo":"Impressora de etiquetas sem conexao de rede","solicitante":"Karen Martins","setor":"Logistica","prioridade":"ALTA","status":"EM_ANDAMENTO"}]
```

**Explicação:** o termo foi digitado em maiúsculas, mas encontrou títulos com "Impressora" e "impressora". Isso confirma que a busca não diferencia maiúsculas de minúsculas. Chamados retornados: 1, 7 e 11.

---

### Teste 7 – Busca sem correspondência

**Requisição**
```bash
curl -i "http://localhost:8080/chamados/busca?texto=satelite"
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[]
```

**Explicação:** nenhum título contém "satelite". A consulta foi válida, então a API responde 200 com uma lista vazia, em vez de erro.

---

### Teste 8 – Chamados críticos

**Requisição**
```bash
curl -i http://localhost:8080/chamados/criticos
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"titulo":"Impressora do financeiro nao imprime","solicitante":"Ana Souza","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},{"id":2,"titulo":"Computador nao liga apos queda de energia","solicitante":"Bruno Lima","setor":"RH","prioridade":"ALTA","status":"EM_ANDAMENTO"},{"id":6,"titulo":"Servidor de arquivos fora do ar","solicitante":"Fabio Nunes","setor":"TI","prioridade":"ALTA","status":"ABERTO"},{"id":11,"titulo":"Impressora de etiquetas sem conexao de rede","solicitante":"Karen Martins","setor":"Logistica","prioridade":"ALTA","status":"EM_ANDAMENTO"}]
```

**Explicação:** o endpoint combina prioridade ALTA com status diferente de RESOLVIDO. Chamados retornados: 1, 2, 6 e 11. O chamado 9 tem prioridade ALTA, mas já está resolvido, por isso ficou de fora.

---

### Teste 9 – Resumo por status

**Requisição**
```bash
curl -i http://localhost:8080/chamados/resumo
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

{"abertos":5,"emAndamento":3,"resolvidos":4,"total":12}
```

**Explicação:** a API conta os chamados de cada status e o total geral. Os dados iniciais têm 5 abertos, 3 em andamento e 4 resolvidos, somando 12.

---

### Teste 10 – Cadastrar novo chamado

**Requisição**
```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Teclado sem resposta","solicitante":"Maria Silva","setor":"Juridico","prioridade":"media"}'
```

**Resposta**
```http
HTTP/1.1 201
Location: /chamados/13
Content-Type: application/json

{"id":13,"titulo":"Teclado sem resposta","solicitante":"Maria Silva","setor":"Juridico","prioridade":"MEDIA","status":"ABERTO"}
```

**Explicação:** o JSON enviado foi convertido em objeto Java por `@RequestBody`. A API gerou o id 13, normalizou a prioridade `media` para `MEDIA`, assumiu o status `ABERTO` (não informado) e respondeu 201 Created, com o cabeçalho `Location` apontando para o novo recurso.

---

### Teste 11 – Verificar o chamado cadastrado

**Requisição**
```bash
curl -i http://localhost:8080/chamados/13
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

{"id":13,"titulo":"Teclado sem resposta","solicitante":"Maria Silva","setor":"Juridico","prioridade":"MEDIA","status":"ABERTO"}
```

**Explicação:** a consulta pelo id 13 comprova que o chamado cadastrado no teste anterior ficou armazenado na lista em memória e pode ser recuperado.

---

### Teste 12 – Resumo após o cadastro

**Requisição**
```bash
curl -i http://localhost:8080/chamados/resumo
```

**Resposta**
```http
HTTP/1.1 200
Content-Type: application/json

{"abertos":6,"emAndamento":3,"resolvidos":4,"total":13}
```

**Explicação:** em comparação com o Teste 9, os abertos passaram de 5 para 6 e o total de 12 para 13, refletindo o novo chamado.

---

### Teste 13 – Cadastro inválido (sem título)

**Requisição**
```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{"solicitante":"Maria","setor":"TI","prioridade":"ALTA"}'
```

**Resposta**
```http
HTTP/1.1 400
Content-Type: application/json

{"timestamp":"2026-11-10T20:35:42.828Z","status":400,"error":"Bad Request","path":"/chamados"}
```

**Explicação:** o campo obrigatório `titulo` não foi enviado. A API rejeita a requisição com 400 Bad Request e não cria nenhum chamado.

---

### Teste 14 – Prioridade inválida

**Requisição**
```bash
curl -i "http://localhost:8080/chamados/prioridade?nivel=urgente"
```

**Resposta**
```http
HTTP/1.1 400
Content-Type: application/json

{"timestamp":"2026-11-10T20:35:42.839Z","status":400,"error":"Bad Request","path":"/chamados/prioridade"}
```

**Explicação:** `urgente` não é um valor válido (os aceitos são BAIXA, MEDIA e ALTA). A API responde 400 Bad Request.

## Análise técnica

**Como o Spring Boot recebe e processa requisições HTTP.**
Ao executar `SpringApplication.run`, o Spring Boot cria o contexto da aplicação e sobe um servidor web embutido na porta 8080. Cada requisição chega ao `DispatcherServlet`, que consulta os mapeamentos (`@GetMapping`, `@PostMapping`...) para achar o método do Controller correspondente ao verbo e à URL. Ele resolve os parâmetros do método (`@PathVariable`, `@RequestParam`, `@RequestBody`), invoca o método e transforma o retorno na resposta HTTP (status, cabeçalhos e corpo). Se nenhuma rota corresponde, o resultado é 404.

**Função do Controller.**
O Controller (`ChamadoController`, anotado com `@RestController`) é a camada de entrada web: expõe os endpoints, recebe os dados da requisição, aplica as regras do enunciado (filtros, validação do POST,
geração de ID) e devolve o resultado com o status HTTP adequado (200, 201, 400, 404). Como o
enunciado dispensa Service e Repository, ele também guarda a coleção em memória.

**Conversão entre objetos Java e JSON.**
O Spring usa a biblioteca Jackson (via `HttpMessageConverter`). Na saída, os `record`s retornados (como `Chamado` e `List<Chamado>`) são serializados para JSON, com enums escritos como texto. Na entrada, o corpo JSON do POST é desserializado em um `NovoChamado` por causa do `@RequestBody`, casando os nomes dos campos com os componentes do record. O cabeçalho `Content-Type: application/json` indica o formato do corpo.


**Por que os dados não permanecem após reiniciar.**
Os chamados ficam apenas em uma coleção Java (`CopyOnWriteArrayList`) na memória heap da JVM. Quando a aplicação é encerrada, o processo e sua memória são descartados; o construtor recria somente os 12 chamados iniciais após reiniciar e o contador de IDs volta a 1. Persistir os dados exigiria um banco ou outro armazenamento externo.