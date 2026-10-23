package br.com.fiap.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AlunoRequest(
        @NotBlank(message = "O nome deve ser informado") String nome,
        @Email(message = "E-mail informado inválido") @NotBlank(message = "O e-mail deve ser informado") String email) {
}
