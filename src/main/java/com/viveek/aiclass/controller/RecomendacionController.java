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
@RequestMapping("/api/recomendaciones")
@Tag(name = "AI Recommendations", description = "API para gestión de recomendaciones generadas por IA")
public class RecomendacionController {
    @Autowired
    private RecomendacionIARepository recomendacionIARepository;

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener recomendaciones por usuario", description = "Retorna todas las recomendaciones generadas para un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recomendaciones obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<RecomendacionIA> getByUsuario(@Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return recomendacionIARepository.findByUsuarioId(usuarioId);
    }

    @GetMapping("/clase/{claseId}")
    @Operation(summary = "Obtener recomendaciones por clase", description = "Retorna todas las recomendaciones generadas para una clase específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recomendaciones obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Clase no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<RecomendacionIA> getByClase(@Parameter(description = "ID de la clase") @PathVariable Long claseId) {
        return recomendacionIARepository.findByClaseId(claseId);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Obtener recomendaciones por tipo", description = "Retorna todas las recomendaciones filtradas por tipo (estudiante, profesor, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de recomendaciones obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Tipo de recomendación inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<RecomendacionIA> getByTipo(@Parameter(description = "Tipo de recomendación (estudiante, profesor, etc.)") @PathVariable String tipo) {
        return recomendacionIARepository.findByTipo(tipo);
    }
}
