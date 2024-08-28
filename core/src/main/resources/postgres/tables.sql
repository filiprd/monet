CREATE TABLE categories (
    id    uuid    PRIMARY KEY,
    label varchar NOT NULL
);

CREATE TABLE techniques (
    id    uuid    PRIMARY KEY,
    label varchar NOT NULL
);

CREATE TABLE users (
    id       uuid    PRIMARY KEY,
    email    varchar UNIQUE NOT NULL,
    password varchar NOT NULL,
    name     varchar NOT NULL
);

CREATE TABLE orders (
    id         uuid    PRIMARY KEY,
    payment_id uuid    UNIQUE NOT NULL,
    user_id    uuid    NOT NULL,
    paintings  jsonb   NOT NULL,
    total      integer NOT NULL,
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT positive_total CHECK (total > 0)
);

CREATE TABLE paintings (
    id           uuid    PRIMARY KEY,
    name         varchar NOT NULL,
    description  varchar NOT NULL,
    category_id  uuid    NOT NULL,
    technique_id uuid    NOT NULL,
    images       jsonb   NOT NULL,
    price        integer NOT NULL,
    user_id      uuid    NOT NULL,
    CONSTRAINT uid_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT cat_id_fkey FOREIGN KEY (category_id)
        REFERENCES categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT tech_id_fkey FOREIGN KEY (technique_id)
        REFERENCES techniques (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT positive_price CHECK (price > 0)
);