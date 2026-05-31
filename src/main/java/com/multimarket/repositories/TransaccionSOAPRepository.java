package com.multimarket.repositories;

import com.multimarket.models.TransaccionSOAP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionSOAPRepository extends JpaRepository<TransaccionSOAP, Long> {
}
