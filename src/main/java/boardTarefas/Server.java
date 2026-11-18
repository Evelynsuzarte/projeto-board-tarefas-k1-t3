package boardTarefas;

import boardTarefas.controller.TarefaController;
import boardTarefas.model.Tarefa;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP local para expor a API REST de tarefas e servir o frontend estático.
 */
public class Server {
    private static final int PORTA_PADRAO = 8080;
    private static final Path STATIC_ROOT = Paths.get("src/main/resources/static").toAbsolutePath().normalize();
    private static final Set<String> STATUS_VALIDOS = Set.of("todo", "doing", "done");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "application/javascript; charset=utf-8",
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "svg", "image/svg+xml",
            "ico", "image/x-icon"
    );

    private final TarefaController controller;

    public Server() {
        this(new TarefaController());
    }

    public Server(TarefaController controller) {
        this.controller = controller;
    }

    public static void main(String[] args) throws IOException {
        int porta = PORTA_PADRAO;
        if (args.length > 0) {
            try {
                porta = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                porta = PORTA_PADRAO;
            }
        }

        Server app = new Server();
        HttpServer server = app.iniciar(porta);
        System.out.println("Servidor Board de Tarefas rodando em http://localhost:" + server.getAddress().getPort());
    }

    public HttpServer iniciar(int porta) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/api/tarefas", this::tratarApiTarefas);
        server.createContext("/", this::tratarArquivosEstaticos);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        return server;
    }

    private void tratarApiTarefas(HttpExchange exchange) throws IOException {
        adicionarCabecalhosCors(exchange.getResponseHeaders());
        String metodo = exchange.getRequestMethod().toUpperCase();

        if ("OPTIONS".equals(metodo)) {
            enviarVazio(exchange, 204);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/api/tarefas".equals(path) || "/api/tarefas/".equals(path)) {
            tratarColecaoTarefas(exchange, metodo);
            return;
        }

        if (path.startsWith("/api/tarefas/")) {
            String sufixo = path.substring("/api/tarefas/".length());
            if (sufixo.contains("/") || sufixo.isEmpty()) {
                enviarErro(exchange, 400, "ID de tarefa inválido.");
                return;
            }
            int id;
            try {
                id = Integer.parseInt(sufixo);
            } catch (NumberFormatException e) {
                enviarErro(exchange, 400, "ID de tarefa inválido.");
                return;
            }
            tratarItemTarefa(exchange, metodo, id);
            return;
        }

        enviarErro(exchange, 404, "Rota não encontrada.");
    }

    private void tratarColecaoTarefas(HttpExchange exchange, String metodo) throws IOException {
        if ("GET".equals(metodo)) {
            String statusFiltro = extrairQueryParam(exchange.getRequestURI().getRawQuery(), "status");
            List<Tarefa> tarefas;
            if (statusFiltro != null && !statusFiltro.isEmpty()) {
                if (!STATUS_VALIDOS.contains(statusFiltro)) {
                    enviarErro(exchange, 400, "Status inválido. Use todo, doing ou done.");
                    return;
                }
                tarefas = controller.buscarSincronizado("status", statusFiltro, null, null);
            } else {
                tarefas = controller.listarSincronizado();
            }
            enviarJson(exchange, 200, toJson(tarefas));
            return;
        }

        if ("POST".equals(metodo)) {
            String corpo = lerCorpo(exchange);
            DadosTarefaValidos dados;
            try {
                dados = validarEParsarPayload(corpo);
            } catch (IllegalArgumentException e) {
                enviarErro(exchange, 400, e.getMessage());
                return;
            }

            Tarefa criada = controller.criarSincronizado(
                    dados.nome,
                    dados.descricao,
                    dados.dataTermino,
                    dados.status,
                    dados.prioridade,
                    dados.categoria
            );
            enviarJson(exchange, 201, toJson(criada));
            return;
        }

        enviarErro(exchange, 405, "Método não permitido.");
    }

    private void tratarItemTarefa(HttpExchange exchange, String metodo, int id) throws IOException {
        if ("PUT".equals(metodo)) {
            String corpo = lerCorpo(exchange);
            DadosTarefaValidos dados;
            try {
                dados = validarEParsarPayload(corpo);
            } catch (IllegalArgumentException e) {
                enviarErro(exchange, 400, e.getMessage());
                return;
            }

            Tarefa atualizada = controller.atualizarSincronizado(
                    id,
                    dados.nome,
                    dados.descricao,
                    dados.dataTermino,
                    dados.status,
                    dados.prioridade,
                    dados.categoria
            );
            if (atualizada == null) {
                enviarErro(exchange, 404, "Tarefa não encontrada.");
                return;
            }
            enviarJson(exchange, 200, toJson(atualizada));
            return;
        }

        if ("DELETE".equals(metodo)) {
            boolean removida = controller.deletarSincronizado(id);
            if (!removida) {
                enviarErro(exchange, 404, "Tarefa não encontrada.");
                return;
            }
            enviarVazio(exchange, 204);
            return;
        }

        enviarErro(exchange, 405, "Método não permitido.");
    }

    private void tratarArquivosEstaticos(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarVazio(exchange, 405);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path) || path.isEmpty()) {
            exchange.getResponseHeaders().set("Location", "/html/index.html");
            enviarVazio(exchange, 302);
            return;
        }

        String relativo = path.startsWith("/") ? path.substring(1) : path;
        Path arquivo = STATIC_ROOT.resolve(relativo).normalize();
        if (!arquivo.startsWith(STATIC_ROOT) || !Files.exists(arquivo) || Files.isDirectory(arquivo)) {
            byte[] notFound = "Arquivo não encontrado".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound);
            }
            return;
        }

        String contentType = resolverContentType(arquivo.getFileName().toString());
        byte[] bytes = Files.readAllBytes(arquivo);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String resolverContentType(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf('.');
        if (ponto < 0 || ponto == nomeArquivo.length() - 1) {
            return "application/octet-stream";
        }
        String extensao = nomeArquivo.substring(ponto + 1).toLowerCase();
        return CONTENT_TYPES.getOrDefault(extensao, "application/octet-stream");
    }

    private void adicionarCabecalhosCors(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    private String lerCorpo(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void enviarJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void enviarErro(HttpExchange exchange, int status, String mensagem) throws IOException {
        enviarJson(exchange, status, "{\"erro\":\"" + escapeJson(mensagem) + "\"}");
    }

    private void enviarVazio(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private String extrairQueryParam(String rawQuery, String chave) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return null;
        }
        for (String par : rawQuery.split("&")) {
            int idx = par.indexOf('=');
            if (idx > 0 && chave.equals(par.substring(0, idx))) {
                return java.net.URLDecoder.decode(par.substring(idx + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public static String toJson(List<Tarefa> tarefas) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tarefas.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(toJson(tarefas.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJson(Tarefa t) {
        return "{"
                + "\"id\":" + t.getId() + ","
                + "\"nome\":\"" + escapeJson(t.getNome()) + "\","
                + "\"descricao\":\"" + escapeJson(t.getDescricao()) + "\","
                + "\"dataTermino\":\"" + t.getDataTermino().toString() + "\","
                + "\"prioridade\":" + t.getPrioridade() + ","
                + "\"categoria\":\"" + escapeJson(t.getCategoria()) + "\","
                + "\"status\":\"" + escapeJson(t.getStatus()) + "\""
                + "}";
    }

    public static String escapeJson(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valor.length());
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static DadosTarefaValidos validarEParsarPayload(String json) {
        Map<String, String> campos = parseJsonObject(json);
        String nome = campos.get("nome");
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'nome' é obrigatório.");
        }

        String descricao = campos.getOrDefault("descricao", "");
        String categoria = campos.getOrDefault("categoria", "");

        String status = campos.get("status");
        if (status == null || !STATUS_VALIDOS.contains(status.trim())) {
            throw new IllegalArgumentException("O campo 'status' deve ser todo, doing ou done.");
        }

        String prioridadeStr = campos.get("prioridade");
        if (prioridadeStr == null || prioridadeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'prioridade' é obrigatório.");
        }
        int prioridade;
        try {
            prioridade = Integer.parseInt(prioridadeStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo 'prioridade' deve ser um número inteiro.");
        }

        String dataStr = campos.get("dataTermino");
        if (dataStr == null || dataStr.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'dataTermino' é obrigatório (yyyy-MM-dd).");
        }
        LocalDate dataTermino;
        try {
            dataTermino = LocalDate.parse(dataStr.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("O campo 'dataTermino' deve estar no formato yyyy-MM-dd.");
        }

        return new DadosTarefaValidos(nome, descricao, dataTermino, status.trim(), prioridade, categoria);
    }

    public static Map<String, String> parseJsonObject(String json) {
        if (json == null) {
            throw new IllegalArgumentException("Corpo JSON ausente.");
        }
        String texto = json.trim();
        if (!texto.startsWith("{") || !texto.endsWith("}")) {
            throw new IllegalArgumentException("Payload deve ser um objeto JSON válido.");
        }

        Map<String, String> resultado = new HashMap<>();
        int[] pos = {1};
        int len = texto.length();

        pularEspacos(texto, pos, len);
        if (pos[0] < len && texto.charAt(pos[0]) == '}') {
            return resultado;
        }

        while (pos[0] < len) {
            pularEspacos(texto, pos, len);
            String chave = lerStringJson(texto, pos, len);
            pularEspacos(texto, pos, len);

            if (pos[0] >= len || texto.charAt(pos[0]) != ':') {
                throw new IllegalArgumentException("JSON inválido: esperado ':'.");
            }
            pos[0]++;
            pularEspacos(texto, pos, len);

            String valor = lerValorJson(texto, pos, len);
            resultado.put(chave, valor);

            pularEspacos(texto, pos, len);
            if (pos[0] < len && texto.charAt(pos[0]) == ',') {
                pos[0]++;
                continue;
            }
            if (pos[0] < len && texto.charAt(pos[0]) == '}') {
                pos[0]++;
                break;
            }
            throw new IllegalArgumentException("JSON inválido: esperado ',' ou '}'.");
        }
        return resultado;
    }

    private static void pularEspacos(String texto, int[] pos, int len) {
        while (pos[0] < len && Character.isWhitespace(texto.charAt(pos[0]))) {
            pos[0]++;
        }
    }

    private static String lerStringJson(String texto, int[] pos, int len) {
        if (pos[0] >= len || texto.charAt(pos[0]) != '"') {
            throw new IllegalArgumentException("JSON inválido: esperada string entre aspas.");
        }
        pos[0]++;
        StringBuilder sb = new StringBuilder();
        while (pos[0] < len) {
            char c = texto.charAt(pos[0]++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (pos[0] >= len) {
                    throw new IllegalArgumentException("JSON inválido: escape incompleto.");
                }
                char esc = texto.charAt(pos[0]++);
                switch (esc) {
                    case '"', '\\', '/' -> sb.append(esc);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos[0] + 4 > len) {
                            throw new IllegalArgumentException("JSON inválido: unicode incompleto.");
                        }
                        String hex = texto.substring(pos[0], pos[0] + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos[0] += 4;
                    }
                    default -> throw new IllegalArgumentException("JSON inválido: escape desconhecido.");
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("JSON inválido: string não fechada.");
    }

    private static String lerValorJson(String texto, int[] pos, int len) {
        if (pos[0] >= len) {
            throw new IllegalArgumentException("JSON inválido: valor ausente.");
        }
        char c = texto.charAt(pos[0]);
        if (c == '"') {
            return lerStringJson(texto, pos, len);
        }
        int inicio = pos[0];
        while (pos[0] < len) {
            char atual = texto.charAt(pos[0]);
            if (atual == ',' || atual == '}' || Character.isWhitespace(atual)) {
                break;
            }
            pos[0]++;
        }
        String token = texto.substring(inicio, pos[0]);
        if ("null".equals(token)) {
            return null;
        }
        return token;
    }

    public record DadosTarefaValidos(
            String nome,
            String descricao,
            LocalDate dataTermino,
            String status,
            int prioridade,
            String categoria
    ) {}
}
