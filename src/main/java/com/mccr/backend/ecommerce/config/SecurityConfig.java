package com.mccr.backend.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(config -> config.disable())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. Control de accesos por rutas (Filtros de Roles globales)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // Permitimos el acceso físico a las rutas por ID. El control de si "soy yo
                        // mismo" lo hará el servicio.
                        .requestMatchers(HttpMethod.GET, "/auth/users/{id}").hasAnyRole("USER", "SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/auth/users/{id}").hasAnyRole("USER", "SUPERVISOR", "ADMIN")

                        // Todo lo demás de usuarios (Crear, Eliminar, Listar todos) queda EXCLUSIVO
                        // para el ADMIN
                        .requestMatchers("/auth/users/**").hasRole("ADMIN")

                        // Control de productos
                        .requestMatchers("/api/products/**").hasAnyRole("SUPERVISOR", "ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
