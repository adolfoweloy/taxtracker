-- use ALTER TYPE to add new types if needed
-- added values cannot be removed later
-- reference: https://www.postgresql.org/docs/current/sql-altertype.html
CREATE TYPE income_type AS ENUM ('cdb', 'vgbl', 'etf', 'dividend', 'royalties', 'interest');

-- table with details about what is paid to ATO useful for instalments
CREATE TABLE ato_payment (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    income income_type NOT NULL,
    income_declared DECIMAL(6, 2) NOT NULL DEFAULT 0,
    tax_paid DECIMAL(6, 2) NOT NULL DEFAULT 0,
    payment_date DATE NOT NULL
);

CREATE INDEX idx__ato_payment__income ON ato_payment(income);
