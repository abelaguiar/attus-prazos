package com.attus.prazos.service;

import com.attus.prazos.domain.Usuario;
import com.attus.prazos.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Liga o Spring Security ao nosso banco: carrega o usuario pelo e-mail (que usamos como username). */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AppUserDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()))
                .build();
    }
}
