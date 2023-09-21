ALTER TABLE IF EXISTS budget
    ADD COLUMN author_id INT;

CREATE TABLE IF NOT EXISTS author
(
    id        SERIAL PRIMARY KEY ,
    name      VARCHAR(64) NOT NULL,
    create_on TIMESTAMP   NOT NULL
);

ALTER TABLE IF EXISTS budget
    ADD CONSTRAINT fk_budget_author FOREIGN KEY (author_id)
        REFERENCES author (id) ON DELETE SET NULL ;