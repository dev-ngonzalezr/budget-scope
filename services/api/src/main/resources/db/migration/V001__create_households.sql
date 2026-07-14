CREATE TABLE households (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    time_zone VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
