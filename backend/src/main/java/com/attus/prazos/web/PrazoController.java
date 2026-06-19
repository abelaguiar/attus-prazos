package com.attus.prazos.web;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.service.PrazoService;
import com.attus.prazos.web.dto.AtualizarPrazoRequest;
import com.attus.prazos.web.dto.CriarPrazoRequest;
import com.attus.prazos.web.dto.PageResponse;
import com.attus.prazos.web.dto.PrazoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private final PrazoService service;

    public PrazoController(PrazoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrazoResponse criar(@Valid @RequestBody CriarPrazoRequest request) {
        Prazo prazo = service.criar(
                request.numeroProcesso(),
                request.descricao(),
                request.dataPrazo());
        return PrazoResponse.from(prazo);
    }

    @GetMapping
    public PageResponse<PrazoResponse> listar(
            @PageableDefault(size = 20, sort = {"dataPrazo", "id"}) Pageable pageable) {
        return PageResponse.from(service.listar(pageable), PrazoResponse::from);
    }

    @GetMapping("/{id}")
    public PrazoResponse buscarPorId(@PathVariable Long id) {
        return PrazoResponse.from(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public PrazoResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody AtualizarPrazoRequest request) {
        Prazo prazo = service.atualizar(
                id,
                request.descricao(),
                request.dataPrazo(),
                request.version());
        return PrazoResponse.from(prazo);
    }

    @PatchMapping("/{id}/cumprir")
    public PrazoResponse cumprir(@PathVariable Long id) {
        return PrazoResponse.from(service.marcarComoCumprido(id));
    }
}
