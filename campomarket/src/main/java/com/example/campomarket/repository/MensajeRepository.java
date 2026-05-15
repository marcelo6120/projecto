package com.example.campomarket.repository;

import com.example.campomarket.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.producto.id = :productoId
          AND (
            (m.remitente.id = :u1 AND m.destinatario.id = :u2)
            OR
            (m.remitente.id = :u2 AND m.destinatario.id = :u1)
          )
        ORDER BY m.fechaEnvio ASC
    """)
    List<Mensaje> findConversacion(
        @Param("productoId") Long productoId,
        @Param("u1") Long u1,
        @Param("u2") Long u2
    );

    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.remitente.id = :usuarioId OR m.destinatario.id = :usuarioId
        ORDER BY m.fechaEnvio DESC
    """)
    List<Mensaje> findMensajesDelUsuario(@Param("usuarioId") Long usuarioId);
}
