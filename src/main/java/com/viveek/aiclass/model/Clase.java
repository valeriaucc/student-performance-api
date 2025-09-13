package com.viveek.aiclass.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    private String grupo;
    private Integer anio;
    private Integer semestre;

    @OneToMany(mappedBy = "clase")
    private List<EstudianteClase> estudiantes;

    @OneToMany(mappedBy = "clase")
    private List<Nota> notas;

    @OneToMany(mappedBy = "clase")
    private List<RecomendacionIA> recomendaciones;
}
