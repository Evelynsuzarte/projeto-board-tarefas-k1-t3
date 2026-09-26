package boardTarefas.controller;

import boardTarefas.model.Tarefa;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.*;
import java.io.IOException;

import static java.lang.Integer.parseInt;
import java.time.format.DateTimeFormatter;

public class TarefaController {
    DateTimeFormatter formatoBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void salvar(List<Tarefa> tarefas){
        File arquivo = new File("src/main/resources/historico.txt");

        try {
            FileWriter escritor = new FileWriter(arquivo);
            for (Tarefa t:tarefas){
                escritor.write(
                    t.getId() + ";" +
                    t.getNome() + ";" +
                    t.getDescricao() + ";" +
                    t.getDataTermino().format(formatoBR) + ";" +
                    t.getStatus() + ";" +
                    t.getPrioridade() + ";" +
                    t.getCategoria() + "\n"
                );
            }
            escritor.close();

        } catch (IOException e) {
            System.out.println("Erro ao salvar tarefas: " + e.getMessage());
        }
    }

    public List<Tarefa> carregar(){
        List<Tarefa> tarefas = new ArrayList<>();
        File arquivo = new File("src/main/resources/historico.txt");

        if (!arquivo.exists()) {
            return tarefas;
        }

        try {
            Scanner scanner = new Scanner(arquivo);
            while (scanner.hasNextLine()) {

                String linha = scanner.nextLine();
                String[] dados = linha.split(";");

                int id = parseInt(dados[0]);
                String nome = dados[1];
                String descricao = dados[2];
                LocalDate dataTermino = LocalDate.parse(dados[3],formatoBR);
                String status = dados[4];
                int prioridade = parseInt(dados[5]);
                String categoria = dados[6];


                Tarefa tarefa = new Tarefa(
                        id,
                        nome,
                        descricao,
                        dataTermino,
                        status,
                        prioridade,
                        categoria
                );

                tarefas.add(tarefa);

            }
            scanner.close();
            tarefas.sort(Comparator.comparingInt(Tarefa::getPrioridade).reversed());

        } catch (IOException e) {
            System.out.println("Erro ao carregar tarefas: " + e.getMessage());
        }
        return tarefas;
    }

    public List<Tarefa> criar (List<Tarefa> tarefas, int id, String nome, String descricao, LocalDate dataTermino, String status, int prioridade, String categoria){
        Tarefa novaTarefa = new Tarefa(id,nome,descricao,dataTermino,status,prioridade,categoria);
        tarefas.add((novaTarefa));
        salvar(tarefas);
        return tarefas;
    }

    public List<Tarefa> buscar (List<Tarefa> tarefas, String filtro, String opcao, LocalDate inicio, LocalDate fim) {
        List<Tarefa> tarefasBusca = new ArrayList<>();

        if (filtro == "categoria" && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (t.getCategoria().equals(opcao)) {
                    tarefasBusca.add(t);
                }
            }
        } else if (filtro == "categoria" && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && t.getCategoria().equals(opcao)) {
                    tarefasBusca.add(t);
                }
            }

        } else if (filtro == "prioridade" && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (t.getPrioridade() == parseInt(opcao, 10)) {
                    tarefasBusca.add(t);
                }
            }

        } else if (filtro == "prioridade" && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && t.getPrioridade() == parseInt(opcao, 10)) {
                    tarefasBusca.add(t);
                }
            }
        } else if (filtro == "status" && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (t.getStatus() == opcao) {
                    tarefasBusca.add(t);
                }
            }

        } else if (filtro == "status" && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && t.getStatus() == opcao) {
                    tarefasBusca.add(t);
                }
            }
        }
        else{
            return null;
        }

        return tarefasBusca;
    }

    public Tarefa buscar_id(int id,List<Tarefa> tarefas){
        for (Tarefa t : tarefas){
            if (t.getId() == id){
                return t;
            }
        }
        return null;
    }

    public int proximoId(List<Tarefa> tarefas) {
        int maiorId = 0;
        for (Tarefa t : tarefas) {
            if (t.getId() > maiorId) {
                maiorId = t.getId();
            }
        }
        return maiorId + 1;
    }

    public List<Tarefa> alterar(List<Tarefa> tarefas, int tarefaId, String campo, String info){
        Tarefa encontrado = buscar_id(tarefaId,tarefas);

        if (info != null && encontrado != null){
            if (campo == "nome"){
                encontrado.setNome(info);
            } else if (campo == "descricao") {
                encontrado.setDescricao(info);
            } else if (campo == "prioridade"){
                encontrado.setPrioridade(parseInt(info));
            } else if (campo == "categoria") {
                encontrado.setCategoria(info);
            } else if (campo == "status"){
                encontrado.setStatus(info);
            } else if (campo == "data"){
                encontrado.setDataTermino(LocalDate.parse(info, formatoBR));
            }

            for (int i = 0; i < tarefas.size(); i++){
                if (tarefas.get(i).getId() == encontrado.getId()){
                    tarefas.set(i, encontrado);
                    break;
                }
            }
        } else{
            return null;
        }

        salvar(tarefas);
        return tarefas;


    }

    public List<Tarefa> deletar(List<Tarefa> tarefas, int tarefaId){
        Tarefa encontrado = buscar_id(tarefaId,tarefas);
        if (encontrado == null){
            return null;
        }

        for (int i = 0; i < tarefas.size(); i++){
            if (tarefas.get(i).getId() == encontrado.getId()){
                tarefas.remove(i);
                break;
            }
        }
        salvar(tarefas);
        return tarefas;

    }

