package tech.geracao;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import tech.geracao.model.EntityCategory;
import tech.geracao.model.EntityProduct;
import tech.geracao.model.EntityProductImage;
import tech.geracao.model.EntityProductOption;
import tech.geracao.model.EntityUsuarios;
import tech.geracao.repository.RepositoryCategory;
import tech.geracao.repository.RepositoryProduct;
import tech.geracao.repository.UsuarioRepository;
import tech.geracao.service.CategoryService;
import tech.geracao.service.ProductService;
import tech.geracao.service.UsuarioService;
import tech.geracao.view.dtoRequest.RequestLogin;
import tech.geracao.view.dtoRequest.RequestProduct;
import tech.geracao.view.dtoRequest.RequestUser;
import tech.geracao.view.dtoResponse.CategoryResponse;
import tech.geracao.view.dtoResponse.CategorySearchResponse;
import tech.geracao.view.dtoResponse.ProductResponse;
import tech.geracao.view.dtoResponse.ProductSearchItem;
import tech.geracao.view.dtoResponse.ProductSearchResponse;
import tech.geracao.view.dtoResponse.ResponseToken;
import tech.geracao.view.dtoResponse.ResponseUser;
import tech.geracao.view.exceptions.BadRequestExceptionMapper;
import tech.geracao.view.exceptions.ErroResponse;
import tech.geracao.view.exceptions.NotFoundExceptionMapper;
import tech.geracao.view.exceptions.UnauthorizedExceptionMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
// Testes unitários diretos de serviços, entidades, repositórios, DTOs e mappers.
class UnitCoverageTest {

    @Inject UsuarioService usuarioService;
    @Inject ProductService productService;
    @Inject CategoryService categoryService;
    @Inject UsuarioRepository usuarioRepository;
    @Inject RepositoryProduct repositoryProduct;
    @Inject RepositoryCategory repositoryCategory;

    @Test
    void cobreEntidadesEDtos() {
        EntityUsuarios usuario = new EntityUsuarios();
        usuario.setFirstname("Ana"); usuario.setSurname("Silva");
        usuario.setEmail("ana-unit@email.com"); usuario.setPassword("senha-plana");
        usuario.criptografarSenha();
        assertNotEquals("senha-plana", usuario.getPassword());

        EntityCategory categoria = new EntityCategory();
        categoria.name = "Acessórios"; categoria.slug = "acessorios"; categoria.useInMenu = true;

        EntityProduct produto = new EntityProduct();
        produto.name = "Produto"; produto.slug = "produto"; produto.price = 100f;
        produto.priceWithDiscount = 80f; produto.stock = 5; produto.enabled = true;

        EntityProductImage imagem = new EntityProductImage();
        imagem.path = "/produto.jpg"; imagem.enabled = true; imagem.product = produto;
        produto.images = new ArrayList<>(List.of(imagem));

        EntityProductOption option = new EntityProductOption();
        option.title = "Cor"; option.shape = EntityProductOption.Shape.circle;
        option.type = EntityProductOption.OptionType.color; option.radius = 10;
        option.values = "Azul,Vermelho"; option.product = produto;
        produto.options = new ArrayList<>(List.of(option));
        produto.categories = new ArrayList<>(List.of(categoria));

        assertEquals(EntityProductOption.Shape.circle, option.shape);
        assertEquals(EntityProductOption.OptionType.color, option.type);
        assertDoesNotThrow(() -> {
            for (EntityProductOption.Shape shape : EntityProductOption.Shape.values()) shape.name();
            for (EntityProductOption.OptionType type : EntityProductOption.OptionType.values()) type.name();
        });

        ProductResponse response = new ProductResponse(produto.id, true, "Produto", "produto", 5,
                "Descrição", 100f, 80f, List.of(1),
                List.of(new ProductResponse.ImageResponse(1, "/produto.jpg")),
                List.of(new ProductResponse.OptionResponse(1, "Cor", "circle", 10, "color", "Azul,Vermelho")));
        assertEquals("Produto", response.name());

        ProductSearchItem itemCompleto = ProductSearchItem.from(response, Set.of());
        ProductSearchItem itemParcial = ProductSearchItem.from(response, Set.of("id", "name"));
        assertEquals("Produto", itemCompleto.name());
        assertEquals("Produto", itemParcial.name());
        assertNull(itemParcial.price());

        assertNotNull(new ProductSearchResponse(List.of(itemCompleto), 1, 12, 1));
        assertNotNull(new CategoryResponse(1, "Nome", "nome", true));
        assertNotNull(new CategorySearchResponse(List.of(Map.of("id", 1)), 1, 12, 1));
        assertNotNull(new ResponseUser(1, "Ana", "Silva", "ana@email.com"));
        assertNotNull(new ResponseToken("token"));
        assertNotNull(new ErroResponse(400, "erro", java.time.LocalDateTime.now()));

        assertNotNull(new RequestUser("Ana", "Silva", "ana@email.com", "senha"));
        assertNotNull(new RequestLogin("ana@email.com", "senha"));
        assertNotNull(new RequestProduct(true, "Produto", "produto", 5, "Descrição", 100f, 80f,
                List.of(1), List.of(new RequestProduct.ImageRequest(null, "/produto.jpg", null, true)),
                List.of(new RequestProduct.OptionRequest(null, "Cor", "circle", 10, "color", "Azul,Vermelho"))));
    }

