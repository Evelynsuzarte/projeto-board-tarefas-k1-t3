package boardTarefas

import boardTarefas.model.Tarefa
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class ServerJsonSpec extends Specification {

    def "deve serializar tarefa e lista com data ISO e escape JSON"() {
        given:
        def tarefa = new Tarefa(
                7,
                "Ler \"Clean Code\"",
                "Capitulo 1\\2\nResumo",
                LocalDate.of(2026, 11, 30),
                "doing",
                4,
                "Estudos"
        )

        when:
        def jsonItem = Server.toJson(tarefa)
        def jsonLista = Server.toJson([tarefa])

        then:
        jsonItem.contains('"id":7')
        jsonItem.contains('"nome":"Ler \\"Clean Code\\""')
        jsonItem.contains('"descricao":"Capitulo 1\\\\2\\nResumo"')
        jsonItem.contains('"dataTermino":"2026-11-30"')
        jsonItem.contains('"status":"doing"')
        jsonLista.startsWith("[{")
        jsonLista.endsWith("}]")
    }

    def "deve validar e parsar payload JSON valido"() {
        given:
        def payload = '''
            {
              "nome": "Finalizar API",
              "descricao": "Testes Spock",
              "dataTermino": "2026-12-01",
              "prioridade": 5,
              "categoria": "Trabalho",
              "status": "todo"
            }
        '''

        when:
        def dados = Server.validarEParsarPayload(payload)

        then:
        dados.nome() == "Finalizar API"
        dados.descricao() == "Testes Spock"
        dados.dataTermino() == LocalDate.of(2026, 12, 1)
        dados.prioridade() == 5
        dados.categoria() == "Trabalho"
        dados.status() == "todo"
    }

    @Unroll
    def "deve rejeitar payload invalido: #cenario"() {
        when:
        Server.validarEParsarPayload(payload)

        then:
        thrown(IllegalArgumentException)

        where:
        cenario            | payload
        "nome vazio"       | '{"nome":"","dataTermino":"2026-12-01","prioridade":1,"status":"todo"}'
        "data formato BR"  | '{"nome":"A","dataTermino":"01/12/2026","prioridade":1,"status":"todo"}'
        "status invalido"  | '{"nome":"A","dataTermino":"2026-12-01","prioridade":1,"status":"invalido"}'
        "prioridade texto" | '{"nome":"A","dataTermino":"2026-12-01","prioridade":"abc","status":"todo"}'
    }

    def "deve parsar objeto JSON vazio"() {
        expect:
        Server.parseJsonObject("{}").isEmpty()
    }
}
