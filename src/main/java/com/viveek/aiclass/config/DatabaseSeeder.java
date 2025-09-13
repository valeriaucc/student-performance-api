package com.viveek.aiclass.config;

import com.viveek.aiclass.model.*;
import com.viveek.aiclass.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepo,
            MateriaRepository materiaRepo,
            ClaseRepository claseRepo,
            EstudianteClaseRepository estudianteClaseRepo,
            NotaRepository notaRepo,
            RecomendacionIARepository recomendacionRepo
    ) {
        return args -> {
            // Verificar si ya existen datos
            if (usuarioRepo.count() > 0) {
                System.out.println("📊 Datos ya existen en la base de datos. Saltando inicialización.");
                return;
            }
            
            System.out.println("🌱 Inicializando base de datos con datos de ejemplo...");
            
            // 1. Crear usuarios
            Usuario profesor = new Usuario(null, "Ana Ruiz", "ana@univ.edu", "profesor", LocalDateTime.now(), null, null);
            Usuario estudiante1 = new Usuario(null, "Carlos Gómez", "carlos@univ.edu", "estudiante", LocalDateTime.now(), null, null);
            Usuario estudiante2 = new Usuario(null, "Laura Pérez", "laura@univ.edu", "estudiante", LocalDateTime.now(), null, null);
            usuarioRepo.saveAll(List.of(profesor, estudiante1, estudiante2));

            // 2. Crear materia
            Materia materia = new Materia(null, "Matemáticas I", "MAT101", "Introducción al álgebra", null);
            materiaRepo.save(materia);

            // 3. Crear clase
            Clase clase = new Clase(null, materia, profesor, "A", 2025, 2, null, null, null);
            claseRepo.save(clase);

            // 4. Asignar estudiantes a la clase
            EstudianteClase ec1 = new EstudianteClase(null, clase, estudiante1);
            EstudianteClase ec2 = new EstudianteClase(null, clase, estudiante2);
            estudianteClaseRepo.saveAll(List.of(ec1, ec2));

            // 5. Registrar notas
            Nota nota1 = new Nota(null, estudiante1, clase, "Parcial 1", 2.8, LocalDateTime.now());
            Nota nota2 = new Nota(null, estudiante2, clase, "Parcial 1", 4.5, LocalDateTime.now());
            notaRepo.saveAll(List.of(nota1, nota2));

            // 6. Generar recomendaciones IA
            RecomendacionIA rec1 = new RecomendacionIA(null, estudiante1, clase,
                    "Tu rendimiento en Álgebra está por debajo del promedio. Revisa los temas de factorización y ecuaciones.",
                    "estudiante", LocalDateTime.now());

            RecomendacionIA rec2 = new RecomendacionIA(null, profesor, clase,
                    "La mayoría del grupo obtuvo bajo puntaje en Álgebra. Se recomienda reforzar los temas de factorización.",
                    "profesor", LocalDateTime.now());

            recomendacionRepo.saveAll(List.of(rec1, rec2));

            System.out.println("✅ Datos de ejemplo insertados correctamente.");
        };
    }
}
