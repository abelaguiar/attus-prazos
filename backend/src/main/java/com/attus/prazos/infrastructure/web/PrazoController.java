package com.attus.prazos.infrastructure.web;

import com.attus.prazos.application.port.in.PrazoUseCase;
import com.attus.prazos.domain.Prazo;
import com.attus.prazos.infrastructure.web.dto.AtualizarPrazoRequest;
import com.attus.prazos.infrastructure.web.dto.CriarPrazoRequest;
import com.attus.prazos.infrastructure.web.dto.PrazoResponse;
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
public class PrazoController {

    private final PrazoUseCase useCase;

    public PrazoController(PrazoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrazoResponse criar(@Valid @RequestBody CriarPrazoRequest request) {
        Prazo prazo = useCase.criar(
                request.numeroProcesso(),
                request.descricao(),
                request.dataPrazo());
        return PrazoResponse.from(prazo);
    }

    @GetMapping
    public List<PrazoResponse> listar() {
        return useCase.listar().stream()
                .map(PrazoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PrazoResponse buscarPorId(@PathVariable Long id) {
        return PrazoResponse.from(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public PrazoResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody AtualizarPrazoRequest request) {
        Prazo prazo = useCase.atualizar(
                id,
                request.descricao(),
                request.dataPrazo(),
                request.version());
        return PrazoResponse.from(prazo);
    }

    @PatchMapping("/{id}/cumprir")
    public PrazoResponse cumprir(@PathVariable Long id) {
        return PrazoResponse.from(useCase.marcarComoCumprido(id));
    }
}
