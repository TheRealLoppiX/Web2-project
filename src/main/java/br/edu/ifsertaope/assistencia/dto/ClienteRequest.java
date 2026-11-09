package br.edu.ifsertaope.assistencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @Schema(example = "Maria da Silva")
        @NotBlank(message = "O campo nome é obrigatório.")
        @Size(max = 120, message = "O campo nome deve ter no máximo 120 caracteres.")
        String nome,

        @Schema(example = "(87) 99999-0000")
        @NotBlank(message = "O campo telefone é obrigatório.")
        @Size(max = 20, message = "O campo telefone deve ter no máximo 20 caracteres.")
        String telefone,

        @Schema(example = "maria@email.com")
        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O campo email deve conter um e-mail válido.")
        @Size(max = 120, message = "O campo email deve ter no máximo 120 caracteres.")
        String email,

        @Schema(example = "123.456.789-09", description = "CPF ou CNPJ, com ou sem formatação; opcional")
        @Pattern(regexp = "^(\\d{11}|\\d{14}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})?$",
                message = "O campo cpfCnpj deve conter um CPF ou CNPJ em formato válido.")
        String cpfCnpj) {
}
