package com.multimarket.repositories;

import com.multimarket.models.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    List<Conversacion> findByCompradorCorreoOrderByFechaCreacionDesc(String compradorCorreo);
    List<Conversacion> findByVendedorUsuarioCorreoOrderByFechaCreacionDesc(String vendedorCorreo);
    Optional<Conversacion> findByCompradorIdAndVendedorId(Long compradorId, Long vendedorId);

    @Query("""
            select case
                when c.comprador.correo = :senderEmail then c.vendedor.usuario.correo
                else c.comprador.correo
            end
            from Conversacion c
            where c.id = :conversationId
            """)
    Optional<String> findRecipientEmailByConversationIdAndSenderEmail(
            @Param("conversationId") Long conversationId,
            @Param("senderEmail") String senderEmail);
}
