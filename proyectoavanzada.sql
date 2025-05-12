-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 11-05-2025 a las 07:49:07
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `proyectoavanzada`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `friends`
--

CREATE TABLE `friends` (
  `user1` varchar(50) NOT NULL,
  `user2` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `friends`
--

INSERT INTO `friends` (`user1`, `user2`) VALUES
('Sofia', 'Zara'),
('Val', 'Zara');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `friend_requests`
--

CREATE TABLE `friend_requests` (
  `sender` varchar(50) NOT NULL,
  `receiver` varchar(50) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `friend_requests`
--

INSERT INTO `friend_requests` (`sender`, `receiver`, `status`) VALUES
('Sofia', 'Zara', 'ACCEPTED'),
('Val', 'Zara', 'ACCEPTED');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `grupos`
--

CREATE TABLE `grupos` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `creador` varchar(50) NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `grupos`
--

INSERT INTO `grupos` (`id`, `nombre`, `creador`, `fecha_creacion`, `activo`) VALUES
(10, 'Hola', 'Zara', '2025-05-07 05:43:39', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `invitaciones_grupo`
--

CREATE TABLE `invitaciones_grupo` (
  `grupo_id` int(11) NOT NULL,
  `usuario_invitado` varchar(50) NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDING',
  `fecha_invitacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `mensajes_grupo`
--

CREATE TABLE `mensajes_grupo` (
  `id` int(11) NOT NULL,
  `grupo_id` int(11) NOT NULL,
  `usuario_emisor` varchar(50) NOT NULL,
  `mensaje` text NOT NULL,
  `fecha_envio` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `mensajes_privados`
--

CREATE TABLE `mensajes_privados` (
  `id` int(11) NOT NULL,
  `usuario_emisor` varchar(255) NOT NULL,
  `usuario_receptor` varchar(255) NOT NULL,
  `mensaje` text NOT NULL,
  `fecha_envio` timestamp NOT NULL DEFAULT current_timestamp(),
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `mensajes_privados`
--

INSERT INTO `mensajes_privados` (`id`, `usuario_emisor`, `usuario_receptor`, `mensaje`, `fecha_envio`, `activo`) VALUES
(86, 'Zara', 'Sofia', 'hola', '2025-05-07 03:01:25', 0),
(87, 'Sofia', 'Zara', 'hola', '2025-05-07 05:27:51', 0),
(88, 'Val', 'Zara', 'hola', '2025-05-07 05:29:04', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `miembros_grupo`
--

CREATE TABLE `miembros_grupo` (
  `grupo_id` int(11) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `fecha_union` timestamp NOT NULL DEFAULT current_timestamp(),
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `miembros_grupo`
--

INSERT INTO `miembros_grupo` (`grupo_id`, `usuario`, `fecha_union`, `activo`) VALUES
(10, 'Zara', '2025-05-07 05:43:39', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `nicknames`
--

CREATE TABLE `nicknames` (
  `user` varchar(50) NOT NULL,
  `friend` varchar(50) NOT NULL,
  `nickname` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `contrasena` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre_usuario`, `contrasena`) VALUES
(18, 'Val', '21300691'),
(19, 'Zara', '21300725'),
(23, 'Sofia', '123');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `friends`
--
ALTER TABLE `friends`
  ADD PRIMARY KEY (`user1`,`user2`),
  ADD KEY `fk_user2` (`user2`);

--
-- Indices de la tabla `friend_requests`
--
ALTER TABLE `friend_requests`
  ADD PRIMARY KEY (`sender`,`receiver`),
  ADD KEY `fk_receiver` (`receiver`);

--
-- Indices de la tabla `grupos`
--
ALTER TABLE `grupos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_creador` (`creador`);

--
-- Indices de la tabla `invitaciones_grupo`
--
ALTER TABLE `invitaciones_grupo`
  ADD PRIMARY KEY (`grupo_id`,`usuario_invitado`),
  ADD KEY `fk_usuario_invitado` (`usuario_invitado`);

--
-- Indices de la tabla `mensajes_grupo`
--
ALTER TABLE `mensajes_grupo`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_grupo_id_mensaje` (`grupo_id`),
  ADD KEY `fk_usuario_emisor` (`usuario_emisor`);

--
-- Indices de la tabla `mensajes_privados`
--
ALTER TABLE `mensajes_privados`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `miembros_grupo`
--
ALTER TABLE `miembros_grupo`
  ADD PRIMARY KEY (`grupo_id`,`usuario`),
  ADD KEY `fk_usuario` (`usuario`);

--
-- Indices de la tabla `nicknames`
--
ALTER TABLE `nicknames`
  ADD PRIMARY KEY (`user`,`friend`),
  ADD KEY `fk_friend` (`friend`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre_usuario` (`nombre_usuario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `grupos`
--
ALTER TABLE `grupos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `mensajes_grupo`
--
ALTER TABLE `mensajes_grupo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `mensajes_privados`
--
ALTER TABLE `mensajes_privados`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=89;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `friends`
--
ALTER TABLE `friends`
  ADD CONSTRAINT `fk_user1` FOREIGN KEY (`user1`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `fk_user2` FOREIGN KEY (`user2`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `friend_requests`
--
ALTER TABLE `friend_requests`
  ADD CONSTRAINT `fk_receiver` FOREIGN KEY (`receiver`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `fk_sender` FOREIGN KEY (`sender`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `grupos`
--
ALTER TABLE `grupos`
  ADD CONSTRAINT `fk_creador` FOREIGN KEY (`creador`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `grupos_ibfk_1` FOREIGN KEY (`creador`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `invitaciones_grupo`
--
ALTER TABLE `invitaciones_grupo`
  ADD CONSTRAINT `fk_grupo_id_invitacion` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `fk_usuario_invitado` FOREIGN KEY (`usuario_invitado`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `invitaciones_grupo_ibfk_1` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `invitaciones_grupo_ibfk_2` FOREIGN KEY (`usuario_invitado`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `mensajes_grupo`
--
ALTER TABLE `mensajes_grupo`
  ADD CONSTRAINT `fk_grupo_id_mensaje` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `fk_usuario_emisor` FOREIGN KEY (`usuario_emisor`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `mensajes_grupo_ibfk_1` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `mensajes_grupo_ibfk_2` FOREIGN KEY (`usuario_emisor`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `miembros_grupo`
--
ALTER TABLE `miembros_grupo`
  ADD CONSTRAINT `fk_grupo_id` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `fk_usuario` FOREIGN KEY (`usuario`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `miembros_grupo_ibfk_1` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id`),
  ADD CONSTRAINT `miembros_grupo_ibfk_2` FOREIGN KEY (`usuario`) REFERENCES `usuarios` (`nombre_usuario`);

--
-- Filtros para la tabla `nicknames`
--
ALTER TABLE `nicknames`
  ADD CONSTRAINT `fk_friend` FOREIGN KEY (`friend`) REFERENCES `usuarios` (`nombre_usuario`),
  ADD CONSTRAINT `fk_user` FOREIGN KEY (`user`) REFERENCES `usuarios` (`nombre_usuario`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
