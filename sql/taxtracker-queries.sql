select * from product;

select p.*, b.*
from balance b
inner join product p on p.id = b.product_id
where b.balance_date between '2022-07-01' and '2022-07-31';


delete from balance;
delete from product;

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



