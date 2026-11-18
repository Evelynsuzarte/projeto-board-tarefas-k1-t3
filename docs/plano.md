# Plano de Implementação (`docs/plano.md`)

Este plano divide a implementação das duas features especificadas em [docs/spec.md](spec.md) em tarefas pequenas, numeradas e com critérios de validação, respeitando as convenções e restrições de [AGENTS.md](../AGENTS.md).

---

## Parte A — Feature 1: API HTTP Local e Sincronização Frontend/Backend

- [x] **1. Preparar `TarefaRepository` para concorrência e arquivo vazio/ausente**
  - **O que será feito:** Tornar `carregar()` e `salvar(List<Tarefa>)` `synchronized` e ignorar linhas em branco ao ler `src/main/resources/historico.txt`, retornando lista vazia sem exceção quando o arquivo não existir ou estiver vazio (mantendo o formato `;`, `dd/MM/yyyy` e a ordenação por prioridade decrescente).
  - **Arquivos criados ou alterados:**
    - Alterar: `src/main/java/boardTarefas/repository/TarefaRepository.java`
  - **Como validar:**
    - Executar `.\gradlew.bat classes` para garantir compilação sem erros.
    - Conferir nos testes unitários da Tarefa 5 que um arquivo vazio ou ausente retorna `[]` sem lançar exceção.

- [x] **2. Adequar `TarefaController` para reuso pela API HTTP com a mesma lógica do terminal**
  - **O que será feito:**
    - Substituir comparações de `String` com `==` por `.equals(...)` em `buscar(...)` e `alterar(...)` para funcionar igualmente com entradas do `Scanner` e da API HTTP.
    - Adicionar helper de sanitização de texto (`;` → `,` e `\r`/`\n` → espaço) aplicado na criação e alteração.
    - Garantir operações que recarreguem o estado mais recente de `historico.txt` antes de criar, alterar, deletar ou buscar via API, mantendo intactas as assinaturas usadas por `boardTarefas.main`.
  - **Arquivos criados ou alterados:**
    - Alterar: `src/main/java/boardTarefas/controller/TarefaController.java`
  - **Como validar:**
    - Executar `.\gradlew.bat classes`.
    - Rodar `java -cp build/classes/java/main boardTarefas.main` manualmente e testar listar, pesquisar, criar, alterar e apagar para confirmar que o modo console continua idêntico.

- [x] **3. Criar `boardTarefas.Server` com API REST/JSON, CORS e arquivos estáticos**
  - **O que será feito:**
    - Criar `boardTarefas.Server` usando `com.sun.net.httpserver.HttpServer` na porta `8080`.
    - Implementar parser/serializer JSON nativo (com `dataTermino` em `yyyy-MM-dd` e escape de caracteres) e cabeçalhos CORS (`*`, `GET, POST, PUT, DELETE, OPTIONS`).
    - Implementar os endpoints `/api/tarefas` (`GET`, `POST`, `OPTIONS`) e `/api/tarefas/{id}` (`PUT`, `DELETE`, `OPTIONS`) delegando toda a regra de negócio para `TarefaController` (`carregar`, `buscar`, `proximoId`, `criar`, `alterar`, `deletar`), retornando `200`, `201`, `204`, `400` (campos obrigatórios/formatos inválidos) e `404` (ID inexistente).
    - Servir os arquivos estáticos de `src/main/resources/static/` (`/` → `html/index.html`, `/html/*`, `/css/*`, `/js/*`, `/images/*`) com proteção contra path traversal.
  - **Arquivos criados ou alterados:**
    - Criar: `src/main/java/boardTarefas/Server.java`
  - **Como validar:**
    - Compilar e subir o servidor: `.\gradlew.bat classes` e `java -cp build/classes/java/main boardTarefas.Server`.
    - Testar manualmente via `curl` ou PowerShell (`Invoke-RestMethod`) os fluxos `GET`, `POST`, `PUT`, `DELETE`, sanitização de `;`, erro `400` e `404`, verificando a atualização imediata em `src/main/resources/historico.txt`.

