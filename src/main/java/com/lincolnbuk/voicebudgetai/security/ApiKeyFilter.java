package com.lincolnbuk.voicebudgetai.security;

/**
 * API-key authentication was replaced by stateless JWT authentication.
 * This legacy type remains only to keep upgrades from older commits source-compatible;
 * it is not a Spring bean or request filter.
 */
@Deprecated
public final class ApiKeyFilter {
    private ApiKeyFilter() { }
}
