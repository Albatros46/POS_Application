package com.sakcadag.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF korumasını kapat
                .authorizeHttpRequests(auth -> auth
                     //   .requestMatchers("/api/roles/**","/api/products/**","/api/categories/**","/api/customers/**").permitAll() // Kategori endpointlerini serbest bırak// Rollere açık erişim
                        .requestMatchers("/api/**").permitAll() //tüm api ler erisim iznine sahip
                        .anyRequest().authenticated() // Diğer istekler kimlik doğrulama gerektirir
                )
                .formLogin(login -> login.disable()) // Form tabanlı giriş sayfasını devre dışı bırak
                .httpBasic(basic -> basic.disable()); // Temel HTTP kimlik doğrulamasını kapat

        return http.build();
    }
}
