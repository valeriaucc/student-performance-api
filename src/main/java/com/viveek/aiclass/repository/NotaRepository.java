package com.viveek.aiclass.repository;

import com.viveek.aiclass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByClaseId(Long claseId);
    List<Nota> findByEstudianteId(Long estudianteId);
}
