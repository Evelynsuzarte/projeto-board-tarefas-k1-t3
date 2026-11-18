package boardTarefas

import boardTarefas.controller.TarefaController
import boardTarefas.repository.TarefaRepository
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class TarefaControllerSpec extends Specification {

    @TempDir
    Path tempDir

    private TarefaController criarController(File arquivo) {
        return new TarefaController(new TarefaRepository(arquivo))
    }

    def "deve retornar lista vazia quando o arquivo nao existe ou esta vazio"() {
        given:
        def inexistente = tempDir.resolve("nao-existe.txt").toFile()
        def vazio = tempDir.resolve("vazio.txt").toFile()
        Files.writeString(vazio.toPath(), "\n   \n")

        expect:
        criarController(inexistente).carregar().isEmpty()
        criarController(vazio).carregar().isEmpty()
    }

    def "deve criar tarefas com proximoId, ordenar por prioridade decrescente e sanitizar delimitadores"() {
        given:
        def controller = criarController(tempDir.resolve("historico.txt").toFile())

        when:
        def baixa = controller.criarSincronizado(
                "Tarefa;1",
                "Linha 1\nLinha 2;extra",
                LocalDate.of(2026, 11, 20),
                "todo",
                2,
                "Casa;Geral"
        )
        def alta = controller.criarSincronizado(
                "Tarefa Alta",
                "Urgente",
                LocalDate.of(2026, 11, 19),
                "doing",
                5,
                "Trabalho"
        )
        def listadas = controller.listarSincronizado()

        then:
        baixa.id == 1
        alta.id == 2
        baixa.nome == "Tarefa,1"
        baixa.descricao == "Linha 1 Linha 2,extra"
        baixa.categoria == "Casa,Geral"
        listadas.size() == 2
        listadas[0].prioridade == 5
        listadas[1].prioridade == 2
    }

    def "deve filtrar por status, categoria e prioridade usando equals"() {
        given:
        def controller = criarController(tempDir.resolve("historico.txt").toFile())
        controller.criarSincronizado("Estudar", "Java", LocalDate.of(2026, 11, 20), "todo", 4, "Estudos")
        controller.criarSincronizado("Academia", "Treino", LocalDate.of(2026, 11, 22), "done", 3, "Saude")

        when:
        def todos = controller.buscarSincronizado("status", new String("todo"), null, null)
        def estudos = controller.buscarSincronizado("categoria", "Estudos", LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 21))
        def prio3 = controller.buscarSincronizado("prioridade", "3", null, null)

        then:
        todos.size() == 1
        todos[0].nome == "Estudar"
        estudos.size() == 1
        prio3.size() == 1
        prio3[0].nome == "Academia"
    }

    def "deve atualizar e deletar tarefa retornando nulo ou falso quando id inexistente"() {
        given:
        def controller = criarController(tempDir.resolve("historico.txt").toFile())
        def criada = controller.criarSincronizado("Original", "Desc", LocalDate.of(2026, 11, 25), "todo", 3, "Geral")

        when:
        def atualizada = controller.atualizarSincronizado(
                criada.id,
                "Atualizada;Nome",
                "Nova\ndesc",
                LocalDate.of(2026, 11, 26),
                "done",
                5,
                "Trabalho"
        )

        then:
        atualizada != null
        atualizada.nome == "Atualizada,Nome"
        atualizada.descricao == "Nova desc"
        atualizada.status == "done"
        controller.atualizarSincronizado(999, "X", "Y", LocalDate.now(), "todo", 1, "Z") == null
        controller.deletarSincronizado(criada.id)
        !controller.deletarSincronizado(criada.id)
        controller.listarSincronizado().isEmpty()
    }
}
