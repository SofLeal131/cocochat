package servidorproyecto;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.stream.Collectors;

public class ManejadorClienteProyecto {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private ServidorProyecto servidor;
    private String usuarioConectado;

    public ManejadorClienteProyecto(Socket socket, ServidorProyecto servidor) throws IOException {
        this.socket = socket;
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.salida = new PrintWriter(socket.getOutputStream(), true);
        this.servidor = servidor;
        this.usuarioConectado = null;
        System.out.println("Nuevo manejador de cliente creado para: " + socket.getInetAddress());
    }

    public void manejarMensajes() throws IOException {
        System.out.println("Listo para recibir mensajes desde: " + socket.getInetAddress());
        while (true) {
            try {
                String modo = entrada.readLine();
                if (modo == null) {
                    System.out.println("Cliente desconectado desde: " + socket.getInetAddress());
                    if (usuarioConectado != null) {
                        System.out.println("Removiendo usuario en línea: " + usuarioConectado);
                        servidor.removerUsuarioEnLinea(usuarioConectado);
                    }
                    servidor.removerCliente(this);
                    break;
                }

                System.out.println("Modo recibido desde " + socket.getInetAddress() + ": " + modo);

                String nombreUsuario = entrada.readLine();
                if (nombreUsuario == null) {
                    System.out.println("Cliente desconectado desde: " + socket.getInetAddress() + " mientras se leía el nombre de usuario.");
                    if (usuarioConectado != null) {
                        System.out.println("Removiendo usuario en línea: " + usuarioConectado);
                        servidor.removerUsuarioEnLinea(usuarioConectado);
                    }
                    servidor.removerCliente(this);
                    break;
                }

                String dato = entrada.readLine();
                if (dato == null) {
                    System.out.println("Cliente desconectado desde: " + socket.getInetAddress() + " mientras se leía el dato.");
                    if (usuarioConectado != null) {
                        System.out.println("Removiendo usuario en línea: " + usuarioConectado);
                        servidor.removerUsuarioEnLinea(usuarioConectado);
                    }
                    servidor.removerCliente(this);
                    break;
                }

                System.out.println("Procesando solicitud: modo=" + modo + ", usuario=" + nombreUsuario + ", dato=" + dato);

                if (modo.equals("CERRAR_SESION")) {
                    if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                        System.out.println("Cierre de sesión para: " + nombreUsuario);
                        servidor.removerUsuarioEnLinea(nombreUsuario);
                        salida.println("Sesión cerrada.");
                    } else {
                        System.out.println("Error: Nombre de usuario no proporcionado para cerrar sesión.");
                        salida.println("Error: Nombre de usuario no proporcionado.");
                    }
                    continue;
                }

                if (modo.equals("LISTAR_USUARIOS_EN_LINEA")) {
                    System.out.println("Listando usuarios en línea para: " + nombreUsuario);
                    String usuarios = servidor.getUsuariosEnLinea().stream()
                            .filter(user -> !user.equals(nombreUsuario))
                            .collect(Collectors.joining(","));
                    System.out.println("Enviando lista de usuarios en línea: " + usuarios);
                    salida.println(usuarios.isEmpty() ? "No hay usuarios en línea." : usuarios);
                    continue;
                }

                if (modo.equals("LISTAR_TODOS_USUARIOS")) {
                    System.out.println("Listando todos los usuarios para: " + nombreUsuario);
                    String usuarios = listarTodosUsuarios(nombreUsuario);
                    System.out.println("Enviando lista de todos los usuarios: " + usuarios);
                    salida.println(usuarios.isEmpty() ? "No hay usuarios registrados." : usuarios);
                    continue;
                }

                if (modo.equals("LISTAR_AMIGOS")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Listando amigos para: " + nombreUsuario);
                        String amigos = listarAmigos(nombreUsuario);
                        System.out.println("Enviando lista de amigos: " + amigos);
                        salida.println(amigos.isEmpty() ? "No hay amigos." : amigos);
                    } else {
                        System.out.println("Error: Intento de listar amigos sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para listar amigos.");
                    }
                    continue;
                }

                if (modo.equals("LISTAR_SOLICITUDES_PENDIENTES")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Listando solicitudes pendientes para: " + nombreUsuario);
                        String solicitudes = listarSolicitudesPendientes(nombreUsuario);
                        System.out.println("Enviando lista de solicitudes pendientes: " + solicitudes);
                        salida.println(solicitudes.isEmpty() ? "No hay solicitudes pendientes." : solicitudes);
                    } else {
                        System.out.println("Error: Intento de listar solicitudes sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para listar solicitudes.");
                    }
                    continue;
                }

