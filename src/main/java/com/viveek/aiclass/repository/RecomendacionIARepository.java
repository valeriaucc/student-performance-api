package com.viveek.aiclass.repository;

import com.viveek.aiclass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecomendacionIARepository extends JpaRepository<RecomendacionIA, Long> {
    List<RecomendacionIA> findByUsuarioId(Long usuarioId);
    List<RecomendacionIA> findByClaseId(Long claseId);
    List<RecomendacionIA> findByTipo(String tipo);
}
