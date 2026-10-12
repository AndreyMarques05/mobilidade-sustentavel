package br.com.fiap.mobilidade.repository;

import br.com.fiap.mobilidade.model.TipoTransporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoTransporteRepository extends JpaRepository<TipoTransporte, Long> {

    List<TipoTransporte> findByAtivoTrue();

    boolean existsByNomeIgnoreCase(String nome);
}
