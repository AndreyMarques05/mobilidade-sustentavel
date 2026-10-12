package br.com.fiap.mobilidade.repository;

import br.com.fiap.mobilidade.model.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {

    List<Recompensa> findByAtivoTrueOrderByPontosNecessariosAsc();
}
