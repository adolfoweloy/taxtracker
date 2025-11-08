CREATE TABLE vgbl_quota (
    cnpj VARCHAR(18) NOT NULL,
    competence_date DATE NOT NULL,
    fund_class VARCHAR(20) NOT NULL,
    quota_value DECIMAL(27, 12) NOT NULL DEFAULT 0,
    PRIMARY KEY (cnpj, competence_date)
);

CREATE INDEX idx_vgbl_quota_cnpj ON vgbl_quota(cnpj);
CREATE INDEX idx_vgbl_quota_competence_date ON vgbl_quota(competence_date);
