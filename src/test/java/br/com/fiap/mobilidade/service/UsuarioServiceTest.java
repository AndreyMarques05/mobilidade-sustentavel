package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.UsuarioRequest;
import br.com.fiap.mobilidade.dto.response.UsuarioResponse;
import br.com.fiap.mobilidade.exception.BusinessException;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import br.com.fiap.mobilidade.repository.ViagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de cadastro de usuário: unicidade de e-mail, hash de senha
 * e papel atribuído por padrão.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ViagemRepository viagemRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioService usuarioService;

    private UsuarioRequest request() {
        return new UsuarioRequest("Andrey", "andrey@fiap.com.br", "senha123",
                Usuario.TipoUsuario.PESSOA_FISICA);
    }

    @Test
    @DisplayName("Cadastro grava a senha com hash e atribui o papel USER")
    void cadastroGravaSenhaComHashEPapelUser() {
        when(usuarioRepository.existsByEmail("andrey@fiap.com.br")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hash-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse response = usuarioService.cadastrar(request());

        assertThat(response.id()).isEqualTo(1L);
        verify(passwordEncoder).encode("senha123");

        org.mockito.ArgumentCaptor<Usuario> captor =
                org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("$2a$10$hash-simulado");
        assertThat(captor.getValue().getSenha()).isNotEqualTo("senha123");
        assertThat(captor.getValue().getRole()).isEqualTo(Usuario.Role.USER);
    }

    @Test
    @DisplayName("E-mail já cadastrado é recusado com BusinessException")
    void emailDuplicadoEhRecusado() {
        when(usuarioRepository.existsByEmail("andrey@fiap.com.br")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("andrey@fiap.com.br");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Buscar usuário inexistente lança ResourceNotFoundException")
    void usuarioInexistenteLancaExcecao() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Desativar usuário mantém o registro, apenas marcando-o como inativo")
    void desativarMantemRegistro() {
        Usuario usuario = Usuario.builder().id(1L).nome("Andrey").ativo(true).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.desativar(1L);

        assertThat(usuario.getAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
        verify(usuarioRepository, never()).delete(any());
    }
}
