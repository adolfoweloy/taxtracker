-- Replace native Postgres enum with varchar + check constraint on vgbl_track.transaction_type

-- V010's check constraint expression binds 'redemption' to the enum type at creation time,
-- so it must be dropped before the column type change and recreated after.
ALTER TABLE vgbl_track DROP CONSTRAINT chk_vgbl_track_br_tax_on_redemption;

ALTER TABLE vgbl_track
    ALTER COLUMN transaction_type TYPE VARCHAR(20)
    USING transaction_type::text;

ALTER TABLE vgbl_track
    ADD CONSTRAINT chk_vgbl_track_transaction_type
    CHECK (transaction_type IN ('CONTRIBUTION', 'REDEMPTION'));

ALTER TABLE vgbl_track ADD CONSTRAINT chk_vgbl_track_br_tax_on_redemption
    CHECK (transaction_type <> 'REDEMPTION' OR br_tax IS NOT NULL);

DROP TYPE vgbl_transaction_type;
