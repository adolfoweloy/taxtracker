select * from product;

select p.*, b.*
from balance b
inner join product p on p.id = b.product_id
where b.balance_date between '2022-07-01' and '2022-07-31';


CREATE TEMPORARY TABLE temp_monthly_interest_analysis AS
WITH monthly_interest AS (
    SELECT 
        DATE_TRUNC('month', balance_date) AS month_year,
        SUM(interest) AS total_interest
    FROM balance
    GROUP BY DATE_TRUNC('month', balance_date)
)
SELECT 
    TO_CHAR(month_year, 'YYYY-MM') AS month,
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

select sum(interest_difference) from temp_monthly_interest_analysis;