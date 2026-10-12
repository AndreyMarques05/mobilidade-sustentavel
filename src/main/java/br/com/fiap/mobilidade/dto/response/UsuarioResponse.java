package br.com.fiap.mobilidade.dto.response;

import br.com.fiap.mobilidade.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Usuario.TipoUsuario tipo,
        String role,
        Integer pontosRecompensa,
        LocalDateTime dataCadastro,
        Boolean ativo
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(), u.getTipo(),
                u.getRole().name(), u.getPontosRecompensa(), u.getDataCadastro(), u.getAtivo()
        );
    }
}
