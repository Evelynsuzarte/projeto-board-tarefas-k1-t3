package boardTarefas.repository;

import boardTarefas.model.Tarefa;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Integer.parseInt;

public class TarefaRepository {
    private final DateTimeFormatter formatoBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final File arquivo;

    public TarefaRepository() {
        this(new File("src/main/resources/historico.txt"));
    }

    public TarefaRepository(File arquivo) {
        this.arquivo = arquivo;
    }

    public synchronized void salvar(List<Tarefa> tarefas) throws IOException {
        try (FileWriter escritor = new FileWriter(arquivo)) {
            for (Tarefa t : tarefas) {
                escritor.write(
                        t.getId() + ";" +
                                t.getNome() + ";" +
                                t.getDescricao() + ";" +
                                t.getDataTermino().format(formatoBR) + ";" +
                                t.getStatus() + ";" +
                                t.getPrioridade() + ";" +
                                t.getCategoria() + "\n");
            }
        }
    }

    public synchronized List<Tarefa> carregar() throws IOException {
        List<Tarefa> tarefas = new ArrayList<>();
        if (!arquivo.exists()) {
            return tarefas;
        }

        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                if (linha.trim().isEmpty()) {
                    continue;
                }
                String[] dados = linha.split(";", -1);
                if (dados.length < 7) {
                    continue;
                }
                tarefas.add(new Tarefa(
                        parseInt(dados[0]),
                        dados[1],
                        dados[2],
                        LocalDate.parse(dados[3], formatoBR),
                        dados[4],
                        parseInt(dados[5]),
                        dados[6]));
            }
        }
        tarefas.sort(Comparator.comparingInt(Tarefa::getPrioridade).reversed());
        return tarefas;
    }
}