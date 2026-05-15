package com.example.campomarket.controller;

import com.example.campomarket.model.Producto;
import com.example.campomarket.model.Usuario;
import com.example.campomarket.repository.ProductoRepository;
import com.example.campomarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*", allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST,
               RequestMethod.PUT, RequestMethod.DELETE})
public class ProductoController {

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    // ── MARKETPLACE PÚBLICO ────────────────────────────
    // GET /api/productos/marketplace?categoria=Ganado
    @GetMapping("/marketplace")
    public List<Producto> marketplace(
            @RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return repository.findByCategoria(categoria);
        }
        return repository.findAll();
    }

    // ── MIS PRODUCTOS ──────────────────────────────────
    // GET /api/productos
    @GetMapping
    public List<Producto> listar() {
        Usuario user = getUsuarioAutenticado();
        return repository.findByUsuarioId(user.getId());
    }

    // ── VER DETALLE ────────────────────────────────────
    // GET /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── CREAR ──────────────────────────────────────────
    // POST /api/productos
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        Usuario user = getUsuarioAutenticado();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        producto.setUsuario(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(producto));
    }

    // ── ACTUALIZAR ─────────────────────────────────────
    // PUT /api/productos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Producto nuevo) {
        Usuario user = getUsuarioAutenticado();
        return repository.findById(id).map(p -> {
            if (!p.getUsuario().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            p.setNombre(nuevo.getNombre());
            p.setPrecio(nuevo.getPrecio());
            p.setStock(nuevo.getStock());
            p.setCategoria(nuevo.getCategoria());
            p.setDescripcion(nuevo.getDescripcion());
            p.setUnidad(nuevo.getUnidad());
            p.setEmoji(nuevo.getEmoji());
            p.setUbicacion(nuevo.getUbicacion());
            return ResponseEntity.ok(repository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── ELIMINAR ───────────────────────────────────────
    // DELETE /api/productos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id) {
        Usuario user = getUsuarioAutenticado();
        return repository.findById(id).map(p -> {
            if (!p.getUsuario().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
