# API E-commerce - Geração Tech

## 👥 Integrantes da Equipe

- **Antonio Lucas da Costa Pereira**
- **Everton Lucas Fernandes**
- **Rubens Paulo Rodrigues Parente**

API REST em Java 21 e Quarkus 3 para usuários, categorias e produtos, com autenticação JWT, MySQL e migrations Flyway.

## 1. Primeira execução

### Pré-requisitos

- Java JDK 21
- Git
- MySQL 8 ou compatível
- Maven Wrapper incluído no projeto

### Configurar o banco

A configuração padrão usa:

- Host: `127.0.0.1`
- Porta: `3308`
- Banco: `projetofinal`
- Usuário: `root`
- Senha: `admin`

As propriedades ficam em `src/main/resources/application.properties`:

```properties
quarkus.datasource.username=root
quarkus.datasource.password=admin
quarkus.datasource.jdbc.url=jdbc:mysql://127.0.0.1:3308/projetofinal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Altere os valores caso o seu banco use credenciais diferentes.

### Iniciar pela primeira vez

No PowerShell:

```powershell
cd D:\Desenvolvimento\ProjetoFinalTech\code-with-quarkus
.\mvnw.cmd quarkus:dev
```

No Git Bash ou terminal Unix:

```shell
cd /d/Desenvolvimento/ProjetoFinalTech/code-with-quarkus
./mvnw quarkus:dev
```

O Flyway executará automaticamente as migrations ao iniciar. Aguarde a aplicação entrar no modo `Listening on`.

Endereços:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI JSON: http://localhost:8080/openapi

## 2. Criar o primeiro usuário

A criação de usuário é pública. O cadastro não exige token.

### Modelo do corpo — `RequestUser`

```json
{
  "firstName": "Joao",
  "surname": "Silva",
  "email": "joao@email.com",
  "password": "senha123"
}
```

Campos:

- `firstName`: nome, obrigatório
- `surname`: sobrenome, obrigatório
- `email`: e-mail válido e único, obrigatório
- `password`: senha, obrigatória

### Requisição

- Método: `POST`
- Rota: `http://localhost:8080/v1/usuarios`
- Header: `Content-Type: application/json`

PowerShell:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/v1/usuarios" `
  -ContentType "application/json" `
  -Body '{"firstName":"Joao","surname":"Silva","email":"joao@email.com","password":"senha123"}'
```

Resposta esperada:

- Status: `201 Created`
- Modelo: `ResponseUser`

```json
{
  "id": 1,
  "firstName": "Joao",
  "surname": "Silva",
  "email": "joao@email.com"
}
```

A senha nunca é retornada pela API. Ela é armazenada criptografada com BCrypt.

## 3. Gerar o token de acesso

Com o usuário criado, use o e-mail e a senha para entrar no sistema.

### Modelo do corpo — `RequestLogin`

```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

Campos:

- `email`: e-mail usado no cadastro
- `password`: senha usada no cadastro

### Requisição

- Método: `POST`
- Rota: `http://localhost:8080/v1/usuarios/token`
- Header: `Content-Type: application/json`

PowerShell:

```powershell
$response = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/v1/usuarios/token" `
  -ContentType "application/json" `
  -Body '{"email":"joao@email.com","password":"senha123"}'

$token = $response.token
$token
```

Resposta esperada:

- Status: `200 OK`
- Modelo: `ResponseToken`

```json
{
  "token": "eyJraWQiOiJ..."
}
```

O token pertence ao grupo `User` e expira em duas horas.

## 4. Liberar as rotas protegidas

Após gerar o token, envie-o em todas as operações protegidas:

```text
Authorization: Bearer SEU_TOKEN_JWT
```

Exemplo no PowerShell:

```powershell
$headers = @{
  Authorization = "Bearer $token"
}

Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/v1/usuarios" `
  -Headers $headers
