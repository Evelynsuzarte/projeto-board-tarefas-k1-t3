# AGENTS.md

Este documento orienta agentes e colaboradores que trabalham neste repositório.

## 1. Visão Geral

O **Board de Tarefas (To-Do List)** é uma aplicação de gerenciamento de tarefas composta por duas interfaces independentes que compartilham o mesmo modelo conceitual de tarefa (`id`, `nome`, `descricao`, `dataTermino`, `prioridade`, `categoria`, `status`):

- **Aplicação Console (Java):** Sistema via terminal com operações completas de CRUD e persistência local em arquivo texto (`src/main/resources/historico.txt`).
  - **Listar tarefas:** Exibe todas as tarefas em tabela formatada no terminal, ordenadas por prioridade decrescente ao carregar.
  - **Pesquisa detalhada:** Filtra tarefas por `categoria`, `prioridade` ou `status` (`todo`, `doing`, `done`), com filtro opcional por intervalo de datas (`dd/MM/yyyy`).
  - **Criar tarefa:** Captura os dados via teclado, gera o próximo ID sequencial e persiste a lista.
  - **Alterar tarefa:** Seleciona uma tarefa por ID e permite atualizar `status`, `nome`, `descricao`, `prioridade`, `categoria` ou `data` de término.
  - **Apagar tarefa:** Remove uma tarefa pelo ID e atualiza o arquivo.
  - **Persistência automática:** Salva as alterações em `src/main/resources/historico.txt` após criar, alterar, deletar e ao sair (`0 - SAIR`).

- **Interface Web Estática (Frontend):** Aplicação client-side em `src/main/resources/static/` que replica o gerenciamento de tarefas no navegador usando `localStorage` (chave `"meuBoard"`, inicializada com `tarefasPadrao` caso não haja dados salvos).
  - `index.html`: Página inicial com navegação para criação e gerenciamento de tarefas.
  - `criar-tarefa.html`: Formulário de criação de nova tarefa com cálculo automático do próximo ID.
  - `editar-tarefas.html`: Tabela interativa com filtro por status (`todo`, `doing`, `done`), edição em linha (`Salvar`) e exclusão com confirmação (`Excluir`).
  - `listar-tarefas.html`: Página de visualização/listagem com filtro por status.

---

## 2. Stack e Estrutura

### Tecnologias
- **Backend / Console:** Java (JDK 21+), Gradle (`9.6.0` via Gradle Wrapper), JUnit Jupiter (`6.0.0` configurado no `build.gradle`).
- **Persistência (Java):** Arquivo texto delimitado por ponto e vírgula (`;`) em `src/main/resources/historico.txt`.
- **Frontend:** HTML5, CSS3 (Grid e Flexbox, Google Fonts `Josefin Sans` e `Tilt Warp`), JavaScript (ES6+ vanilla) e `localStorage`.

### Organização das Pastas

```text
projeto-board-tarefas-k1-t3/
├── build.gradle                         # Configuração Gradle (plugin java + JUnit Jupiter)
├── settings.gradle                      # Nome do projeto (projeto-board-tarefas-k1-t3)
├── gradlew / gradlew.bat                # Gradle Wrapper
├── README.MD                            # Documentação geral do projeto
└── src/main/
    ├── java/boardTarefas/
    │   ├── main.java                    # Ponto de entrada console e fluxo de menus (Scanner)
    │   ├── model/
    │   │   └── Tarefa.java              # Entidade Tarefa (atributos, getters e setters)
    │   ├── controller/
    │   │   └── TarefaController.java    # Regras de negócio (CRUD, busca com filtros, próximo ID)
    │   ├── repository/
    │   │   └── TarefaRepository.java    # Leitura/escrita em historico.txt e ordenação por prioridade
    │   └── view/
    │       ├── Menu.java                # Menus, títulos e prompts do terminal
    │       └── TarefaView.java          # Formatação tabular e listagem de categorias
    └── resources/
        ├── historico.txt                # Base local de tarefas da aplicação Java
        └── static/
            ├── css/style.css            # Estilos globais e layout
            ├── html/
            │   ├── index.html
            │   ├── criar-tarefa.html
            │   ├── editar-tarefas.html
            │   └── listar-tarefas.html
            ├── images/                  # Logotipos e ícones (logo.png, logo2.jpg, logo2.png)
            └── js/script.js             # Lógica client-side (CRUD, filtro e localStorage)
```

