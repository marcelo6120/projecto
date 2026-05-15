package com.example.campomarket.controller;

import com.example.campomarket.model.Usuario;
import com.example.campomarket.repository.UsuarioRepository;
import com.example.campomarket.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    // ── LOGIN ──────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        Optional<Usuario> encontrado = repository.findByUsername(usuario.getUsername());

        if (encontrado.isPresent() &&
            encontrado.get().getPassword().equals(usuario.getPassword())) {

            String token = jwtUtil.generarToken(usuario.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", encontrado.get().getId());
            response.put("username", encontrado.get().getUsername());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario o contraseña incorrectos");
    }

    // ── REGISTRO ───────────────────────────────────────
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {

        // Validar que no exista ya el username
        if (repository.findByUsername(usuario.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El nombre de usuario ya está en uso");
        }

        // Validar que no vengan vacíos
        if (usuario.getUsername() == null || usuario.getUsername().isBlank() ||
            usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("El usuario y la contraseña son obligatorios");
        }

        Usuario guardado = repository.save(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put("id", guardado.getId());
        response.put("username", guardado.getUsername());
        response.put("mensaje", "Cuenta creada correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
