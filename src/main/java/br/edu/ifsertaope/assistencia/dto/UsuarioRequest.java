package br.edu.ifsertaope.assistencia.dto;

import br.edu.ifsertaope.assistencia.model.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @Schema(example = "Carla Souza")
        @NotBlank(message = "O campo nome é obrigatório.")
        @Size(max = 120, message = "O campo nome deve ter no máximo 120 caracteres.")
        String nome,

        @Schema(example = "carla.atendimento")
        @NotBlank(message = "O campo login é obrigatório.")
        @Size(min = 3, max = 60, message = "O campo login deve ter entre 3 e 60 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9._@-]*$",
                message = "O campo login deve conter apenas letras, números, ponto, hífen, sublinhado ou arroba.")
        String login,

        @Schema(example = "senhaForte123")
        @NotBlank(message = "O campo senha é obrigatório.")
        @Size(min = 8, max = 72, message = "O campo senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @Schema(example = "ATENDENTE")
        @NotNull(message = "O campo perfil é obrigatório.")
        Perfil perfil) {
}
