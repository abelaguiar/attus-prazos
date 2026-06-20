package com.attus.prazos.service;

import com.attus.prazos.domain.Role;
import com.attus.prazos.domain.Usuario;
import com.attus.prazos.exception.EmailJaCadastradoException;
import com.attus.prazos.repository.UsuarioRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public Usuario registrar(String nome, String email, String senha) {
        String emailNormalizado = normalizar(email);
        if (repository.existsByEmail(emailNormalizado)) {
            log.warn("Tentativa de cadastro com e-mail já existente");
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(nome, emailNormalizado, passwordEncoder.encode(senha), Role.USER);
        Usuario salvo = repository.save(usuario);
        log.info("Usuário cadastrado id={}", salvo.getId());
        return salvo;
    }

    /** Valida credenciais via Spring Security; lanca AuthenticationException se invalidas. */
    public Authentication autenticar(String email, String senha) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizar(email), senha));
        log.info("Login efetuado para usuário autenticado");
        return auth;
    }

    private String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
