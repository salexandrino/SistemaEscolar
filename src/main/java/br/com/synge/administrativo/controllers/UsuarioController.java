package br.com.synge.administrativo.controllers;

import br.com.synge.administrativo.dtos.UsuarioCadastroDTO;
import br.com.synge.administrativo.models.Usuario;
import br.com.synge.administrativo.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable UUID id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@Valid @RequestBody UsuarioCadastroDTO usuarioDTO) {
        // Mapear DTO para Model
        Usuario usuario = new Usuario();
        usuario.setEscolaId(usuarioDTO.escolaId()); // Assumindo que o escolaId virá do contexto de segurança ou do DTO
        usuario.setCpf(usuarioDTO.cpf());
        usuario.setSenha(usuarioDTO.senha());
        usuario.setNome(usuarioDTO.nome());
        usuario.setEmail(usuarioDTO.email());
        usuario.setRole(usuarioDTO.role());
        usuario.setAtivo(usuarioDTO.ativo() != null ? usuarioDTO.ativo() : true);

        Usuario createdUsuario = usuarioService.create(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable UUID id, @Valid @RequestBody UsuarioCadastroDTO usuarioDTO) {
        Usuario usuarioDetails = new Usuario();
        usuarioDetails.setNome(usuarioDTO.nome());
        usuarioDetails.setEmail(usuarioDTO.email());
        usuarioDetails.setRole(usuarioDTO.role());
        usuarioDetails.setAtivo(usuarioDTO.ativo());
        usuarioDetails.setSenha(usuarioDTO.senha()); // A lógica de hash está no serviço

        Usuario updatedUsuario = usuarioService.update(id, usuarioDetails);
        return ResponseEntity.ok(updatedUsuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable UUID id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
