package com.viveek.aiclass.api;

/**
 * Constants for API versioning.
 * Defines version paths and deprecation information.
 */
public final class ApiVersions {

    private ApiVersions() {
        // Utility class, no instantiation
    }

    /**
     * Base path for all API endpoints.
     */
    public static final String API_BASE = "/api";

    /**
     * Version 1 API path prefix.
     * Current stable version.
     */
    public static final String V1 = API_BASE + "/v1";

    /**
     * Version 2 API path prefix (future).
     * To be used when introducing breaking changes.
     */
    public static final String V2 = API_BASE + "/v2";

    /**
     * Deprecation header name used in responses.
     */
    public static final String DEPRECATION_HEADER = "X-API-Deprecation";

    /**
     * Sunset header name indicating when an API version will be removed.
     */
    public static final String SUNSET_HEADER = "Sunset";
}


