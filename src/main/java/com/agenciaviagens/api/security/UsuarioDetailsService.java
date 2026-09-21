package com.agenciaviagens.api.security;

import com.agenciaviagens.api.model.Usuario;
import com.agenciaviagens.api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ponto de integracao entre o Spring Security e o BANCO DE DADOS.
 *
 * Quando chega uma requisicao autenticada, o Spring chama este metodo
 * passando o login informado. Nos buscamos o usuario na tabela "usuarios"
 * e devolvemos seus dados (incluindo o hash da senha e os perfis); o
 * proprio Spring se encarrega de comparar a senha enviada com o hash.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario nao encontrado: " + login));
        return new UsuarioDetails(usuario);
    }
}