---

## 3. Como Executar e Testar

### Compilar o projeto Java
Na raiz do repositório:

- **Windows (PowerShell/CMD):**
  ```powershell
  .\gradlew.bat build
  ```
- **Linux/macOS:**
  ```bash
  ./gradlew build
  ```

### Executar a aplicação Console (Java)
O `build.gradle` utiliza apenas o plugin `java` (não possui o plugin `application` configurado). Compile e execute a classe principal a partir da **raiz do projeto** (necessário para resolver o caminho relativo `src/main/resources/historico.txt`):

- **Windows:**
  ```powershell
  .\gradlew.bat classes
  java -cp build/classes/java/main boardTarefas.main
  ```
- **Linux/macOS:**
  ```bash
  ./gradlew classes
  java -cp build/classes/java/main boardTarefas.main
  ```
Também é possível executar `src/main/java/boardTarefas/main.java` diretamente pela IDE (IntelliJ IDEA, Eclipse ou VS Code).

### Executar a Interface Web (Frontend)
Abra `src/main/resources/static/html/index.html` diretamente no navegador ou sirva os arquivos estáticos com uma extensão como o *Live Server* no VS Code.

### Executar Testes
- **Windows:**
  ```powershell
  .\gradlew.bat test
  ```
- **Linux/macOS:**
  ```bash
  ./gradlew test
  ```
> **Observação:** O `build.gradle` já configura `useJUnitPlatform()` com `junit-jupiter`, porém atualmente não existem testes implementados em `src/test/` (`:test NO-SOURCE`).

---

## 4. Convenções

- **Arquitetura (Java):** Padrão **MVC + Repository** sob o pacote raiz `boardTarefas` (`model`, `view`, `controller`, `repository`).
  - `TarefaRepository` isola o acesso a `src/main/resources/historico.txt`.
  - `TarefaController` concentra as regras de negócio e delega persistência ao repositório.
  - `Menu` e `TarefaView` concentram toda a saída formatada no terminal.
- **Nomenclatura (Java):**
  - Pacotes e variáveis/métodos em `camelCase` em português (`dataTermino`, `proximoId`, `listarTodas`). Exceção existente: método `buscar_id` em `TarefaController`.
  - Classes em `PascalCase` (`Tarefa`, `TarefaController`, `TarefaRepository`, `Menu`, `TarefaView`). Exceção existente: classe principal `main` em `main.java`.
- **Nomenclatura (Frontend):**
  - Arquivos HTML em `kebab-case` (`criar-tarefa.html`, `editar-tarefas.html`, `listar-tarefas.html`).
  - Classes CSS em `kebab-case` em português (`formulario-adicionar-tarefa`, `formulario-listar-tarefa-table`, `btn-salvar`, `btn-excluir`).
  - Funções JavaScript em `camelCase` em português (`renderizarTabela`, `atualizarTarefa`, `deletarTarefa`, `adicionarTarefa`, `salvarNoNavegador`).
- **Formatos de dados:**
  - Valores de `status`: `"todo"`, `"doing"`, `"done"`.
  - Datas no Java: `LocalDate` com formato brasileiro `dd/MM/yyyy` na entrada, saída e em `historico.txt`.
  - Datas no Frontend: string ISO `yyyy-MM-dd` (padrão de `<input type="date">`).
  - Campos em `historico.txt`: `id;nome;descricao;dataTermino;status;prioridade;categoria`.
- **Idioma:** Identificadores, comentários, mensagens de interface e documentação são escritos em português (pt-BR).

---

## 5. Restrições

- **Não altere funcionalidades existentes:** Qualquer modificação no repositório deve preservar integralmente o comportamento atual da aplicação console Java e da interface web.
- Não modifique o formato de persistência de `src/main/resources/historico.txt` (`;`, `dd/MM/yyyy`), o caminho relativo esperado a partir da raiz do projeto, o nome da classe `boardTarefas.main` nem a chave `"meuBoard"` no `localStorage`, salvo solicitação explícita.
