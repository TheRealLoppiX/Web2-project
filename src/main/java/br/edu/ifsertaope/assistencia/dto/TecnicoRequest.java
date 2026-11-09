package br.edu.ifsertaope.assistencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(

        @Schema(example = "João Pereira")
        @NotBlank(message = "O campo nome é obrigatório.")
        @Size(max = 120, message = "O campo nome deve ter no máximo 120 caracteres.")
        String nome,

        @Schema(example = "joao@assistencia.com")
        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O campo email deve conter um e-mail válido.")
        @Size(max = 120, message = "O campo email deve ter no máximo 120 caracteres.")
        String email,

        @Schema(example = "hardware")
        @Size(max = 80, message = "O campo especialidade deve ter no máximo 80 caracteres.")
        String especialidade) {
}
