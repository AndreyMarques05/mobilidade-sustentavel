package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.ViagemRequest;
import br.com.fiap.mobilidade.dto.response.ViagemResponse;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.TipoTransporte;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.model.Viagem;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import br.com.fiap.mobilidade.repository.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final UsuarioService usuarioService;
    private final TransporteService transporteService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ViagemResponse registrar(Long idUsuario, ViagemRequest request) {
        Usuario usuario = usuarioService.getOrThrow(idUsuario);
        TipoTransporte transporte = transporteService.getOrThrow(request.idTipoTransporte());

        Viagem viagem = Viagem.builder()
                .usuario(usuario)
                .tipoTransporte(transporte)
                .origem(request.origem())
                .destino(request.destino())
                .distanciaKm(request.distanciaKm())
                .descricao(request.descricao())
                .build();

        viagem = viagemRepository.save(viagem);

        // Atualiza pontos do usuário
        usuario.setPontosRecompensa(usuario.getPontosRecompensa() + viagem.getPontosGanhos());
        usuarioRepository.save(usuario);

        return ViagemResponse.from(viagem);
    }

    @Transactional(readOnly = true)
    public ViagemResponse buscarPorId(Long id) {
        return ViagemResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ViagemResponse> listarPorUsuario(Long idUsuario) {
        usuarioService.getOrThrow(idUsuario);
        return viagemRepository.findByUsuarioIdOrderByDataViagemDesc(idUsuario)
                .stream().map(ViagemResponse::from).toList();
    }

    @Transactional
    public ViagemResponse atualizar(Long id, ViagemRequest request) {
        Viagem viagem = getOrThrow(id);
        TipoTransporte transporte = transporteService.getOrThrow(request.idTipoTransporte());

        int pontosAntigos = viagem.getPontosGanhos();

        viagem.setTipoTransporte(transporte);
        viagem.setOrigem(request.origem());
        viagem.setDestino(request.destino());
        viagem.setDistanciaKm(request.distanciaKm());
        viagem.setDescricao(request.descricao());
        // Recalcula via @PrePersist manual
        recalcular(viagem);

        viagem = viagemRepository.save(viagem);

        // Ajusta pontos do usuário
        Usuario usuario = viagem.getUsuario();
        usuario.setPontosRecompensa(usuario.getPontosRecompensa() - pontosAntigos + viagem.getPontosGanhos());
        usuarioRepository.save(usuario);

        return ViagemResponse.from(viagem);
    }

    @Transactional
    public void excluir(Long id) {
        Viagem viagem = getOrThrow(id);
        Usuario usuario = viagem.getUsuario();
        usuario.setPontosRecompensa(Math.max(0, usuario.getPontosRecompensa() - viagem.getPontosGanhos()));
        usuarioRepository.save(usuario);
        viagemRepository.delete(viagem);
    }

    private void recalcular(Viagem v) {
        double economizado = (Viagem.EMISSAO_CARRO_REFERENCIA_KG_POR_KM
                - v.getTipoTransporte().getEmissaoCo2PorKm()) * v.getDistanciaKm();
        v.setCo2EconomizadoKg(Math.max(0.0, economizado));
        v.setPontosGanhos((int) (v.getDistanciaKm() * v.getTipoTransporte().getPontosPorKm()));
    }

    public Viagem getOrThrow(Long id) {
        return viagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada: id=" + id));
    }
}
