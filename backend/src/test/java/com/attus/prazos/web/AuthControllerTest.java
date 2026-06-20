package com.attus.prazos.web;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attus.prazos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        repository.deleteAll();
    }

    @Test
    void deveCadastrarUsuarioComDadosValidos() throws Exception {
        String corpo = """
                {"nome":"Maria Souza","email":"maria@exemplo.com","senha":"senhaForte123"}
                """;

        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRejeitarEmailDuplicadoCom409() throws Exception {
        String corpo = """
                {"nome":"Maria","email":"maria@exemplo.com","senha":"senhaForte123"}
                """;

        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRejeitarCadastroInvalidoCom400() throws Exception {
        String corpo = """
                {"nome":"","email":"nao-e-email","senha":"123"}
                """;

        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void deveFazerLoginEDevolverToken() throws Exception {
        String cadastro = """
                {"nome":"Maria","email":"maria@exemplo.com","senha":"senhaForte123"}
                """;
        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isCreated());

        String login = """
                {"email":"maria@exemplo.com","senha":"senhaForte123"}
                """;
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void deveRejeitarLoginComSenhaErradaCom401() throws Exception {
        String cadastro = """
                {"nome":"Maria","email":"maria@exemplo.com","senha":"senhaForte123"}
                """;
        mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isCreated());

        String login = """
                {"email":"maria@exemplo.com","senha":"senhaErrada123"}
                """;
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
