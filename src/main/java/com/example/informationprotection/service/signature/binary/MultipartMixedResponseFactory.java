package com.example.informationprotection.service.signature.binary;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class MultipartMixedResponseFactory {

    public ResponseEntity<MultiValueMap<String, Object>> build(BinarySignaturePackage binaryPackage) {
        if (binaryPackage == null) {
            throw new IllegalArgumentException("binaryPackage is required");
        }

        LinkedMultiValueMap<String, Object> responseBody = new LinkedMultiValueMap<>();
        responseBody.add("manifest", createPart("manifest.bin", binaryPackage.manifestBytes()));
        responseBody.add("data", createPart("data.bin", binaryPackage.dataBytes()));

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(responseBody);
    }

    private HttpEntity<byte[]> createPart(String fileName, byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("Multipart content is required for " + fileName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        headers.setContentLength(content.length);
        return new HttpEntity<>(content, headers);
    }
}
