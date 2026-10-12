package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.RecompensaRequest;
import br.com.fiap.mobilidade.dto.response.RecompensaResponse;
import br.com.fiap.mobilidade.exception.BusinessException;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.Recompensa;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.repository.RecompensaRepository;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<RecompensaResponse> listarAtivas() {
        return recompensaRepository.findByAtivoTrueOrderByPontosNecessariosAsc()
                .stream().map(RecompensaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RecompensaResponse buscarPorId(Long id) {
        return RecompensaResponse.from(getOrThrow(id));
    }

    @Transactional
    public RecompensaResponse criar(RecompensaRequest request) {
        Recompensa r = Recompensa.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .pontosNecessarios(request.pontosNecessarios())
                .quantidade(request.quantidade())
                .build();
        return RecompensaResponse.from(recompensaRepository.save(r));
    }

    @Transactional
    public void desativar(Long id) {
        Recompensa r = getOrThrow(id);
        r.setAtivo(false);
        recompensaRepository.save(r);
    }

    @Transactional
    public String resgatar(Long idUsuario, Long idRecompensa) {
        Usuario usuario = usuarioService.getOrThrow(idUsuario);
        Recompensa recompensa = getOrThrow(idRecompensa);

        if (!recompensa.getAtivo()) {
            throw new BusinessException("Recompensa não está disponível.");
        }
        if (recompensa.getQuantidade() <= 0) {
            throw new BusinessException("Recompensa esgotada.");
        }
        if (usuario.getPontosRecompensa() < recompensa.getPontosNecessarios()) {
            throw new BusinessException(String.format(
                    "Pontos insuficientes. Necessário: %d, disponível: %d",
                    recompensa.getPontosNecessarios(), usuario.getPontosRecompensa()
            ));
        }

        usuario.setPontosRecompensa(usuario.getPontosRecompensa() - recompensa.getPontosNecessarios());
        recompensa.setQuantidade(recompensa.getQuantidade() - 1);

        usuarioRepository.save(usuario);
        recompensaRepository.save(recompensa);

        return String.format("Recompensa '%s' resgatada com sucesso! Pontos restantes: %d",
                recompensa.getNome(), usuario.getPontosRecompensa());
    }

    public Recompensa getOrThrow(Long id) {
        return recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa não encontrada: id=" + id));
    }
}
