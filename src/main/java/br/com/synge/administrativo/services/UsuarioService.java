package br.com.synge.administrativo.services;

import br.com.synge.administrativo.models.Usuario;
import br.com.synge.administrativo.repositories.UsuarioRepository;
import br.com.synge.seguranca.exceptions.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(UUID id) {
        return usuarioRepository.findById(id);
    }

    public Usuario create(Usuario usuario) {
        if (usuarioRepository.existsByCpf(usuario.getCpf())) {
            logger.warn("Tentativa de criar usuário com CPF duplicado: {}", usuario.getCpf().replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"));
            throw new DomainException("CPF já cadastrado.", HttpStatus.CONFLICT);
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtivo(true);
        usuario.setDataCadastro(LocalDateTime.now());
        Usuario savedUser = usuarioRepository.save(usuario);
        logger.info("Usuário criado com sucesso: {}", savedUser.getId());
        return savedUser;
    }

    public Usuario update(UUID id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new DomainException("Usuário não encontrado.", HttpStatus.NOT_FOUND));

        usuario.setNome(usuarioDetails.getNome());
        usuario.setEmail(usuarioDetails.getEmail());
        usuario.setRole(usuarioDetails.getRole());
        usuario.setAtivo(usuarioDetails.getAtivo());

        // A senha só deve ser atualizada se uma nova for fornecida
        if (usuarioDetails.getSenha() != null && !usuarioDetails.getSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(usuarioDetails.getSenha()));
        }

        Usuario updatedUser = usuarioRepository.save(usuario);
        logger.info("Usuário atualizado com sucesso: {}", updatedUser.getId());
        return updatedUser;
    }

    public void delete(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new DomainException("Usuário não encontrado.", HttpStatus.NOT_FOUND);
        }
        usuarioRepository.deleteById(id);
        logger.info("Usuário excluído com sucesso: {}", id);
    }
}
