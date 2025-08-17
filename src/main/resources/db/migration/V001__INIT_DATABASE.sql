create table product
(
	id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	name VARCHAR(255),
	certificate VARCHAR(255),
	issued_at DATE,
	mature_at DATE
);

create table balance
(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id INTEGER NOT NULL,
    percent INTEGER NOT NULL,
    principal INTEGER NOT NULL,
    balance INTEGER NOT NULL,
    interest INTEGER NOT NULL,
    iof INTEGER NOT NULL,
    br_tax INTEGER NOT NULL,
    balance_net INTEGER NOT NULL,
    balance_date DATE,
    br_au_forex INTEGER NOT NULL,
    constraint fk_balance_product
        foreign key (product_id) references product (id)
);

create table investment
(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id INTEGER NOT NULL,
    principal INTEGER NOT NULL,
    percent INTEGER NOT NULL,
    constraint fk_investment_product
        foreign key (product_id) references product (id)
);

create table transaction
(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id INTEGER NOT NULL,
    payment_date DATE,
    percent INTEGER NOT NULL,
    principal INTEGER NOT NULL,
    redemption INTEGER NOT NULL,
    interest INTEGER NOT NULL,
    iof INTEGER NOT NULL,
    br_tax INTEGER NOT NULL,
    br_au_forex INTEGER NOT NULL,
    credited INTEGER NOT NULL,
    description VARCHAR(255),

    constraint fk_transaction_product
        foreign key (product_id) references product (id)
);
