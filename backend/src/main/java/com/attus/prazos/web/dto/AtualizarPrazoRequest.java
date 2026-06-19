package com.attus.prazos.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AtualizarPrazoRequest(

        @NotBlank(message = "descricao e' obrigatoria")
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "dataPrazo e' obrigatoria")
        @FutureOrPresent(message = "dataPrazo nao pode estar no passado")
        LocalDate dataPrazo,

        @NotNull(message = "version e' obrigatoria")
        Long version
) {
}
