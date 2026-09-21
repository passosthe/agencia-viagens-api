package com.agenciaviagens.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma avaliacao individual feita por um usuario
 * sobre um destino.
 */
@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal nota;

    @Column(length = 500)
    private String comentario;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Destino destino;

    /**
     * Guarda o login de quem avaliou, obtido do contexto de seguranca.
     */
    @Column(name = "usuario_login", length = 60)
    private String usuarioLogin;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao = LocalDateTime.now();

    public Avaliacao(BigDecimal nota, String comentario, String usuarioLogin) {
        this.nota = nota;
        this.comentario = comentario;
        this.usuarioLogin = usuarioLogin;
        this.dataAvaliacao = LocalDateTime.now();
    }
}
