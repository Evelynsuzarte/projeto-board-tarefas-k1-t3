const API_BASE_URL =
    window.location.protocol === "file:" || window.location.port !== "8080"
        ? "http://localhost:8080/api/tarefas"
        : "/api/tarefas";

const CHAVE_ALARMES_CLIENTE = "meuBoardAlarmes";
const ANTECEDENCIA_PADRAO_MINUTOS = 120;
const OPCOES_ANTECEDENCIA_PADRAO = [15, 30, 60, 120, 1440];
const INTERVALO_VERIFICACAO_ALARMES_MS = 30000;

function alternarAntecedenciaCustom(selectEl, customInputEl) {
    if (!selectEl || !customInputEl) {
        return;
    }
    const ehCustom = selectEl.value === "custom";
    customInputEl.style.display = ehCustom ? "inline-block" : "none";
    if (!ehCustom) {
        customInputEl.value = "";
    }
}

function obterAntecedenciaEmMinutos(selectEl, customInputEl) {
    if (!selectEl) {
        return ANTECEDENCIA_PADRAO_MINUTOS;
    }
    if (selectEl.value === "custom") {
        const valorCustom = parseInt(customInputEl?.value, 10);
        return Number.isFinite(valorCustom) && valorCustom > 0 ? valorCustom : null;
    }
    return parseInt(selectEl.value, 10) || ANTECEDENCIA_PADRAO_MINUTOS;
}

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

function carregarMapaAlarmes() {
    try {
        const bruto = localStorage.getItem(CHAVE_ALARMES_CLIENTE);
        return bruto ? JSON.parse(bruto) : {};
    } catch (_) {
        return {};
    }
}

function salvarConfigAlarmeCliente(idTarefa, config) {
    const mapa = carregarMapaAlarmes();
    mapa[String(idTarefa)] = config;
    localStorage.setItem(CHAVE_ALARMES_CLIENTE, JSON.stringify(mapa));
}

function removerConfigAlarmeCliente(idTarefa) {
    const mapa = carregarMapaAlarmes();
    delete mapa[String(idTarefa)];
    localStorage.setItem(CHAVE_ALARMES_CLIENTE, JSON.stringify(mapa));
}