//    public void listarTodas (List<Tarefa> tarefas){
//        System.out.println("ID\tCATEGORIA\tPRIORIDADE\tSTATUS\tNOME\tDESCRICAO\tDATA TERMINO");
//        for (Tarefa t : tarefas){
//            System.out.println(t.getId()+"\t"+t.getCategoria()+"\t"+t.getPrioridade()+"\t"+t.getStatus()+"\t"+t.getNome()+"\t"+t.getDescricao()+"\t"+t.getDataTermino());
//        }
//    }


    public void listarTodas (List<Tarefa> tarefas){
        tarefas.sort(Comparator.comparingInt(Tarefa::getPrioridade).reversed());

        String formato = "%-4s | %-15s | %-10s | %-8s | %-20s | %-12s | %s%n";
        System.out.println("\n====================================================================================================================");
        System.out.printf(formato, "ID", "CATEGORIA", "PRIORIDADE", "STATUS", "NOME", "DATA", "DESCRICAO");
        System.out.println("--------------------------------------------------------------------------------------------------------------------");
        for (Tarefa t : tarefas){
            System.out.printf(formato,
                    t.getId(),
                    t.getCategoria(),
                    t.getPrioridade(),
                    t.getStatus().toUpperCase(),
                    t.getNome(),
                    t.getDataTermino().format(formatoBR),
                    t.getDescricao()
            );
        }
        System.out.println("====================================================================================================================\n");
    }

//    public void listarUnico (List<Tarefa> tarefas, int id){
//        System.out.println("ID\tCATEGORIA\tPRIORIDADE\tSTATUS\tNOME\tDESCRICAO\tDATA TERMINO");
//        for (Tarefa t : tarefas){
//            if (t.getId() == id){
//                System.out.println(t.getId()+"\t"+t.getCategoria()+"\t"+t.getPrioridade()+"\t"+t.getStatus()+"\t"+t.getNome()+"\t"+t.getDescricao()+"\t"+t.getDataTermino());
//            }
//        }
//    }

    public void listarUnico (List<Tarefa> tarefas, int id){
        String formato = "%-4s | %-15s | %-10s | %-8s | %-20s | %-12s | %s%n";
        System.out.println("\n====================================================================================================================");
        System.out.printf(formato, "ID", "CATEGORIA", "PRIORIDADE", "STATUS", "NOME", "DATA", "DESCRICAO");
        System.out.println("--------------------------------------------------------------------------------------------------------------------");
        for (Tarefa t : tarefas){
            if (t.getId() == id) {
                System.out.printf(formato,
                        t.getId(),
                        t.getCategoria(),
                        t.getPrioridade(),
                        t.getStatus().toUpperCase(),
                        t.getNome(),
                        t.getDataTermino().format(formatoBR),
                        t.getDescricao()
                );
                break;
            }
        }
        System.out.println("====================================================================================================================\n");
    }


    public void listarCategorias(List<Tarefa> tarefas) {
        Set<String> categorias = new HashSet<>();

        for (Tarefa t : tarefas) {
            categorias.add(t.getCategoria());
        }

        for (String categoria : categorias) {
            System.out.println(categoria);
        }
    }

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



}























