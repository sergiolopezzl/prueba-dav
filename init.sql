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
('1', 'Ryzen 5 7600x', 'Procesador de escritorio de 6 nucleos y 12 hilos', 750.000, 10),
('2', 'Razer Viper Ultimate', 'Mouse inalambrico ligero para juegos y base de carga RGB', 450.990, 50),
('3', 'Radeon RX 5700 XT', 'Tarjeta grafica, 8 GB GDDR6 256-Bit RDNA Architecture 1755/1905 MHz', 750.000, 31),
('4', 'Monitor', 'Monitor Samsung 24 pulgadas', 700.000, 20),
('5', 'NB North Bayou', 'Soporte de escritorio de doble monitor de 17 a 27 pulgadas', 180.000, 15),
('6', 'HyperX Cloud III', 'Auriculares con cable para juegos', 510.000, 23),
('7', 'Crucial P3 Plus 1TB', 'PCIe Gen4 3D NAND NVMe M.2 SSD, hasta 5000MB/s', 220.000, 4),
('8', 'Cooler Master MasterLiquid PL360 FLUX', 'Enfriador liquido de CPU AIO de bucle cerrado blanco, bomba de doble bucle Gen2 ARGB', 330.000, 12),
('9', 'HyperX Alloy Origins 65', 'Teclado mecanico para juegos, factor de forma ultra compacto del 65 %', 350.000, 9)
