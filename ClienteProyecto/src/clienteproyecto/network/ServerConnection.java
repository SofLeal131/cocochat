package clienteproyecto.network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ServerConnection {
    private static final String HOST = "100.97.198.45";
    private static final int PUERTO = 5000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private Consumer<String> onMessageReceived;
    private volatile boolean isRunning;
    private final Object lock = new Object();

    public ServerConnection() {
        isRunning = false;
    }

    public void setOnMessageReceived(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public String sendRequest(String modo, String nombreUsuario, String dato) throws IOException {
        Socket tempSocket = null;
        PrintWriter tempOut = null;
        BufferedReader tempIn = null;
        boolean usePersistentConnection = modo.equals("INICIO_SESION") || modo.equals("CERRAR_SESION") ||
                modo.equals("LISTAR_USUARIOS_EN_LINEA") || modo.equals("CARGAR_CHAT_PRIVADO") ||
                modo.equals("ENTRAR_CHAT_PRIVADO") || modo.equals("SALIR_CHAT_PRIVADO") ||
                modo.equals("CHAT_PRIVADO") || modo.equals("SEND_FRIEND_REQUEST") ||
                modo.equals("LISTAR_TODOS_USUARIOS") || modo.equals("LISTAR_AMIGOS") ||
                modo.equals("LISTAR_SOLICITUDES_PENDIENTES") || modo.equals("ACCEPT_FRIEND_REQUEST") ||
                modo.equals("REJECT_FRIEND_REQUEST") || modo.equals("SET_NICKNAME") ||
                modo.equals("LISTAR_APODOS") || modo.equals("CREATE_GROUP") ||           // Add these new modes
                modo.equals("INVITE_TO_GROUP") ||
                modo.equals("LEAVE_GROUP") ||
                modo.equals("GROUP_MESSAGE");

        try {
            if (usePersistentConnection) {
                synchronized (lock) {
                    if (socket == null || socket.isClosed()) {
                        socket = new Socket(HOST, PUERTO);
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        if (!isRunning && (modo.equals("INICIO_SESION") || modo.equals("CARGAR_CHAT_PRIVADO") ||
                                modo.equals("ENTRAR_CHAT_PRIVADO") || modo.equals("SEND_FRIEND_REQUEST") ||
                                modo.equals("LISTAR_TODOS_USUARIOS") || modo.equals("LISTAR_AMIGOS") ||
                                modo.equals("LISTAR_SOLICITUDES_PENDIENTES") || modo.equals("ACCEPT_FRIEND_REQUEST") ||
                                modo.equals("REJECT_FRIEND_REQUEST") || modo.equals("SET_NICKNAME") ||
                                modo.equals("LISTAR_APODOS"))) {
                            startListener();
                        }
                    }
                    tempOut = out;
                    tempIn = in;
                }
            } else {
                tempSocket = new Socket(HOST, PUERTO);
                tempOut = new PrintWriter(tempSocket.getOutputStream(), true);
                tempIn = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
            }

            // Enviar solicitud
            System.out.println("Enviando solicitud: modo=" + modo + ", usuario=" + nombreUsuario + ", dato=" + dato);
            tempOut.println(modo);
            if (!modo.equals("CERRAR_SESION")) {
                tempOut.println(nombreUsuario);
                tempOut.println(dato);
            }

            // Leer respuesta
            synchronized (lock) {
                String response = usePersistentConnection ? tempIn.readLine() : tempIn.readLine();
                System.out.println("Respuesta recibida: " + response);
                return response;
            }
        } catch (UnknownHostException ex) {
            System.err.println("No se pudo encontrar el servidor: " + ex.getMessage());
            throw new IOException("No se pudo encontrar el servidor: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Error de IO en sendRequest: " + ex.getMessage());
            throw ex;
        } finally {
            if (!usePersistentConnection) {
                try {
                    if (tempIn != null) tempIn.close();
                    if (tempOut != null) tempOut.close();
                    if (tempSocket != null) tempSocket.close();
                } catch (IOException ex) {
                    System.err.println("Error al cerrar conexión temporal: " + ex.getMessage());
                }
            }
        }
    }

    private void startListener() {
        isRunning = true;
        listenerThread = new Thread(() -> {
            try {
                while (isRunning && socket != null && !socket.isClosed()) {
                    synchronized (lock) {
                        if (in.ready()) {
                            String message = in.readLine();
                            if (message == null) {
                                System.out.println("Servidor desconectado.");
                                break;
                            }
                            if (onMessageReceived != null) {
                                onMessageReceived.accept(message);
                            }
                        }
                    }
                    Thread.yield();
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error en el listener: " + e.getMessage());
                }
            } finally {
                if (isRunning) {
                    closeConnection();
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void closeConnection() {
        isRunning = false;
        try {
            synchronized (lock) {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            }
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
        } catch (IOException ex) {
            System.err.println("Error al cerrar conexión: " + ex.getMessage());
        } finally {
            socket = null;
            in = null;
            out = null;
            listenerThread = null;
        }
    }

    public String createGroup(String groupName, String creator) throws IOException {
        return sendRequest("CREATE_GROUP", creator, groupName);
    }

    public String inviteToGroup(String groupId, String invitedUser) throws IOException {
        return sendRequest("INVITE_TO_GROUP", groupId, invitedUser);
    }

    public String acceptGroupInvite(String groupId, String user) throws IOException {
        return sendRequest("ACCEPT_GROUP_INVITE", user, groupId);
    }

    public String leaveGroup(String groupId, String user) throws IOException {
        return sendRequest("LEAVE_GROUP", user, groupId);
    }

    public String sendGroupMessage(String groupId, String user, String message) throws IOException {
        return sendRequest("GROUP_MESSAGE", user, groupId + ":" + message);
    }

    public String listGroups(String user) throws IOException {
        return sendRequest("LIST_GROUPS", user, "");
    }

    public String listGroupMembers(String groupId, String user) throws IOException {
        return sendRequest("LIST_GROUP_MEMBERS", user, groupId);
    }

    public String listGroupInvitations(String user) throws IOException {
        return sendRequest("LIST_GROUP_INVITATIONS", user, "");
    }

    public String rejectGroupInvite(String groupId, String user) throws IOException {
        return sendRequest("REJECT_GROUP_INVITE", user, groupId);
    }
}