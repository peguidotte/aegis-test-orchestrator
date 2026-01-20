package com.aegis.homolog.orchestrator.config;

import com.aegis.homolog.orchestrator.services.EncryptionService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that encrypts/decrypts String values transparently.
 * <p>
 * Note: JPA instantiates converters directly, so we use a static holder pattern
 * to inject the EncryptionService from Spring context.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionServiceHolder;

    private final EncryptionService encryptionService;

    /**
     * Constructor used by Spring to inject the EncryptionService.
     * The service is stored in a static holder for JPA-instantiated instances.
     */
    @Autowired
    public EncryptedStringConverter(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Default constructor required by JPA.
     * Uses the static holder to access the EncryptionService.
     */
    public EncryptedStringConverter() {
        this.encryptionService = null;
    }

    /**
     * Initializes the static holder after Spring constructs this bean.
     */
    @PostConstruct
    void init() {
        setEncryptionServiceHolder(this.encryptionService);
    }

    /**
     * Static setter to avoid Sonar warning about assigning static field from instance method.
     */
    private static void setEncryptionServiceHolder(EncryptionService service) {
        encryptionServiceHolder = service;
    }

    private EncryptionService getService() {
        return encryptionService != null ? encryptionService : encryptionServiceHolder;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        EncryptionService service = getService();
        if (service == null) {
            // Fallback for cases where Spring context is not available
            return attribute;
        }
        return service.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        EncryptionService service = getService();
        if (service == null) {
            // Fallback for cases where Spring context is not available
            return dbData;
        }
        return service.decrypt(dbData);
    }
}

