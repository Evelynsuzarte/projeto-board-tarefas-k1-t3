# 📋 Board de Tarefas (To-Do List)

Um sistema de gerenciamento de tarefas via console desenvolvido em Java, focado em operações completas de CRUD (Criar, Ler, Atualizar, Deletar) com persistência de dados em arquivo de texto.

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Java e Gradle

## 🚀 Como Executar
1. Clone este repositório.
2. Certifique-se de ter o Java (JDK) instalado na sua máquina.
3. O projeto exige uma estrutura de pastas específica para o banco de dados local. Verifique se o arquivo de texto está no caminho: `src/main/resources/historico.txt`.
4. Execute a classe principal `main.java`através da sua IDE preferida (IntelliJ, Eclipse, VS Code) ou via terminal.

## 💡 Comentários sobre a solução gerada
O projeto foi estruturado utilizando o padrão arquitetural **MVC (Model-View-Controller)** de forma simplificada:
* **Model:** A classe `Tarefa.java` encapsula os atributos de uma tarefa (id, nome, descrição, data, prioridade, categoria e status).
* **Controller:** A classe `TarefaController.java` concentra toda a regra de negócio, algoritmos de pesquisa avançada com filtros cruzados, ordenação automática por prioridade e a manipulação do arquivo de texto.
* **View (Console):** A classe `main.java` atua como a interface do usuário, lidando com a captura de dados via teclado (`Scanner`) e exibição dos menus.

**Destaques:**
* **Persistência Simples:** Os dados são salvos em formato CSV delimitado por ponto e vírgula (`;`), garantindo que o histórico não seja perdido ao fechar o programa.
* **Tratamento de Datas:** O sistema formata automaticamente as entradas e saídas de dados para o padrão `dd/MM/yyyy`, traduzindo internamente para o padrão ISO do `LocalDate`.