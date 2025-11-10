package com.viveek.aiclass.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiVersionsTest {

    @Test
    void constructor_PrivateConstructor() throws Exception {
        // Use reflection to test private constructor exists
        java.lang.reflect.Constructor<ApiVersions> constructor = ApiVersions.class.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();
    }

    @Test
    void constants_HaveCorrectValues() {
        assertThat(ApiVersions.API_BASE).isEqualTo("/api");
        assertThat(ApiVersions.V1).isEqualTo("/api/v1");
        assertThat(ApiVersions.V2).isEqualTo("/api/v2");
        assertThat(ApiVersions.DEPRECATION_HEADER).isEqualTo("X-API-Deprecation");
        assertThat(ApiVersions.SUNSET_HEADER).isEqualTo("Sunset");
    }
}

