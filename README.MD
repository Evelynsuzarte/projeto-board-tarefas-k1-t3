# 📋 Board de Tarefas (To-Do List)

Um sistema de gerenciamento de tarefas desenvolvido em Java e Web (HTML/CSS/JS), com operações completas de CRUD, API HTTP local sincronizada com arquivo texto (`src/main/resources/historico.txt`) e alarme de proximidade de prazo no frontend.

## 🛠️ Tecnologias Utilizadas
* **Backend:** Java (JDK 21+), `com.sun.net.httpserver.HttpServer` (API REST/JSON nativa) e Gradle (`9.6.0`)
* **Testes:** Spock Framework (Groovy)
* **Frontend:** HTML5, CSS3 (Grid/Flexbox) e JavaScript (ES6+)

## 🚀 Como Executar

1. Clone este repositório e certifique-se de ter o Java (JDK 21+) instalado.
2. Verifique se o arquivo de persistência existe em `src/main/resources/historico.txt`.
3. Compile o projeto na raiz do repositório:
   - **Windows:** `.\gradlew.bat classes`
   - **Linux/macOS:** `./gradlew classes`

### Modo Web + API HTTP Local (Recomendado)
Suba o servidor HTTP a partir da **raiz do projeto**:
```bash
java -cp build/classes/java/main boardTarefas.Server
```
Depois acesse no navegador: `http://localhost:8080/`

### Modo Console (Terminal)
O modo via terminal continua disponível a partir da **raiz do projeto**:
```bash
java -cp build/classes/java/main boardTarefas.main
```

### Executar Testes Automatizados (Spock)
- **Windows:** `.\gradlew.bat test`
- **Linux/macOS:** `./gradlew test`

---

## ✨ Novas Funcionalidades

### 1. API HTTP Local e Sincronização Console ↔ Web (`boardTarefas.Server`)
* **Fonte única de verdade:** As operações de listar, criar, editar e excluir feitas pela interface web comunicam via `fetch` (`/api/tarefas`) com o backend Java, persistindo imediatamente em `src/main/resources/historico.txt`.
* **Mesma regra de negócio do terminal:** O `Server` delega todas as operações ao `TarefaController` e `TarefaRepository` (geração de `proximoId`, ordenação por prioridade decrescente, filtros por status e sanitização automática de `;` e quebras de linha).
* **Resiliência:** Suporte a CORS, respostas HTTP padronizadas (`200`, `201`, `204`, `400`, `404`), sincronização para acessos concorrentes e feedback visual no frontend quando o servidor está offline.

### 2. Alarme de Proximidade do Prazo das Tarefas (Frontend)
* **Data e hora de término:** Os formulários de criação (`criar-tarefa.html`) e edição (`editar-tarefas.html`) utilizam `datetime-local` para definir o prazo com horário.
* **Antecedência configurável:** Ao criar ou editar uma tarefa, é possível ativar o alarme e escolher entre opções pré-definidas (`15 min`, `30 min`, `1h`, `2h` padrão, `24h`) ou informar um tempo **personalizado em minutos**.
* **Monitoramento e persistência visual:** A verificação ocorre ao carregar qualquer página e periodicamente via `setInterval`, ignorando tarefas concluídas (`done`). O painel de alertas permanece visível em todas as páginas até que o usuário clique em **Dispensar** (voltando a disparar apenas se o prazo ou o alarme forem reconfigurados).

---

## 💡 Arquitetura da Solução
O projeto segue o padrão **MVC (Model-View-Controller)** com camada adicional de **Repository** e adaptador HTTP:
* **Model (`Tarefa.java`):** Encapsula os atributos da tarefa (`id`, `nome`, `descricao`, `dataTermino`, `prioridade`, `categoria`, `status`).
* **View Console (`Menu.java`, `TarefaView.java`):** Menus, prompts e exibição tabular no terminal.
* **Controller (`TarefaController.java`):** Regras de negócio (CRUD, pesquisa com filtros, geração de próximo ID e sanitização de texto).
* **Repository (`TarefaRepository.java`):** Leitura/gravação sincronizada em `src/main/resources/historico.txt` (`;` e `dd/MM/yyyy`) com ordenação por prioridade decrescente.
* **Server (`Server.java`):** API REST/JSON (`/api/tarefas`) e servidor de arquivos estáticos em `src/main/resources/static/`.
* **Main (`main.java`):** Interface interativa de linha de comando (`Scanner`).
