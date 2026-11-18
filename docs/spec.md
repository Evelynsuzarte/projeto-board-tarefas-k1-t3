# Especificação Técnica (`docs/spec.md`)

Este documento especifica as duas próximas features do **Board de Tarefas (To-Do List)**:
1. **Feature 1:** API HTTP Local e Sincronização Frontend/Backend (`historico.txt` como fonte única de verdade).
2. **Feature 2:** Alarme de Proximidade do Prazo das Tarefas (Frontend).

---

## Feature 1: API HTTP Local e Sincronização Frontend/Backend

### 1.1. Contexto e Objetivo

Atualmente, o sistema opera com duas interfaces isoladas:
- **Aplicação Console (Java):** persiste os dados em `src/main/resources/historico.txt` (delimitado por `;` e datas em `dd/MM/yyyy`).
- **Interface Web (Frontend):** persiste os dados exclusivamente no navegador via `localStorage` (chave `"meuBoard"`, com datas no padrão ISO `yyyy-MM-dd`).

**Objetivo:** Expor a camada de regras de negócio do backend Java (`TarefaController` e `TarefaRepository`) por meio de uma **API HTTP local (REST/JSON)** e integrar o frontend (`src/main/resources/static/js/script.js`) a essa API. Assim, tanto o terminal quanto o navegador passam a consumir e atualizar **a mesma fonte única de verdade** (`src/main/resources/historico.txt`).

### 1.2. Decisões Alinhadas e Regras de Negócio

1. **Fonte Única de Verdade:** Toda operação de leitura, criação, edição e exclusão iniciada pela interface web é processada pelo backend Java e persistida imediatamente em `src/main/resources/historico.txt`.
2. **Mesma Lógica de Funcionamento do Terminal:**
   - A camada HTTP (`boardTarefas.Server`) atua exclusivamente como adaptador HTTP/JSON, delegando todas as operações aos mesmos fluxos e métodos de `TarefaController` e `TarefaRepository` utilizados por `boardTarefas.main` (sem duplicar regras de negócio):
     - **Carregamento e ordenação:** `carregar()` lê `historico.txt` e ordena por `prioridade` decrescente.
     - **Filtros:** Reaproveitamento de `TarefaController.buscar(tarefas, filtro, opcao, inicio, fim)` (por `status`, `categoria` ou `prioridade`, com intervalo opcional de datas) e `buscar_id(id, tarefas)`.
     - **Criação:** Geração do próximo ID sequencial via `TarefaController.proximoId(tarefas)` e persistência via `criar(...)`.
     - **Alteração e exclusão:** Uso de `alterar(...)` e `deletar(...)` do `TarefaController`.
     - **Compatibilidade de comparação de strings:** No `TarefaController`, comparações atualmente feitas com `==` (como `filtro == "status"`, `t.getStatus() == opcao` e `campo == "..."`) devem usar `.equals(...)` para garantir comportamento idêntico entre entradas vindas do `Scanner` (terminal) e strings vindas da API HTTP.
3. **Ponto de Entrada Dedicado e Preservação do Terminal:**
   - Nova classe dedicada `boardTarefas.Server` (`src/main/java/boardTarefas/Server.java`) utilizando `com.sun.net.httpserver.HttpServer` nativo do JDK 21 (sem frameworks externos).
   - A classe `boardTarefas.main` permanece intacta para uso via terminal (`java -cp build/classes/java/main boardTarefas.main`).
4. **Porta Padrão, Arquivos Estáticos e Resolução de URL:**
   - O `Server` sobe na porta padrão `8080` e também serve os arquivos estáticos de `src/main/resources/static/` (`/` redireciona ou serve `/html/index.html`).
   - No `script.js`, quando aberto na mesma origem (`http://localhost:8080`), utiliza `/api/tarefas`; quando aberto via `file://` ou outra porta (ex.: Live Server na `5500`), utiliza fallback `http://localhost:8080/api/tarefas`.
   - `editar-tarefas.html` permanece como a tela principal de listagem e edição (`listar-tarefas.html` não é utilizada no fluxo ativo).
5. **Mapeamento e Conversão de Datas:**
   - O backend mantém `LocalDate` e persiste `dd/MM/yyyy` em `historico.txt`.
   - Na comunicação JSON com o frontend, `dataTermino` é serializada e desserializada no formato ISO `yyyy-MM-dd`.
6. **Contrato de Campos e Status:**
   - Payload JSON: `id` (inteiro), `nome` (string), `descricao` (string), `dataTermino` (`yyyy-MM-dd`), `prioridade` (inteiro), `categoria` (string) e `status` (`"todo"`, `"doing"`, `"done"`).
