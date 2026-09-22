package com.example.studentmanagement.config;

import com.example.studentmanagement.repository.UserAccountRepository;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserAccountRepository repository
    ) {
        return username -> {
            var account = repository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Sai thông tin đăng nhập"));

            return User.withUsername(account.getUsername())
                    .password(account.getPassword())
                    .roles(account.getRole().name())
                    .disabled(!Boolean.TRUE.equals(account.getEnabled()))
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider
    ) throws Exception {

        http.authenticationProvider(provider);

        // Giữ CSRF để bảo vệ các thao tác dùng phiên đăng nhập.
        http.csrf(Customizer.withDefaults());

        http.requestCache(cache ->
                cache.requestCache(new NullRequestCache()));

        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/**").authenticated()

                .requestMatchers("/api/me/**").hasRole("STUDENT")
                .requestMatchers("/api/teaching/**").hasRole("TEACHER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Các API quản lý cũ chỉ ADMIN được dùng.
                .requestMatchers("/api/**").hasRole("ADMIN")

                .anyRequest().denyAll()
        );

        http.formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) ->
                        writeJson(response, 200,
                                "{\"message\":\"Đăng nhập thành công\"}"))
                .failureHandler((request, response, exception) ->
                        writeJson(response, 401,
                                "{\"message\":\"Sai thông tin đăng nhập hoặc tài khoản bị khóa\"}"))
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) ->
                        writeJson(response, 200,
                                "{\"message\":\"Đã đăng xuất\"}"))
        );

        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) ->
                        writeJson(response, 401,
                                "{\"message\":\"Bạn cần đăng nhập\"}"))
                .accessDeniedHandler((request, response, exception) ->
                        writeJson(response, 403,
                                "{\"message\":\"Không có quyền hoặc CSRF token không hợp lệ\"}"))
        );

        return http.build();
    }

    private static void writeJson(
            HttpServletResponse response,
            int status,
            String body
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}