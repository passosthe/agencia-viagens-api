package com.agenciaviagens.api.repository;

import com.agenciaviagens.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository de Usuario, usado pelo Spring Security durante o login
 * para localizar o usuario pelo login informado na requisicao.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);
}
