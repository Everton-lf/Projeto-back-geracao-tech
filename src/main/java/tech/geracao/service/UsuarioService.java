package tech.geracao.service;


import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import tech.geracao.view.dtoRequest.RequestUser;
import tech.geracao.view.dtoResponse.ResponseUser;
import tech.geracao.model.EntityUsuarios;
import tech.geracao.repository.UsuarioRepository;

import java.util.List;


@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository userRepository;

    @Transactional
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


    public List<ResponseUser> listarUsuarios() {
        List<EntityUsuarios> usuarios = userRepository.listAll();
        return usuarios.stream()
                .map(usuario -> new ResponseUser(usuario.getId(), usuario.getFirstname(), usuario.getSurname(), usuario.getEmail()))
                .toList();
    }

    public ResponseUser usuarioPorId(Integer id) {
        EntityUsuarios usuarioOptional = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

            return new ResponseUser(usuarioOptional.getId(), usuarioOptional.getFirstname(), usuarioOptional.getSurname(), usuarioOptional.getEmail());
    }

    public void deletarUsuario(Integer id) {
        EntityUsuarios usuario = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.delete(usuario);
    }
    public ResponseUser editarUsuario(Integer id, RequestUser usuario) {
        EntityUsuarios entityUsuario = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        entityUsuario.setFirstname(usuario.firstName());
        entityUsuario.setSurname(usuario.surname());
        entityUsuario.setEmail(usuario.email());
        entityUsuario.setPassword(usuario.password());
        userRepository.persist(entityUsuario);
        return new ResponseUser(entityUsuario.getId(), entityUsuario.getFirstname(), entityUsuario.getSurname(), entityUsuario.getEmail());
    }

}