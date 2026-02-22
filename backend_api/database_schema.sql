-- Creamos la tabla usuarios
CREATE TABLE usuarios (
	id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Agregamos un usuario de prueba
INSERT INTO usuarios (nombre, rol) VALUES
('Felipe Hernandez', 'Administrador');

-- Comprobamos los datos ingresados
SELECT * FROM usuarios;

-- Creamos la tabla fundos
CREATE TABLE fundos (
	id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_fundo VARCHAR(50) NOT NULL UNIQUE,
    nombre_fundo VARCHAR(100) NOT NULL,
    comuna VARCHAR(100),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
    );
    
-- Se insertan fundos a modo de ejemplo
INSERT INTO fundos (codigo_fundo, nombre_fundo, comuna) VALUES
('1641', 'Los Negros', 'Pencahue'),
('4144','Huachi','Santa Barbara'),
('5145','Sabanilla','Angol');

-- Nos aseguramos de que los datos se ingresaron correctamente
SELECT * FROM fundos;

-- Creacion de una tabla hija "Canchas"
CREATE TABLE canchas(
	id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_fundo VARCHAR(50) NOT NULL,
    numero_cancha VARCHAR(50) NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (codigo_fundo) REFERENCES fundos(codigo_fundo) ON DELETE CASCADE
    );

-- Se agregan datos a la tabla "canchas"
INSERT INTO canchas (codigo_fundo, numero_cancha,fecha_creacion) VALUES
('1641','Cancha 3','2025-09-22'),
('4144','Cancha 1','2021-08-27'),
('4144','Cancha 3','2023-11-21'),
('5145','Cancha 2','2023-07-05');

-- Corroboramos los datos ingresados
SELECT * FROM canchas;