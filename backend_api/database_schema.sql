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

-- Creacion de tabla para PRs
CREATE TABLE puntos_referencia (
	id INT AUTO_INCREMENT PRIMARY KEY,
	cancha_id INT NOT NULL,
	descriptor VARCHAR(50) NOT NULL,
	norte DECIMAL(15,4) NOT NULL,
	este DECIMAL(15,4) NOT NULL,
	cota DECIMAL(15,4) NOT NULL,
	fecha_creacion TIMESTAMP, -- Se creará un TRIGGER para utilizar fecha de la cancha asociada 
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cancha_id) REFERENCES canchas(id) ON DELETE CASCADE);

-- TRIGGER para heredar fecha de cancha
DELIMITER //

CREATE TRIGGER heredar_fecha_cancha
BEFORE INSERT ON puntos_referencia
FOR EACH ROW
BEGIN
	DECLARE fecha_inicial TIMESTAMP;
    -- Esto busca la fecha de creación de la cancha a la que pertenecen los PRs
    SELECT fecha_creacion INTO fecha_inicial
    FROM canchas
    WHERE id = NEW.cancha_id;
    -- Se le asigna la fecha de creacion al PR antes de guardar la info
    SET NEW.fecha_creacion = fecha_inicial;
END; //

DELIMITER ; 

-- Insertamos los datos de Huachi C1 reordenando las columnas a: Norte, Este, Cota
INSERT INTO puntos_referencia (cancha_id, norte, este, cota, descriptor) VALUES
(2, 5828008.689, 254098.215, 446.040, 'PR1'),
(2, 5827981.346, 254059.546, 445.290, 'PR2'),
(2, 5827992.962, 254046.902, 444.851, 'PR3'),
(2, 5828017.054, 254060.695, 445.002, 'PR4'),
(2, 5828036.073, 254084.676, 445.027, 'PR5');

-- Revisamos la integridad de nuestros datos
Select * from puntos_referencia;

-- Agregamos columnas a tabla usuarios (nombre de usuario y contraseña)
ALTER TABLE usuarios
ADD COLUMN usuario VARCHAR(50) NOT NULL UNIQUE AFTER nombre,
ADD COLUMN contraseña VARCHAR(50) NOT NULL AFTER usuario;

-- Actualizar información del usuario registrado anteriormente (Felipe Hernandez)
UPDATE usuarios
SET usuario = 'fhernandez',
contraseña = 12345
WHERE id = 1;

-- Revisamos los nuevos datos de la tabla usuarios
SELECT * FROM usuarios;

-- Modificamos la tabla puntos_referencia agregando las columnas para latitud y longitud
ALTER TABLE puntos_referencia
ADD COLUMN latitud DECIMAL(10,8) NULL AFTER cota,
ADD COLUMN longitud DECIMAL(10,8) NULL AFTER latitud;

-- Revisamos la actualizacion de la tabla
SELECT * FROM puntos_referencia;

-- ALERTA: debido al fortmato libre de las coordenadas anteriormente ingresadas, no es posibles ubicarlas en el mapa satelital
-- se agrega un nuevo fundo->cancha->prs con coordenadas en formato UTM, las cuales pueden ser convertidas para visualizarse en google maps con precisión 
INSERT INTO fundos (codigo_fundo, nombre_fundo, comuna) VALUES
(8296,'Sta. Eugenia', 'Cautin - Osorno');

SELECT * FROM fundos;

INSERT INTO canchas (codigo_fundo, numero_cancha) VALUES
(8296, 'Pozo');

SELECT * FROM canchas;

INSERT INTO puntos_referencia (cancha_id, descriptor, norte, este, cota, latitud, longitud) VALUES
(6, 'PR1', 5675639.158, 734767.183, 226.661, -39.03679256, -72.28761066),
(6,'PR6', 5675589.282, 734729.249, 228.839, -39.03725168, -72.28803109),
(6,'PR7', 5675690.379, 734832.193, 219.536, -39.03631416, -72.28687774),
(6,'PR8', 5675641.565, 734907.186, 220.766, -39.03673344, -72.28599563),
(6,'PR9', 5675525.083, 734912.545, 229.189, -39.03778034, -72.28589371);

SELECT * FROM puntos_referencia
WHERE cancha_id = 6;



