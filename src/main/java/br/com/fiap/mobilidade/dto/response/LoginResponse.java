package br.com.fiap.mobilidade.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        Long id,
        String nome,
        String email,
        String role
) {}
