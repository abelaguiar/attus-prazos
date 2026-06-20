package com.attus.prazos.infrastructure.web;

import com.attus.prazos.application.port.in.PrazoUseCase;
import com.attus.prazos.domain.Prazo;
import com.attus.prazos.infrastructure.web.dto.AtualizarPrazoRequest;
import com.attus.prazos.infrastructure.web.dto.CriarPrazoRequest;
import com.attus.prazos.infrastructure.web.dto.PrazoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prazos")
@Tag(name = "Prazos", description = "Operacoes para controle de prazos processuais")
public class PrazoController {

    private final PrazoUseCase useCase;

    public PrazoController(PrazoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um prazo processual")
    @ApiResponse(responseCode = "201", description = "Prazo criado",
            content = @Content(schema = @Schema(implementation = PrazoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Entrada invalida",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Prazo duplicado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PrazoResponse criar(@Valid @RequestBody CriarPrazoRequest request) {
        Prazo prazo = useCase.criar(
                request.numeroProcesso(),
                request.descricao(),
                request.dataPrazo());
        return PrazoResponse.from(prazo);
    }

    @GetMapping
    @Operation(summary = "Lista os prazos processuais")
    @ApiResponse(responseCode = "200", description = "Prazos encontrados",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PrazoResponse.class))))
    public List<PrazoResponse> listar() {
        return useCase.listar().stream()
                .map(PrazoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um prazo processual por id")
    @ApiResponse(responseCode = "200", description = "Prazo encontrado",
            content = @Content(schema = @Schema(implementation = PrazoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Prazo nao encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PrazoResponse buscarPorId(@Parameter(description = "Identificador do prazo", example = "1")
            @PathVariable Long id) {
        return PrazoResponse.from(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um prazo processual")
    @ApiResponse(responseCode = "200", description = "Prazo atualizado",
            content = @Content(schema = @Schema(implementation = PrazoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Entrada invalida",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Prazo nao encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de versao",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PrazoResponse atualizar(@Parameter(description = "Identificador do prazo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPrazoRequest request) {
        Prazo prazo = useCase.atualizar(
                id,
                request.descricao(),
                request.dataPrazo(),
                request.version());
        return PrazoResponse.from(prazo);
    }

    @PatchMapping("/{id}/cumprir")
    @Operation(summary = "Marca um prazo processual como cumprido")
    @ApiResponse(responseCode = "200", description = "Prazo cumprido",
            content = @Content(schema = @Schema(implementation = PrazoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Prazo nao encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PrazoResponse cumprir(@Parameter(description = "Identificador do prazo", example = "1")
            @PathVariable Long id) {
        return PrazoResponse.from(useCase.marcarComoCumprido(id));
    }
}
