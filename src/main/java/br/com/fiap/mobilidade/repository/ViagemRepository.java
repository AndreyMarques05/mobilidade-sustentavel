package br.com.fiap.mobilidade.repository;

import br.com.fiap.mobilidade.model.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    List<Viagem> findByUsuarioIdOrderByDataViagemDesc(Long usuarioId);

    @Query("SELECT COALESCE(SUM(v.co2EconomizadoKg), 0) FROM Viagem v WHERE v.usuario.id = :usuarioId")
    Double sumCo2EconomizadoByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(v.distanciaKm), 0) FROM Viagem v WHERE v.usuario.id = :usuarioId")
    Double sumDistanciaByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(v.pontosGanhos), 0) FROM Viagem v WHERE v.usuario.id = :usuarioId")
    Integer sumPontosGanhosByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(v.co2EconomizadoKg), 0) FROM Viagem v")
    Double sumCo2EconomizadoTotal();

    @Query("SELECT COUNT(v) FROM Viagem v")
    Long countTotalViagens();
}
