package clienteproyecto.controller;

import clienteproyecto.gui.MainFrame;
import clienteproyecto.network.ServerConnection;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

public class ClientController {
    private final MainFrame mainFrame;
    private final ServerConnection serverConnection;
    private String currentUser;
    private String currentChatRecipient;
    private Map<String, String> friendNicknames;
    private Timer onlineUsersTimer;

    public ClientController(MainFrame mainFrame, ServerConnection serverConnection) {
        this.mainFrame = mainFrame;
        this.serverConnection = serverConnection;
        this.currentUser = null;
        this.currentChatRecipient = null;
        this.friendNicknames = new HashMap<>();
        System.out.println("Inicializando ClientController.");
        setupListeners();
    }

    public void start() {
        System.out.println("Mostrando MainFrame.");
        mainFrame.display();
    }

    private void setupListeners() {
        // Panel principal
        mainFrame.getMainPanel().setOnRegister(v -> {
            System.out.println("Clic en botón Registrar en MainPanel.");
            mainFrame.showPanel("Registro");
        });
        mainFrame.getMainPanel().setOnLogin(v -> {
            System.out.println("Clic en botón Iniciar Sesión en MainPanel.");
            mainFrame.showPanel("InicioSesion");
        });

        // Panel de registro
        mainFrame.getRegisterPanel().setOnRegister((username, password) -> {
            System.out.println("Procesando registro para usuario: " + username);
            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Error: Campos vacíos en registro.");
                showError("Por favor, complete todos los campos.");
                return;
            }
            try {
                System.out.println("Enviando solicitud de registro: modo=REGISTRO, usuario=" + username);
                String response = serverConnection.sendRequest("REGISTRO", username, password);
                System.out.println("Respuesta del servidor para registro: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para registro.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                showMessage(response);
                if (response.contains("Registro exitoso")) {
                    System.out.println("Registro exitoso. Limpiando campos y mostrando MainPanel.");
                    mainFrame.getRegisterPanel().clearFields();
                    mainFrame.showPanel("Main");
                }
            } catch (IOException ex) {
                System.out.println("Excepción en registro: " + ex.getMessage());
                showError("Error al conectar con el servidor: " + ex.getMessage());
            }
        });
        mainFrame.getRegisterPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en RegisterPanel.");
            mainFrame.showPanel("Main");
        });

        // Panel de inicio de sesión
        mainFrame.getLoginPanel().setOnLogin((username, password) -> {
            System.out.println("Procesando inicio de sesión para usuario: " + username);
            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Error: Campos vacíos en inicio de sesión.");
                showError("Por favor, complete todos los campos.");
                return;
            }
            try {
                System.out.println("Enviando solicitud de inicio de sesión: modo=INICIO_SESION, usuario=" + username);
                String response = serverConnection.sendRequest("INICIO_SESION", username, password);
                System.out.println("Respuesta del servidor para inicio de sesión: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para inicio de sesión.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                showMessage(response);
                if (response.contains("Inicio de sesión exitoso")) {
                    currentUser = username;
                    System.out.println("Inicio de sesión exitoso. Usuario actual: " + currentUser);
                    mainFrame.getLoginPanel().clearFields();
                    // Cargar datos iniciales
                    loadFriendNicknames();
                    loadFriendsList();
                    loadPendingRequests();
                    mainFrame.showPanel("Home");
                } else {
                    System.out.println("Inicio de sesión fallido. Limpiando campos.");
                    mainFrame.getLoginPanel().clearFields();
                }
            } catch (IOException ex) {
                System.out.println("Excepción en inicio de sesión: " + ex.getMessage());
                showError("Error al conectar con el servidor: " + ex.getMessage());
                serverConnection.closeConnection();
            }
        });
        mainFrame.getLoginPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en LoginPanel.");
            mainFrame.showPanel("Main");
        });
        mainFrame.getLoginPanel().setOnForgotPassword(v -> {
            System.out.println("Clic en botón Olvidé mi Contraseña en LoginPanel.");
            mainFrame.showPanel("CambiarContrasena");
        });

        // Panel de cambio de contraseña
        mainFrame.getChangePasswordPanel().setOnChangePassword((username, newPassword) -> {
            System.out.println("Procesando cambio de contraseña para usuario: " + username);
            if (username.isEmpty() || newPassword.isEmpty()) {
                System.out.println("Error: Campos vacíos en cambio de contraseña.");
                showError("Por favor, complete todos los campos.");
                return;
            }
            try {
                System.out.println("Enviando solicitud de cambio de contraseña: modo=CAMBIAR_CONTRASENA, usuario=" + username);
                String response = serverConnection.sendRequest("CAMBIAR_CONTRASENA", username, newPassword);
                System.out.println("Respuesta del servidor para cambio de contraseña: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para cambio de contraseña.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                showMessage(response);
                if (response.contains("Contraseña actualizada")) {
                    System.out.println("Cambio de contraseña exitoso. Mostrando LoginPanel.");
                    mainFrame.getChangePasswordPanel().clearFields();
                    mainFrame.showPanel("InicioSesion");
                }
            } catch (IOException ex) {
                System.out.println("Excepción en cambio de contraseña: " + ex.getMessage());
                showError("Error al conectar con el servidor: " + ex.getMessage());
            }
        });
        mainFrame.getChangePasswordPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en ChangePasswordPanel.");
            mainFrame.showPanel("InicioSesion");
        });

        // Panel de home
        mainFrame.getHomePanel().setOnUserSelected(username -> {
            System.out.println("Evento onUserSelected disparado con usuario: " + username);
            if (username == null || username.isEmpty()) {
                System.out.println("Error: No se seleccionó un usuario válido.");
                showError("Por favor, seleccione un usuario válido.");
                return;
            }
            try {
                System.out.println("Iniciando chat privado con: " + username);
                currentChatRecipient = username;
                System.out.println("Enviando solicitud: modo=ENTRAR_CHAT_PRIVADO, usuario=" + currentUser + ", dato=" + username);
                String enterResponse = serverConnection.sendRequest("ENTRAR_CHAT_PRIVADO", currentUser, username);
                System.out.println("Respuesta del servidor para entrar al chat: " + enterResponse);
                if (enterResponse == null || !enterResponse.contains("Entró al chat privado")) {
                    System.out.println("Error: Fallo al entrar al chat privado. Respuesta: " + enterResponse);
                    showError("Error al entrar al chat privado.");
                    currentChatRecipient = null;
                    return;
                }
                System.out.println("Enviando solicitud: modo=CARGAR_CHAT_PRIVADO, usuario=" + currentUser + ", dato=" + username);
                String history = serverConnection.sendRequest("CARGAR_CHAT_PRIVADO", currentUser, username);
                System.out.println("Respuesta del servidor para historial de chat: " + history);
                if (history == null) {
                    System.out.println("Error: No se recibió historial del chat.");
                    showError("Error al cargar el historial del chat.");
                    return;
                }
                System.out.println("Configurando ChatPanel con usuario: " + username);
                mainFrame.getChatPanel().setChattingWith(username);
                mainFrame.getChatPanel().loadChatHistory(history);
                mainFrame.showPanel("Chat");
            } catch (IOException ex) {
                System.out.println("Excepción al cargar chat: " + ex.getMessage());
                showError("Error al cargar el chat: " + ex.getMessage());
                currentChatRecipient = null;
            }
        });
        mainFrame.getHomePanel().setOnAddFriend(username -> {
            System.out.println("Evento onAddFriend disparado con usuario: " + username);
            if (username == null || username.isEmpty()) {
                System.out.println("Error: No se seleccionó un usuario válido para agregar.");
                showError("Por favor, seleccione un usuario válido.");
                return;
            }
            if (username.equals(currentUser)) {
                System.out.println("Error: Intento de enviarse solicitud a sí mismo: " + username);
                showError("No puedes enviarte una solicitud de amistad a ti mismo.");
                return;
            }
            try {
                System.out.println("Enviando solicitud de amistad: modo=SEND_FRIEND_REQUEST, usuario=" + currentUser + ", dato=" + username);
                String response = serverConnection.sendRequest("SEND_FRIEND_REQUEST", currentUser, username);
                System.out.println("Respuesta del servidor para solicitud de amistad: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para solicitud de amistad.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                if (response.contains("Solicitud de amistad enviada")) {
                    System.out.println("Solicitud de amistad enviada exitosamente a: " + username);
                    showMessage("Solicitud de amistad enviada a " + username + ".");
                } else {
                    System.out.println("Error del servidor al enviar solicitud: " + response);
                    showError(response);
                }
            } catch (IOException ex) {
                System.out.println("Excepción al enviar solicitud de amistad: " + ex.getMessage());
                showError("Error al enviar la solicitud de amistad: " + ex.getMessage());
            }
        });
        mainFrame.getHomePanel().setOnShowFriends(v -> {
            System.out.println("Clic en botón Ver Amigos en HomePanel.");
            loadFriendNicknames(); // Asegurar que los apodos se carguen primero
            loadFriendsList();
            mainFrame.showPanel("Friends");
        });
        mainFrame.getHomePanel().setOnShowRequests(v -> {
            System.out.println("Clic en botón Ver Solicitudes en HomePanel.");
            loadPendingRequests();
            mainFrame.showPanel("Requests");
        });

        mainFrame.getHomePanel().setOnShowGroups(v -> {
            System.out.println("Cambiando al panel de grupos");
            loadGroups();
            loadGroupInvitations();
            updateGroupPanelOnlineUsers();
            mainFrame.showPanel("Group");
        });

        mainFrame.getGroupPanel().setOnCreateGroupConfirmed((groupName, selectedUsers) -> {
            try {
                if (groupName.isEmpty()) {
                    showError("Ingrese un nombre para el grupo");
                    return;
                }

                String response = serverConnection.sendRequest("CREATE_GROUP", currentUser, groupName);
                if (!response.startsWith("Error")) {
                    String groupId = response.split(":")[1];
                    
                    for (String user : selectedUsers) {
                        serverConnection.sendRequest(
                            "INVITE_TO_GROUP", 
                            currentUser, 
                            groupId + ":" + user
                        );
                    }
                    
                    showMessage("Grupo creado exitosamente: " + groupName);
                    loadGroups(); // Reload groups after creation
                } else {
                    showError(response);
                }
            } catch (IOException ex) {
                showError("Error al crear el grupo: " + ex.getMessage());
            }
        });

        // Add timer for updating online users in GroupPanel
        onlineUsersTimer = new Timer(5000, e -> {
            if (mainFrame.getGroupPanel().isVisible()) {
                updateGroupPanelOnlineUsers();
            }
        });
        onlineUsersTimer.start();

        // Panel de amigos
        mainFrame.getFriendsPanel().setOnSetNickname((friend, nickname) -> {
            System.out.println("Procesando establecimiento de apodo: amigo=" + friend + ", apodo=" + nickname);
            try {
                System.out.println("Enviando solicitud: modo=SET_NICKNAME, usuario=" + currentUser + ", dato=" + friend + ":" + nickname);
                String response = serverConnection.sendRequest("SET_NICKNAME", currentUser, friend + ":" + nickname);
                System.out.println("Respuesta del servidor para establecer apodo: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para establecer apodo.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                if (response.contains("Apodo establecido")) {
                    System.out.println("Apodo establecido exitosamente: " + nickname + " para " + friend);
                    friendNicknames.put(friend, nickname);
                    loadFriendsList();
                    showMessage("Apodo establecido exitosamente.");
                } else {
                    System.out.println("Error del servidor al establecer apodo: " + response);
                    showError(response);
                }
            } catch (IOException ex) {
                System.out.println("Excepción al establecer apodo: " + ex.getMessage());
                showError("Error al establecer el apodo: " + ex.getMessage());
            }
        });
        mainFrame.getFriendsPanel().setOnFriendSelected(username -> {
            if (username == null || username.isEmpty()) {
                showError("Por favor, seleccione un amigo válido.");
                return;
            }
            try {
                System.out.println("Iniciando chat privado con: " + username);
                currentChatRecipient = username;
                
                String enterResponse = serverConnection.sendRequest("ENTRAR_CHAT_PRIVADO", currentUser, username);
                if (enterResponse == null || !enterResponse.contains("Entró al chat privado")) {
                    showError("Error al entrar al chat privado.");
                    currentChatRecipient = null;
                    return;
                }

                String history = serverConnection.sendRequest("CARGAR_CHAT_PRIVADO", currentUser, username);
                if (history == null) {
                    showError("Error al cargar el historial del chat.");
                    return;
                }

                mainFrame.getChatPanel().setChattingWith(username);
                mainFrame.getChatPanel().loadChatHistory(history);
                
                System.out.println("Mostrando panel de chat");
                mainFrame.showPanel("Chat");
            } catch (IOException ex) {
                System.out.println("Error al iniciar chat: " + ex.getMessage());
                showError("Error al iniciar el chat: " + ex.getMessage());
                currentChatRecipient = null;
            }
        });
        mainFrame.getFriendsPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en FriendsPanel.");
            mainFrame.showPanel("Home");
        });

        // Panel de solicitudes
        mainFrame.getRequestPanel().setOnAcceptRequest((sender, unused) -> {
            System.out.println("Procesando aceptación de solicitud de: " + sender);
            try {
                System.out.println("Enviando solicitud: modo=ACCEPT_FRIEND_REQUEST, usuario=" + currentUser + ", dato=" + sender);
                String response = serverConnection.sendRequest("ACCEPT_FRIEND_REQUEST", currentUser, sender);
                System.out.println("Respuesta del servidor para aceptar solicitud: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para aceptar solicitud.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                showMessage(response);
                if (response.contains("Solicitud de amistad aceptada")) {
                    System.out.println("Solicitud aceptada. Actualizando lista de solicitudes y amigos.");
                    loadPendingRequests();
                    loadFriendsList();
                }
            } catch (IOException ex) {
                System.out.println("Excepción al aceptar solicitud: " + ex.getMessage());
                showError("Error al aceptar la solicitud: " + ex.getMessage());
            }
        });
        mainFrame.getRequestPanel().setOnRejectRequest((sender, unused) -> {
            System.out.println("Procesando rechazo de solicitud de: " + sender);
            try {
                System.out.println("Enviando solicitud: modo=REJECT_FRIEND_REQUEST, usuario=" + currentUser + ", dato=" + sender);
                String response = serverConnection.sendRequest("REJECT_FRIEND_REQUEST", currentUser, sender);
                System.out.println("Respuesta del servidor para rechazar solicitud: " + response);
                System.out.println("Respuesta del servidor para rechazar solicitud: " + response);
                if (response == null) {
                    System.out.println("Error: No se recibió respuesta del servidor para rechazar solicitud.");
                    showError("No se recibió respuesta del servidor.");
                    return;
                }
                showMessage(response);
                if (response.contains("Solicitud de amistad rechazada")) {
                    System.out.println("Solicitud rechazada. Actualizando lista de solicitudes.");
                    loadPendingRequests();
                }
            } catch (IOException ex) {
                System.out.println("Excepción al rechazar solicitud: " + ex.getMessage());
                showError("Error al rechazar la solicitud: " + ex.getMessage());
            }
        });
        mainFrame.getRequestPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en RequestPanel.");
            mainFrame.showPanel("Home");
        });

        // Panel de chat
        mainFrame.getChatPanel().setOnSendMessage((message, recipient) -> {
            System.out.println("Procesando envío de mensaje privado a: " + recipient);
            if (message.isEmpty() || recipient.isEmpty()) {
                System.out.println("Error: Mensaje o destinatario vacíos.");
                showError("El mensaje o el destinatario no pueden estar vacíos.");
                return;
            }
            try {
                System.out.println("Enviando solicitud: modo=CHAT_PRIVADO, usuario=" + currentUser + ", dato=" + recipient + ":" + message);
                String formattedMessage = recipient + ":" + message;
                String response = serverConnection.sendRequest("CHAT_PRIVADO", currentUser, formattedMessage);
                System.out.println("Respuesta del servidor para enviar mensaje: " + response);
                if (response != null && response.contains("Mensaje privado enviado")) {
                    System.out.println("Mensaje enviado exitosamente. Añadiendo al ChatPanel.");
                    mainFrame.getChatPanel().appendMessage(currentUser, message, new Timestamp(System.currentTimeMillis()).toString());
                } else {
                    System.out.println("Error: Fallo al enviar mensaje. Respuesta: " + response);
                    showError("Error al enviar el mensaje: " + (response != null ? response : "No response"));
                }
            } catch (IOException ex) {
                System.out.println("Excepción al enviar mensaje: " + ex.getMessage());
                showError("Error al enviar el mensaje: " + ex.getMessage());
            }
        });
        mainFrame.getChatPanel().setOnBack(v -> {
            System.out.println("Clic en botón Volver en ChatPanel.");
            try {
                if (currentChatRecipient != null) {
                    System.out.println("Enviando solicitud: modo=SALIR_CHAT_PRIVADO, usuario=" + currentUser + ", dato=" + currentChatRecipient);
                    serverConnection.sendRequest("SALIR_CHAT_PRIVADO", currentUser, currentChatRecipient);
                }
                mainFrame.showPanel("Home");
                currentChatRecipient = null;
            } catch (IOException ex) {
                System.out.println("Excepción al salir del chat: " + ex.getMessage());
                showError("Error al salir del chat: " + ex.getMessage());
            }
        });

        mainFrame.getGroupPanel().setOnBack(v -> {
            System.out.println("Volviendo al panel principal");
            mainFrame.showPanel("Home");
        });

        mainFrame.getGroupPanel().setOnAcceptInvite(groupId -> {
            try {
                String response = serverConnection.sendRequest("ACCEPT_GROUP_INVITE", currentUser, groupId);
                if (!response.startsWith("Error")) {
                    showMessage("Te has unido al grupo exitosamente");
                    loadGroups();
                    loadGroupInvitations();
                } else {
                    showError(response);
                }
            } catch (IOException ex) {
                showError("Error al aceptar invitación: " + ex.getMessage());
            }
        });

        mainFrame.getGroupPanel().setOnRejectInvite(groupId -> {
            try {
                String response = serverConnection.sendRequest("REJECT_GROUP_INVITE", currentUser, groupId);
                if (!response.startsWith("Error")) {
                    showMessage("Invitación rechazada");
                    loadGroupInvitations();
                } else {
                    showError(response);
                }
            } catch (IOException ex) {
                showError("Error al rechazar invitación: " + ex.getMessage());
            }
        });

        // Update server message handling for group invitations
        serverConnection.setOnMessageReceived(message -> {
            System.out.println("Received message: " + message);
            SwingUtilities.invokeLater(() -> {
                if (message.startsWith("GROUP_INVITATION:")) {
                    String groupId = message.substring("GROUP_INVITATION:".length());
                    showMessage("Nueva invitación a grupo recibida");
                    loadGroupInvitations();
                } else if (message.startsWith("CHAT_PRIVADO:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String sender = parts[1];
                        String msg = parts[2];
                        System.out.println("Mensaje privado recibido de: " + sender + ": " + msg);
                        if (mainFrame.getChatPanel().isVisible() && currentChatRecipient != null && currentChatRecipient.equals(sender)) {
                            System.out.println("Añadiendo mensaje al ChatPanel.");
                            mainFrame.getChatPanel().appendMessage(sender, msg, new Timestamp(System.currentTimeMillis()).toString());
                        } else {
                            System.out.println("Notificando nuevo mensaje de: " + sender);
                            showMessage("Nuevo mensaje de " + sender + ": " + msg);
                        }
                    } else {
                        System.out.println("Error: Formato de mensaje privado inválido: " + message);
                        showError("Formato de mensaje privado inválido: " + message);
                    }
                } else if (message.startsWith("USER_CONNECTED:")) {
                    String username = message.substring("USER_CONNECTED:".length());
                    System.out.println("Usuario conectado: " + username);
                    if (!username.equals(currentUser)) {
                        mainFrame.getHomePanel().addUser(username);
                    }
                } else if (message.startsWith("USER_DISCONNECTED:")) {
                    String username = message.substring("USER_DISCONNECTED:".length());
                    System.out.println("Usuario desconectado: " + username);
                    mainFrame.getHomePanel().removeUser(username);
                } else if (message.startsWith("INITIAL_USERS:")) {
                    String usersStr = message.substring("INITIAL_USERS:".length());
                    System.out.println("Usuarios iniciales recibidos: " + usersStr);
                    if (!usersStr.equals("No hay usuarios en línea.")) {
                        String[] users = usersStr.split(",");
                        mainFrame.getHomePanel().updateOnlineUsers(users);
                    } else {
                        mainFrame.getHomePanel().updateOnlineUsers(new String[]{});
                    }
                } else if (message.startsWith("FRIEND_REQUEST:")) {
                    String sender = message.substring("FRIEND_REQUEST:".length());
                    System.out.println("Nueva solicitud de amistad recibida de: " + sender);
                    showMessage("Nueva solicitud de amistad de: " + sender);
                    loadPendingRequests();
                } else if (message.startsWith("FRIEND_REQUEST_ACCEPTED:")) {
                    String receiver = message.substring("FRIEND_REQUEST_ACCEPTED:".length());
                    System.out.println("Solicitud de amistad aceptada por: " + receiver);
                    showMessage(receiver + " ha aceptado tu solicitud de amistad.");
                    loadFriendsList();
                } else if (message.startsWith("FRIEND_REQUEST_REJECTED:")) {
                    String receiver = message.substring("FRIEND_REQUEST_REJECTED:".length());
                    System.out.println("Solicitud de amistad rechazada por: " + receiver);
                    showMessage(receiver + " ha rechazado tu solicitud de amistad.");
                } else if (message.startsWith("GROUP_MESSAGE:")) {
                    handleGroupMessage(message);
                } else {
                    System.out.println("Mensaje desconocido recibido: " + message);
                    // Evitar procesar mensajes no relacionados como apodos
                }
            });
        });

        // Cerrar conexión al cerrar la ventana
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Cerrando ventana. Finalizando sesión para: " + currentUser);
                try {
                    if (currentUser != null) {
                        System.out.println("Enviando solicitud: modo=CERRAR_SESION, usuario=" + currentUser);
                        serverConnection.sendRequest("CERRAR_SESION", currentUser, "");
                    }
                } catch (IOException ex) {
                    System.out.println("Excepción al cerrar sesión: " + ex.getMessage());
                } finally {
                    System.out.println("Cerrando conexión con el servidor.");
                    serverConnection.closeConnection();
                }
            }
        });
    }

    private void loadPendingRequests() {
        System.out.println("Cargando solicitudes pendientes para: " + currentUser);
        try {
            System.out.println("Enviando solicitud: modo=LISTAR_SOLICITUDES_PENDIENTES, usuario=" + currentUser);
            String response = serverConnection.sendRequest("LISTAR_SOLICITUDES_PENDIENTES", currentUser, "");
            System.out.println("Respuesta del servidor para solicitudes pendientes: " + response);
            if (response == null) {
                System.out.println("Error: No se recibió respuesta del servidor para solicitudes pendientes.");
                showError("No se recibió respuesta del servidor.");
                return;
            }
            if (response.equals("No hay solicitudes pendientes.") || response.startsWith("Error")) {
                System.out.println("No hay solicitudes pendientes o error: " + response);
                mainFrame.getRequestPanel().updateRequests(new String[]{});
            } else {
                String[] senders = response.split(",");
                System.out.println("Solicitudes pendientes recibidas: " + String.join(", ", senders));
                mainFrame.getRequestPanel().updateRequests(senders);
            }
        } catch (IOException ex) {
            System.out.println("Excepción al cargar solicitudes pendientes: " + ex.getMessage());
            showError("Error al obtener la lista de solicitudes: " + ex.getMessage());
        }
    }

    private void loadFriendNicknames() {
        System.out.println("Cargando apodos para: " + currentUser);
        try {
            System.out.println("Enviando solicitud: modo=LISTAR_APODOS, usuario=" + currentUser);
            String response = serverConnection.sendRequest("LISTAR_APODOS", currentUser, "");
            System.out.println("Respuesta del servidor para lista de apodos: " + response);
            if (response == null) {
                System.out.println("Error: No se recibió respuesta del servidor para lista de apodos.");
                showError("No se recibió respuesta del servidor.");
                return;
            }
            friendNicknames.clear(); // Limpiar antes de cargar nuevos apodos
            if (!response.equals("No hay apodos.") && !response.startsWith("Error")) {
                String[] nicknamePairs = response.split(";");
                for (String pair : nicknamePairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        System.out.println("Apodo encontrado: amigo=" + parts[0] + ", apodo=" + parts[1]);
                        friendNicknames.put(parts[0], parts[1]);
                    } else {
                        System.out.println("Formato de apodo inválido: " + pair);
                    }
                }
            } else {
                System.out.println("No hay apodos o error: " + response);
            }
            System.out.println("Mapa de apodos actualizado: " + friendNicknames);
        } catch (IOException ex) {
            System.out.println("Excepción al cargar apodos: " + ex.getMessage());
            showError("Error al obtener la lista de apodos: " + ex.getMessage());
        }
    }

    private void loadFriendsList() {
        System.out.println("Cargando lista de amigos para: " + currentUser);
        try {
            System.out.println("Enviando solicitud: modo=LISTAR_AMIGOS, usuario=" + currentUser);
            String response = serverConnection.sendRequest("LISTAR_AMIGOS", currentUser, "");
            System.out.println("Respuesta del servidor para lista de amigos: " + response);
            if (response == null) {
                System.out.println("Error: No se recibió respuesta del servidor para lista de amigos.");
                showError("No se recibió respuesta del servidor.");
                return;
            }
            if (response.equals("No hay amigos.") || response.startsWith("Error")) {
                System.out.println("No hay amigos o error: " + response);
                mainFrame.getFriendsPanel().updateFriends(new String[]{}, friendNicknames);
            } else {
                String[] friends = response.split(",");
                System.out.println("Amigos recibidos: " + Arrays.toString(friends));
                mainFrame.getFriendsPanel().updateFriends(friends, friendNicknames);
            }
        } catch (IOException ex) {
            System.out.println("Excepción al listar amigos: " + ex.getMessage());
            showError("Error al obtener la lista de amigos: " + ex.getMessage());
        }
    }

    private void loadGroups() {
        try {
            String response = serverConnection.listGroups(currentUser);
            System.out.println("Groups response: " + response);
            if (!response.startsWith("Error") && !response.equals("No hay grupos disponibles")) {
                String[] groups = response.split(";");
                mainFrame.getGroupPanel().updateGroupsList(groups);
            } else {
                mainFrame.getGroupPanel().updateGroupsList(new String[0]);
            }
        } catch (IOException ex) {
            showError("Error al cargar grupos: " + ex.getMessage());
        }
    }

    private void loadGroupMembers(String groupId) {
        try {
            String response = serverConnection.listGroupMembers(groupId, currentUser);
            if (!response.startsWith("Error")) {
                String[] members = response.split(";");
                mainFrame.getGroupPanel().updateMembersList(members);
            } else {
                showError("Error al cargar miembros: " + response);
            }
        } catch (IOException ex) {
            showError("Error al cargar miembros: " + ex.getMessage());
        }
    }

    private void loadGroupInvitations() {
        try {
            // Copy same pattern as loadPendingRequests
            String response = serverConnection.sendRequest("LIST_GROUP_INVITATIONS", currentUser, "");
            System.out.println("Group invitations response: " + response);
            
            if (!response.equals("No hay invitaciones") && !response.startsWith("Error")) {
                String[] invitations = response.split(",");
                System.out.println("Processing " + invitations.length + " group invitations");
                mainFrame.getGroupPanel().updateInvitationsList(invitations);
            } else {
                mainFrame.getGroupPanel().updateInvitationsList(new String[0]);
            }
        } catch (IOException ex) {
            System.out.println("Error loading group invitations: " + ex.getMessage());
            showError("Error al cargar invitaciones: " + ex.getMessage());
        }
    }

    private void showMessage(String message) {
        System.out.println("Mostrando mensaje al usuario: " + message);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, message));
    }

    private void showError(String message) {
        System.out.println("Mostrando error al usuario: " + message);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    public void inviteToGroup(String groupId, String username) {
        try {
            String response = serverConnection.sendRequest("INVITE_TO_GROUP", currentUser, groupId + ":" + username);
            if (!response.startsWith("Error")) {
                mainFrame.showMessage("Invitación enviada exitosamente");
            } else {
                mainFrame.showMessage(response);
            }
        } catch (IOException ex) {
            mainFrame.showMessage("Error al enviar la invitación: " + ex.getMessage());
        }
    }

    public void leaveGroup(String groupId) {
        try {
            String response = serverConnection.sendRequest("LEAVE_GROUP", currentUser, groupId);
            if (!response.startsWith("Error")) {
                mainFrame.showMessage("Has salido del grupo exitosamente");
            } else {
                mainFrame.showMessage(response);
            }
        } catch (IOException ex) {
            mainFrame.showMessage("Error al salir del grupo: " + ex.getMessage());
        }
    }

    private void handleGroupMessage(String message) {
        String[] parts = message.split(":", 4); 
        if (parts.length == 4) {
            String groupId = parts[1];
            String sender = parts[2];
            String content = parts[3];
            
            if (mainFrame.getGroupPanel().getCurrentGroupId() != null && 
                mainFrame.getGroupPanel().getCurrentGroupId().equals(groupId)) {
                mainFrame.getGroupPanel().getChatArea().append(
                    String.format("[%s] %s: %s\n", 
                        new Timestamp(System.currentTimeMillis()), sender, content)
                );
            }
        }
    }

    private void updateGroupPanelOnlineUsers() {
        try {
            String response = serverConnection.sendRequest("LISTAR_USUARIOS_EN_LINEA", currentUser, "");
            System.out.println("Response from server for online users: " + response);
            
            if (!response.startsWith("Error") && !response.equals("No hay usuarios en línea.")) {
                String[] users = response.split(",");
                
                String[] otherUsers = Arrays.stream(users)
                    .filter(user -> !user.equals(currentUser))
                    .toArray(String[]::new);
                System.out.println("Updating GroupPanel with " + otherUsers.length + " online users");
                mainFrame.getGroupPanel().updateOnlineUsers(otherUsers);
            } else {
                mainFrame.getGroupPanel().updateOnlineUsers(new String[]{});
            }
        } catch (IOException ex) {
            System.out.println("Error loading online users: " + ex.getMessage());
            showError("Error al cargar usuarios en línea: " + ex.getMessage());
        }
    }

    public void createGroup(String groupName) {
        try {
            String[] selectedUsers = mainFrame.getGroupPanel().getSelectedUsers();
            if (selectedUsers.length < 2) {
                showError("Debes seleccionar al menos 2 usuarios");
                return;
            }

            String response = serverConnection.sendRequest("CREATE_GROUP", currentUser, groupName);
            if (!response.startsWith("Error")) {
                String groupId = response.split(":")[1];
                
                for (String user : selectedUsers) {
                    String inviteResponse = serverConnection.sendRequest(
                        "INVITE_TO_GROUP", 
                        currentUser, 
                        groupId + ":" + user
                    );
                    if (inviteResponse.startsWith("Error")) {
                        showError("Error al invitar a " + user);
                    }
                }
                
                showMessage("Grupo creado y invitaciones enviadas");
                loadGroups();
            } else {
                showError(response);
            }
        } catch (IOException ex) {
            showError("Error al crear el grupo: " + ex.getMessage());
        }
    }
}