/**
 * Projeto: Gerenciador de Tarefas (To-Do List)
 * Tecnologias: Java (JDK 21+)
 *
 * Como executar:
 * 1. Certifique-se de que a estrutura de pastas "src/main/resources" existe.
 * 2. Adicione o arquivo "historico.txt" na pasta resources.
 * 3. Compile e execute a classe 'main.java' através da sua IDE ou terminal.
 */
package boardTarefas;
import boardTarefas.controller.TarefaController;
import boardTarefas.model.Tarefa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;

public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TarefaController gerenciador = new TarefaController();
        List<Tarefa> tarefas = new ArrayList<>();
        tarefas = gerenciador.carregar();
        DateTimeFormatter formatoBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int id;
        String nome;
        String descricao;
        String dataTermino;
        int prioridade;
        String categoria;
        String status;
        int opcao, opcao2, opcao3;

        do{
            gerenciador.menuPrincipal();

            opcao = scanner.nextInt();
            scanner.nextLine();
            switch (opcao) {
                case 1:
                    System.out.println("======== TODAS AS TAREFAS ========");
                    gerenciador.listarTodas(tarefas);
                    break;

                case 2:
                    List<Tarefa> busca = new ArrayList<>();
                    String opcao_texto = "";
                    String texto = "";

                    System.out.println("======== PESQUISA DETALHADA ========");
                    System.out.println("Pesquisar por:");
                    System.out.println("1 - CATEGORIA");
                    System.out.println("2 - PRIORIDADE");
                    System.out.println("3 - STATUS");
                    System.out.println("===== ESCOLHA:");
                    opcao2 = scanner.nextInt();
                    scanner.nextLine();

                    if (opcao2 == 1){
                        opcao_texto = "categoria";
                        System.out.println("CATEGORIAS DISPONÍVEIS");
                        gerenciador.listarCategorias(tarefas);

                        System.out.println("==== ESCOLHA A CATEGORIA");
                        texto = scanner.nextLine();

                    } else if (opcao2 == 2){
                        opcao_texto = "prioridade";

                        System.out.println("==== ESCOLHA A PRIORIDADE");
                        texto = scanner.nextLine();


                    } else if (opcao2 == 3){
                        opcao_texto = "status";

                        System.out.println("==== ESCOLHA O STATUS");
                        System.out.println("1 - TODO\n2 - DOING\n3 - DONE");
                        texto = scanner.nextLine();


                        if (texto.equals("1")){
                            texto = "todo";
                        } else if (texto.equals("2")) {
                            texto = "doing";
                        } else if (texto.equals("3")) {
                            texto = "done";
                        }
                    }
                    System.out.println("*** ADICIONAR FILTRO DE DATA? ****\n1 - SIM\n2 - NAO");
                    opcao3 = scanner.nextInt();
                    scanner.nextLine();

                    //se tiver filtro
                    if (opcao3 == 1){
                        System.out.println("====== DIGITE A DATA INICIAL");
                        String dataI = scanner.next();
                        System.out.println("====== DIGITE A DATA FINAL");
                        String dataF = scanner.next();

                        scanner.nextLine();

                        busca = gerenciador.buscar(tarefas,opcao_texto,texto,LocalDate.parse(dataI,formatoBR), LocalDate.parse(dataF,formatoBR));

                        if (busca.isEmpty()){
                            System.out.println("Nenhuma tarefa encontrada, verifique corretamente!");
                        } else{
                            gerenciador.listarTodas(busca);
                        }
                    }
                    else if (opcao3 == 2){
                        busca = gerenciador.buscar(tarefas,opcao_texto, texto, null, null);

                        if (busca.isEmpty()){
                            System.out.println("Nenhuma tarefa encontrada, verifique corretamente!");
                        } else{
                            gerenciador.listarTodas(busca);
                        }
                    }
                    break;
                case 3:
                    System.out.println("======== CRIAR TAREFA ========= ");
                    System.out.println("NOME = ");
                    nome = scanner.nextLine();
                    System.out.println("DESCRICAO = ");
                    descricao = scanner.nextLine();
                    scanner.nextLine();
                    System.out.println("PRIORIDADE = ");
                    prioridade = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("CATEGORIA = ");
                    categoria = scanner.nextLine();
                    System.out.println("STATUS (todo/ doing/ done)= ");
                    status = scanner.nextLine();
                    System.out.println("DATA DE TÉRMINO = ");
                    dataTermino = scanner.next();
                    scanner.nextLine();
                    id = gerenciador.proximoId(tarefas);
                    gerenciador.criar(tarefas,id,nome,descricao,LocalDate.parse(dataTermino,formatoBR),status,prioridade,categoria);

                    System.out.println("!!!!! CRIADO COM SUCESSO !!!!!");
                    break;

                case 4:
                    System.out.println("======== ALTERAR TAREFA ========= ");
                    gerenciador.listarTodas(tarefas);


                    System.out.println("\n=== DIGITE O NÚMERO DO ID DA TAREFA: ");
                    id = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("===== TAREFA SELECIONADA:");
                    gerenciador.listarUnico(tarefas,id);


                    System.out.println("======== SELECIONE A AÇÃO");
                    System.out.println("1 - MUDAR STATUS (todo, doing, done) ");
                    System.out.println("2 - MUDAR NOME");
                    System.out.println("3 - MUDAR DESCRIÇÃO");
                    System.out.println("4 - MUDAR PRIORIDADE");
                    System.out.println("5 - MUDAR CATEGORIA");
                    System.out.println("6 - MUDAR DATA DE TÉRMINO (dd/mm/aaaa)");
                    System.out.println("===== ESCOLHA:");
                    opcao2 = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Digite a nova informação: ");
                    String info = scanner.nextLine();

                    switch (opcao2) {
                        case 1:
                            gerenciador.alterar(tarefas,id,"status",info);
                            System.out.println("Status atualizado com sucesso!");
                            break;
                        case 2:
                            gerenciador.alterar(tarefas,id,"nome",info);
                            System.out.println("Nome atualizado com sucesso!");
                            break;
                        case 3:
                            gerenciador.alterar(tarefas,id,"descricao",info);
                            System.out.println("Descrição atualizada com sucesso!");
                            break;
                        case 4:
                            gerenciador.alterar(tarefas,id,"prioridade",info);
                            System.out.println("Prioridade atualizada com sucesso!");
                            break;
                        case 5:
                            gerenciador.alterar(tarefas,id,"categoria",info);
                            System.out.println("Categoria atualizada com sucesso!");
                            break;
                        case 6:
                            LocalDate dataFormatada = LocalDate.parse(info, formatoBR);
                            gerenciador.alterar(tarefas, id, "data", dataFormatada.toString());
                            System.out.println("Data de término atualizada com sucesso!");
                            break;

                    }
                break;
                case 5:
                    System.out.println("======== APAGAR TAREFA ========= ");
                    gerenciador.listarTodas(tarefas);


                    System.out.println("\n=== DIGITE O NÚMERO DO ID DA TAREFA PARA APAGAAR: ");
                    id = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("===== TAREFA SELECIONADA:");
                    gerenciador.listarUnico(tarefas,id);

                    List<Tarefa> tarefasDel = gerenciador.deletar(tarefas, id);
                    if (tarefasDel == null){
                        System.out.println("Tarefa não encontrada");
                    } else {
                        tarefas = tarefasDel;
                        System.out.println("Tarefa apagada com sucesso!");
                    }
                    break;
            }

        } while(opcao !=0);

        gerenciador.salvar(tarefas);
        scanner.close();

    }
}
