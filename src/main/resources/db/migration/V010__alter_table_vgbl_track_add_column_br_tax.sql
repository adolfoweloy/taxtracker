ALTER TABLE vgbl_track ADD COLUMN br_tax DECIMAL(27, 12);

ALTER TABLE vgbl_track ADD CONSTRAINT chk_vgbl_track_br_tax_on_redemption
    CHECK (transaction_type <> 'redemption' OR br_tax IS NOT NULL);
