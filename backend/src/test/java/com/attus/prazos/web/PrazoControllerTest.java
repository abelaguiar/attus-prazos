package com.attus.prazos.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class PrazoControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PrazoRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new RequestIdFilter())
                .build();
        repository.deleteAll();
    }

    @Test
    void deveCriarPrazoComDadosValidos() throws Exception {
        String corpo = """
                {"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"Contestacao","dataPrazo":"%s"}
                """.formatted(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
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
    void deveRejeitarPrazoInvalidoCom400EDetalheDosCampos() throws Exception {
        String corpo = """
                {"numeroProcesso":"","descricao":"","dataPrazo":"2020-01-01"}
                """;

        mockMvc.perform(post("/prazos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
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
}
