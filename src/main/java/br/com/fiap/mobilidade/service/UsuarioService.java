package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.UsuarioRequest;
import br.com.fiap.mobilidade.dto.response.ImpactoResponse;
import br.com.fiap.mobilidade.dto.response.RankingResponse;
import br.com.fiap.mobilidade.dto.response.UsuarioResponse;
import br.com.fiap.mobilidade.exception.BusinessException;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import br.com.fiap.mobilidade.repository.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ViagemRepository viagemRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado: " + request.email());
        }
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .tipo(request.tipo())
                .role(Usuario.Role.USER)
                .build();
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findByAtivoTrue()
                .stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(getOrThrow(id));
    }

    @Transactional
    public UsuarioResponse atualizarNome(Long id, String novoNome) {
        Usuario usuario = getOrThrow(id);
        usuario.setNome(novoNome);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = getOrThrow(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public ImpactoResponse calcularImpacto(Long id) {
        Usuario usuario = getOrThrow(id);
        int totalViagens = viagemRepository.findByUsuarioIdOrderByDataViagemDesc(id).size();
        Double distancia = viagemRepository.sumDistanciaByUsuario(id);
        Double co2 = viagemRepository.sumCo2EconomizadoByUsuario(id);
        Integer pontos = viagemRepository.sumPontosGanhosByUsuario(id);

        return new ImpactoResponse(
                usuario.getId(),
                usuario.getNome(),
                totalViagens,
                distancia,
                co2,
                ImpactoResponse.calcularArvores(co2),
                pontos
        );
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> ranking() {
        List<Usuario> usuarios = usuarioRepository.findRankingByPontos();
        List<RankingResponse> resultado = new ArrayList<>();
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            resultado.add(new RankingResponse(
                    i + 1, u.getId(), u.getNome(), u.getTipo().name(), u.getPontosRecompensa()
            ));
        }
        return resultado;
    }

    public Usuario getOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + id));
    }
}
