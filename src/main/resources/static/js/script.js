// LISTA PADRÃO DE SEGURANÇA (Caso o localStorage esteja vazio na primeira execução)
const tarefasPadrao = [
    {
        id: 1,
        nome: "Estudar Estrutura de Dados",
        descricao: "Revisar listas encadeadas e árvores binárias",
        prioridade: 1,
        categoria: "Estudos",
        status: "doing",
        dataTermino: "2026-10-05"
    },
    {
        id: 2,
        nome: "Finalizar Front-end da ACZG",
        descricao: "Implementar e integrar o arquivo script.js com HTML",
        prioridade: 2,
        categoria: "Trabalho",
        status: "todo",
        dataTermino: "2026-09-30"
    },
    {
        id: 3,
        nome: "Academia",
        descricao: "Treino de pernas e corrida leve",
        prioridade: 5,
        categoria: "Saúde",
        status: "done",
        dataTermino: "2026-09-26"
    },
    {
        id: 4,
        nome: "Comprar mantimentos",
        descricao: "Ir ao supermercado comprar frutas, frango e ovos",
        prioridade: 3,
        categoria: "Pessoal",
        status: "todo",
        dataTermino: "2026-09-28"
    },
    {
        id: 5,
        nome: "Refatorar API do Backend",
        descricao: "Otimizar as consultas do banco de dados e aplicar Clean Code",
        prioridade: 1,
        categoria: "Trabalho",
        status: "doing",
        dataTermino: "2026-10-02"
    },
    {
        id: 6,
        nome: "Ler livro de Metodologias Ágeis",
        descricao: "Ler os capítulos 3 e 4 sobre Scrum e Kanban",
        prioridade: 4,
        categoria: "Estudos",
        status: "todo",
        dataTermino: "2026-10-10"
    },
    {
        id: 7,
        nome: "Consulta com Dentista",
        descricao: "Check-up de rotina e limpeza semestral",
        prioridade: 2,
        categoria: "Saúde",
        status: "todo",
        dataTermino: "2026-10-01"
    }
];

let dadosSalvos = localStorage.getItem("meuBoard");
let tarefas;

if (dadosSalvos !== null) {
    tarefas = JSON.parse(dadosSalvos);
} else {
    tarefas = tarefasPadrao;
}

let filtroAtual = "";

// salvar a lista de tarefas para não limpar toda vez que recarrega
function salvarNoNavegador() {
    localStorage.setItem("meuBoard", JSON.stringify(tarefas));
}

// apagar tarefa
function deletarTarefa(idTarefa) {
    const confirmou = confirm("Tem certeza que deseja excluir esta tarefa?");

    if (confirmou) {
        tarefas = tarefas.filter(tarefa => tarefa.id !== idTarefa);
        salvarNoNavegador();
        alert("Tarefa removida com sucesso da lista de dados!");
        renderizarTabela(filtroAtual);
    }
}

function renderizarTabela(statusFiltro = "") {
    filtroAtual = statusFiltro;
    const tabelaBody = document.querySelector(".formulario-listar-tarefa-table tbody");
    if (!tabelaBody)
        return;

    tabelaBody.innerHTML = "";
    const tarefasFiltradas = tarefas.filter(tarefa => {
        if (statusFiltro === "")
            return true;
        return tarefa.status === statusFiltro;
    });

    tarefasFiltradas.forEach(tarefa => {
        const linha = document.createElement("tr");

        linha.innerHTML = `
            <td><input type="text" class="edit-nome" value="${tarefa.nome}"></td>
            <td><input type="text" class="edit-descricao" value="${tarefa.descricao}"></td>
            <td><input type="number" class="edit-prioridade" value="${tarefa.prioridade}" min="1" max="5"></td>
            <td><input type="text" class="edit-categoria" value="${tarefa.categoria}"></td>
            <td>
                <select class="edit-status">
                    <option value="todo" ${tarefa.status === 'todo' ? 'selected' : ''}>To Do</option>
                    <option value="doing" ${tarefa.status === 'doing' ? 'selected' : ''}>Doing</option>
                    <option value="done" ${tarefa.status === 'done' ? 'selected' : ''}>Done</option>
                </select>
            </td>
            <td><input type="date" class="edit-data" value="${tarefa.dataTermino}"></td>
            <td>
                <button class="btn-salvar" onclick="atualizarTarefa(${tarefa.id}, this)">Salvar</button>
                <button class="btn-excluir" onclick="deletarTarefa(${tarefa.id})">Excluir</button>
            </td>
        `;
        tabelaBody.appendChild(linha);
    });
}