function enriquecerComAlarmes(listaBackend) {
    const mapa = carregarMapaAlarmes();
    return listaBackend.map(tarefa => {
        const salvo = mapa[String(tarefa.id)];
        const dataIso = tarefa.dataTermino || "";
        const dataCompletaSalva = salvo?.dataTerminoCompleta && salvo.dataTerminoCompleta.startsWith(dataIso)
            ? salvo.dataTerminoCompleta
            : (dataIso ? `${dataIso}T23:59` : "");

        return {
            ...tarefa,
            dataTerminoCompleta: dataCompletaSalva,
            alarme: salvo ? Boolean(salvo.alarme) : false,
            antecedenciaMinutos: salvo ? (Number(salvo.antecedenciaMinutos) || ANTECEDENCIA_PADRAO_MINUTOS) : ANTECEDENCIA_PADRAO_MINUTOS,
            alarmeDisparado: salvo ? Boolean(salvo.alarmeDisparado) : false
        };
    });
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

function obterOuCriarPainelAlarmes() {
    let painel = document.getElementById("painel-alarmes");
    if (!painel) {
        const container = document.querySelector("main") || document.body;
        painel = document.createElement("div");
        painel.id = "painel-alarmes";
        painel.className = "painel-alarmes";
        painel.setAttribute("role", "status");
        painel.style.display = "none";
        painel.innerHTML = `
            <div class="painel-alarmes-cabecalho">
                <span>Alarmes de Prazo</span>
                <button type="button" class="btn-dispensar-alarme" onclick="dispensarPainelAlarmes()">Dispensar</button>
            </div>
            <ul id="lista-alarmes-disparados"></ul>
        `;
        container.prepend(painel);
    }
    return painel;
}

function dispensarPainelAlarmes() {
    const painel = document.getElementById("painel-alarmes");
    const lista = document.getElementById("lista-alarmes-disparados");
    if (lista) {
        lista.innerHTML = "";
    }
    if (painel) {
        painel.style.display = "none";
    }
}

function formatarDataHoraExibicao(dataCompleta) {
    if (!dataCompleta) {
        return "";
    }
    return dataCompleta.replace("T", " ");
}

function verificarAlarmes() {
    const agora = new Date();
    const disparadosAgora = [];

    tarefas.forEach(tarefa => {
        if (!tarefa.alarme || tarefa.status === "done" || tarefa.alarmeDisparado || !tarefa.dataTerminoCompleta) {
            return;
        }

        const prazo = new Date(tarefa.dataTerminoCompleta);
        if (Number.isNaN(prazo.getTime())) {
            return;
        }

        const antecedenciaMs = (tarefa.antecedenciaMinutos || ANTECEDENCIA_PADRAO_MINUTOS) * 60000;
        const inicioJanela = new Date(prazo.getTime() - antecedenciaMs);

        if (agora >= inicioJanela) {
            tarefa.alarmeDisparado = true;
            salvarConfigAlarmeCliente(tarefa.id, {
                dataTerminoCompleta: tarefa.dataTerminoCompleta,
                alarme: tarefa.alarme,
                antecedenciaMinutos: tarefa.antecedenciaMinutos,
                alarmeDisparado: true
            });
            disparadosAgora.push(tarefa);
        }
    });

    if (disparadosAgora.length === 0) {
        return;
    }

    const painel = obterOuCriarPainelAlarmes();
    const lista = document.getElementById("lista-alarmes-disparados");
    disparadosAgora.forEach(tarefa => {
        const item = document.createElement("li");
        item.textContent = `Tarefa "${tarefa.nome}" vence em ${formatarDataHoraExibicao(tarefa.dataTerminoCompleta)} (status: ${tarefa.status}).`;
        lista.appendChild(item);
    });
    painel.style.display = "block";
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
        const listaBackend = await resposta.json();
        tarefas = enriquecerComAlarmes(listaBackend);
        definirEstadoOffline(false);
        renderizarTabela(filtroAtual);
        verificarAlarmes();
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
            removerConfigAlarmeCliente(idTarefa);
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

        removerConfigAlarmeCliente(idTarefa);
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
        linhaVazia.innerHTML = `<td colspan="9" style="text-align: center;">Nenhuma tarefa encontrada.</td>`;
        tabelaBody.appendChild(linhaVazia);
        return;
    }

    tarefasFiltradas.forEach(tarefa => {
        const linha = document.createElement("tr");
        const desabilitado = servidorOffline ? "disabled" : "";
        const ant = Number(tarefa.antecedenciaMinutos) || ANTECEDENCIA_PADRAO_MINUTOS;
        const ehPredefinida = OPCOES_ANTECEDENCIA_PADRAO.includes(ant);

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
            <td><input type="datetime-local" class="edit-data" value="${escaparHtml(tarefa.dataTerminoCompleta)}"></td>
            <td style="text-align: center;">
                <input type="checkbox" class="edit-alarme" ${tarefa.alarme ? "checked" : ""}>
            </td>
            <td>
                <div class="antecedencia-controles">
                    <select class="edit-antecedencia" onchange="alternarAntecedenciaCustom(this, this.parentElement.querySelector('.edit-antecedencia-custom'))">
                        <option value="15" ${ant === 15 ? "selected" : ""}>15 min</option>
                        <option value="30" ${ant === 30 ? "selected" : ""}>30 min</option>
                        <option value="60" ${ant === 60 ? "selected" : ""}>1 hora</option>
                        <option value="120" ${ant === 120 ? "selected" : ""}>2 horas</option>
                        <option value="1440" ${ant === 1440 ? "selected" : ""}>24 horas</option>
                        <option value="custom" ${!ehPredefinida ? "selected" : ""}>Personalizado (min)</option>
                    </select>
                    <input type="number" class="edit-antecedencia-custom antecedencia-custom" min="1" placeholder="Min" value="${!ehPredefinida ? ant : ""}" style="display: ${!ehPredefinida ? "inline-block" : "none"};">
                </div>
            </td>
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

    const novaDataCompleta = linha.querySelector(".edit-data").value;
    const novoAlarme = Boolean(linha.querySelector(".edit-alarme")?.checked);
    const novaAntecedencia = obterAntecedenciaEmMinutos(
        linha.querySelector(".edit-antecedencia"),
        linha.querySelector(".edit-antecedencia-custom")
    );
    if (novaAntecedencia === null) {
        alert("Informe um tempo de antecedência personalizado válido (maior que 0 minutos).");
        botoesLinha.forEach(b => { b.disabled = false; });
        return;
    }

    const tarefaAnterior = tarefas.find(t => t.id === idTarefa);
    const reconfigurouAlarme = !tarefaAnterior ||
        novaDataCompleta !== tarefaAnterior.dataTerminoCompleta ||
        novoAlarme !== tarefaAnterior.alarme ||
        novaAntecedencia !== tarefaAnterior.antecedenciaMinutos;

    const payload = {
        nome: linha.querySelector(".edit-nome").value,
        descricao: linha.querySelector(".edit-descricao").value,
        prioridade: parseInt(linha.querySelector(".edit-prioridade").value, 10),
        categoria: linha.querySelector(".edit-categoria").value,
        status: linha.querySelector(".edit-status").value,
        dataTermino: novaDataCompleta ? novaDataCompleta.split("T")[0] : ""
    };

    try {
        const resposta = await fetch(`${API_BASE_URL}/${idTarefa}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (resposta.status === 404) {
            removerConfigAlarmeCliente(idTarefa);
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

        salvarConfigAlarmeCliente(idTarefa, {
            dataTerminoCompleta: novaDataCompleta,
            alarme: novoAlarme,
            antecedenciaMinutos: novaAntecedencia,
            alarmeDisparado: reconfigurouAlarme ? false : Boolean(tarefaAnterior?.alarmeDisparado)
        });

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
    const selectAntecedencia = formulario.querySelector(".form-antecedencia select");
    const inputAntecedenciaCustom = formulario.querySelector("#antecedencia-custom");

    if (selectAntecedencia && inputAntecedenciaCustom) {
        selectAntecedencia.addEventListener("change", () => {
            alternarAntecedenciaCustom(selectAntecedencia, inputAntecedenciaCustom);
        });
    }

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        if (servidorOffline) {
            exibirBannerErro("Servidor offline — não foi possível sincronizar");
            return;
        }

        const dataHoraInput = formulario.querySelector(".form-dataTermino input").value;
        const alarmeAtivo = Boolean(formulario.querySelector(".form-alarme input")?.checked);
        const antecedenciaMinutos = obterAntecedenciaEmMinutos(selectAntecedencia, inputAntecedenciaCustom);
        if (antecedenciaMinutos === null) {
            alert("Informe um tempo de antecedência personalizado válido (maior que 0 minutos).");
            return;
        }

        const payload = {
            nome: formulario.querySelector(".form-nome input").value,
            descricao: formulario.querySelector(".form-descricao textarea")?.value || formulario.querySelector(".form-descricao input")?.value || "",
            prioridade: parseInt(formulario.querySelector(".form-prioridade input").value, 10),
            categoria: formulario.querySelector(".form-categoria input").value,
            status: formulario.querySelector(".form-status select").value || "todo",
            dataTermino: dataHoraInput ? dataHoraInput.split("T")[0] : ""
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

            const tarefaCriada = await resposta.json();
            salvarConfigAlarmeCliente(tarefaCriada.id, {
                dataTerminoCompleta: dataHoraInput,
                alarme: alarmeAtivo,
                antecedenciaMinutos,
                alarmeDisparado: false
            });

            limparBannerErro();
            alert("Tarefa criada com sucesso!!");
            formulario.reset();
            alternarAntecedenciaCustom(selectAntecedencia, inputAntecedenciaCustom);
            if (botaoEnviar) {
                botaoEnviar.disabled = false;
            }
            await carregarTarefas();
        } catch (_) {
            definirEstadoOffline(true);
        }
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    inicializarFiltro();
    inicializarCriacaoTarefa();
    await carregarTarefas();
    setInterval(verificarAlarmes, INTERVALO_VERIFICACAO_ALARMES_MS);
});
