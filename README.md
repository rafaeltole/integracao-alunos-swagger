
# Spring Docs | OpenAPI | Swagger

Para o exemplo de hoje vamos desenvolver a API de Alunos com as operações CRUD. Em seguida, documentar a API para o que outros desenvolvedores possam integrar, sem a necessidade de ler o código fonte.

Antes de iniciar vamos seguir os passos:

| Assunto | O que compreender |
| --- | --- |
| Documentação de APIs | Por que documentar e o que deve ser documentado |
| OpenAPI | O que é a especificação e como funciona |
| Swagger UI | Como visualizar e testar a documentação |
| Springdoc OpenAPI | Como gerar documentação a partir de uma aplicação Spring Boot |
| Anotações OpenAPI | Como documentar operações, parâmetros e respostas |

## Parte 1 — Fundamentos da documentação de APIs

### 1.1. Por que documentar uma API?

Temos os nossa API de Alunos com os seguintes endpoins:

- `POST /alunos`
- `GET /alunos`
- `GET /alunos/{id}`
- `PUT /alunos/{id}`
- `DELETE /alunos/{id}`
Quem for integrar com a nossa aplicação, ainda precisa descobrir:

- Quais informações são obrigatórias para cadastrar um aluno?
- Qual formato JSON deve ser enviado?
- O que a API retorna quando o cadastro é realizado?
- O que acontece quando o aluno não existe?
- Quais endpoints exigem autenticação?
- Quais códigos HTTP podem ser retornados?
Sem documentação, ele precisaria ter acesso ao código-fonte ou conversar com o desenvolvedor.

### 1.2. O que deve ser documentado?

**Endpoint:**
- `POST /alunos`
**Descrição:**
Cadastra novo aluno no sistema.

**Corpo da requisição:**
```json
{
  "nome": "Nome do Aluno"
}
```

**Resposta de sucesso:**
HTTP 201 Created

```json
{
  "id": 10,
  "nome": "Nome do Aluno"
}
```

**Possíveis respostas:**
| Código HTTP | Significado |
| --- | --- |
| 201 | Aluno cadastrado |
| 400 | Dados inválidos |
| 401 | Usuário não autenticado |
| 403 | Usuário sem permissão |
| 500 | Erro interno |

## Parte 2 — OpenAPI e Swagger

### 2.1. O que é OpenAPI?

OpenAPI é uma especificação utilizada para descrever APIs HTTP de maneira padronizada.

Ela permite documentar:

- Endpoints e métodos HTTP.
- Parâmetros de entrada.
- Estrutura das requisições.
- Estrutura das respostas.
- Códigos HTTP.
- Mecanismos de autenticação.
A especificação pode ser representada em JSON ou YAML.

### 2.2. O que é Swagger?

Swagger é o nome associado a um conjunto de ferramentas utilizadas no ecossistema OpenAPI.

O Swagger UI apresenta uma interface gráfica para consultar e experimentar os endpoints documentados.

| Tecnologia | Responsabilidade |
| --- | --- |
| OpenAPI | Especificação da documentação |
| Swagger UI | Interface visual para explorar e testar a API |
| Springdoc OpenAPI | Biblioteca que integra OpenAPI ao Spring Boot |

## Parte 3 — Integrando OpenAPI ao Spring Boot

### Adicionando a dependência Spring Docs

A dependência permite gerar automaticamente a descrição OpenAPI e disponibilizar a interface Swagger UI. Verificar a compatibilidade com o versão do Spring Boot utilizada em: https://springdoc.org/

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.0</version>
</dependency>
```

### Acesso as documentações

**Documento OpenAPI**
- http://localhost:8080/v3/api-docs
**Interface Swagger UI**
- http://localhost:8080/swagger-ui/index.html
Caso a aplicação esteja com o Spring Security, precisamos configurar para que as URLs poossam ser acessadas sem autenticação.

```java
//--- acesso a documentação OpenAPI permitido sem autenticação
.requestMatchers(
        "/v3/api-docs/**",
        "/swagger-ui/**")
.permitAll()
```

## Parte 4 — Documentando os endpoints

### 4.1. Documentação inicial

Vamos documentar as informações sobre a aplicação.

```java
@Configuration
//--- Configuração caso a aplicação utilize Spring Security
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Integração Alunos Spring")
                        .description("Aplicação de exemplo com os módulos do Spring."));
    }

}
```

### 4.1. Documentação API de autenticação

```java
@RestController
@RequestMapping("/autenticacao")
@Tag(name = "Autenticação", description = "API para autenticação dos usuários.") //--- agrupando os endpoints por API
public class AutenticacaoResource {

