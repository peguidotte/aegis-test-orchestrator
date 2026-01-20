package com.aegis.homolog.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class DomainNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "DOMAIN_NOT_FOUND";

    public DomainNotFoundException(Long domainId) {
        super(
                String.format("Domain with ID %d not found", domainId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}

