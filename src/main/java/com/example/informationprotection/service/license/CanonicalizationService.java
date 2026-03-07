package com.example.informationprotection.service.license;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CanonicalizationService {

    private final ObjectMapper canonicalObjectMapper;

    public CanonicalizationService(ObjectMapper objectMapper) {
        this.canonicalObjectMapper = objectMapper.copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public byte[] canonicalize(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("INPUT_INVALID: payload is empty");
        }

        Map<String, Object> normalized = normalizeMap(payload);

        try {
            return canonicalObjectMapper.writeValueAsBytes(normalized);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("OUTPUT_ENCODING_FAILED: cannot encode canonical payload");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeMap(Map<String, Object> input) {
        Map<String, Object> normalized = new TreeMap<>();

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key == null || key.isBlank() || value == null) {
                throw new IllegalArgumentException("CANONICALIZATION_FAILED: null or blank field");
            }

            if (value instanceof String stringValue) {
                normalized.put(key.trim(), stringValue.trim());
            } else if (value instanceof Map<?, ?> mapValue) {
                normalized.put(key.trim(), normalizeMap((Map<String, Object>) mapValue));
            } else if (value instanceof List<?> listValue) {
                List<Object> normalizedList = new ArrayList<>();
                for (Object item : listValue) {
                    if (item == null) {
                        throw new IllegalArgumentException("CANONICALIZATION_FAILED: null list item");
                    }
                    normalizedList.add(item);
                }
                normalized.put(key.trim(), normalizedList);
            } else {
                normalized.put(key.trim(), value);
            }
        }

        return normalized;
    }
}
