package br.com.fiap.mobilidade.dto.response;

public record RankingResponse(
        Integer posicao,
        Long idUsuario,
        String nomeUsuario,
        String tipoUsuario,
        Integer pontosRecompensa
) {}
