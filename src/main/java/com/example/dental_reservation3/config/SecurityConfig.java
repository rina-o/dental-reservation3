package com.example.dental_reservation3.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // POSTを通すためにCSRF無効化（本番では検討要）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/logout", "/css/**", "/js/**").permitAll()
                        .anyRequest().permitAll() // 全部自作で制御するならここもpermitAll()
                );

        return http.build();
    }
}
