package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.domain.model.enums.Semester;
import com.viveek.aiclass.dto.request.CreateClassRequest;
import com.viveek.aiclass.dto.request.UpdateClassRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.ClassResponse;
import com.viveek.aiclass.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Class management operations.
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Class management APIs")
public class ClassController {

    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new class", description = "Creates a new academic class (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Class created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subject or Teacher not found")
    })
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(
            @Valid @RequestBody CreateClassRequest request) {
        ClassResponse classResponse = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Class created successfully", classResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update class", description = "Updates an existing class (TEACHER only)")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClass(
            @Parameter(description = "Class ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateClassRequest request) {
        ClassResponse classResponse = classService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Class updated successfully", classResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get class by ID", description = "Retrieves a class by its ID (authenticated users)")
    public ResponseEntity<ApiResponse<ClassResponse>> getClassById(
            @Parameter(description = "Class ID") @PathVariable UUID id) {
        ClassResponse classResponse = classService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.success(classResponse));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all classes", description = "Retrieves all classes with optional filters (authenticated users)")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAllClasses(
            @Parameter(description = "Filter by teacher ID") @RequestParam(required = false) UUID teacherId,
            @Parameter(description = "Filter by subject ID") @RequestParam(required = false) UUID subjectId,
            @Parameter(description = "Filter by year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by semester") @RequestParam(required = false) Semester semester) {
        
        List<ClassResponse> classes;
        if (teacherId != null) {
            classes = classService.getClassesByTeacherId(teacherId);
        } else if (subjectId != null) {
            classes = classService.getClassesBySubjectId(subjectId);
        } else if (year != null && semester != null) {
            classes = classService.getClassesByYearAndSemester(year, semester);
        } else {
            classes = classService.getAllClasses();
        }
        
        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete class", description = "Deletes a class by its ID (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Class deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<Void> deleteClass(
            @Parameter(description = "Class ID") @PathVariable UUID id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}

