package com.viveek.aiclass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AiclassApplicationTest {

    @Test
    void main_HasCorrectSignature() throws NoSuchMethodException {
        Method mainMethod = AiclassApplication.class.getMethod("main", String[].class);
        
        assertThat(mainMethod).isNotNull();
        assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
    }

    @Test
    void main_IsPublicStatic() throws NoSuchMethodException {
        Method mainMethod = AiclassApplication.class.getMethod("main", String[].class);
        
        assertThat(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers())).isTrue();
        assertThat(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())).isTrue();
    }

    @Test
    void applicationClass_HasSpringBootApplicationAnnotation() {
        assertThat(AiclassApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class
        )).isTrue();
    }

    @Test
    void applicationClass_IsPublic() {
        assertThat(java.lang.reflect.Modifier.isPublic(
                AiclassApplication.class.getModifiers()
        )).isTrue();
    }

    @Test
    void applicationClass_CanBeInstantiated() {
        AiclassApplication app = new AiclassApplication();
        
        assertThat(app).isNotNull();
    }

    @Test
    void applicationClass_ExtendsNoClass() {
        assertThat(AiclassApplication.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void applicationClass_ImplementsNoInterfaces() {
        assertThat(AiclassApplication.class.getInterfaces()).isEmpty();
    }

    @Test
    void main_AcceptsStringArray() throws NoSuchMethodException {
        Method mainMethod = AiclassApplication.class.getMethod("main", String[].class);
        
        assertThat(mainMethod.getParameterTypes()).hasSize(1);
        assertThat(mainMethod.getParameterTypes()[0]).isEqualTo(String[].class);
    }
}

