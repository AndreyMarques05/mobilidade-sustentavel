package br.com.fiap.mobilidade.repository;

import br.com.fiap.mobilidade.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByIdAndAtivoTrue(Long id);

    boolean existsByEmail(String email);

    List<Usuario> findByAtivoTrue();

    @Query("SELECT u FROM Usuario u WHERE u.ativo = true ORDER BY u.pontosRecompensa DESC")
    List<Usuario> findRankingByPontos();
}
