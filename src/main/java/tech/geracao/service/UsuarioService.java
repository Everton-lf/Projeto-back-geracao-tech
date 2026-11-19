package tech.geracao.service;


import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import io.quarkus.security.UnauthorizedException;
import tech.geracao.view.dtoRequest.RequestLogin;
import tech.geracao.view.dtoRequest.RequestUser;
import tech.geracao.view.dtoResponse.ResponseToken;
import tech.geracao.view.dtoResponse.ResponseUser;
import tech.geracao.model.EntityUsuarios;
import tech.geracao.repository.UsuarioRepository;

import java.time.Duration;
import java.util.List;
import java.util.Set;


// UsuarioService — Camada de negócio dos usuários: cadastro, consulta, atualização, exclusão e autenticação.
@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository userRepository;

    @Transactional
    // Verifica duplicidade, protege a senha e cria o usuário.
    public ResponseUser criarUsuario(RequestUser request) {
        if (userRepository.existePorEmail(request.email())) {
            throw new BadRequestException("Já existe um usuário cadastrado com este e-mail.");
        }

        EntityUsuarios novoUsuario = new EntityUsuarios();
        novoUsuario.setFirstname(request.firstName());
        novoUsuario.setSurname(request.surname());
        novoUsuario.setEmail(request.email());
        novoUsuario.setPassword(BcryptUtil.bcryptHash(request.password()));


        userRepository.persist(novoUsuario);

        return new ResponseUser(
                novoUsuario.getId(),
                novoUsuario.getFirstname(),
                novoUsuario.getSurname(),
                novoUsuario.getEmail()
        );
    }


    // Consulta todos os usuários e remove dados sensíveis da resposta.
    public List<ResponseUser> listarUsuarios() {
        List<EntityUsuarios> usuarios = userRepository.listAll();
        return usuarios.stream()
                .map(usuario -> new ResponseUser(usuario.getId(), usuario.getFirstname(), usuario.getSurname(), usuario.getEmail()))
                .toList();
    }

    // Busca um usuário pelo identificador ou informa que ele não existe.
    public ResponseUser usuarioPorId(Integer id) {
        EntityUsuarios usuarioOptional = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

            return new ResponseUser(usuarioOptional.getId(), usuarioOptional.getFirstname(), usuarioOptional.getSurname(), usuarioOptional.getEmail());
    }

    // Localiza e remove um usuário existente.
    @Transactional
    public void deletarUsuario(Integer id) {
        EntityUsuarios usuario = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.delete(usuario);
    }
    // Atualiza os dados pessoais de um usuário já cadastrado.
    @Transactional
    public ResponseUser editarUsuario(Integer id, RequestUser usuario) {
        EntityUsuarios entityUsuario = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (userRepository.existePorEmailExcetoId(usuario.email(), id)) {
            throw new BadRequestException("Já existe um usuário cadastrado com este e-mail.");
        }
        entityUsuario.setFirstname(usuario.firstName());
        entityUsuario.setSurname(usuario.surname());
        entityUsuario.setEmail(usuario.email());
        entityUsuario.setPassword(BcryptUtil.bcryptHash(usuario.password()));
        userRepository.persist(entityUsuario);
        return new ResponseUser(entityUsuario.getId(), entityUsuario.getFirstname(), entityUsuario.getSurname(), entityUsuario.getEmail());
    }

    // Valida as credenciais e gera um token JWT válido por duas horas.
    public ResponseToken gerarToken(RequestLogin resquest){
        EntityUsuarios usuario = userRepository.find("email",resquest.email()).firstResultOptional().orElseThrow(() -> new UnauthorizedException("Usuário ou senha não encontrados"));
        if (!BcryptUtil.matches(resquest.password(), usuario.getPassword())) {
            throw new UnauthorizedException("Usuário ou senha não encontrados");
        }
        String token = Jwt.issuer("https://geracao.tech")
                .upn(resquest.email())
                .groups(Set.of("User"))
                .expiresIn(Duration.ofHours(2))
                .sign();
        return new ResponseToken(token);
    }

}