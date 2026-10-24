package boardTarefas.view;

public class Menu {

    public void menuPrincipal(){
        System.out.println("======== GERENCIADOR DE TAREFAS - TO DO LIST ========");
        System.out.println("1 - VER TODAS AS TAREFAS");
        System.out.println("2 - PESQUISA DETALHADA");
        System.out.println("3 - CRIAR TAREFA");
        System.out.println("4 - ALTERAR TAREFA");
        System.out.println("5 - APAGAR TAREFA");
        System.out.println("0 - SAIR");
        System.out.println("===== ESCOLHA:");
    }

    public void menuPesquisa() {
        System.out.println("======== PESQUISA DETALHADA ========");
        System.out.println("Pesquisar por:");
        System.out.println("1 - CATEGORIA");
        System.out.println("2 - PRIORIDADE");
        System.out.println("3 - STATUS");
        System.out.println("===== ESCOLHA:");
    }

    public void menuStatus() {
        System.out.println("==== ESCOLHA O STATUS");
        System.out.println("1 - TODO\n2 - DOING\n3 - DONE");
    }

    public void menuFiltroData() {
        System.out.println("*** ADICIONAR FILTRO DE DATA? ****\n1 - SIM\n2 - NAO");
    }

    public void menuAlterar() {
        System.out.println("======== SELECIONE A AÇÃO");
        System.out.println("1 - MUDAR STATUS (todo, doing, done) ");
        System.out.println("2 - MUDAR NOME");
        System.out.println("3 - MUDAR DESCRIÇÃO");
        System.out.println("4 - MUDAR PRIORIDADE");
        System.out.println("5 - MUDAR CATEGORIA");
        System.out.println("6 - MUDAR DATA DE TÉRMINO (dd/mm/aaaa)");
        System.out.println("===== ESCOLHA:");
    }

    public void tituloTodas(){
        System.out.println("======== TODAS AS TAREFAS ========");
    }

    public void tituloCriar(){
        System.out.println("======== CRIAR TAREFA ========= ");
    }

    public void tituloAlterar() {
        System.out.println("======== ALTERAR TAREFA ========= ");
    }

    public void tituloApagar(){
        System.out.println("======== APAGAR TAREFA ========= ");
    }

    public void pedirIdAlterar() {
        System.out.println("\n=== DIGITE O NÚMERO DO ID DA TAREFA: ");
    }

    public void pedirIdApagar()  {
        System.out.println("\n=== DIGITE O NÚMERO DO ID DA TAREFA PARA APAGAR: ");
    }

    public void tarefaSelecionada() {
        System.out.println("===== TAREFA SELECIONADA:");
    }

}
