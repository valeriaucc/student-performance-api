package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Grade management operations.
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade management APIs")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @Operation(summary = "Create a new grade", description = "Creates a new grade for a student")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Grade created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class or Student not found")
    })
    public ResponseEntity<ApiResponse<GradeResponse>> createGrade(
            @Valid @RequestBody CreateGradeRequest request) {
        GradeResponse grade = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grade created successfully", grade));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update grade", description = "Updates an existing grade")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @Parameter(description = "Grade ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateGradeRequest request) {
        GradeResponse grade = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", grade));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID", description = "Retrieves a grade by its ID")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(
            @Parameter(description = "Grade ID") @PathVariable UUID id) {
        GradeResponse grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @GetMapping
    @Operation(summary = "Get grades", description = "Retrieves grades with optional filters")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGrades(
            @Parameter(description = "Filter by class ID") @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by student ID") @RequestParam(required = false) UUID studentId) {
        
        List<GradeResponse> grades;
        if (classId != null && studentId != null) {
            grades = gradeService.getGradesByClassAndStudent(classId, studentId);
        } else if (classId != null) {
            grades = gradeService.getGradesByClassId(classId);
        } else if (studentId != null) {
            grades = gradeService.getGradesByStudentId(studentId);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide at least one filter parameter (classId or studentId)"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete grade", description = "Deletes a grade by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Grade deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Grade not found")
    })
    public ResponseEntity<Void> deleteGrade(
            @Parameter(description = "Grade ID") @PathVariable UUID id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}

