package com.attus.prazos.web;

import com.attus.prazos.domain.Usuario;
import com.attus.prazos.service.AuthService;
import com.attus.prazos.service.TokenService;
import com.attus.prazos.web.dto.LoginRequest;
import com.attus.prazos.web.dto.RegistrarRequest;
import com.attus.prazos.web.dto.TokenResponse;
import com.attus.prazos.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Cadastro e login de usuarios")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario cadastrado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "400", description = "Entrada invalida",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public UsuarioResponse registrar(@Valid @RequestBody RegistrarRequest request) {
        Usuario usuario = authService.registrar(request.nome(), request.email(), request.senha());
        return UsuarioResponse.from(usuario);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e devolve um token JWT")
    @ApiResponse(responseCode = "200", description = "Login efetuado",
            content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "400", description = "Entrada invalida",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais invalidas",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authService.autenticar(request.email(), request.senha());
        String token = tokenService.gerarToken(auth);
        return TokenResponse.bearer(token, tokenService.getExpiracaoSegundos());
    }
}
