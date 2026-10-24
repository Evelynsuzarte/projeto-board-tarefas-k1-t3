# 📋 Board de Tarefas (To-Do List)

Um sistema de gerenciamento de tarefas via console desenvolvido em Java, focado em operações completas de CRUD (Criar, Ler, Atualizar, Deletar) com persistência de dados em arquivo de texto.

## 🛠️ Tecnologias Utilizadas
* **Backend:** Java e Gradle
* **Frontend:** HTML5, CSS3 (Grid/Flexbox) e JavaScript (ES6+)

## 🚀 Como Executar
1. Clone este repositório.
2. Certifique-se de ter o Java (JDK) instalado na sua máquina.
3. O projeto exige uma estrutura de pastas específica para o banco de dados local. Verifique se o arquivo de texto está no caminho: `src/main/resources/historico.txt`.
4. Execute a classe principal `main.java` através da sua IDE preferida (IntelliJ, Eclipse, VS Code) ou via terminal.
5. Para testar a interface gráfica (Frontend), acesse a pasta `Frontend` e abra o arquivo `index.html` diretamente no seu navegador ou utilize a extensão *Live Server* no VS Code.

## 💡 Comentários sobre a solução gerada
O projeto foi estruturado utilizando o padrão arquitetural **MVC (Model-View-Controller)**, com uma camada adicional de **Repository** para isolar o acesso aos dados:
* **Model:** A classe `Tarefa.java` encapsula os atributos de uma tarefa (id, nome, descrição, data, prioridade, categoria e status).
* **View (Console):** As classes `Menu.java` e `TarefaView.java` concentram tudo o que é exibido no terminal. A `Menu` cuida dos menus, títulos e prompts, e a `TarefaView` cuida das listagens em tabela formatada e da lista de categorias.
* **Controller:** A classe `TarefaController.java` concentra a regra de negócio: criação, pesquisa avançada com filtros cruzados, alteração, exclusão e geração do próximo ID, delegando ao repositório a leitura e a gravação.
* **Repository:** A classe `TarefaRepository.java` é a única que conhece o arquivo `historico.txt` e o seu formato, além de ordenar as tarefas por prioridade ao carregar. Trocar o arquivo por JSON ou banco de dados exigiria alterar só essa classe.
* **Main:** A classe `main.java` coordena o fluxo, capturando a entrada do teclado (`Scanner`) e chamando a view e o controller.

**Destaques:**
* **Persistência Simples:** Os dados são salvos em formato CSV delimitado por ponto e vírgula (`;`), garantindo que o histórico não seja perdido ao fechar o programa.
* **Tratamento de Datas:** O sistema formata automaticamente as entradas e saídas de dados para o padrão `dd/MM/yyyy`, traduzindo internamente para o padrão ISO do `LocalDate`.
* **Separação de responsabilidades:** Cada classe tem um único motivo para mudar. Alterar o visual da tabela mexe só na `TarefaView`, e alterar uma regra de busca mexe só no controller.

## 🎨 Desenvolvimento do Frontend (Interface Web)
Para a trilha de JavaScript, foi desenvolvida uma interface web completa e responsiva que replica de forma visual todas as regras de negócio criadas no ecossistema Java:

* **Modelagem de Dados e Simulação de Banco:** Criação de uma lista de objetos estruturada em um array JavaScript espelhando todos os atributos da classe `Tarefa` do Java.
* **CRUD Completo no Cliente (Memória):**
    * **Create (Criar):** Formulário estruturado com CSS que captura os dados de entrada, calcula o próximo ID disponível através de um laço de repetição tradicional e valida as informações antes da inserção.
    * **Read (Listar/Filtrar):** Exibição dinâmica das tarefas através de manipulação da DOM na tabela HTML, contando com um monitoramento de evento (`change`) para filtragem instantânea por Status (*To Do, Doing, Done*).
    * **Update (Atualizar):** Edição direta em linha permitindo ao usuário alterar qualquer campo na tabela de visualização e salvar o estado atualizado através de varredura de ID.
    * **Delete (Excluir):** Botão de remoção integrada que utiliza o método `.filter()` para expurgar fisicamente o objeto correto do Array de dados após confirmação de segurança.
* **Persistência Local (Web):** Integração com a API do `localStorage` para serializar (via `JSON.stringify`) e deserializar (via `JSON.parse`) a lista de tarefas diretamente no armazenamento do navegador, impedindo a perda de dados ao atualizar a página (F5) ou navegar entre as telas.
* **Estilização e Layout (CSS):** Construção visual baseada em fontes modernas (*Josefin Sans*), paleta de cores azulada/harmônica, botões com transições e efeitos de brilho em foco (*hover*), além do uso de CSS Flexbox para alinhar menus verticais em lista e centralizar componentes de navegação.


