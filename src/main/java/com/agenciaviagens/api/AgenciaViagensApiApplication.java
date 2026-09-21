package com.agenciaviagens.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao.
 *
 * @SpringBootApplication dispara a auto-configuracao do JPA
 * (conexao com o PostgreSQL, criacao das tabelas) e do Spring Security.
 */
@SpringBootApplication
public class AgenciaViagensApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenciaViagensApiApplication.class, args);
    }

}
