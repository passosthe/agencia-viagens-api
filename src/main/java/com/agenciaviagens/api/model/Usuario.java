package com.agenciaviagens.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidade JPA que representa um usuario do sistema.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * unique = true impede dois usuarios com o mesmo login.
     */
    @Column(nullable = false, unique = true, length = 60)
    private String login;

    /**
     * IMPORTANTE: nunca armazenar a senha em texto puro.
     * Esta coluna guarda o hash BCrypt, gerado pelo PasswordEncoder.
     */
    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * Um usuario pode ter um ou mais perfis. @ElementCollection cria a tabela
     * auxiliar usuario_perfis; @Enumerated(STRING) grava o texto "ADMIN"/"USER"
     * em vez de um numero, deixando o banco legivel e resistente a reordenacao
     * dos valores do enum.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_perfis",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 20)
    private Set<Perfil> perfis = new HashSet<>();

    public Usuario(String login, String senha, Set<Perfil> perfis) {
        this.login = login;
        this.senha = senha;
        this.perfis = perfis;
        this.ativo = true;
    }
}
