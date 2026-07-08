create database NETARQ;

use netarq;


CREATE TABLE computadores (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE,
    nome VARCHAR(100),
    ip VARCHAR(50),
    porta INTEGER,
    online BOOLEAN,
    ultimo_acesso TIMESTAMP
);
select * from computadores;