```

### Autorizar no Swagger

1. Abra http://localhost:8080/swagger-ui.
2. Clique em **Authorize**.
3. Informe apenas o conteúdo do campo `token`, sem escrever `Bearer`.
4. Clique em **Authorize** e depois em **Close**.
5. Execute os endpoints marcados como protegidos.

Observação: o Swagger adiciona o prefixo `Bearer` ao enviar o header.

## 5. Rotas protegidas e públicas

### Usuários

| Operação | Método | Rota | Acesso |
|---|---|---|---|
| Criar usuário | POST | `/v1/usuarios` | Público |
| Gerar token | POST | `/v1/usuarios/token` | Público |
| Listar usuários | GET | `/v1/usuarios` | Público |
| Buscar usuário | GET | `/v1/usuarios/{id}` | Público |
| Atualizar usuário | PUT | `/v1/usuarios/{id}` | JWT |
| Excluir usuário | DELETE | `/v1/usuarios/{id}` | JWT |

Modelo para atualizar usuário:

```json
{
  "firstName": "Joao",
  "surname": "Silva",
  "email": "joao@email.com",
  "password": "nova-senha"
}
```

### Categorias

| Operação | Método | Rota | Acesso |
|---|---|---|---|
| Buscar categorias | GET | `/v1/category/search` | Público |
| Buscar categoria | GET | `/v1/category/{id}` | Público |
| Criar categoria | POST | `/v1/category` | JWT |
| Atualizar categoria | PUT | `/v1/category/{id}` | JWT |
| Excluir categoria | DELETE | `/v1/category/{id}` | JWT |

Modelo `CategoryResponse`:

```json
{
  "id": null,
  "name": "Cadeiras",
  "slug": "cadeiras",
  "useInMenu": true
}
```

Ao criar, envie `id` como `null` ou omita o campo.

Exemplo protegido:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/v1/category" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"name":"Cadeiras","slug":"cadeiras","useInMenu":true}'
```

### Produtos

| Operação | Método | Rota | Acesso |
|---|---|---|---|
| Buscar produtos | GET | `/v1/product/search` | Público |
| Buscar produto | GET | `/v1/product/{id}` | Público |
| Criar produto | POST | `/v1/product` | JWT |
| Atualizar produto | PUT | `/v1/product/{id}` | JWT |
| Excluir produto | DELETE | `/v1/product/{id}` | JWT |

Modelo `RequestProduct`:

```json
{
  "enabled": true,
  "name": "Cadeira Gamer",
  "slug": "cadeira-gamer",
  "stock": 10,
  "description": "Cadeira confortável",
  "price": 1500.0,
  "priceWithDiscount": 1299.0,
  "categoryIds": [1],
  "images": [
    {
      "id": null,
      "content": "/imagens/cadeira-gamer.jpg",
      "path": null,
      "enabled": true
    }
  ],
  "options": [
    {
      "id": null,
      "title": "Cor",
      "shape": "circle",
      "radius": 12,
      "type": "color",
      "values": "Preto,Vermelho"
    }
  ]
}
```

Exemplo protegido:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/v1/product" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"enabled":true,"name":"Cadeira Gamer","slug":"cadeira-gamer","stock":10,"description":"Cadeira confortável","price":1500,"priceWithDiscount":1299,"categoryIds":[1],"images":[],"options":[]}'
```

## 6. Fluxo completo recomendado

1. Inicie o MySQL.
2. Inicie a API com `./mvnw quarkus:dev`.
3. Crie o usuário em `POST /v1/usuarios`.
4. Gere o token em `POST /v1/usuarios/token`.
5. Copie o campo `token` da resposta.
6. Envie `Authorization: Bearer SEU_TOKEN` nas rotas protegidas.
7. No Swagger, use o botão **Authorize** e informe somente o token.
8. Crie categorias e produtos.

## 7. Status HTTP

- `200 OK`: consulta ou atualização concluída
- `201 Created`: recurso criado
- `204 No Content`: exclusão concluída sem corpo
- `400 Bad Request`: dados inválidos
- `401 Unauthorized`: credenciais ou token inválido
- `404 Not Found`: recurso inexistente

## 8. Testes

```powershell
.\mvnw.cmd test
```

Os testes cobrem autenticação, CRUD, filtros, validações e status HTTP. O relatório JaCoCo fica em `target/jacoco-report/index.html`.

## 9. Empacotamento

```powershell
.\mvnw.cmd package
java -jar target\quarkus-app\quarkus-run.jar
```
