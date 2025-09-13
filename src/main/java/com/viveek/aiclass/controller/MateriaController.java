package com.viveek.aiclass.controller;

import com.viveek.aiclass.model.*;
import com.viveek.aiclass.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
@Tag(name = "Subject Management", description = "API para gestión de materias académicas")
public class MateriaController {
    @Autowired
    private MateriaRepository materiaRepository;

    @GetMapping
    @Operation(summary = "Obtener todas las materias", description = "Retorna una lista de todas las materias registradas en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de materias obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Materia> getAllMaterias() {
        return materiaRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "Crear nueva materia", description = "Crea una nueva materia académica en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Materia creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Materia createMateria(@Parameter(description = "Datos de la materia a crear") @RequestBody Materia materia) {
        return materiaRepository.save(materia);
    }
}
