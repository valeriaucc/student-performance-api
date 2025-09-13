package com.viveek.aiclass.repository;

import com.viveek.aiclass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstudianteClaseRepository extends JpaRepository<EstudianteClase, Long> {
    List<EstudianteClase> findByClaseId(Long claseId);
    List<EstudianteClase> findByEstudianteId(Long estudianteId);
}
