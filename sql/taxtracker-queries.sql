
select * from vgbl_track where cnpj = '26.756.416/0001-28';

select * from vgbl_quota 
where cnpj = '26.756.416/0001-28' 
and competence_date between '2025-11-01' and '2025-11-28'
order by competence_date desc;


select * from fund;




WITH
redemption AS (
    SELECT DISTINCT ON (cnpj) cnpj, transaction_type, transaction_date
    FROM vgbl_track
    WHERE cnpj = '26.756.416/0001-28'
      AND transaction_type = 'REDEMPTION'
    ORDER BY cnpj, transaction_date DESC
    LIMIT 1
),
monthly_income AS (
    SELECT DISTINCT ON (DATE_TRUNC('month', vq.competence_date))
        vq.competence_date,
        (f.quotas * vq.quota_value) AS income
    FROM vgbl_quota vq
    INNER JOIN fund f ON f.cnpj = vq.cnpj
    LEFT JOIN redemption r ON r.cnpj = f.cnpj
    WHERE f.cnpj = '26.756.416/0001-28'
      AND vq.competence_date >= '2025-06-01'
      AND CASE
            WHEN EXISTS (select 1 from redemption r where r.cnpj = f.cnpj and r.transaction_date <= vq.competence_date)
            THEN vq.competence_date < r.transaction_date
            ELSE vq.competence_date < '2026-07-01'
          END
    ORDER BY DATE_TRUNC('month', vq.competence_date), vq.competence_date DESC
)
SELECT
    competence_date,
    income,
    LAG(income) OVER (ORDER BY competence_date) AS previous_income
FROM monthly_income
ORDER BY competence_date;



WITH
redemption AS (
    SELECT DISTINCT ON (cnpj) cnpj, transaction_type, transaction_date
    FROM vgbl_track
    WHERE cnpj = '26.756.416/0001-28'
      AND transaction_type = 'REDEMPTION'
    ORDER BY cnpj, transaction_date DESC
    LIMIT 1
),
monthly_income AS (
    SELECT DISTINCT ON (DATE_TRUNC('month', vq.competence_date))
        vq.competence_date,
        (f.quotas * vq.quota_value) AS income
    FROM vgbl_quota vq
    INNER JOIN fund f ON f.cnpj = vq.cnpj
    LEFT JOIN redemption r ON r.cnpj = f.cnpj
    WHERE f.cnpj = '26.756.416/0001-28'
      AND vq.competence_date >= '2025-06-01'
      AND CASE
            WHEN r.transaction_date IS NOT NULL
            THEN vq.competence_date < r.transaction_date
            ELSE vq.competence_date < '2026-07-01'
          END
    ORDER BY DATE_TRUNC('month', vq.competence_date), vq.competence_date DESC
)
SELECT
    competence_date,
    income,
    LAG(income) OVER (ORDER BY competence_date) AS previous_income
FROM monthly_income
ORDER BY competence_date;



select * from fund;

select * from balance;

select p.*, b.*
from balance b
inner join product p on p.id = b.product_id
where b.balance_date between '2022-07-01' and '2022-07-31';
--
--
--delete from balance;
--delete from product;

  
select sum(interest), sum(br_tax) from "transaction";

CREATE TEMPORARY TABLE temp_monthly_interest_analysis AS
WITH monthly_interest AS (
    SELECT 
        DATE_TRUNC('month', balance_date) AS month_year,
        SUM(interest) AS total_interest
    FROM balance
    GROUP BY DATE_TRUNC('month', balance_date)
)
SELECT 
    TO_CHAR(month_year, 'YYYYMM') AS month,
    TO_CHAR(month_year, 'Mon YYYY') AS month_display,
    total_interest,
    LAG(total_interest) OVER (ORDER BY month_year) AS previous_month_interest,
    total_interest - LAG(total_interest) OVER (ORDER BY month_year) AS interest_difference,
    CASE 
        WHEN LAG(total_interest) OVER (ORDER BY month_year) IS NULL THEN 'First Month'
        WHEN total_interest > LAG(total_interest) OVER (ORDER BY month_year) THEN 'Increase'
        WHEN total_interest < LAG(total_interest) OVER (ORDER BY month_year) THEN 'Decrease'
        ELSE 'No Change'
    END AS trend
FROM monthly_interest
ORDER BY month_year;


select * from forex;
--update balance set br_au_forex = 271244;
--update transaction set br_au_forex = 271244;

----------------------------------------------------------------------------------
-- get total interest since begining



select sum(r.balance_sum) + sum(r.redemption_sum)
from (
    select sum(interest_difference) balance_sum, 0 redemption_sum
    from temp_monthly_interest_analysis 
--    where month = '202302'
    
    union all 
    
    select 0 balance_sum, sum(interest) redemption_sum
    from transaction
--    where payment_date between '2023-02-01' and '2023-02-28'
) as r;

-- get total tax paid in BR
select sum(br_tax) from transaction;



------------------------------------------------------------------------
WITH income_by_date AS (
    SELECT 
        vq.competence_date,
        (f.quotas * vq.quota_value) AS income,
        DATE_TRUNC('month', vq.competence_date)::date as month_start
    FROM vgbl_quota vq
    INNER JOIN fund f ON f.cnpj = vq.cnpj
    WHERE f.cnpj = '26.756.416/0001-28'   
    AND competence_date >= '2025-06-01'
    AND competence_date < '2025-10-01'
),
last_day_per_month AS (
    SELECT 
        month_start,
        MAX(competence_date) as last_saved_date
    FROM income_by_date
    GROUP BY month_start
),
monthly_income AS (
    SELECT 
        ibd.competence_date,
        ibd.income
    FROM income_by_date ibd
    INNER JOIN last_day_per_month ldpm 
        ON ibd.competence_date = ldpm.last_saved_date
)
SELECT 
    competence_date,
    income,
    LAG(income) OVER (ORDER BY competence_date) AS previous_income
FROM monthly_income
ORDER BY competence_date;





WITH monthly_income AS (
    SELECT DISTINCT ON (DATE_TRUNC('month', vq.competence_date))
        vq.competence_date,
        (f.quotas * vq.quota_value) AS income
    FROM vgbl_quota vq
    INNER JOIN fund f ON f.cnpj = vq.cnpj
    WHERE f.cnpj = '26.756.416/0001-28'   
      AND vq.competence_date >= '2025-06-01'
      AND vq.competence_date <  '2025-09-01'
    ORDER BY DATE_TRUNC('month', vq.competence_date), vq.competence_date desc
)
SELECT competence_date, income,
       LAG(income) OVER (ORDER BY competence_date) AS previous_income
FROM monthly_income
ORDER BY competence_date;