    private GeradorTokenJWTService geradorTokenService;

    public AutenticacaoResource(GeradorTokenJWTService geradorTokenService) {
        this.geradorTokenService = geradorTokenService;
    }

    @Operation(
            summary = "Autenticar usuário.",
            description = "Responsável pela autenticação do usuário",
            security = @SecurityRequirement(name = "basicAuth")) //---configuração necessária somente se aplicação estiver usando Spring Security. Autenticação HTTP Basic
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário autenticado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TokenResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário ou senha inválido.",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(Authentication authentication) {
        TokenResponse tokenResponse = geradorTokenService.gerar(authentication);
        return ResponseEntity.ok(tokenResponse);
    }

}
```

### 4.1. Documentação API de alunos

```java
@RestController
@RequestMapping("/alunos")
@Tag(name = "Alunos", description = "API de Alunos com as operações CRUD.")
public class AlunoResource {

    private final AlunoService alunoService;

    public AlunoResource(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @Operation(
            summary = "Cadastrar aluno",
            description = "Responsável pelo cadastro de alunos.",
            security = @SecurityRequirement(name = "bearerAuth") //---configuração necessária somente se aplicação estiver usando Spring Security
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Aluno cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AlunoResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Falha durante a validação dos dados do aluno.",
                    content = @Content
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dados do aluno a ser cadastrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AlunoRequest.class)))
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest novoAluno) {
        AlunoResponse alunoResponse = alunoService.cadastrar(novoAluno);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(alunoResponse);
    }

    @Operation(
            summary = "Listar alunos",
            description = "Listar alunos cadastrados.",
            security = @SecurityRequirement(name = "bearerAuth") //---configuração necessária somente se aplicação estiver usando Spring Security
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista com os alunos cadastrados."
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AlunoResponse>> consultar(
            @Parameter(description = "Nome do aluno") @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "nome") Pageable paginacao) {
        Page<AlunoResponse> alunosCadastrados = alunoService.consultar(nome, paginacao);
        return ResponseEntity.ok(alunosCadastrados);
    }

    @Operation(
            summary = "Consultar aluno pelo código",
            description = "Consulta aluno utilizando o identificador.",
            security = @SecurityRequirement(name = "bearerAuth") //---configuração necessária somente se aplicação estiver usando Spring Security
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Aluno encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AlunoResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Aluno não encontrado",
                    content = @Content
            )
    })
    @GetMapping(path = "/{alunoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlunoResponse> consultarPorCodigo(@Parameter(required = true, description = "Código do aluno") @PathVariable Long alunoId) {
        AlunoResponse alunoResponse = alunoService.consultarPorCodigo(alunoId);
        return ResponseEntity.ok(alunoResponse);
    }

    @GetMapping(path = "/{alunoId}/turmas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TurmaResponse>> consultarTurmas(@PathVariable Long alunoId) {
        List<TurmaResponse> turmas = alunoService.consultarTurmas(alunoId);
        return ResponseEntity.ok(turmas);
    }

    @PutMapping(path = "/{alunoId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlunoResponse> atualizar(@PathVariable Long alunoId, @Valid @RequestBody AlunoRequest aluno) {
        AlunoResponse alunoResponse = alunoService.atualizar(alunoId, aluno);
        return ResponseEntity.ok(alunoResponse);
    }

    @DeleteMapping(path = "/{alunoId}")
    public ResponseEntity<Void> remover(@PathVariable Long alunoId) {
        alunoService.remover(alunoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{alunoId}/matriculas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MatriculaResponse>> consultarMatriculas(@PathVariable Long alunoId) {
        List<MatriculaResponse> matriculas = alunoService.consultarMatriculas(alunoId);
        return ResponseEntity.ok(matriculas);
    }

    @GetMapping(path = "/{alunoId}/perfil", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerfilResponse> consultarPerfil(@PathVariable Long alunoId) {
        PerfilResponse perfilResponse = alunoService.consultarPerfil(alunoId);
        return ResponseEntity.ok(perfilResponse);
    }

}
```

### URLs

**Documento OpenAPI**
- http://localhost:8080/v3/api-docs
**Interface Swagger UI**
- http://localhost:8080/swagger-ui/index.html
### Referências

- https://github.com/springdoc/springdoc-openapi
- https://demos.springdoc.org/demo-spring-boot-webmvc/swagger-ui/index.html
- https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
- https://developers.google.com/identity/openid-connect/openid-connect