// editar tarefa
function atualizarTarefa(idTarefa, botaoClicado) {
    const linha = botaoClicado.closest("tr");

    const novoNome = linha.querySelector(".edit-nome").value;
    const novaDescricao = linha.querySelector(".edit-descricao").value;
    const novaPrioridade = parseInt(linha.querySelector(".edit-prioridade").value, 10);
    const novaCategoria = linha.querySelector(".edit-categoria").value;
    const novoStatus = linha.querySelector(".edit-status").value;
    const novaData = linha.querySelector(".edit-data").value;

    for (let i = 0; i < tarefas.length; i++) {
        if (tarefas[i].id === idTarefa) {
            tarefas[i].nome = novoNome;
            tarefas[i].descricao = novaDescricao;
            tarefas[i].prioridade = novaPrioridade;
            tarefas[i].categoria = novaCategoria;
            tarefas[i].status = novoStatus;
            tarefas[i].dataTermino = novaData;
            break;
        }
    }

    salvarNoNavegador();
    alert("Tarefa alterada e salva com sucesso no navegador!");
    renderizarTabela(filtroAtual);
}


// busca com filtro
function inicializarFiltro() {
    const selectStatusFiltro = document.querySelector("select[name='status']") || document.querySelector("#status");
    const botaoFiltrar = document.querySelector(".btn-filtrar") || document.querySelector("button[type='button']");

    if (!selectStatusFiltro)
        return;

    selectStatusFiltro.addEventListener("change", (evento) => {
        renderizarTabela(evento.target.value);
    });

    if (botaoFiltrar) {
        botaoFiltrar.addEventListener("click", () => {
            renderizarTabela(selectStatusFiltro.value);
        });
    }
}


// adicionar tarefa
function adicionarTarefa() {
    const formulario = document.querySelector(".formulario-adicionar-tarefa");
    if (!formulario)
        return;

    formulario.addEventListener("submit", (evento) => {
        evento.preventDefault(); // Impede a página de recarregar

        // dados de entrada
        const novoNome = formulario.querySelector(".form-nome input").value;
        const novaDescricao = formulario.querySelector(".form-descricao textarea")?.value || formulario.querySelector(".form-descricao input")?.value;
        const novaPrioridade = parseInt(formulario.querySelector(".form-prioridade input").value, 10);
        const novaCategoria = formulario.querySelector(".form-categoria input").value;
        const novoStatus = formulario.querySelector(".form-status select").value || "todo";
        const novaData = formulario.querySelector(".form-dataTermino input").value;

        let maiorId = 0;
        for (let i = 0; i < tarefas.length; i++) {
            let t = tarefas[i];
            if (t.id > maiorId) {
                maiorId = t.id;
            }
        }

        const novaTarefa = {
            id: maiorId + 1,
            nome: novoNome,
            descricao: novaDescricao,
            prioridade: novaPrioridade,
            categoria: novaCategoria,
            status: novoStatus,
            dataTermino: novaData
        };

        tarefas.push(novaTarefa);
        salvarNoNavegador();

        alert("Tarefa criada com sucesso!!");
        formulario.reset();

        renderizarTabela(filtroAtual);
    });
}

// Inicializacao da pag
document.addEventListener("DOMContentLoaded", () => {
    renderizarTabela();
    inicializarFiltro();
    adicionarTarefa();
});