7. **Casos de Borda:**
   - **Sanitização e validação:** Caracteres `;` e quebras de linha (`\r`, `\n`) em `nome`, `descricao` e `categoria` são sanitizados automaticamente no backend (`;` → `,`, quebras de linha → espaço) antes de gravar em `historico.txt`. Retornar `HTTP 400 Bad Request` para campos obrigatórios ausentes ou formatos inválidos (ex.: `prioridade` não numérica, `status` fora de `todo|doing|done`, `dataTermino` inválida).
   - **ID inexistente:** `PUT /api/tarefas/{id}` ou `DELETE /api/tarefas/{id}` para ID não encontrado retornam `HTTP 404 Not Found`; o frontend exibe mensagem informativa e recarrega a tabela.
   - **Arquivo `historico.txt` vazio ou ausente:** O backend retorna `[]` com `HTTP 200 OK` sem lançar exceção, e o frontend exibe estado vazio (sem injetar `tarefasPadrao`).
   - **Concorrência simples (Console + Web):** Métodos `carregar()` e `salvar(...)` em `TarefaRepository` sincronizados (`synchronized`), com releitura de `historico.txt` antes de operações de leitura/mutação pela API para evitar sobrescrita entre terminal e web.
   - **Backend indisponível (offline):** Em falhas de rede no `fetch`, o frontend exibe feedback visual claro (`"Servidor offline — não foi possível sincronizar"`) e bloqueia ações destrutivas ou dessincronizadas, sem resetar para `tarefasPadrao`.
   - **CORS:** Respostas `/api/*` incluem `Access-Control-Allow-Origin: *`, `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS` e `Access-Control-Allow-Headers: Content-Type` (respondendo `204 No Content` para `OPTIONS`).

### 1.3. Critérios de Aceite (Feature 1)

- **Cenário 1: Listagem sincronizada entre Console e Web**
  - **Dado** que existem tarefas salvas em `src/main/resources/historico.txt` (criadas via console Java ou manualmente)
  - **Quando** o usuário inicia o servidor backend (`boardTarefas.Server`) e acessa `editar-tarefas.html`
  - **Então** a interface web deve exibir exatamente as tarefas contidas em `historico.txt`, ordenadas por prioridade decrescente.

- **Cenário 2: Criação de tarefa pela Web refletida no Console**
  - **Dado** que o servidor backend está em execução e o usuário está na página `criar-tarefa.html`
  - **Quando** o usuário preenche `nome`, `descricao`, `dataTermino`, `prioridade`, `categoria` e `status` e submete o formulário
  - **Então** o frontend envia `POST /api/tarefas`, o backend gera o próximo ID sequencial via `TarefaController`, grava a nova linha no formato `id;nome;descricao;dd/MM/yyyy;status;prioridade;categoria` em `historico.txt` e a tarefa passa a aparecer tanto na web quanto ao listar no console Java.

- **Cenário 3: Edição e exclusão em linha na Web**
  - **Dado** que o usuário está em `editar-tarefas.html` visualizando uma tarefa existente
  - **Quando** o usuário altera o `status` ou outro campo e clica em `Salvar` (ou clica em `Excluir` e confirma)
  - **Então** o frontend envia `PUT /api/tarefas/{id}` (ou `DELETE /api/tarefas/{id}`), o `historico.txt` é atualizado imediatamente e a interface reflete o estado persistido sem recorrer ao `localStorage`.

- **Cenário 4: Sanitização de delimitador `;`**
  - **Dado** que o usuário tenta cadastrar ou editar uma tarefa contendo ponto e vírgula (`;`) na descrição pelo formulário web
  - **Quando** a requisição chega ao backend Java
  - **Então** o backend sanitiza o caractere (`;` → `,`) antes de persistir em `historico.txt`, garantindo que a leitura futura do arquivo não quebre o parsing das colunas.

- **Cenário 5: Tratamento de backend indisponível**
  - **Dado** que o servidor HTTP Java está desligado
  - **Quando** o usuário abre qualquer página HTML do board e tenta carregar ou salvar tarefas
  - **Então** a interface exibe uma mensagem amigável de erro de conexão com o servidor e não sobrescreve dados com mocks locais.

### 1.4. Impacto no que já existe (Feature 1)

- `src/main/java/boardTarefas/Server.java` (novo): Servidor HTTP + roteamento `/api/tarefas` + servidor de arquivos estáticos + serialização/desserialização JSON.
- `src/main/java/boardTarefas/controller/TarefaController.java`: Sanitização de `;`/`\n`, uso de `.equals(...)` em comparações de `String` e execução sobre estado atualizado do arquivo.
- `src/main/java/boardTarefas/repository/TarefaRepository.java`: Sincronização (`synchronized`) e tratamento seguro de arquivo ausente/vazio.
- `src/main/resources/static/js/script.js`: Substituição de `localStorage` (`"meuBoard"`) e `tarefasPadrao` por chamadas `fetch` (`async/await`), tratamento de erros/offline e bloqueio de múltiplos cliques durante requisições.
- `src/test/java/boardTarefas/`: Adição de testes JUnit Jupiter cobrindo JSON, sanitização, validações e regras do controller/repository.

### 1.5. Fora do Escopo (Feature 1)

- Autenticação, login, tokens JWT ou separação por usuário.
- Migração de `historico.txt` para banco de dados relacional ou NoSQL.
- Uso de frameworks externos (Spring Boot, React, Angular, Vue).
- Atualizações em tempo real via WebSockets ou SSE.
- Criação de novas páginas ou redesign do `style.css` (apenas mensagens pontuais de feedback/erro).

