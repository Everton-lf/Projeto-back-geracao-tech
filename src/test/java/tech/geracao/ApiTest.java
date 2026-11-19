package tech.geracao;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// Integração HTTP cobrindo autenticação, CRUD, filtros, validações e status REST.
class ApiTest {

    private static final String EMAIL = "api." + UUID.randomUUID() + "@email.com";
    private static final String PASSWORD = "senha123";
    private static String token;
    private static Integer userId;
    private static Integer categoryId;
    private static Integer productId;

    @BeforeAll
    static void setupRestAssured() {
        io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    void publicEndpointsRejeitamDadosInvalidosECredenciais() {
        given().contentType(ContentType.JSON)
                .body("{\"firstName\":\"\",\"surname\":\"\",\"email\":\"email-invalido\",\"password\":\"\"}")
                .post("/v1/usuarios")
                .then().statusCode(400);

        given().contentType(ContentType.JSON)
                .body("{\"email\":\"inexistente@email.com\",\"password\":\"errada\"}")
                .post("/v1/usuarios/token")
                .then().statusCode(401);

        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Produto protegido\",\"slug\":\"produto-protegido\",\"price\":10,\"priceWithDiscount\":8}")
                .post("/v1/product")
                .then().statusCode(401);

        given().get("/v1/product/999999")
                .then().statusCode(404);

        given().get("/v1/category/999999")
                .then().statusCode(404);

        given().get("/v1/usuarios/999999")
                .then().statusCode(404);
    }

    @Test
    @Order(2)
    void criaUsuarioEGeraJwt() {
        Response response = given().contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Teste",
                          "surname": "API",
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(EMAIL, PASSWORD))
                .post("/v1/usuarios");

        response.then().statusCode(201).body("email", equalTo(EMAIL));
        userId = response.jsonPath().getInt("id");

        Response login = given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(EMAIL, PASSWORD))
                .post("/v1/usuarios/token");

        login.then().statusCode(200).body("token", not(isEmptyOrNullString()));
        token = login.jsonPath().getString("token");

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"senha-incorreta"}
                        """.formatted(EMAIL))
                .post("/v1/usuarios/token")
                .then().statusCode(401);

        given().contentType(ContentType.JSON)
                .body("""
                        {"firstName":"Teste","surname":"Duplicado","email":"%s","password":"outra-senha"}
                        """.formatted(EMAIL))
                .post("/v1/usuarios")
                .then().statusCode(400);
    }

    @Test
    @Order(3)
    void testaCrudUsuarios() {
        given().get("/v1/usuarios")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));

        given().get("/v1/usuarios/{id}", userId)
                .then().statusCode(200).body("email", equalTo(EMAIL));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                        {"firstName":"Teste Atualizado","surname":"API","email":"%s","password":"nova-senha"}
                        """.formatted(EMAIL))
                .put("/v1/usuarios/{id}", userId)
                .then().statusCode(200).body("firstName", equalTo("Teste Atualizado"));

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"nova-senha"}
                        """.formatted(EMAIL))
                .post("/v1/usuarios/token")
                .then().statusCode(200);
    }

    @Test
    @Order(4)
    void testaCrudCategoriasEBuscas() {
        Response create = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("{\"name\":\"Eletrônicos\",\"slug\":\"eletronicos\",\"useInMenu\":true}")
                .post("/v1/category");

        create.then().statusCode(201).body("name", equalTo("Eletrônicos"));
        categoryId = create.jsonPath().getInt("id");

        given().get("/v1/category/search?limit=12&page=1&use_in_menu=true&fields=id,name,slug,use_in_menu")
                .then().statusCode(200).body("data", notNullValue()).body("total", greaterThanOrEqualTo(1));

        given().get("/v1/category/search?limit=-1&page=0&fields=name")
                .then().statusCode(200).body("page", equalTo(1));

        given().get("/v1/category/{id}", categoryId)
                .then().statusCode(200).body("slug", equalTo("eletronicos"));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("{\"name\":\"Eletrônicos Atualizados\",\"slug\":\"eletronicos-atualizados\",\"useInMenu\":false}")
                .put("/v1/category/{id}", categoryId)
                .then().statusCode(200).body("name", equalTo("Eletrônicos Atualizados"));
    }

    @Test
    @Order(5)
    void testaCrudProdutosEFiltros() {
        Response create = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                        {
                          "enabled": true,
                          "name": "Headphone Teste",
                          "slug": "headphone-teste",
                          "stock": 15,
                          "description": "Som de alta qualidade",
                          "price": 350,
                          "priceWithDiscount": 299,
                          "categoryIds": [%d],
                          "images": [{"content":"/imagens/headphone.jpg","enabled":true}],
                          "options": [{"title":"Cor","shape":"circle","radius":12,"type":"color","values":"Preto,Branco"}]
                        }
                        """.formatted(categoryId))
                .post("/v1/product");

        create.then().statusCode(201).body("name", equalTo("Headphone Teste"));
        productId = create.jsonPath().getInt("id");

        given().get("/v1/product/search?limit=12&page=1&match=headphone&category_ids={id}&price-range=200-400&fields=id,name,price,category_ids,images,options", categoryId)
                .then().statusCode(200).body("data", notNullValue()).body("total", greaterThanOrEqualTo(1));

        given().get("/v1/product/search?limit=-1&page=0&fields=name")
                .then().statusCode(200).body("page", equalTo(1));

        given().get("/v1/product/{id}", productId)
                .then().statusCode(200).body("name", equalTo("Headphone Teste"));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                        {
                          "enabled": true,
                          "name": "Headphone Atualizado",
                          "slug": "headphone-atualizado",
                          "stock": 10,
                          "description": "Atualizado",
                          "price": 400,
                          "priceWithDiscount": 320,
                          "categoryIds": [%d],
                          "images": [],
                          "options": []
                        }
                        """.formatted(categoryId))
                .put("/v1/product/{id}", productId)
                .then().statusCode(200).body("name", equalTo("Headphone Atualizado"));

        given().auth().oauth2(token).delete("/v1/product/{id}", productId)
                .then().statusCode(204);
    }

    @Test
    @Order(6)
    void testaValidacoesDeFiltrosE404() {
        given().get("/v1/product/search?limit=0")
                .then().statusCode(400);

        given().get("/v1/product/search?category_ids=abc")
                .then().statusCode(400);

        given().get("/v1/product/search?category_ids=-1")
                .then().statusCode(400);

        given().get("/v1/product/search?price-range=400-200")
                .then().statusCode(400);

        given().get("/v1/product/search?price-range=abc")
                .then().statusCode(400);

        given().get("/v1/product/search?option[abc]=Preto")
                .then().statusCode(400);

        given().get("/v1/product/search?option[1]=")
                .then().statusCode(400);

        given().get("/v1/product/999999")
                .then().statusCode(404);
    }

    @Test
    @Order(7)
    void testaDeleteDeCategoriaEUsuario() {
        given().auth().oauth2(token).delete("/v1/category/{id}", categoryId)
                .then().statusCode(204);

        given().auth().oauth2(token).delete("/v1/usuarios/{id}", userId)
                .then().statusCode(204);

        given().auth().oauth2(token).delete("/v1/usuarios/{id}", userId)
                .then().statusCode(404);
    }
}
