package boardTarefas;

import boardTarefas.controller.TarefaController;
import boardTarefas.model.Tarefa;
import boardTarefas.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TarefaControllerTest {

    @TempDir
    Path tempDir;

    private TarefaController criarControllerComArquivo(File arquivo) {
        return new TarefaController(new TarefaRepository(arquivo));
    }

    @Test
    void deveRetornarListaVaziaQuandoArquivoNaoExisteOuEstaVazio() throws IOException {
        File inexistente = tempDir.resolve("nao-existe.txt").toFile();
        TarefaController controller = criarControllerComArquivo(inexistente);
        assertTrue(controller.carregar().isEmpty());

        File vazio = tempDir.resolve("vazio.txt").toFile();
        Files.writeString(vazio.toPath(), "\n   \n");
        TarefaController controllerVazio = criarControllerComArquivo(vazio);
        assertTrue(controllerVazio.carregar().isEmpty());
    }

    @Test
    void deveCriarOrdenarPorPrioridadeESanitizarDelimitadores() {
        File arquivo = tempDir.resolve("historico.txt").toFile();
        TarefaController controller = criarControllerComArquivo(arquivo);

        Tarefa baixa = controller.criarSincronizado(
                "Tarefa;1",
                "Linha 1\nLinha 2;extra",
                LocalDate.of(2026, 11, 20),
                "todo",
                2,
                "Casa;Geral"
        );
        Tarefa alta = controller.criarSincronizado(
                "Tarefa Alta",
                "Urgente",
                LocalDate.of(2026, 11, 19),
                "doing",
                5,
                "Trabalho"
        );

        assertEquals(1, baixa.getId());
        assertEquals(2, alta.getId());
        assertEquals("Tarefa,1", baixa.getNome());
        assertEquals("Linha 1 Linha 2,extra", baixa.getDescricao());
        assertEquals("Casa,Geral", baixa.getCategoria());

        List<Tarefa> listadas = controller.listarSincronizado();
        assertEquals(2, listadas.size());
        assertEquals(5, listadas.get(0).getPrioridade());
        assertEquals(2, listadas.get(1).getPrioridade());
    }

    @Test
    void deveFiltrarPorStatusCategoriaEPrioridadeComEquals() {
        File arquivo = tempDir.resolve("historico.txt").toFile();
        TarefaController controller = criarControllerComArquivo(arquivo);

        controller.criarSincronizado("Estudar", "Java", LocalDate.of(2026, 11, 20), "todo", 4, "Estudos");
        controller.criarSincronizado("Academia", "Treino", LocalDate.of(2026, 11, 22), "done", 3, "Saude");

        List<Tarefa> todos = controller.buscarSincronizado("status", new String("todo"), null, null);
        assertEquals(1, todos.size());
        assertEquals("Estudar", todos.get(0).getNome());

        List<Tarefa> estudos = controller.buscarSincronizado("categoria", "Estudos", LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 21));
        assertEquals(1, estudos.size());

        List<Tarefa> prio3 = controller.buscarSincronizado("prioridade", "3", null, null);
        assertEquals(1, prio3.size());
        assertEquals("Academia", prio3.get(0).getNome());
    }

    @Test
    void deveAtualizarEDeletarTarefaRetornandoNuloOuFalsoQuandoIdInexistente() {
        File arquivo = tempDir.resolve("historico.txt").toFile();
        TarefaController controller = criarControllerComArquivo(arquivo);

        Tarefa criada = controller.criarSincronizado("Original", "Desc", LocalDate.of(2026, 11, 25), "todo", 3, "Geral");

        Tarefa atualizada = controller.atualizarSincronizado(
                criada.getId(),
                "Atualizada;Nome",
                "Nova\ndesc",
                LocalDate.of(2026, 11, 26),
                "done",
                5,
                "Trabalho"
        );
        assertNotNull(atualizada);
        assertEquals("Atualizada,Nome", atualizada.getNome());
        assertEquals("Nova desc", atualizada.getDescricao());
        assertEquals("done", atualizada.getStatus());

        assertNull(controller.atualizarSincronizado(999, "X", "Y", LocalDate.now(), "todo", 1, "Z"));
        assertTrue(controller.deletarSincronizado(criada.getId()));
        assertFalse(controller.deletarSincronizado(criada.getId()));
        assertTrue(controller.listarSincronizado().isEmpty());
    }
}
