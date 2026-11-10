package br.ufba.matc76.catalogo.model;

public record Chamado(
        Long id,
        String titulo,
        String solicitante,
        String setor,
        Prioridade prioridade,
        Status status
) {}
