package com.example.informationprotection.service.license;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CanonicalizationService {

    private final ObjectMapper objectMapper;

    public CanonicalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] canonicalize(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("INPUT_INVALID: payload is empty");
        }

        try {
            String json = objectMapper.writeValueAsString(payload);
            JsonCanonicalizer canonicalizer = new JsonCanonicalizer(json);
            return canonicalizer.getEncodedUTF8();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("CANONICALIZATION_FAILED: cannot serialize payload");
        } catch (Exception e) {
            throw new IllegalArgumentException("CANONICALIZATION_FAILED: cannot canonicalize payload");
        }
    }
}
