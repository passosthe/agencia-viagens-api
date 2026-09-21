package com.agenciaviagens.api.security;

import com.agenciaviagens.api.model.Perfil;
import com.agenciaviagens.api.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador entre a NOSSA entidade Usuario e o contrato UserDetails que o
 * Spring Security entende.
 *
 * Fazemos essa ponte em vez de a entidade implementar UserDetails diretamente,
 * para manter o modelo de dominio independente do framework de seguranca.
 */
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Converte os perfis (ADMIN, USER) nas authorities que o Spring usa
     * para decidir o acesso: ROLE_ADMIN, ROLE_USER.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getPerfis().stream()
                .map(Perfil::getAuthority)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Usuarios marcados como inativos no banco nao conseguem autenticar.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getAtivo());
    }

    public List<String> getPerfis() {
        return usuario.getPerfis().stream().map(Enum::name).toList();
    }
}
