-- use ALTER TYPE to add new types if needed
-- added values cannot be removed later
-- reference: https://www.postgresql.org/docs/current/sql-altertype.html
CREATE TYPE vgbl_transaction_type AS ENUM ('acquisition', 'redemption');

-- table that tracks a VGBL fund from its acquisition to withdraws
CREATE TABLE vgbl_track (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fund VARCHAR(20) REFERENCES fund(cnpj),
    quotas DECIMAL(27, 12) NOT NULL DEFAULT 0,
    transaction_date DATE NOT NULL,
    transaction_type vgbl_transaction_type NOT NULL
);