                if (modo.equals("ENTRAR_CHAT_PRIVADO")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String emisor = nombreUsuario;
                        String receptor = dato;
                        if (emisor != null && receptor != null) {
                            System.out.println("Entrando al chat privado: " + emisor + " -> " + receptor);
                            servidor.agregarUsuarioActivoEnChat(emisor, receptor, emisor);
                            salida.println("Entró al chat privado.");
                        } else {
                            System.out.println("Error: Datos incompletos para entrar al chat privado.");
                            salida.println("Error: Datos incompletos para entrar al chat.");
                        }
                    } else {
                        System.out.println("Error: Intento de entrar al chat privado sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para entrar al chat.");
                    }
                    continue;
                }

                if (modo.equals("SALIR_CHAT_PRIVADO")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String emisor = nombreUsuario;
                        String receptor = dato;
                        if (emisor != null && receptor != null) {
                            System.out.println("Saliendo del chat privado: " + emisor + " -> " + receptor);
                            servidor.removerUsuarioActivoEnChat(emisor, receptor, emisor);
                            salida.println("Salió del chat privado.");
                        } else {
                            System.out.println("Error: Datos incompletos para salir del chat privado.");
                            salida.println("Error: Datos incompletos para salir del chat.");
                        }
                    } else {
                        System.out.println("Error: Intento de salir del chat privado sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para salir del chat.");
                    }
                    continue;
                }

                if (modo.equals("CARGAR_CHAT_PRIVADO")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String emisor = nombreUsuario;
                        String receptor = dato;
                        if (emisor != null && receptor != null) {
                            System.out.println("Cargando historial de chat privado: " + emisor + " <-> " + receptor);
                            String historial = cargarHistorialChat(emisor, receptor);
                            System.out.println("Enviando historial de chat: " + historial);
                            salida.println(historial);
                        } else {
                            System.out.println("Error: Datos incompletos para cargar historial de chat.");
                            salida.println("Error: Datos incompletos para cargar el chat.");
                        }
                    } else {
                        System.out.println("Error: Intento de cargar chat privado sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para cargar el chat.");
                    }
                    continue;
                }

                if (modo.equals("CHAT")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String mensaje = "[" + nombreUsuario + "]: " + dato;
                        System.out.println("Difundiendo mensaje de chat general: " + mensaje);
                        servidor.difundirMensaje(mensaje, this);
                        salida.println("Mensaje enviado.");
                    } else {
                        System.out.println("Error: Intento de enviar mensaje de chat general sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para enviar mensajes de chat.");
                    }
                    continue;
                }

                if (modo.equals("CHAT_PRIVADO")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Procesando mensaje privado de: " + nombreUsuario);
                        guardarMensajePrivado(nombreUsuario, dato);
                        enviarMensajePrivado(nombreUsuario, dato);
                        salida.println("Mensaje privado enviado.");
                    } else {
                        System.out.println("Error: Intento de enviar mensaje privado sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para enviar mensajes privados.");
                    }
                    continue;
                }

                if (modo.equals("SEND_FRIEND_REQUEST")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String receiver = dato;
                        System.out.println("Recibida solicitud de amistad: emisor=" + nombreUsuario + ", receptor=" + receiver);
                        if (receiver.equals(nombreUsuario)) {
                            System.out.println("Error: Intento de solicitud de amistad a sí mismo: " + nombreUsuario);
                            salida.println("Error: No puedes enviarte una solicitud de amistad a ti mismo.");
                            continue;
                        }
                        if (areFriends(nombreUsuario, receiver)) {
                            System.out.println("Error: Ya son amigos: " + nombreUsuario + ", " + receiver);
                            salida.println("Error: Ya son amigos.");
                            continue;
                        }
                        if (hasPendingRequest(nombreUsuario, receiver)) {
                            System.out.println("Error: Solicitud pendiente existente: " + nombreUsuario + " -> " + receiver);
                            salida.println("Error: Ya hay una solicitud pendiente.");
                            continue;
                        }
                        if (sendFriendRequest(nombreUsuario, receiver)) {
                            System.out.println("Solicitud de amistad procesada exitosamente. Notificando a: " + receiver);
                            notifyFriendRequest(receiver, nombreUsuario);
                        } else {
                            System.out.println("Error: Fallo al procesar solicitud de amistad.");
                        }
                    } else {
                        System.out.println("Error: Intento de enviar solicitud de amistad sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para enviar solicitudes de amistad.");
                    }
                    continue;
                }

                if (modo.equals("ACCEPT_FRIEND_REQUEST")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String sender = dato;
                        System.out.println("Procesando aceptación de solicitud de amistad: emisor=" + sender + ", receptor=" + nombreUsuario);
                        if (acceptFriendRequest(sender, nombreUsuario)) {
                            System.out.println("Solicitud de amistad aceptada exitosamente.");
                            salida.println("Solicitud de amistad aceptada.");
                            notifyFriendRequestAccepted(sender, nombreUsuario);
                        } else {
                            System.out.println("Error: Fallo al aceptar solicitud de amistad.");
                            salida.println("Error: No se pudo aceptar la solicitud.");
                        }
                    } else {
                        System.out.println("Error: Intento de aceptar solicitud de amistad sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para aceptar solicitudes de amistad.");
                    }
                    continue;
                }

                if (modo.equals("REJECT_FRIEND_REQUEST")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String sender = dato;
                        System.out.println("Procesando rechazo de solicitud de amistad: emisor=" + sender + ", receptor=" + nombreUsuario);
                        if (rejectFriendRequest(sender, nombreUsuario)) {
                            System.out.println("Solicitud de amistad rechazada exitosamente.");
                            salida.println("Solicitud de amistad rechazada.");
                            notifyFriendRequestRejected(sender, nombreUsuario);
                        } else {
                            System.out.println("Error: Fallo al rechazar solicitud de amistad.");
                            salida.println("Error: No se pudo rechazar la solicitud.");
                        }
                    } else {
                        System.out.println("Error: Intento de rechazar solicitud de amistad sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para rechazar solicitudes de amistad.");
                    }
                    continue;
                }

                if (modo.equals("SET_NICKNAME")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String[] parts = dato.split(":", 2);
                        if (parts.length < 2) {
                            System.out.println("Error: Formato de apodo inválido: " + dato);
                            salida.println("Error: Formato de apodo inválido.");
                            continue;
                        }
                        String friend = parts[0];
                        String nickname = parts[1];
                        System.out.println("Procesando establecimiento de apodo: usuario=" + nombreUsuario + ", amigo=" + friend + ", apodo=" + nickname);
                        if (setNickname(nombreUsuario, friend, nickname)) {
                            System.out.println("Apodo establecido exitosamente.");
                            salida.println("Apodo establecido exitosamente.");
                        } else {
                            System.out.println("Error: Fallo al establecer apodo para: " + friend);
                            salida.println("Error: No se pudo establecer el apodo.");
                        }
                    } else {
                        System.out.println("Error: Intento de establecer apodo sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para establecer apodos.");
                    }
                    continue;
                }

                if (modo.equals("LISTAR_APODOS")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Listando apodos para: " + nombreUsuario);
                        String apodos = listarApodos(nombreUsuario);
                        System.out.println("Enviando lista de apodos: " + apodos);
                        salida.println(apodos.isEmpty() ? "No hay apodos." : apodos);
                    } else {
                        System.out.println("Error: Intento de listar apodos sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para listar apodos.");
                    }
                    continue;
                }

                if (modo.equals("CREATE_GROUP")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String groupName = dato.trim();
                        if (groupName.isEmpty()) {
                            salida.println("Error: El nombre del grupo no puede estar vacío");
                            continue;
                        }
                        System.out.println("Creando grupo: " + groupName + " por usuario: " + nombreUsuario);
                        
                        if (createGroup(nombreUsuario, groupName)) {
                            System.out.println("Grupo creado exitosamente");
                        } else {
                            System.out.println("Error al crear el grupo");
                        }
                    } else {
                        salida.println("Error: Debes iniciar sesión para crear grupos");
                    }
                    continue;
                }
        
                if (modo.equals("INVITE_TO_GROUP")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String[] parts = dato.split(":", 2);
                        if (parts.length == 2) {
                            String groupId = parts[0];
                            String invitedUser = parts[1];
                            if (inviteToGroup(groupId, invitedUser)) {
                                salida.println("Invitación enviada exitosamente");
                            } else {
                                salida.println("Error al enviar la invitación");
                            }
                        } else {
                            salida.println("Error: Formato inválido para invitación");
                        }
                    } else {
                        salida.println("Error: Debes iniciar sesión para invitar usuarios");
                    }
                    continue;
                }
        
                if (modo.equals("LEAVE_GROUP")) {
                    String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
                    String user = "root";
                    String password = "";
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String groupId = dato;
                        try (Connection conn = DriverManager.getConnection(url, user, password)) {
                            String sql = "UPDATE miembros_grupo SET activo = FALSE WHERE grupo_id = ? AND usuario = ?";
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            stmt.setInt(1, Integer.parseInt(groupId));
                            stmt.setString(2, nombreUsuario);
                            int affectedRows = stmt.executeUpdate();
                            
                            if (affectedRows > 0) {
                                checkGroupMinMembers(groupId);
                                salida.println("Has salido del grupo exitosamente");
                            } else {
                                salida.println("Error al salir del grupo");
                            }
                        } catch (SQLException e) {
                            System.out.println("Error SQL al salir del grupo: " + e.getMessage());
                            salida.println("Error al salir del grupo: " + e.getMessage());
                        }
                    } else {
                        salida.println("Error: Debes iniciar sesión para salir del grupo");
                    }
                    continue;
                }

                if (modo.equals("LISTAR_GRUPOS")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Listando grupos para: " + nombreUsuario);
                        String grupos = listGroups(nombreUsuario);
                        System.out.println("Enviando lista de grupos: " + grupos);
                        salida.println(grupos.isEmpty() ? "No hay grupos disponibles." : grupos);
                    } else {
                        System.out.println("Error: Intento de listar grupos sin autenticación para: " + nombreUsuario);
                        salida.println("Error: Debes iniciar sesión para listar grupos.");
                    }
                    continue;
                }

                if (modo.equals("LIST_GROUP_MEMBERS")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String groupId = dato;
                        System.out.println("Listando miembros del grupo: " + groupId);
                        String miembros = listarMiembrosGrupo(groupId);
                        salida.println(miembros);
                    } else {
                        salida.println("Error: Debes iniciar sesión para ver miembros del grupo");
                    }
                    continue;
                }

                if (modo.equals("ACCEPT_GROUP_INVITE")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String groupId = dato;
                        if (acceptGroupInvitation(groupId, nombreUsuario)) {
                            salida.println("Te has unido al grupo exitosamente");
                        } else {
                            salida.println("Error al aceptar la invitación");
                        }
                    } else {
                        salida.println("Error: Debes iniciar sesión para aceptar invitaciones");
                    }
                    continue;
                }

                if (modo.equals("LIST_GROUP_INVITATIONS")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        System.out.println("Listando invitaciones de grupo para: " + nombreUsuario);
                        String invitaciones = listarInvitacionesGrupo(nombreUsuario);
                        salida.println(invitaciones);
                    } else {
                        salida.println("Error: Debes iniciar sesión para ver invitaciones");
                    }
                    continue;
                }

                if (modo.equals("REJECT_GROUP_INVITE")) {
                    if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                        String groupId = dato;
                        if (rechazarInvitacionGrupo(groupId, nombreUsuario)) {
                            salida.println("Invitación rechazada exitosamente");
                        } else {
                            salida.println("Error al rechazar la invitación");
                        }
                    } else {
                        salida.println("Error: Debes iniciar sesión para rechazar invitaciones");
                    }
                    continue;
                }

                String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
                String user = "root";
                String password = "";

                try (Connection conn = DriverManager.getConnection(url, user, password)) {
                    System.out.println("Conexión a la base de datos establecida para modo: " + modo);
                    if (modo.equals("REGISTRO")) {
                        System.out.println("Procesando registro de usuario: " + nombreUsuario);
                        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena) VALUES (?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, nombreUsuario);
                        stmt.setString(2, dato);

                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas > 0) {
                            System.out.println("Usuario registrado exitosamente: " + nombreUsuario);
                            salida.println("Registro exitoso.");
                            servidor.actualizarTablaUsuarios();
                        } else {
                            System.out.println("Error: No se pudo registrar el usuario: " + nombreUsuario);
                            salida.println("Error al registrar.");
                        }
                    } else if (modo.equals("INICIO_SESION")) {
                        System.out.println("Procesando inicio de sesión para: " + nombreUsuario);
                        if (servidor.estaUsuarioEnLinea(nombreUsuario)) {
                            System.out.println("Error: Usuario ya está conectado: " + nombreUsuario);
                            salida.println("Error: El usuario ya está conectado desde otro cliente.");
                            continue;
                        }

                        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contrasena = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, nombreUsuario);
                        stmt.setString(2, dato);

                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            System.out.println("Inicio de sesión exitoso para: " + nombreUsuario);
                            salida.println("Inicio de sesión exitoso.");
                            usuarioConectado = nombreUsuario;
                            servidor.agregarUsuarioEnLinea(nombreUsuario);
                            String usuarios = servidor.getUsuariosEnLinea().stream()
                                    .filter(username -> !username.equals(nombreUsuario))
                                    .collect(Collectors.joining(","));
                            System.out.println("Enviando lista inicial de usuarios en línea: " + usuarios);
                            salida.println("INITIAL_USERS:" + (usuarios.isEmpty() ? "No hay usuarios en línea." : usuarios));
                        } else {
                            System.out.println("Error: Credenciales incorrectas para: " + nombreUsuario);
                            salida.println("Usuario o contraseña incorrectos.");
                        }
                    } else if (modo.equals("CAMBIAR_CONTRASENA")) {
                        System.out.println("Procesando cambio de contraseña para: " + nombreUsuario);
                        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, nombreUsuario);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            sql = "UPDATE usuarios SET contrasena = ? WHERE nombre_usuario = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, dato);
                            stmt.setString(2, nombreUsuario);

                            int filasAfectadas = stmt.executeUpdate();
                            if (filasAfectadas > 0) {
                                System.out.println("Contraseña actualizada exitosamente para: " + nombreUsuario);
                                salida.println("Contraseña actualizada exitosamente.");
                                servidor.actualizarTablaUsuarios();
                            } else {
                                System.out.println("Error: No se pudo actualizar la contraseña para: " + nombreUsuario);
                                salida.println("Error al actualizar la contraseña.");
                            }
                        } else {
                            System.out.println("Error: Usuario no encontrado: " + nombreUsuario);
                            salida.println("Error: Usuario no encontrado.");
                        }
                    } else if (modo.equals("ELIMINAR_USUARIO")) {
                        System.out.println("Procesando eliminación de usuario: " + nombreUsuario);
                        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, nombreUsuario);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            
                            
                            sql = "DELETE FROM nicknames WHERE user = ? OR friend = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, nombreUsuario);
                            stmt.setString(2, nombreUsuario);
                            stmt.executeUpdate();
                            System.out.println("Registros eliminados de nicknames para: " + nombreUsuario);

                            
                            sql = "DELETE FROM friend_requests WHERE sender = ? OR receiver = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, nombreUsuario);
                            stmt.setString(2, nombreUsuario);
                            stmt.executeUpdate();
                            System.out.println("Registros eliminados de friend_requests para: " + nombreUsuario);

                            
                            sql = "DELETE FROM friends WHERE user1 = ? OR user2 = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, nombreUsuario);
                            stmt.setString(2, nombreUsuario);
                            stmt.executeUpdate();
                            System.out.println("Registros eliminados de friends para: " + nombreUsuario);

                            
                            sql = "DELETE FROM mensajes_privados WHERE usuario_emisor = ? OR usuario_receptor = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, nombreUsuario);
                            stmt.setString(2, nombreUsuario);
                            stmt.executeUpdate();
                            System.out.println("Registros eliminados de mensajes_privados para: " + nombreUsuario);

                            
                            sql = "DELETE FROM usuarios WHERE nombre_usuario = ?";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1, nombreUsuario);
                            int filasAfectadas = stmt.executeUpdate();

                            if (filasAfectadas > 0) {
                                System.out.println("Usuario eliminado exitosamente: " + nombreUsuario);
                                salida.println("Usuario eliminado.");
                                servidor.actualizarTablaUsuarios();
                                if (usuarioConectado != null && usuarioConectado.equals(nombreUsuario)) {
                                    usuarioConectado = null;
                                    servidor.removerUsuarioEnLinea(nombreUsuario);
                                }
                            } else {
                                System.out.println("Error: No se pudo eliminar el usuario: " + nombreUsuario);
                                salida.println("Error al eliminar el usuario.");
                            }
                        } else {
                            System.out.println("Error: Usuario no encontrado: " + nombreUsuario);
                            salida.println("Error: Usuario no encontrado.");
                        }
                    } else {
                        System.out.println("Error: Modo no válido recibido: " + modo);
                        salida.println("Error: Modo no válido.");
                    }
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1062) {
                        System.out.println("Error: Nombre de usuario ya existe: " + nombreUsuario);
                        salida.println("Error: El nombre de usuario ya existe.");
                    } else {
                        System.out.println("Error SQL en modo " + modo + ": " + e.getMessage() + " (Código: " + e.getErrorCode() + ")");
                        salida.println("Error en la base de datos: " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                System.out.println("Excepción al recibir mensaje desde " + socket.getInetAddress() + ": " + e.getMessage());
                if (usuarioConectado != null) {
                    System.out.println("Removiendo usuario en línea: " + usuarioConectado);
                    servidor.removerUsuarioEnLinea(usuarioConectado);
                }
                servidor.removerCliente(this);
                break;
            }
        }
        cerrar();
    }

    private String listarTodosUsuarios(String currentUser) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        StringBuilder usuarios = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Ejecutando consulta para listar todos los usuarios (excluyendo " + currentUser + ")");
            String sql = "SELECT nombre_usuario FROM usuarios WHERE nombre_usuario != ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                if (usuarios.length() > 0) {
                    usuarios.append(",");
                }
                String username = rs.getString("nombre_usuario");
                usuarios.append(username);
                count++;
            }
            System.out.println("Usuarios encontrados para listarTodosUsuarios (excluyendo " + currentUser + "): " + usuarios + " (" + count + " registros)");
        } catch (SQLException e) {
            System.out.println("Error SQL al listar todos los usuarios para " + currentUser + ": " + e.getMessage());
            return "Error al listar usuarios: " + e.getMessage();
        }
        return usuarios.length() > 0 ? usuarios.toString() : "No hay usuarios registrados.";
    }

    private String listarAmigos(String username) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        StringBuilder amigos = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT user1, user2 FROM friends WHERE user1 = ? OR user2 = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String friend = rs.getString("user1").equals(username) ? rs.getString("user2") : rs.getString("user1");
                if (amigos.length() > 0) {
                    amigos.append(",");
                }
                amigos.append(friend);
            }
            System.out.println("Amigos encontrados para " + username + ": " + amigos);
        } catch (SQLException e) {
            System.out.println("Error SQL al listar amigos: " + e.getMessage());
            return "Error al listar amigos.";
        }
        return amigos.length() > 0 ? amigos.toString() : "No hay amigos.";
    }

    private String listarSolicitudesPendientes(String username) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        StringBuilder solicitudes = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Ejecutando consulta para listar solicitudes pendientes de: " + username);
            String sql = "SELECT sender FROM friend_requests WHERE receiver = ? AND status = 'PENDING'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            System.out.println("Consulta SQL: " + sql + " con receiver = " + username);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String sender = rs.getString("sender");
                if (solicitudes.length() > 0) {
                    solicitudes.append(",");
                }
                solicitudes.append(sender);
                count++;
            }
            System.out.println("Solicitudes pendientes encontradas para " + username + ": " + solicitudes + " (" + count + " registros)");
            return solicitudes.length() > 0 ? solicitudes.toString() : "No hay solicitudes pendientes.";
        } catch (SQLException e) {
            System.out.println("Error SQL al listar solicitudes pendientes para " + username + ": " + e.getMessage());
            return "Error al listar solicitudes.";
        }
    }

    private String cargarHistorialChat(String emisor, String receptor) {
        StringBuilder historial = new StringBuilder();
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT usuario_emisor, mensaje, fecha_envio FROM mensajes_privados WHERE activo = TRUE AND ((usuario_emisor = ? AND usuario_receptor = ?) OR (usuario_emisor = ? AND usuario_receptor = ?)) ORDER BY fecha_envio";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emisor);
            stmt.setString(2, receptor);
            stmt.setString(3, receptor);
            stmt.setString(4, emisor);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String emisorMensaje = rs.getString("usuario_emisor");
                String mensaje = rs.getString("mensaje");
                String fecha = rs.getTimestamp("fecha_envio").toString();
                historial.append(emisorMensaje).append(":").append(mensaje).append(":").append(fecha).append(";");
            }
            System.out.println("Historial de chat cargado para " + emisor + " <-> " + receptor + ": " + historial);
        } catch (SQLException e) {
            System.out.println("Error SQL al cargar historial de chat: " + e.getMessage());
            return "Error al cargar historial.";
        }
        return historial.length() > 0 ? historial.toString() : "Sin mensajes.";
    }

    private void guardarMensajePrivado(String emisor, String receptorMensaje) {
        String[] partes = receptorMensaje.split(":", 2);
        if (partes.length < 2) {
            System.out.println("Error: Formato de mensaje privado inválido: " + receptorMensaje);
            return;
        }
        String receptor = partes[0];
        String mensaje = partes[1];

        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO mensajes_privados (usuario_emisor, usuario_receptor, mensaje, fecha_envio, activo) VALUES (?, ?, ?, NOW(), TRUE)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emisor);
            stmt.setString(2, receptor);
            stmt.setString(3, mensaje);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Mensaje privado guardado: " + emisor + " -> " + receptor + ": " + mensaje + " (Filas afectadas: " + rowsAffected + ")");
        } catch (SQLException e) {
            System.out.println("Error SQL al guardar mensaje privado: " + e.getMessage());
        }
    }

    private void enviarMensajePrivado(String emisor, String receptorMensaje) {
        String[] partes = receptorMensaje.split(":", 2);
        if (partes.length < 2) {
            System.out.println("Error: Formato de mensaje privado inválido: " + receptorMensaje);
            return;
        }
        String receptor = partes[0];
        String mensaje = partes[1];
        String mensajeFormateado = "CHAT_PRIVADO:" + emisor + ":" + mensaje;

        boolean enviado = false;
        for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
            if (cliente.getUsuarioConectado() != null && cliente.getUsuarioConectado().equals(receptor)) {
                cliente.enviarMensaje(mensajeFormateado);
                System.out.println("Mensaje privado enviado a: " + receptor + ": " + mensajeFormateado);
                enviado = true;
            }
        }
        if (!enviado) {
            System.out.println("No se pudo enviar mensaje privado a " + receptor + ": usuario no está conectado.");
        }
    }

    private boolean areFriends(String user1, String user2) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        String[] users = new String[]{user1, user2};
        java.util.Arrays.sort(users);
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM friends WHERE user1 = ? AND user2 = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, users[0]);
            stmt.setString(2, users[1]);
            ResultSet rs = stmt.executeQuery();
            boolean sonAmigos = rs.next();
            System.out.println("Verificación de amistad: " + user1 + ", " + user2 + ": " + sonAmigos);
            return sonAmigos;
        } catch (SQLException e) {
            System.out.println("Error SQL al verificar amistad: " + e.getMessage());
            return false;
        }
    }

    private boolean hasPendingRequest(String sender, String receiver) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM friend_requests WHERE sender = ? AND receiver = ? AND status = 'PENDING'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            ResultSet rs = stmt.executeQuery();
            boolean hasRequest = rs.next();
            System.out.println("Verificación de solicitud pendiente: " + sender + " -> " + receiver + ": " + hasRequest);
            return hasRequest;
        } catch (SQLException e) {
            System.out.println("Error SQL al verificar solicitud pendiente: " + e.getMessage());
            return false;
        }
    }

    private boolean sendFriendRequest(String sender, String receiver) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Verificando existencia de usuarios: sender=" + sender + ", receiver=" + receiver);
            String checkSql = "SELECT nombre_usuario FROM usuarios WHERE nombre_usuario IN (?, ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, sender);
            checkStmt.setString(2, receiver);
            ResultSet rs = checkStmt.executeQuery();
            int userCount = 0;
            while (rs.next()) {
                userCount++;
            }
            System.out.println("Usuarios encontrados en la base de datos: " + userCount);
            if (userCount != 2) {
                System.out.println("Error: Uno o ambos usuarios no existen: " + sender + ", " + receiver);
                salida.println("Error: Uno o ambos usuarios no existen.");
                return false;
            }

            System.out.println("Insertando solicitud de amistad: " + sender + " -> " + receiver);
            String sql = "INSERT INTO friend_requests (sender, receiver, status) VALUES (?, ?, 'PENDING')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Filas afectadas en friend_requests: " + rowsAffected);
            if (rowsAffected > 0) {
                System.out.println("Solicitud de amistad guardada exitosamente: " + sender + " -> " + receiver);
                salida.println("Solicitud de amistad enviada.");
                return true;
            } else {
                System.out.println("Error: No se insertó la solicitud en friend_requests.");
                salida.println("Error: No se pudo guardar la solicitud.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error SQL al enviar solicitud de amistad: " + e.getMessage() + " (Código: " + e.getErrorCode() + ")");
            if (e.getErrorCode() == 1452) {
                System.out.println("Violación de clave foránea: uno o ambos usuarios no existen en la tabla usuarios.");
                salida.println("Error: Uno o ambos usuarios no existen.");
            } else {
                salida.println("Error en la base de datos: " + e.getMessage());
            }
            return false;
        }
    }

    private void notifyFriendRequest(String receiver, String sender) {
        String message = "FRIEND_REQUEST:" + sender;
        boolean sent = false;
        for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
            if (cliente.getUsuarioConectado() != null && cliente.getUsuarioConectado().equals(receiver)) {
                cliente.enviarMensaje(message);
                System.out.println("Notificación de solicitud enviada a: " + receiver + ": " + message);
                sent = true;
            }
        }
        if (!sent) {
            System.out.println("No se pudo enviar notificación a " + receiver + ": usuario no está conectado o no encontrado.");
        }
    }

    private boolean acceptFriendRequest(String sender, String receiver) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String checkSql = "SELECT * FROM friend_requests WHERE sender = ? AND receiver = ? AND status = 'PENDING'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, sender);
            checkStmt.setString(2, receiver);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Error: No hay solicitud pendiente de " + sender + " para " + receiver);
                return false;
            }

            String updateSql = "UPDATE friend_requests SET status = 'ACCEPTED' WHERE sender = ? AND receiver = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, sender);
            updateStmt.setString(2, receiver);
            updateStmt.executeUpdate();

            String[] users = new String[]{sender, receiver};
            java.util.Arrays.sort(users);
            String insertSql = "INSERT INTO friends (user1, user2) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, users[0]);
            insertStmt.setString(2, users[1]);
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Amistad registrada: " + users[0] + ", " + users[1]);
                return true;
            } else {
                System.out.println("Error: No se pudo registrar la amistad.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error SQL al aceptar solicitud de amistad: " + e.getMessage());
            return false;
        }
    }

    private void notifyFriendRequestAccepted(String sender, String receiver) {
        String message = "FRIEND_REQUEST_ACCEPTED:" + receiver;
        boolean sent = false;
        for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
            if (cliente.getUsuarioConectado() != null && cliente.getUsuarioConectado().equals(sender)) {
                cliente.enviarMensaje(message);
                System.out.println("Notificación de aceptación enviada a: " + sender + ": " + message);
                sent = true;
            }
        }
        if (!sent) {
            System.out.println("No se pudo enviar notificación de aceptación a " + sender + ": usuario no está conectado.");
        }
    }

    private boolean rejectFriendRequest(String sender, String receiver) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String checkSql = "SELECT * FROM friend_requests WHERE sender = ? AND receiver = ? AND status = 'PENDING'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, sender);
            checkStmt.setString(2, receiver);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Error: No hay solicitud pendiente de " + sender + " para " + receiver);
                return false;
            }

            String updateSql = "UPDATE friend_requests SET status = 'REJECTED' WHERE sender = ? AND receiver = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, sender);
            updateStmt.setString(2, receiver);
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Solicitud de amistad rechazada: " + sender + " -> " + receiver);
                return true;
            } else {
                System.out.println("Error: No se pudo rechazar la solicitud.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error SQL al rechazar solicitud de amistad: " + e.getMessage());
            return false;
        }
    }

    private void notifyFriendRequestRejected(String sender, String receiver) {
        String message = "FRIEND_REQUEST_REJECTED:" + receiver;
        boolean sent = false;
        for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
            if (cliente.getUsuarioConectado() != null && cliente.getUsuarioConectado().equals(sender)) {
                cliente.enviarMensaje(message);
                System.out.println("Notificación de rechazo enviada a: " + sender + ": " + message);
                sent = true;
            }
        }
        if (!sent) {
            System.out.println("No se pudo enviar notificación de rechazo a " + sender + ": usuario no está conectado.");
        }
    }

    private boolean setNickname(String user, String friend, String nickname) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String userDb = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, userDb, password)) {
            String checkSql = "SELECT * FROM friends WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, user);
            checkStmt.setString(2, friend);
            checkStmt.setString(3, friend);
            checkStmt.setString(4, user);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Error: No son amigos: " + user + ", " + friend);
                return false;
            }

            String sql = "INSERT INTO nicknames (user, friend, nickname) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nickname = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, friend);
            stmt.setString(3, nickname);
            stmt.setString(4, nickname);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Apodo establecido para " + friend + " por " + user + ": " + nickname + " (Filas afectadas: " + rowsAffected + ")");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error SQL al establecer apodo: " + e.getMessage());
            return false;
        }
    }

    private String listarApodos(String user) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String userDb = "root";
        String password = "";
        StringBuilder apodos = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, userDb, password)) {
            String sql = "SELECT friend, nickname FROM nicknames WHERE user = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String friend = rs.getString("friend");
                String nickname = rs.getString("nickname");
                if (apodos.length() > 0) {
                    apodos.append(";");
                }
                apodos.append(friend).append(":").append(nickname);
            }
            System.out.println("Apodos encontrados para " + user + ": " + apodos);
        } catch (SQLException e) {
            System.out.println("Error SQL al listar apodos: " + e.getMessage());
            return "Error al listar apodos.";
        }
        return apodos.length() > 0 ? apodos.toString() : "No hay apodos.";
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
        System.out.println("Mensaje enviado a cliente " + (usuarioConectado != null ? usuarioConectado : socket.getInetAddress()) + ": " + mensaje);
    }

    public String getUsuarioConectado() {
        return usuarioConectado;
    }

    public void cerrar() {
        try {
            System.out.println("Cerrando recursos para cliente: " + (usuarioConectado != null ? usuarioConectado : socket.getInetAddress()));
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error al cerrar recursos: " + e.getMessage());
        }
    }

    private boolean createGroup(String creator, String groupName) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // First check if group already exists
            String checkSql = "SELECT id FROM grupos WHERE nombre = ? AND creador = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, groupName);
            checkStmt.setString(2, creator);
            
            if (checkStmt.executeQuery().next()) {
                salida.println("Error: Ya existe un grupo con ese nombre");
                return false;
            }

            // Create group with activo = true
            String sql = "INSERT INTO grupos (nombre, creador, activo) VALUES (?, ?, TRUE)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, groupName);
            stmt.setString(2, creator);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int groupId = rs.getInt(1);
                    // Add creator as first member
                    sql = "INSERT INTO miembros_grupo (grupo_id, usuario, activo) VALUES (?, ?, TRUE)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, groupId);
                    stmt.setString(2, creator);
                    stmt.executeUpdate();
                    
                    salida.println("SUCCESS:" + groupId);
                    return true;
                }
            }
            salida.println("Error: No se pudo crear el grupo");
            return false;
        } catch (SQLException e) {
            System.out.println("Error SQL al crear grupo: " + e.getMessage());
            salida.println("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean inviteToGroup(String groupId, String invitedUser) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // First check if already a member
            String checkMemberSql = "SELECT * FROM miembros_grupo WHERE grupo_id = ? AND usuario = ? AND activo = TRUE";
            PreparedStatement checkStmt = conn.prepareStatement(checkMemberSql);
            checkStmt.setInt(1, Integer.parseInt(groupId));
            checkStmt.setString(2, invitedUser);
            
            if (checkStmt.executeQuery().next()) {
                salida.println("Error: Usuario ya es miembro del grupo");
                return false;
            }

            // Check existing invitation
            String checkInviteSql = "SELECT * FROM invitaciones_grupo WHERE grupo_id = ? AND usuario_invitado = ? AND estado = 'PENDING'";
            checkStmt = conn.prepareStatement(checkInviteSql);
            checkStmt.setInt(1, Integer.parseInt(groupId));
            checkStmt.setString(2, invitedUser);
            
            if (checkStmt.executeQuery().next()) {
                salida.println("Error: Ya existe una invitación pendiente");
                return false;
            }

            // Insert invitation
            String sql = "INSERT INTO invitaciones_grupo (grupo_id, usuario_invitado, estado) VALUES (?, ?, 'PENDING')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            stmt.setString(2, invitedUser);
            
            if (stmt.executeUpdate() > 0) {
                // Send notification to invited user
                String notifyMsg = "GROUP_INVITATION:" + groupId;
                for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
                    if (cliente.getUsuarioConectado() != null && 
                        cliente.getUsuarioConectado().equals(invitedUser)) {
                        cliente.enviarMensaje(notifyMsg);
                        break;
                    }
                }
                salida.println("Invitación enviada exitosamente");
                return true;
            }
            
            salida.println("Error: No se pudo enviar la invitación");
            return false;
        } catch (SQLException e) {
            System.out.println("Error inviting to group: " + e.getMessage());
            salida.println("Error: " + e.getMessage());
            return false;
        }
    }

    private void notifyGroupInvitation(String groupId, String invitedUser) {
        try {
            // Get group name for the notification
            String groupName = "";
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC",
                "root", ""
            );
            
            String sql = "SELECT nombre FROM grupos WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                groupName = rs.getString("nombre");
                String notificationMessage = "GROUP_INVITATION:" + groupId + ":" + groupName;
                
                // Send to specific user like in friend requests
                for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
                    if (cliente.getUsuarioConectado() != null && 
                        cliente.getUsuarioConectado().equals(invitedUser)) {
                        cliente.enviarMensaje(notificationMessage);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting group details: " + e.getMessage());
        }
    }

    private void checkGroupMinMembers(String groupId) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT COUNT(*) as count FROM miembros_grupo WHERE grupo_id = ? AND activo = TRUE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") < 3) {
                sql = "UPDATE grupos SET activo = FALSE WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(groupId));
                stmt.executeUpdate();
                
                notifyGroupDeleted(groupId);
            }
        } catch (SQLException e) {
            System.out.println("Error SQL al verificar miembros: " + e.getMessage());
        }
    }

    private void notifyGroupDeleted(String groupId) {
        String message = "GROUP_DELETED:" + groupId;
        
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC", 
                "root", 
                "")) {
                
            String sql = "SELECT usuario FROM miembros_grupo WHERE grupo_id = ? AND activo = TRUE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String member = rs.getString("usuario");
                for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
                    if (cliente.getUsuarioConectado() != null && 
                        cliente.getUsuarioConectado().equals(member)) {
                        cliente.enviarMensaje(message);
                        System.out.println("Notificación de grupo eliminado enviada a: " + 
                            member + ": " + message);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error SQL al notificar eliminación de grupo: " + e.getMessage());
        }
    }

    private String listGroups(String username) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        StringBuilder grupos = new StringBuilder();
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            
            String sql = """
                SELECT g.id, g.nombre, g.creador,
                       (SELECT COUNT(*) FROM miembros_grupo mg WHERE mg.grupo_id = g.id AND mg.activo = TRUE) as member_count
                FROM grupos g
                JOIN miembros_grupo mg ON g.id = mg.grupo_id
                WHERE mg.usuario = ? AND mg.activo = TRUE AND g.activo = TRUE
                """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                if (grupos.length() > 0) grupos.append(";");
                grupos.append(String.format("%d - %s (%d miembros)%s",
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("member_count"),
                    rs.getString("creador").equals(username) ? " [Creador]" : ""));
            }
            
            System.out.println("Groups found for " + username + ": " + grupos);
            return grupos.toString();
            
        } catch (SQLException e) {
            System.out.println("Error SQL al listar grupos: " + e.getMessage());
            return "Error al listar grupos";
        }
    }

    private String listarMiembrosGrupo(String groupId) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT usuario FROM miembros_grupo WHERE grupo_id = ? AND activo = TRUE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder members = new StringBuilder();
            while (rs.next()) {
                if (members.length() > 0) members.append(";");
                members.append(rs.getString("usuario"));
            }
            return members.length() > 0 ? members.toString() : "No hay miembros";
        } catch (SQLException e) {
            System.out.println("Error SQL al listar miembros: " + e.getMessage());
            return "Error al listar miembros";
        }
    }

    private boolean acceptGroupInvitation(String groupId, String username) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try {
                // Update invitation status
                String updateSql = "UPDATE invitaciones_grupo SET estado = 'ACCEPTED' WHERE grupo_id = ? AND usuario_invitado = ? AND estado = 'PENDING'";
                PreparedStatement stmt = conn.prepareStatement(updateSql);
                stmt.setInt(1, Integer.parseInt(groupId));
                stmt.setString(2, username);
                
                if (stmt.executeUpdate() > 0) {
                    // Add to members
                    String insertSql = "INSERT INTO miembros_grupo (grupo_id, usuario, activo) VALUES (?, ?, TRUE)";
                    stmt = conn.prepareStatement(insertSql);
                    stmt.setInt(1, Integer.parseInt(groupId));
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                    
                    conn.commit();
                    salida.println("Te has unido al grupo exitosamente");
                    return true;
                }
                
                conn.rollback();
                salida.println("Error: No se encontró la invitación");
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error accepting invitation: " + e.getMessage());
            salida.println("Error: " + e.getMessage());
            return false;
        }
    }

    private void notifyGroupMembershipChanged(String groupId) {
        String message = "GROUP_MEMBERS_CHANGED:" + groupId;
        for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
            if (cliente.getUsuarioConectado() != null) {
                cliente.enviarMensaje(message);
            }
        }
    }

    private String listarInvitacionesGrupo(String username) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        StringBuilder solicitudes = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Copy same pattern as friend requests
            String sql = "SELECT g.id, g.nombre FROM invitaciones_grupo ig " +
                        "JOIN grupos g ON ig.grupo_id = g.id " +
                        "WHERE ig.usuario_invitado = ? AND ig.estado = 'PENDING'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                if (solicitudes.length() > 0) {
                    solicitudes.append(",");
                }
                solicitudes.append(rs.getInt("id")).append("-").append(rs.getString("nombre"));
                count++;
            }
            System.out.println("Found " + count + " group invitations for " + username);
            return solicitudes.length() > 0 ? solicitudes.toString() : "No hay invitaciones";
        } catch (SQLException e) {
            System.out.println("Error SQL: " + e.getMessage());
            return "Error al listar invitaciones";
        }
    }

    private boolean rechazarInvitacionGrupo(String groupId, String usuario) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE invitaciones_grupo SET estado = 'REJECTED' WHERE grupo_id = ? AND usuario_invitado = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            stmt.setString(2, usuario);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error SQL al rechazar invitación: " + e.getMessage());
            return false;
        }
    }

    private void processGroupMessage(String groupId, String sender, String message) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC",
                "root", "")) {
                
            // Save message to database
            String sql = "INSERT INTO mensajes_grupo (grupo_id, usuario_emisor, mensaje) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            stmt.setString(2, sender);
            stmt.setString(3, message);
            stmt.executeUpdate();
            
            // Notify all group members like in chat broadcast
            String notifyMessage = "GROUP_MESSAGE:" + groupId + ":" + sender + ":" + message;
            
            // Get all active group members
            sql = "SELECT usuario FROM miembros_grupo WHERE grupo_id = ? AND activo = TRUE";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupId));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String member = rs.getString("usuario");
                // Send to each member like in private chat
                for (ManejadorClienteProyecto cliente : servidor.getClientes()) {
                    if (cliente.getUsuarioConectado() != null && 
                        cliente.getUsuarioConectado().equals(member)) {
                        cliente.enviarMensaje(notifyMessage);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error processing group message: " + e.getMessage());
        }
    }
}