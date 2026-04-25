CREATE TABLE IF NOT EXISTS signature_files (
    signature_id UUID PRIMARY KEY,
    bucket_name VARCHAR(128) NOT NULL,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_signature_files_signature
        FOREIGN KEY (signature_id) REFERENCES signatures (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_signature_files_uploaded_at ON signature_files (uploaded_at);
