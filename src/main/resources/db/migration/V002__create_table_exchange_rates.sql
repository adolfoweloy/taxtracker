CREATE TABLE exchange_rate
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	source VARCHAR(3),
	target VARCHAR(3),
	rate_at DATE,
	rate INTEGER NOT NULL,
    constraint unique_rate UNIQUE (source, target, rate_at)
);