    @Test
    void cobreCategoryServiceCrudContagemEBusca() {
        String slug = "cat-unit-" + UUID.randomUUID();
        CategoryResponse criada = categoryService.criarCategoria(new CategoryResponse(null, "Categoria Unit", slug, true));
        assertNotNull(criada.id());

        assertTrue(categoryService.listarCategorias(12, 1, true).size() >= 1);
        assertTrue(categoryService.contarCategorias(true) >= 1);
        assertEquals(categoryService.contarCategorias(null), categoryService.contarCategorias(null));
        assertEquals("Categoria Unit", categoryService.buscarCategoriaPorId(criada.id()).name());

        CategoryResponse editada = categoryService.editarCategoria(criada.id(),
                new CategoryResponse(criada.id(), "Categoria Editada", slug, false));
        assertEquals("Categoria Editada", editada.name());

        categoryService.deletarCategoria(criada.id());
        assertThrows(NotFoundException.class, () -> categoryService.deletarCategoria(999999));
        assertThrows(NotFoundException.class, () -> categoryService.editarCategoria(999999,
                new CategoryResponse(null, "Nome", "slug", true)));
    }

    @Test
    void cobreUsuarioServiceCrudDuplicidadeEAutenticacao() {
        String email = "unit." + UUID.randomUUID() + "@email.com";
        RequestUser request = new RequestUser("Nome", "Sobrenome", email, "senha-segura");
        ResponseUser criada = usuarioService.criarUsuario(request);
        assertNotNull(criada.id());
        assertTrue(usuarioService.listarUsuarios().size() >= 1);
        assertEquals(criada.email(), usuarioService.usuarioPorId(criada.id()).email());

        assertThrows(BadRequestException.class, () -> usuarioService.criarUsuario(request));

        String outroEmail = "unit.outro." + UUID.randomUUID() + "@email.com";
        ResponseUser outroUsuario = usuarioService.criarUsuario(
                new RequestUser("Outro", "Usuário", outroEmail, "outra-senha"));
        assertThrows(BadRequestException.class, () -> usuarioService.editarUsuario(criada.id(),
                new RequestUser("Nome", "Sobrenome", outroEmail, "senha")));

        ResponseUser editada = usuarioService.editarUsuario(criada.id(),
                new RequestUser("Nome Editado", "Sobrenome", email, "nova-senha"));
        assertEquals("Nome Editado", editada.firstName());

        assertThrows(UnauthorizedException.class,
                () -> usuarioService.gerarToken(new RequestLogin(email, "senha-errada")));

        usuarioService.deletarUsuario(criada.id());
        usuarioService.deletarUsuario(outroUsuario.id());
        assertThrows(NotFoundException.class, () -> usuarioService.deletarUsuario(criada.id()));
        assertThrows(NotFoundException.class, () -> usuarioService.editarUsuario(criada.id(), request));
    }

    @Test
    void cobreProductServiceFiltrosCrudValidacoes() {
        CategoryResponse categoria = categoryService.criarCategoria(new CategoryResponse(null,
                "Categoria Produto", "cat-prod-" + UUID.randomUUID(), true));

        RequestProduct request = new RequestProduct(true, "Produto Unit", "produto-unit-" + UUID.randomUUID(), 7,
                "Descrição", 250f, 200f, List.of(categoria.id()),
                List.of(new RequestProduct.ImageRequest(null, "/unit.jpg", null, true)),
                List.of(new RequestProduct.OptionRequest(null, "Tamanho", "square", 4, "text", "P,M,G")));

        ProductResponse criada = productService.criar(request);
        assertNotNull(criada.id());
        assertEquals("Produto Unit", productService.buscarPorId(criada.id()).name());

        assertFalse(productService.listar(12, 1, "unit", List.of(categoria.id()), 100f, 300f,
                Map.of()).isEmpty());
        assertTrue(productService.contar("unit", List.of(categoria.id()), 100f, 300f,
                Map.of()) >= 1);
        assertFalse(productService.listar(-1, 1, null, List.of(), null, null,
                Map.of()).isEmpty());

        ProductResponse editada = productService.atualizar(criada.id(),
                new RequestProduct(true, "Produto Unit Editado", criada.slug(), 5,
                        "Nova descrição", 300f, 250f, List.of(categoria.id()),
                        List.of(), List.of()));
        assertEquals("Produto Unit Editado", editada.name());

        assertThrows(BadRequestException.class, () -> productService.criar(null));
        assertThrows(BadRequestException.class, () -> productService.criar(
                new RequestProduct(true, "", "slug", 1, null, 10f, 8f, null, null, null)));
        assertThrows(BadRequestException.class, () -> productService.criar(
                new RequestProduct(true, "Nome", "", 1, null, 10f, 8f, null, null, null)));
        assertThrows(BadRequestException.class, () -> productService.criar(
                new RequestProduct(true, "Nome", "slug", 1, null, null, null, null, null, null)));

        productService.deletar(criada.id());
        assertThrows(NotFoundException.class, () -> productService.atualizar(999999, request));
        assertThrows(NotFoundException.class, () -> productService.deletar(999999));

        categoryService.deletarCategoria(categoria.id());
    }

    @Test
    void cobreRepositóriosMappersEExcecoes() {
        assertFalse(usuarioRepository.existePorEmail("nao-existe-" + UUID.randomUUID() + "@email.com"));
        assertFalse(usuarioRepository.existePorEmailExcetoId("nao-existe@email.com", 999999));
        assertNotNull(repositoryProduct);
        assertNotNull(repositoryCategory);

        ErroResponse bad = (ErroResponse) new BadRequestExceptionMapper()
                .toResponse(new BadRequestException("dados inválidos")).getEntity();
        assertEquals(400, bad.status());

        ErroResponse unauthorized = (ErroResponse) new UnauthorizedExceptionMapper()
                .toResponse(new UnauthorizedException("token inválido")).getEntity();
        assertEquals(401, unauthorized.status());

        ErroResponse notFound = (ErroResponse) new NotFoundExceptionMapper()
                .toResponse(new NotFoundException("não encontrado")).getEntity();
        assertEquals(404, notFound.status());
    }
}
