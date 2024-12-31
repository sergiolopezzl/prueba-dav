-- Crear tablas

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL
);

-- Insertar datos de ejemplo
INSERT INTO users (username, password) VALUES ('sergio', '123');
INSERT INTO users (username, password) VALUES ('daniel', '111');
INSERT INTO users (username, password) VALUES ('admin', '321');

INSERT INTO products (id, name, description, price, quantity) VALUES
('1', 'CPU', 'Ryzen 5 7600x', 750.000, 10),
('2', 'Mouse', 'Mouse inalambrico Logitech', 450.990, 50),
('3', 'Teclado', 'Teclado mecanico HyperX', 890.990, 30),
('4', 'Monitor', 'Monitor Samsung 24 pulgadas', 700.000, 20),
('5', 'Silla Gamer', 'Silla ergonomica para videojuegos', 650.000, 15);
