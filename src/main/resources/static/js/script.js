const API_BASE_URL =
    window.location.protocol === "file:" || window.location.port !== "8080"
        ? "http://localhost:8080/api/tarefas"
        : "/api/tarefas";

let tarefas = [];
let filtroAtual = "";
let servidorOffline = false;

function escaparHtml(valor) {
    if (valor === null || valor === undefined) {
        return "";
    }
    return String(valor)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function obterOuCriarBanner() {
    let banner = document.getElementById("banner-status-api");
    if (!banner) {
        const container = document.querySelector("main") || document.body;
        banner = document.createElement("div");
        banner.id = "banner-status-api";
        banner.className = "banner-status-api";
        banner.setAttribute("role", "alert");
        container.prepend(banner);
    }
    return banner;
}

function exibirBannerErro(mensagem) {
    const banner = obterOuCriarBanner();
    banner.textContent = mensagem;
    banner.style.display = "block";
}

function limparBannerErro() {
    const banner = document.getElementById("banner-status-api");
    if (banner) {
        banner.style.display = "none";
        banner.textContent = "";
    }
}

function definirEstadoOffline(offline) {
    servidorOffline = offline;
    if (offline) {
        exibirBannerErro("Servidor offline — não foi possível sincronizar");
    } else {
        limparBannerErro();
    }

    document.querySelectorAll(".btn-enviar, .btn-salvar, .btn-excluir").forEach(btn => {
        btn.disabled = offline;
    });
}

async function extrairMensagemErro(resposta, fallback) {
    try {
        const dados = await resposta.json();
        if (dados && dados.erro) {
            return dados.erro;
        }
    } catch (_) {
        // Ignora erro de parse caso a resposta não seja JSON
    }
    return fallback;
}

async function carregarTarefas() {
    try {
        const resposta = await fetch(API_BASE_URL);
        if (!resposta.ok) {
            const msg = await extrairMensagemErro(resposta, "Erro ao carregar tarefas do servidor.");
            exibirBannerErro(msg);
            tarefas = [];
            renderizarTabela(filtroAtual);
            return false;
        }
        tarefas = await resposta.json();
        definirEstadoOffline(false);
        renderizarTabela(filtroAtual);
        return true;
    } catch (_) {
        tarefas = [];
        definirEstadoOffline(true);
        renderizarTabela(filtroAtual);
        return false;
    }
}

async function deletarTarefa(idTarefa, botaoClicado) {
    if (servidorOffline) {
        exibirBannerErro("Servidor offline — não foi possível sincronizar");
        return;
    }

    const confirmou = confirm("Tem certeza que deseja excluir esta tarefa?");
    if (!confirmou) {
        return;
    }

    if (botaoClicado) {
        botaoClicado.disabled = true;
    }

    try {
        const resposta = await fetch(`${API_BASE_URL}/${idTarefa}`, {
            method: "DELETE"
        });

        if (resposta.status === 404) {
            alert("Tarefa não encontrada no servidor. A lista será atualizada.");
            await carregarTarefas();
            return;
        }

        if (!resposta.ok) {
            const msg = await extrairMensagemErro(resposta, "Erro ao excluir tarefa.");
            exibirBannerErro(msg);
            if (botaoClicado) {
                botaoClicado.disabled = false;
            }
            return;
        }

        alert("Tarefa removida com sucesso!");
        await carregarTarefas();
    } catch (_) {
        definirEstadoOffline(true);
    }
}

function renderizarTabela(statusFiltro = "") {
    filtroAtual = statusFiltro;
    const tabelaBody = document.querySelector(".formulario-listar-tarefa-table tbody");
    if (!tabelaBody) {
        return;
    }

    tabelaBody.innerHTML = "";
    const tarefasFiltradas = tarefas.filter(tarefa => {
        if (statusFiltro === "") {
            return true;
        }
        return tarefa.status === statusFiltro;
    });

    if (tarefasFiltradas.length === 0) {
        const linhaVazia = document.createElement("tr");
        linhaVazia.innerHTML = `<td colspan="7" style="text-align: center;">Nenhuma tarefa encontrada.</td>`;
        tabelaBody.appendChild(linhaVazia);
        return;
    }

    tarefasFiltradas.forEach(tarefa => {
        const linha = document.createElement("tr");
        const desabilitado = servidorOffline ? "disabled" : "";

        linha.innerHTML = `
            <td><input type="text" class="edit-nome" value="${escaparHtml(tarefa.nome)}"></td>
            <td><input type="text" class="edit-descricao" value="${escaparHtml(tarefa.descricao)}"></td>
            <td><input type="number" class="edit-prioridade" value="${escaparHtml(tarefa.prioridade)}" min="1" max="5"></td>
            <td><input type="text" class="edit-categoria" value="${escaparHtml(tarefa.categoria)}"></td>
            <td>
                <select class="edit-status">
                    <option value="todo" ${tarefa.status === "todo" ? "selected" : ""}>To Do</option>
                    <option value="doing" ${tarefa.status === "doing" ? "selected" : ""}>Doing</option>
                    <option value="done" ${tarefa.status === "done" ? "selected" : ""}>Done</option>
                </select>
            </td>
            <td><input type="date" class="edit-data" value="${escaparHtml(tarefa.dataTermino)}"></td>
            <td>
                <button type="button" class="btn-salvar" ${desabilitado} onclick="atualizarTarefa(${tarefa.id}, this)">Salvar</button>
                <button type="button" class="btn-excluir" ${desabilitado} onclick="deletarTarefa(${tarefa.id}, this)">Excluir</button>
            </td>
        `;
        tabelaBody.appendChild(linha);
    });
}

async function atualizarTarefa(idTarefa, botaoClicado) {
    if (servidorOffline) {
        exibirBannerErro("Servidor offline — não foi possível sincronizar");
        return;
    }

    const linha = botaoClicado.closest("tr");
    const botoesLinha = linha.querySelectorAll(".btn-salvar, .btn-excluir");
    botoesLinha.forEach(b => { b.disabled = true; });

    const payload = {
        nome: linha.querySelector(".edit-nome").value,
        descricao: linha.querySelector(".edit-descricao").value,
        prioridade: parseInt(linha.querySelector(".edit-prioridade").value, 10),
        categoria: linha.querySelector(".edit-categoria").value,
        status: linha.querySelector(".edit-status").value,
        dataTermino: linha.querySelector(".edit-data").value
    };

    try {
        const resposta = await fetch(`${API_BASE_URL}/${idTarefa}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (resposta.status === 404) {
            alert("Tarefa não encontrada no servidor. A lista será atualizada.");
            await carregarTarefas();
            return;
        }

        if (!resposta.ok) {
            const msg = await extrairMensagemErro(resposta, "Erro ao atualizar tarefa.");
            exibirBannerErro(msg);
            botoesLinha.forEach(b => { b.disabled = false; });
            return;
        }

        limparBannerErro();
        alert("Tarefa alterada e salva com sucesso!");
        await carregarTarefas();
    } catch (_) {
        definirEstadoOffline(true);
    }
}

function inicializarFiltro() {
    const formFiltro = document.querySelector(".formulario-listar-tarefa");
    const selectStatusFiltro = document.querySelector("select[name='status']") || document.querySelector("#status");

    if (formFiltro) {
        formFiltro.addEventListener("submit", (evento) => {
            evento.preventDefault();
            if (selectStatusFiltro) {
                renderizarTabela(selectStatusFiltro.value);
            }
        });
    }

    if (!selectStatusFiltro) {
        return;
    }

    selectStatusFiltro.addEventListener("change", (evento) => {
        renderizarTabela(evento.target.value);
    });
}

function inicializarCriacaoTarefa() {
    const formulario = document.querySelector(".formulario-adicionar-tarefa");
    if (!formulario) {
        return;
    }

    const botaoEnviar = formulario.querySelector(".btn-enviar");

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        if (servidorOffline) {
            exibirBannerErro("Servidor offline — não foi possível sincronizar");
            return;
        }

        const payload = {
            nome: formulario.querySelector(".form-nome input").value,
            descricao: formulario.querySelector(".form-descricao textarea")?.value || formulario.querySelector(".form-descricao input")?.value || "",
            prioridade: parseInt(formulario.querySelector(".form-prioridade input").value, 10),
            categoria: formulario.querySelector(".form-categoria input").value,
            status: formulario.querySelector(".form-status select").value || "todo",
            dataTermino: formulario.querySelector(".form-dataTermino input").value
        };

        if (botaoEnviar) {
            botaoEnviar.disabled = true;
        }

        try {
            const resposta = await fetch(API_BASE_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!resposta.ok) {
                const msg = await extrairMensagemErro(resposta, "Erro ao criar tarefa. Verifique os campos.");
                exibirBannerErro(msg);
                if (botaoEnviar) {
                    botaoEnviar.disabled = false;
                }
                return;
            }

            limparBannerErro();
            alert("Tarefa criada com sucesso!!");
            formulario.reset();
            if (botaoEnviar) {
                botaoEnviar.disabled = false;
            }
        } catch (_) {
            definirEstadoOffline(true);
        }
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    inicializarFiltro();
    inicializarCriacaoTarefa();
    await carregarTarefas();
});
