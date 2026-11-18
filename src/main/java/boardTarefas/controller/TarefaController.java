package boardTarefas.controller;

import boardTarefas.model.Tarefa;
import boardTarefas.repository.TarefaRepository;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.*;
import java.io.IOException;

import static java.lang.Integer.parseInt;
import java.time.format.DateTimeFormatter;

public class TarefaController {
    DateTimeFormatter formatoBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TarefaRepository repositorio;

    public TarefaController() {
        this(new TarefaRepository());
    }

    public TarefaController(TarefaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public String sanitizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(";", ",")
                .replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }

    public void salvar(List<Tarefa> tarefas) {
        try {
            repositorio.salvar(tarefas);
        } catch (IOException e) {
            System.out.println("Erro ao salvar tarefas: " + e.getMessage());
        }
    }

    public List<Tarefa> carregar() {
        try {
            return repositorio.carregar();
        } catch (IOException e) {
            System.out.println("Erro ao carregar tarefas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Tarefa> criar (List<Tarefa> tarefas, int id, String nome, String descricao, LocalDate dataTermino, String status, int prioridade, String categoria){
        Tarefa novaTarefa = new Tarefa(
                id,
                sanitizarTexto(nome),
                sanitizarTexto(descricao),
                dataTermino,
                status,
                prioridade,
                sanitizarTexto(categoria)
        );
        tarefas.add(novaTarefa);
        salvar(tarefas);
        return tarefas;
    }

    public List<Tarefa> buscar (List<Tarefa> tarefas, String filtro, String opcao, LocalDate inicio, LocalDate fim) {
        List<Tarefa> tarefasBusca = new ArrayList<>();

        if ("categoria".equals(filtro) && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (Objects.equals(t.getCategoria(), opcao)) {
                    tarefasBusca.add(t);
                }
            }
        } else if ("categoria".equals(filtro) && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && Objects.equals(t.getCategoria(), opcao)) {
                    tarefasBusca.add(t);
                }
            }

        } else if ("prioridade".equals(filtro) && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (t.getPrioridade() == parseInt(opcao, 10)) {
                    tarefasBusca.add(t);
                }
            }

        } else if ("prioridade".equals(filtro) && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && t.getPrioridade() == parseInt(opcao, 10)) {
                    tarefasBusca.add(t);
                }
            }
        } else if ("status".equals(filtro) && inicio == null && fim == null) {
            for (Tarefa t : tarefas) {
                if (Objects.equals(t.getStatus(), opcao)) {
                    tarefasBusca.add(t);
                }
            }

        } else if ("status".equals(filtro) && inicio != null && fim != null) {
            for (Tarefa t : tarefas) {
                if ((t.getDataTermino().isAfter(inicio) || t.getDataTermino().isEqual(inicio)) &&
                        (t.getDataTermino().isBefore(fim) || t.getDataTermino().isEqual(fim)) && Objects.equals(t.getStatus(), opcao)) {
                    tarefasBusca.add(t);
                }
            }
        }
        else{
            return null;
        }

        return tarefasBusca;
    }

    public Tarefa buscar_id(int id,List<Tarefa> tarefas){
        for (Tarefa t : tarefas){
            if (t.getId() == id){
                return t;
            }
        }
        return null;
    }

    public int proximoId(List<Tarefa> tarefas) {
        int maiorId = 0;
        for (Tarefa t : tarefas) {
            if (t.getId() > maiorId) {
                maiorId = t.getId();
            }
        }
        return maiorId + 1;
    }

    public List<Tarefa> alterar(List<Tarefa> tarefas, int tarefaId, String campo, String info){
        Tarefa encontrado = buscar_id(tarefaId,tarefas);

        if (info != null && encontrado != null){
            if ("nome".equals(campo)){
                encontrado.setNome(sanitizarTexto(info));
            } else if ("descricao".equals(campo)) {
                encontrado.setDescricao(sanitizarTexto(info));
            } else if ("prioridade".equals(campo)){
                encontrado.setPrioridade(parseInt(info));
            } else if ("categoria".equals(campo)) {
                encontrado.setCategoria(sanitizarTexto(info));
            } else if ("status".equals(campo)){
                encontrado.setStatus(info);
            } else if ("data".equals(campo)){
                encontrado.setDataTermino(LocalDate.parse(info, formatoBR));
            }

            for (int i = 0; i < tarefas.size(); i++){
                if (tarefas.get(i).getId() == encontrado.getId()){
                    tarefas.set(i, encontrado);
                    break;
                }
            }
        } else{
            return null;
        }

        salvar(tarefas);
        return tarefas;


    }

    public List<Tarefa> deletar(List<Tarefa> tarefas, int tarefaId){
        Tarefa encontrado = buscar_id(tarefaId,tarefas);
        if (encontrado == null){
            return null;
        }

        for (int i = 0; i < tarefas.size(); i++){
            if (tarefas.get(i).getId() == encontrado.getId()){
                tarefas.remove(i);
                break;
            }
        }
        salvar(tarefas);
        return tarefas;

    }

    public synchronized List<Tarefa> listarSincronizado() {
        return carregar();
    }

    public synchronized List<Tarefa> buscarSincronizado(String filtro, String opcao, LocalDate inicio, LocalDate fim) {
        return buscar(carregar(), filtro, opcao, inicio, fim);
    }

    public synchronized Tarefa criarSincronizado(String nome, String descricao, LocalDate dataTermino, String status, int prioridade, String categoria) {
        List<Tarefa> tarefas = carregar();
        int id = proximoId(tarefas);
        criar(tarefas, id, nome, descricao, dataTermino, status, prioridade, categoria);
        return buscar_id(id, tarefas);
    }

    public synchronized Tarefa atualizarSincronizado(int id, String nome, String descricao, LocalDate dataTermino, String status, int prioridade, String categoria) {
        List<Tarefa> tarefas = carregar();
        Tarefa encontrado = buscar_id(id, tarefas);
        if (encontrado == null) {
            return null;
        }
        encontrado.setNome(sanitizarTexto(nome));
        encontrado.setDescricao(sanitizarTexto(descricao));
        encontrado.setDataTermino(dataTermino);
        encontrado.setStatus(status);
        encontrado.setPrioridade(prioridade);
        encontrado.setCategoria(sanitizarTexto(categoria));
        salvar(tarefas);
        return encontrado;
    }

    public synchronized boolean deletarSincronizado(int id) {
        List<Tarefa> tarefas = carregar();
        return deletar(tarefas, id) != null;
    }

//    public void listarTodas (List<Tarefa> tarefas){
//        System.out.println("ID\tCATEGORIA\tPRIORIDADE\tSTATUS\tNOME\tDESCRICAO\tDATA TERMINO");
//        for (Tarefa t : tarefas){
//            System.out.println(t.getId()+"\t"+t.getCategoria()+"\t"+t.getPrioridade()+"\t"+t.getStatus()+"\t"+t.getNome()+"\t"+t.getDescricao()+"\t"+t.getDataTermino());
//        }
//    }




//    public void listarUnico (List<Tarefa> tarefas, int id){
//        System.out.println("ID\tCATEGORIA\tPRIORIDADE\tSTATUS\tNOME\tDESCRICAO\tDATA TERMINO");
//        for (Tarefa t : tarefas){
//            if (t.getId() == id){
//                System.out.println(t.getId()+"\t"+t.getCategoria()+"\t"+t.getPrioridade()+"\t"+t.getStatus()+"\t"+t.getNome()+"\t"+t.getDescricao()+"\t"+t.getDataTermino());
//            }
//        }
//    }




}