- [x] **4. Integrar `script.js` à API HTTP (substituindo `localStorage` na camada de dados)**
  - **O que será feito:**
    - Configurar `API_BASE_URL` (`/api/tarefas` na porta `8080` e fallback `http://localhost:8080/api/tarefas` em `file://` ou outra porta).
    - Substituir `localStorage` (`"meuBoard"`) e `tarefasPadrao` por chamadas `fetch` (`async/await`) para listar (`GET`), criar (`POST`), editar (`PUT`) e excluir (`DELETE`).
    - Adicionar banner de feedback quando o servidor estiver offline (`"Servidor offline — não foi possível sincronizar"`), bloqueando ações destrutivas/envios, tratando `404` com recarga da tabela e desabilitando botões durante requisições para evitar múltiplos cliques.
  - **Arquivos criados ou alterados:**
    - Alterar: `src/main/resources/static/js/script.js`
    - Alterar (pontualmente, estilos do banner de status/erro): `src/main/resources/static/css/style.css`
  - **Como validar:**
    - Com `boardTarefas.Server` rodando, abrir `http://localhost:8080/`, criar uma tarefa em `criar-tarefa.html`, editar/excluir em `editar-tarefas.html` e conferir no console (`boardTarefas.main`) que `historico.txt` reflete tudo.
    - Desligar o servidor, abrir `editar-tarefas.html` e confirmar que o banner de servidor offline aparece e não injeta `tarefasPadrao`.

- [ ] **5. Criar testes automatizados JUnit 6 para o Backend**
  - **O que será feito:** Criar suíte de testes unitários (usando diretório/arquivo temporário para não alterar `src/main/resources/historico.txt`) cobrindo:
    - Sanitização de `;` e `\n` e validações de campos (`status`, `prioridade`, `dataTermino`).
    - Serialização/desserialização JSON (incluindo `LocalDate` ↔ `yyyy-MM-dd`).
    - CRUD, geração de `proximoId`, filtros de `buscar(...)` e ordenação por prioridade decrescente em `TarefaController`/`TarefaRepository`.
  - **Arquivos criados ou alterados:**
    - Criar: `src/test/java/boardTarefas/TarefaControllerTest.java` (e testes auxiliares para o JSON do `Server`, se aplicável)
  - **Como validar:**
    - Executar `.\gradlew.bat test` e confirmar que todos os testes passam (`BUILD SUCCESSFUL` sem `NO-SOURCE`).

---

## Parte B — Feature 2: Alarme de Proximidade do Prazo das Tarefas (Frontend)

- [ ] **6. Atualizar formulários HTML para prazo com data/hora e campos de alarme**
  - **O que será feito:**
    - Em `criar-tarefa.html`: trocar `<input type="date">` por `<input type="datetime-local">` e adicionar checkbox/switch para ativar alarme e `<select>` de antecedência (`15 min`, `30 min`, `1h`, `2h` [padrão], `24h`).
    - Em `editar-tarefas.html`: adicionar colunas/controles na tabela para ativar/desativar alarme e alterar a antecedência em linha, compatível com `datetime-local`.
  - **Arquivos criados ou alterados:**
    - Alterar: `src/main/resources/static/html/criar-tarefa.html`
    - Alterar: `src/main/resources/static/html/editar-tarefas.html`
    - Alterar (se necessário para alinhar novos campos): `src/main/resources/static/css/style.css`
  - **Como validar:**
    - Abrir `criar-tarefa.html` e `editar-tarefas.html` no navegador e verificar visualmente se todos os campos renderizam sem quebrar o layout nem os filtros por `status`.

- [ ] **7. Implementar lógica de alarme, verificação periódica e painel visual no `script.js`**
  - **O que será feito:**
    - Estender o modelo de tarefa no frontend com `alarme` (`boolean`), `antecedenciaMinutos` (`number`, padrão `120`) e `alarmeDisparado` (`boolean`), assumindo `alarme: false` para tarefas antigas sem esses campos.
    - Na edição em linha (`editar-tarefas.html`), resetar `alarmeDisparado` para `false` quando o prazo (`dataTermino`) ou a configuração do alarme forem alterados.
    - Implementar função de verificação executada ao carregar a página e periodicamente via `setInterval` (a cada 30s/1min): dispara quando `alarme === true`, `status !== "done"`, `!alarmeDisparado` e `agora >= (dataTermino - antecedenciaMinutos)`.
    - Exibir os alarmes disparados em um painel/banner visual na página e marcar `alarmeDisparado = true` para evitar disparos repetidos.
    - Garantir compatibilidade de `dataTermino` (`datetime-local`) com o envio à API da Feature 1 (ex.: extraindo `yyyy-MM-dd` para o backend atual enquanto os campos de alarme permanecem no cliente).
  - **Arquivos criados ou alterados:**
    - Alterar: `src/main/resources/static/js/script.js`
    - Alterar (estilo do painel de alertas de alarme): `src/main/resources/static/css/style.css`
  - **Como validar:**
    - Conferir manualmente os 3 cenários de aceite no navegador:
      1. Criar tarefa com prazo próximo (dentro da janela de antecedência) e alarme ativado → painel de alerta é exibido ao carregar / no ciclo do `setInterval` e não repete após disparar.
      2. Marcar tarefa com alarme ativo como `"done"` antes do disparo → nenhum alerta é exibido.
      3. Criar tarefa com alarme desativado → salva normalmente sem exibir alertas.
