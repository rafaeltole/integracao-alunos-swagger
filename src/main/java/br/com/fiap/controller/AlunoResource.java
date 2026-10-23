package br.com.fiap.controller;

import br.com.fiap.dto.AlunoRequest;
import br.com.fiap.dto.AlunoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/alunos")
@Tag(name = "Alunos", description = "Serviços relacionados a API de Alunos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AlunoResource {

    private AtomicLong sequence = new AtomicLong();
    private List<AlunoResponse> alunos = new ArrayList<>();


    @Operation(
            summary = "Cadastro de alunos.",
            description = "Responsável pelo cadastro de alunos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Retorna os dados do aluno cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AlunoResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = AlunoRequest.class)))
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest request) {
        AlunoResponse novoAluno = new AlunoResponse(
                sequence.incrementAndGet(),
                request.nome(),
                request.email());

        alunos.add(novoAluno);

        return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
    }

    @Operation(
            summary = "Consulta de alunos.",
            description = "Responsável pela consulta de alunos cadastrados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retorna a lista com os alunos cadastrados",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = AlunoResponse.class
                                    )
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<AlunoResponse>> consultar() {
        return ResponseEntity.ok(alunos);
    }

    @Operation(
            summary = "Remove um aluno.",
            description = "Responsável por remover um aluno cadastrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    content = @Content

            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@Parameter(name = "id", description = "Código do aluno") @PathVariable Long id) {
        alunos.removeIf((aluno) -> aluno.id().equals(id));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
