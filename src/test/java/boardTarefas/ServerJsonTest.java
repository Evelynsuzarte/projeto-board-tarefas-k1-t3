package boardTarefas;

import boardTarefas.model.Tarefa;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServerJsonTest {

    @Test
    void deveSerializarTarefaEListaComDataIsoEEscapeJson() {
        Tarefa t = new Tarefa(
                7,
                "Ler \"Clean Code\"",
                "Capitulo 1\\2\nResumo",
                LocalDate.of(2026, 11, 30),
                "doing",
                4,
                "Estudos"
        );

        String jsonItem = Server.toJson(t);
        assertTrue(jsonItem.contains("\"id\":7"));
        assertTrue(jsonItem.contains("\"nome\":\"Ler \\\"Clean Code\\\"\""));
        assertTrue(jsonItem.contains("\"descricao\":\"Capitulo 1\\\\2\\nResumo\""));
        assertTrue(jsonItem.contains("\"dataTermino\":\"2026-11-30\""));
        assertTrue(jsonItem.contains("\"status\":\"doing\""));

        String jsonLista = Server.toJson(List.of(t));
        assertTrue(jsonLista.startsWith("[{"));
        assertTrue(jsonLista.endsWith("}]"));
    }

    @Test
    void deveValidarEParsarPayloadValido() {
        String payload = """
                {
                  "nome": "Finalizar API",
                  "descricao": "Testes JUnit",
                  "dataTermino": "2026-12-01",
                  "prioridade": 5,
                  "categoria": "Trabalho",
                  "status": "todo"
                }
                """;

        Server.DadosTarefaValidos dados = Server.validarEParsarPayload(payload);
        assertEquals("Finalizar API", dados.nome());
        assertEquals("Testes JUnit", dados.descricao());
        assertEquals(LocalDate.of(2026, 12, 1), dados.dataTermino());
        assertEquals(5, dados.prioridade());
        assertEquals("Trabalho", dados.categoria());
        assertEquals("todo", dados.status());
    }

    @Test
    void deveRejeitarPayloadInvalidoComIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                Server.validarEParsarPayload("{\"nome\":\"\",\"dataTermino\":\"2026-12-01\",\"prioridade\":1,\"status\":\"todo\"}"));

        assertThrows(IllegalArgumentException.class, () ->
                Server.validarEParsarPayload("{\"nome\":\"A\",\"dataTermino\":\"01/12/2026\",\"prioridade\":1,\"status\":\"todo\"}"));

        assertThrows(IllegalArgumentException.class, () ->
                Server.validarEParsarPayload("{\"nome\":\"A\",\"dataTermino\":\"2026-12-01\",\"prioridade\":1,\"status\":\"invalido\"}"));

        assertThrows(IllegalArgumentException.class, () ->
                Server.validarEParsarPayload("{\"nome\":\"A\",\"dataTermino\":\"2026-12-01\",\"prioridade\":\"abc\",\"status\":\"todo\"}"));

        Map<String, String> vazio = Server.parseJsonObject("{}");
        assertTrue(vazio.isEmpty());
    }
}
