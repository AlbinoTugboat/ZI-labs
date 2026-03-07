ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_credentials_expired BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_disabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) UNIQUE NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS license_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    default_duration_in_days INTEGER NOT NULL CHECK (default_duration_in_days > 0),
    description VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS license (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT,
    product_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    first_activation_date TIMESTAMP,
    ending_date TIMESTAMP,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    device_count INTEGER NOT NULL CHECK (device_count > 0),
    owner_id BIGINT NOT NULL,
    description VARCHAR(2048),

    CONSTRAINT fk_license_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_license_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_license_type FOREIGN KEY (type_id) REFERENCES license_type(id),
    CONSTRAINT fk_license_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS device (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    mac_address VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS device_license (
    id BIGSERIAL PRIMARY KEY,
    license_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    activation_date TIMESTAMP NOT NULL,

    CONSTRAINT fk_device_license_license FOREIGN KEY (license_id) REFERENCES license(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_license_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE,
    CONSTRAINT uq_device_license UNIQUE (license_id, device_id)
);

CREATE TABLE IF NOT EXISTS license_history (
    id BIGSERIAL PRIMARY KEY,
    license_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    change_date TIMESTAMP NOT NULL,
    description VARCHAR(2048),

    CONSTRAINT fk_license_history_license FOREIGN KEY (license_id) REFERENCES license(id) ON DELETE CASCADE,
    CONSTRAINT fk_license_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_license_user_id ON license(user_id);
CREATE INDEX IF NOT EXISTS idx_license_product_id ON license(product_id);
CREATE INDEX IF NOT EXISTS idx_license_type_id ON license(type_id);
CREATE INDEX IF NOT EXISTS idx_license_owner_id ON license(owner_id);
CREATE INDEX IF NOT EXISTS idx_license_blocked_ending ON license(blocked, ending_date);

CREATE INDEX IF NOT EXISTS idx_device_user_id ON device(user_id);
CREATE INDEX IF NOT EXISTS idx_device_mac_address ON device(mac_address);

CREATE INDEX IF NOT EXISTS idx_device_license_license_id ON device_license(license_id);
CREATE INDEX IF NOT EXISTS idx_device_license_device_id ON device_license(device_id);

CREATE INDEX IF NOT EXISTS idx_license_history_license_id ON license_history(license_id);
CREATE INDEX IF NOT EXISTS idx_license_history_user_id ON license_history(user_id);
CREATE INDEX IF NOT EXISTS idx_license_history_change_date ON license_history(change_date);

INSERT INTO product (name, is_blocked)
VALUES ('Sample Product', FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO license_type (name, default_duration_in_days, description)
VALUES ('STANDARD_30', 30, 'Default 30-day license')
ON CONFLICT (name) DO NOTHING;
