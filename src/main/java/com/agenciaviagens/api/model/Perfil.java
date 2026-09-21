package com.agenciaviagens.api.model;

/**
 * Perfis de acesso do sistema.
 *
 * ADMIN — administra o catalogo: pode cadastrar, atualizar e excluir destinos.
 * USER  — cliente/parceiro: pode consultar destinos e registrar avaliacoes.
 *
 * O Spring Security espera que as autoridades comecem com o prefixo "ROLE_",
 * por isso o metodo getAuthority() monta esse nome a partir do enum.
 */
public enum Perfil {

    ADMIN,
    USER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
