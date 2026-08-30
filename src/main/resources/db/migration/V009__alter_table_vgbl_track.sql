-- an initial buy and a later top-up are the same kind of event
ALTER TYPE vgbl_transaction_type RENAME VALUE 'acquisition' TO 'contribution';

ALTER TABLE vgbl_track RENAME COLUMN fund TO cnpj;
ALTER TABLE vgbl_track ALTER COLUMN cnpj SET NOT NULL;

ALTER TABLE vgbl_track ADD COLUMN amount DECIMAL(27, 12) NOT NULL;
ALTER TABLE vgbl_track ADD COLUMN quota_price DECIMAL(27, 12) NOT NULL;
ALTER TABLE vgbl_track ALTER COLUMN quotas DROP DEFAULT;

ALTER TABLE vgbl_track ADD CONSTRAINT uq_vgbl_track_cnpj_date_type
    UNIQUE (cnpj, transaction_date, transaction_type);

CREATE INDEX idx_vgbl_track_cnpj ON vgbl_track(cnpj);
