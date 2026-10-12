package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.TransporteRequest;
import br.com.fiap.mobilidade.dto.response.TransporteResponse;
import br.com.fiap.mobilidade.exception.BusinessException;
import br.com.fiap.mobilidade.exception.ResourceNotFoundException;
import br.com.fiap.mobilidade.model.TipoTransporte;
import br.com.fiap.mobilidade.repository.TipoTransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransporteService {

    private final TipoTransporteRepository repository;

    @Transactional(readOnly = true)
    public List<TransporteResponse> listarAtivos() {
        return repository.findByAtivoTrue().stream().map(TransporteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransporteResponse buscarPorId(Long id) {
        return TransporteResponse.from(getOrThrow(id));
    }

    @Transactional
    public TransporteResponse criar(TransporteRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException("Tipo de transporte já cadastrado: " + request.nome());
        }
        TipoTransporte t = TipoTransporte.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .emissaoCo2PorKm(request.emissaoCo2PorKm())
                .pontosPorKm(request.pontosPorKm())
                .build();
        return TransporteResponse.from(repository.save(t));
    }

    @Transactional
    public TransporteResponse atualizar(Long id, TransporteRequest request) {
        TipoTransporte t = getOrThrow(id);
        t.setNome(request.nome());
        t.setDescricao(request.descricao());
        t.setEmissaoCo2PorKm(request.emissaoCo2PorKm());
        t.setPontosPorKm(request.pontosPorKm());
        return TransporteResponse.from(repository.save(t));
    }

    @Transactional
    public void desativar(Long id) {
        TipoTransporte t = getOrThrow(id);
        t.setAtivo(false);
        repository.save(t);
    }

    public TipoTransporte getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de transporte não encontrado: id=" + id));
    }
}
