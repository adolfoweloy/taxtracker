CREATE TABLE investment_fund_daily_info (
    cnpj VARCHAR(18) NOT NULL,
    competence_date DATE NOT NULL,
    fund_class VARCHAR(20) NOT NULL,
    quota_value DECIMAL(27, 12) NOT NULL DEFAULT 0,
    PRIMARY KEY (cnpj, competence_date)
);

CREATE INDEX idx_investment_fund_daily_info_cnpj ON investment_fund_daily_info(cnpj);
CREATE INDEX idx_investment_fund_daily_info_competence_date ON investment_fund_daily_info(competence_date);
