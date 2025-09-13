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
@RequestMapping("/api/notas")
@Tag(name = "Grade Management", description = "API para gestión de calificaciones y notas")
public class NotaController {
    @Autowired
    private NotaRepository notaRepository;

    @GetMapping("/clase/{claseId}")
    @Operation(summary = "Obtener notas por clase", description = "Retorna todas las notas registradas para una clase específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notas obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Clase no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Nota> getNotasByClase(@Parameter(description = "ID de la clase") @PathVariable Long claseId) {
        return notaRepository.findByClaseId(claseId);
    }

    @GetMapping("/estudiante/{estudianteId}")
    @Operation(summary = "Obtener notas por estudiante", description = "Retorna todas las notas registradas para un estudiante específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notas obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Nota> getNotasByEstudiante(@Parameter(description = "ID del estudiante") @PathVariable Long estudianteId) {
        return notaRepository.findByEstudianteId(estudianteId);
    }

    @PostMapping
    @Operation(summary = "Crear nueva nota", description = "Registra una nueva calificación para un estudiante en una clase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Nota createNota(@Parameter(description = "Datos de la nota a crear") @RequestBody Nota nota) {
        return notaRepository.save(nota);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nota", description = "Actualiza una calificación existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Nota updateNota(@Parameter(description = "ID de la nota") @PathVariable Long id, 
                          @Parameter(description = "Datos actualizados de la nota") @RequestBody Nota nota) {
        nota.setId(id);
        return notaRepository.save(nota);
    }
}
