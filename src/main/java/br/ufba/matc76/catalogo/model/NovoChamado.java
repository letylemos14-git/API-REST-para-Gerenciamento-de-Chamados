package br.ufba.matc76.catalogo.model;

public record NovoChamado(
        String titulo,
        String solicitante,
        String setor,
        String prioridade,
        String status
) {}