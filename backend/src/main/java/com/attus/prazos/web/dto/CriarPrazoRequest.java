package com.attus.prazos.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CriarPrazoRequest(

        @NotBlank(message = "numeroProcesso e' obrigatorio")
        @Size(max = 25, message = "numeroProcesso deve ter no maximo 25 caracteres")
        String numeroProcesso,

        @NotBlank(message = "descricao e' obrigatoria")
        @Size(max = 2000, message = "descricao deve ter no maximo 2000 caracteres")
        String descricao,

        @NotNull(message = "dataPrazo e' obrigatoria")
        @FutureOrPresent(message = "dataPrazo nao pode estar no passado")
        LocalDate dataPrazo
) {
}
