package com.example.campomarket.controller;

import com.example.campomarket.model.Mensaje;
import com.example.campomarket.model.Producto;
import com.example.campomarket.model.Usuario;
import com.example.campomarket.repository.MensajeRepository;
import com.example.campomarket.repository.ProductoRepository;
import com.example.campomarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*", allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST})
public class MensajeController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Usuario getUsuarioAutenticado() {
        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    // ── CONVERSACIÓN ───────────────────────────────────
    // GET /api/mensajes/conversacion?productoId=1&otroUsuarioId=3
    @GetMapping("/conversacion")
    public ResponseEntity<?> getConversacion(
            @RequestParam Long productoId,
            @RequestParam Long otroUsuarioId) {

        Usuario yo = getUsuarioAutenticado();
        if (yo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!usuarioRepository.existsById(otroUsuarioId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El usuario no existe");
        }

        List<Mensaje> mensajes = mensajeRepository
                .findConversacion(productoId, yo.getId(), otroUsuarioId);

        return ResponseEntity.ok(mensajes);
    }

    // ── MIS CHATS ──────────────────────────────────────
    // GET /api/mensajes/mis-chats
    @GetMapping("/mis-chats")
    public ResponseEntity<?> getMisChats() {
        Usuario yo = getUsuarioAutenticado();
        if (yo == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(mensajeRepository.findMensajesDelUsuario(yo.getId()));
    }

    // ── ENVIAR MENSAJE ─────────────────────────────────
    // POST /api/mensajes/enviar
    // Body: { "destinatarioId": 3, "productoId": 5, "contenido": "Hola" }
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@RequestBody Map<String, Object> body) {

        Usuario remitente = getUsuarioAutenticado();
        if (remitente == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long destinatarioId = Long.valueOf(body.get("destinatarioId").toString());
        Long productoId     = Long.valueOf(body.get("productoId").toString());
        String contenido    = body.get("contenido").toString().trim();

        if (contenido.isEmpty())
            return ResponseEntity.badRequest().body("El mensaje no puede estar vacío");

        if (destinatarioId.equals(remitente.getId()))
            return ResponseEntity.badRequest().body("No puedes enviarte mensajes a ti mismo");

        Usuario destinatario = usuarioRepository.findById(destinatarioId).orElse(null);
        if (destinatario == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Destinatario no existe");

        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no existe");

        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setProducto(producto);
        mensaje.setContenido(contenido);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeRepository.save(mensaje));
    }
}
