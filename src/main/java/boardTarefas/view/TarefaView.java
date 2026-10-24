package boardTarefas.view;

import boardTarefas.model.Tarefa;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TarefaView {
    private DateTimeFormatter formatoBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String FORMATO = "%-4s | %-15s | %-10s | %-8s | %-20s | %-12s | %s%n";
    private static String LINHA_DUPLA = "\n====================================================================================================================";
    private static String LINHA = "--------------------------------------------------------------------------------------------------------------------";

    public void listarTodas(List<Tarefa> tarefas) {
        cabecalho();
        for (Tarefa t : tarefas) {
            linha(t);
        }
        rodape();
    }

    public void listarUnico(List<Tarefa> tarefas, int id) {
        cabecalho();
        for (Tarefa t : tarefas) {
            if (t.getId() == id) {
                linha(t);
                break;
            }
        }
        rodape();
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

    private void cabecalho() {
        System.out.println(LINHA_DUPLA);
        System.out.printf(FORMATO, "ID", "CATEGORIA", "PRIORIDADE", "STATUS", "NOME", "DATA", "DESCRICAO");
        System.out.println(LINHA);
    }

    private void linha(Tarefa t) {
        System.out.printf(FORMATO,
                t.getId(),
                t.getCategoria(),
                t.getPrioridade(),
                t.getStatus().toUpperCase(),
                t.getNome(),
                t.getDataTermino().format(formatoBR),
                t.getDescricao());
    }

    private void rodape() {
        System.out.println("====================================================================================================================\n");
    }
}