package com.agenciaviagens.api.config;

import com.agenciaviagens.api.model.Perfil;
import com.agenciaviagens.api.model.Usuario;
import com.agenciaviagens.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Cria os usuarios de teste na primeira execucao da aplicacao.
 */
@Configuration
public class CargaInicialUsuarios implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialUsuarios.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CargaInicialUsuarios(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        criarSeNaoExistir("admin", "admin123", Set.of(Perfil.ADMIN));
        criarSeNaoExistir("usuario", "usuario123", Set.of(Perfil.USER));
    }

    private void criarSeNaoExistir(String login, String senhaEmTextoPuro, Set<Perfil> perfis) {
        if (usuarioRepository.existsByLogin(login)) {
            return;
        }

        Usuario usuario = new Usuario(
                login,
                passwordEncoder.encode(senhaEmTextoPuro),
                perfis
        );
        usuarioRepository.save(usuario);
        log.info("Usuario de teste criado: {} com perfis {}", login, perfis);
    }
}
