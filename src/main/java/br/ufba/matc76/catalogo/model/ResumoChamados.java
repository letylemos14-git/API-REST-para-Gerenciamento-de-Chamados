package br.ufba.matc76.catalogo.model;

public record ResumoChamados(
        long abertos,
        long emAndamento,
        long resolvidos,
        long total
) {}
