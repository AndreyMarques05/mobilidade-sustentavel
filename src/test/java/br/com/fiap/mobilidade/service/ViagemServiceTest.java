package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.ViagemRequest;
import br.com.fiap.mobilidade.dto.response.ViagemResponse;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.TipoTransporte;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.model.Viagem;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import br.com.fiap.mobilidade.repository.ViagemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de regra de negócio do registro de viagens — inclusive o efeito
 * colateral de creditar os pontos de recompensa ao usuário.
 */
@ExtendWith(MockitoExtension.class)
class ViagemServiceTest {

    @Mock private ViagemRepository viagemRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private TransporteService transporteService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private ViagemService viagemService;

    private Usuario usuario;
    private TipoTransporte bicicleta;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).nome("Andrey").email("andrey@fiap.com.br")
                .pontosRecompensa(50)
                .build();

        bicicleta = TipoTransporte.builder()
                .id(2L).nome("Bicicleta")
                .emissaoCo2PorKm(0.0).pontosPorKm(10)
                .build();
    }

    private Viagem viagemPersistida(int pontos, double co2) {
        return Viagem.builder()
                .id(99L)
                .usuario(usuario)
                .tipoTransporte(bicicleta)
                .origem("Casa").destino("Faculdade")
                .distanciaKm(10.0)
                .dataViagem(LocalDateTime.now())
                .co2EconomizadoKg(co2)
                .pontosGanhos(pontos)
                .build();
    }

    @Test
    @DisplayName("Registrar viagem devolve os dados calculados e credita pontos ao usuário")
    void registrarCreditaPontosAoUsuario() {
        ViagemRequest request = new ViagemRequest(2L, "Casa", "Faculdade", 10.0, "Ida para a aula");

        when(usuarioService.getOrThrow(1L)).thenReturn(usuario);
        when(transporteService.getOrThrow(2L)).thenReturn(bicicleta);
        when(viagemRepository.save(any(Viagem.class))).thenReturn(viagemPersistida(100, 2.1));

        ViagemResponse response = viagemService.registrar(1L, request);

        assertThat(response.pontosGanhos()).isEqualTo(100);
        assertThat(response.co2EconomizadoKg()).isEqualTo(2.1);
        assertThat(response.tipoTransporte()).isEqualTo("Bicicleta");
        // 50 pontos que o usuário já tinha + 100 da viagem
        assertThat(usuario.getPontosRecompensa()).isEqualTo(150);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Buscar viagem inexistente lança ResourceNotFoundException")
    void buscarViagemInexistenteLancaExcecao() {
        when(viagemRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> viagemService.buscarPorId(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Excluir viagem estorna os pontos do usuário")
    void excluirEstornaPontos() {
        Viagem viagem = viagemPersistida(30, 0.6);
        when(viagemRepository.findById(99L)).thenReturn(Optional.of(viagem));

        viagemService.excluir(99L);

        // 50 pontos iniciais - 30 estornados
        assertThat(usuario.getPontosRecompensa()).isEqualTo(20);
        verify(viagemRepository).delete(viagem);
    }

    @Test
    @DisplayName("Estorno nunca deixa o saldo de pontos negativo")
    void estornoNaoDeixaSaldoNegativo() {
        Viagem viagem = viagemPersistida(500, 12.0);
        when(viagemRepository.findById(99L)).thenReturn(Optional.of(viagem));

        viagemService.excluir(99L);

        assertThat(usuario.getPontosRecompensa()).isZero();
    }

    @Test
    @DisplayName("Listar viagens valida a existência do usuário antes de consultar")
    void listarValidaUsuario() {
        when(usuarioService.getOrThrow(1L)).thenReturn(usuario);
        when(viagemRepository.findByUsuarioIdOrderByDataViagemDesc(1L))
                .thenReturn(java.util.List.of(viagemPersistida(100, 2.1)));

        assertThat(viagemService.listarPorUsuario(1L)).hasSize(1);
        verify(usuarioService).getOrThrow(1L);
    }
}
