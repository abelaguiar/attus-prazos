package com.attus.prazos.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.repository.PrazoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@WithMockUser
class PrazoControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PrazoRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .addFilters(new RequestIdFilter())
                .build();
        repository.deleteAll();
    }

    @Test
    @WithAnonymousUser
    void deveExigirAutenticacaoNosPrazos() throws Exception {
        mockMvc.perform(get("/prazos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveCriarPrazoComDadosValidos() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.numeroProcesso").value("0001234-56.2026.8.26.0100"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.vencido").value(false))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void deveRejeitarPrazoDuplicadoCom409() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRejeitarDuplicadoComNumeroProcessoMascaradoENaoMascarado() throws Exception {
        String dataPrazo = LocalDate.now().plusDays(30).toString();
        String corpoSemMascara = """
                {"numeroProcesso":"00012345620268260100","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(dataPrazo);
        String corpoComMascara = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(dataPrazo);

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpoSemMascara))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroProcesso").value("0001234-56.2026.8.26.0100"));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpoComMascara))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveAceitarDescricaoCom2000CaracteresAoCriar() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"%s","dataPrazo":"%s"}
                """.formatted(descricaoComTamanho(2000), LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value(descricaoComTamanho(2000)));
    }

    @Test
    void deveRejeitarDescricaoComMaisDe2000CaracteresAoCriar() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"%s","dataPrazo":"%s"}
                """.formatted(descricaoComTamanho(2001), LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("descricao")));
    }

    @Test
    void deveRejeitarPrazoInvalidoCom400EDetalheDosCampos() throws Exception {
        String corpo = """
                {"numeroProcesso":"","descricao":"","dataPrazo":"2020-01-01"}
                """;

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }

    @Test
    void deveRejeitarNumeroProcessoIncompleto() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("numeroProcesso")));
    }

    @Test
    void deveRetornar404ParaPrazoInexistente() throws Exception {
        mockMvc.perform(get("/prazos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveMarcarPrazoComoCumprido() throws Exception {
        Prazo prazo = repository.save(
                new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10)));

        mockMvc.perform(patch("/prazos/{id}/cumprir", prazo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CUMPRIDO"))
                .andExpect(jsonPath("$.cumpridoEm").isNotEmpty());
    }

    @Test
    void deveAtualizarPrazoComVersaoCorreta() throws Exception {
        Prazo prazo = repository.save(
                new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10)));
        Long versao = prazo.getVersion();

        String corpo = """
                {"descricao":"Apelacao","dataPrazo":"%s","version":%d}
                """.formatted(LocalDate.now().plusDays(20), versao);

        mockMvc.perform(put("/prazos/{id}", prazo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Apelacao"))
                .andExpect(jsonPath("$.version").value(versao + 1));
    }

    @Test
    void deveAceitarDescricaoCom2000CaracteresAoAtualizar() throws Exception {
        Prazo prazo = repository.save(
                new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10)));
        String descricao = descricaoComTamanho(2000);
        String corpo = """
                {"descricao":"%s","dataPrazo":"%s","version":%d}
                """.formatted(descricao, LocalDate.now().plusDays(20), prazo.getVersion());

        mockMvc.perform(put("/prazos/{id}", prazo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value(descricao));
    }

    @Test
    void deveRejeitarDescricaoComMaisDe2000CaracteresAoAtualizar() throws Exception {
        Prazo prazo = repository.save(
                new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10)));
        String corpo = """
                {"descricao":"%s","dataPrazo":"%s","version":%d}
                """.formatted(descricaoComTamanho(2001), LocalDate.now().plusDays(20), prazo.getVersion());

        mockMvc.perform(put("/prazos/{id}", prazo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("descricao")));
    }

    @Test
    void deveRejeitarAtualizacaoComVersaoDesatualizadaCom409() throws Exception {
        Prazo prazo = repository.save(
                new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10)));
        Long versaoDesatualizada = prazo.getVersion();

        String corpo = """
                {"descricao":"Apelacao","dataPrazo":"%s","version":%d}
                """.formatted(LocalDate.now().plusDays(20), versaoDesatualizada);

        // 1a edicao com a versao correta -> OK (a versao do prazo incrementa)
        mockMvc.perform(put("/prazos/{id}", prazo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());

        // 2a edicao reusando a versao ja desatualizada -> 409
        mockMvc.perform(put("/prazos/{id}", prazo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveExporDocumentacaoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$['paths']['/prazos'].get").exists())
                .andExpect(jsonPath("$['paths']['/prazos'].post").exists());
    }

    @Test
    void deveExporSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private String descricaoComTamanho(int tamanho) {
        return "a".repeat(tamanho);
    }
}