---

## Feature 2: Alarme de Proximidade do Prazo das Tarefas (Frontend)

### 2.1. Contexto e Objetivo

Implementar um sistema de alarme na interface web para alertar o usuário quando uma tarefa pendente estiver próxima do prazo final, permitindo definir com quanto tempo de antecedência o alarme deve ser disparado.

> **Escopo atual:** Como o frontend e o backend ainda não estão integrados no momento desta especificação, esta feature será implementada **apenas no frontend**.

### 2.2. Decisões Alinhadas e Regras de Negócio

1. **Ativação e Configuração do Alarme:**
   - Ao criar (`criar-tarefa.html`) ou editar (`editar-tarefas.html`) uma tarefa, o usuário pode optar por ativar ou desativar o alarme (checkbox/switch).
   - O usuário pode definir a janela de antecedência em relação ao prazo final por meio de opções pré-definidas (ex.: `15 min`, `30 min`, `1h`, `2h` [padrão = `120` minutos], `24h`) ou campo numérico equivalente.
2. **Prazo com Data e Hora:**
   - Substituir `<input type="date">` por `<input type="datetime-local">` em `criar-tarefa.html` e `editar-tarefas.html` para permitir o cálculo preciso da antecedência em horas/minutos.
3. **Verificação e Disparo:**
   - A verificação é executada ao carregar a página e periodicamente em background via `setInterval` (a cada 30s ou 1 minuto) enquanto a página estiver aberta.
   - Regra de disparo: dispara quando `alarme === true`, `status !== "done"`, `alarmeDisparado === false` e `agora >= (dataTermino - antecedenciaMinutos)`.
4. **Tarefas Concluídas:**
   - Tarefas com status `"done"` (Concluída) **não** devem disparar alarmes.
5. **Controle de Múltiplos Disparos e Reconfiguração:**
   - Uma vez disparado, `alarmeDisparado` é marcado como `true` para evitar múltiplos disparos repetitivos.
   - Em `editar-tarefas.html`, se o usuário alterar o prazo (`dataTermino`) ou reconfigurar o alarme/antecedência, `alarmeDisparado` é resetado para `false`.
6. **Exibição Visual:**
   - Os alarmes disparados são exibidos em um banner/painel visual de alertas na própria página.
7. **Retrocompatibilidade:**
   - Tarefas já existentes sem os novos atributos de alarme devem assumir `alarme: false`, `antecedenciaMinutos: 120` e `alarmeDisparado: false` por padrão.

### 2.3. Critérios de Aceite (Feature 2)

- **Cenário 1: Disparo bem-sucedido do alarme de uma tarefa próxima**
  - **Dado** que o usuário criou a tarefa `"Ir comprar leite"` para quinta-feira às `15:00` com o alarme ativado (antecedência de 2 horas)
  - **Quando** a aplicação é executada na quinta-feira às `13:00` (ou após esse horário, estando a tarefa pendente)
  - **Então** o sistema deve emitir um aviso/alarme visual na página informando sobre a tarefa próxima do vencimento.

- **Cenário 2: Tarefa concluída não deve disparar alarme**
  - **Dado** que o usuário possui uma tarefa com alarme ativado para daqui a 1 hora
  - **Quando** o usuário marca essa tarefa como `"done"` (Concluída) antes do alarme disparar
  - **Então** o sistema não deve emitir nenhuma notificação de alarme para essa tarefa.

- **Cenário 3: Criação de tarefa sem alarme**
  - **Dado** que o usuário está criando uma nova tarefa no TODO List
  - **Quando** ele opta por deixar a opção de alarme desativada (`Não`)
  - **Então** a tarefa deve ser salva normalmente sem registrar disparo/notificação de alarme.

### 2.4. Impacto no que já existe (Feature 2)

- **Estrutura de Dados (`script.js`):** Adição dos atributos `alarme` (`boolean`), `antecedenciaMinutos` (`number`) e `alarmeDisparado` (`boolean`) nos objetos de tarefa.
- **Interface de Criação e Edição:** Inclusão dos campos de alarme e antecedência em `criar-tarefa.html` e na tabela de edição em linha de `editar-tarefas.html`, além da troca de `input type="date"` para `input type="datetime-local"`.
- **Listagem e Filtros:** Garantir que a exibição da tabela e os filtros por `status` (`todo`, `doing`, `done`) continuem funcionando corretamente com as novas propriedades.

### 2.5. Fora do Escopo (Feature 2)

- Envio de notificações push para dispositivos móveis ou e-mails externos.
- Reprodução de áudio ou sistema de som personalizado para o alarme.
- Persistência dos campos de alarme no backend Java / `historico.txt` nesta etapa (feature escopada apenas no frontend).

---

## Nota de Evolução Entre as Features

Quando a **Feature 1** e a **Feature 2** forem unificadas no futuro, será necessário estender o contrato JSON da API e o formato de `src/main/resources/historico.txt` para suportar data e hora (`dd/MM/yyyy HH:mm` / ISO `yyyy-MM-ddTHH:mm`) e os campos `alarme`, `antecedenciaMinutos` e `alarmeDisparado` no backend Java.
