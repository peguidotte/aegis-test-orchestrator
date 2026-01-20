package com.aegis.homolog.orchestrator.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as containing sensitive data that should never be logged.
 *
 * <p>Fields annotated with this should:</p>
 * <ul>
 *     <li>Be excluded from toString() via @ToString(exclude = ...)</li>
 *     <li>Maybe be encrypted at rest via @Convert(converter = EncryptedStringConverter.class)</li>
 *     <li>Never appear in logs, error messages, or API responses</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;Sensitive
 * &#64;Convert(converter = EncryptedStringConverter.class)
 * private String password;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {
}

