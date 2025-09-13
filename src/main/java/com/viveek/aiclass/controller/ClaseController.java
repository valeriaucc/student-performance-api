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
@RequestMapping("/api/clases")
@Tag(name = "Class Management", description = "API para gestión de clases académicas")
public class ClaseController {
    @Autowired
    private ClaseRepository claseRepository;

    @GetMapping
    @Operation(summary = "Obtener todas las clases", description = "Retorna una lista de todas las clases registradas en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clases obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Clase> getAllClases() {
        return claseRepository.findAll();
    }

    @GetMapping("/profesor/{profesorId}")
    @Operation(summary = "Obtener clases por profesor", description = "Retorna todas las clases asignadas a un profesor específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clases del profesor obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Clase> getClasesByProfesor(@Parameter(description = "ID del profesor") @PathVariable Long profesorId) {
        return claseRepository.findByProfesorId(profesorId);
    }

    @PostMapping
    @Operation(summary = "Crear nueva clase", description = "Crea una nueva clase académica en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clase creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Clase createClase(@Parameter(description = "Datos de la clase a crear") @RequestBody Clase clase) {
        return claseRepository.save(clase);
    }
}
