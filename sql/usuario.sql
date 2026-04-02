-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 18-06-2025 a las 19:10:13
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12
-- clicperu@gmail.com
-- $12345678P

use peloteros_bd;
select * from cancha;
select * from reserva;
select * from usuario;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `peloteros_bd`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `roles` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id`, `email`, `nombre`, `password`, `roles`, `telefono`) VALUES
(1, 'arduinoia@gmail.com', 'Sandro Perez', '$2a$10$boIv7Z8a.04im6TzrNpJPetkWlfVoCMef7bI9UoLD.sR9UElUL8T2', 'ROLE_USER', '926768933'),
(3, 'clicperu@gmail.com', 'richi utp', '$2a$10$MLPCtONZDySvlc1feXCuHODzzsWvtiknYO9RlV3FXvjrt8edx1LvC', 'ROLE_ADMIN', '987654437'),
(4, 'ardui.noi.a@gmail.com', 'pepe', '$2a$10$9uZOCrn5PiiJLIYX0fXvCedp0ZeB6CHGK.K.sNiokZ38cfxqVsuXm', 'ROLE_USER', '987654043'),
(5, 'test@gmail.com', 'Nuevo Test', '$2a$10$jP5vlS8mRSpjd6fVcb6fDeEih2je8KHWG2tg3v5ObTai.GZPfafiC', 'ROLE_USER', '554328987');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
