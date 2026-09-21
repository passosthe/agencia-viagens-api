package com.agenciaviagens.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao central de seguranca da API.
 *
 * Define TRES coisas:
 *  1) como as senhas sao criptografadas (BCrypt);
 *  2) que a autenticacao e HTTP Basic e sem sessao (stateless, como manda REST);
 *  3) quais perfis podem acessar cada endpoint.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt e um algoritmo de hash proprio para senhas: e lento de proposito
     * (dificulta ataques de forca bruta) e gera um "salt" aleatorio para cada
     * senha, de modo que duas senhas iguais produzem hashes diferentes.
     * Como e um hash, a senha original nao pode ser recuperada do banco.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protege formularios com sessao em navegador; uma API REST
                // stateless consumida por apps e parceiros nao usa esse fluxo.
                .csrf(csrf -> csrf.disable())

                // Sem sessao no servidor: cada requisicao carrega suas credenciais.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // --- Consultas: qualquer usuario autenticado (USER ou ADMIN) ---
                        .requestMatchers(HttpMethod.GET, "/api/destinos/**")
                        .hasAnyRole("USER", "ADMIN")

                        // --- Avaliar um destino: USER ou ADMIN ---
                        .requestMatchers(HttpMethod.PATCH, "/api/destinos/*/avaliacoes")
                        .hasAnyRole("USER", "ADMIN")

                        // --- Operacoes sensiveis sobre o catalogo: somente ADMIN ---
                        .requestMatchers(HttpMethod.POST, "/api/destinos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/destinos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/destinos/**").hasRole("ADMIN")

                        // --- Endpoint publico: identifica quem esta logado ---
                        .requestMatchers("/api/auth/**").authenticated()

                        // Qualquer outra rota exige autenticacao (regra de seguranca
                        // por padrao: o que nao foi liberado explicitamente, e negado)
                        .anyRequest().authenticated()
                )

                // Autenticacao HTTP Basic: login e senha vao no cabecalho da requisicao.

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
