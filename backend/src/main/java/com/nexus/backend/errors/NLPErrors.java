package com.nexus.backend.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NLPErrors extends RuntimeException {
    private String message;
}
