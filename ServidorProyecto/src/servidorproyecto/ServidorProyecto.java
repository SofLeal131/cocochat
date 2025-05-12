package servidorproyecto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorProyecto {
    private ServerSocket serverSocket;
    private static final int PUERTO = 5000;
    private JFrame frame;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private Set<String> usuariosEnLinea;
    private CopyOnWriteArrayList<ManejadorClienteProyecto> clientes;
    private JTextArea chatArea;
    private Map<String, Set<String>> usuariosActivosEnChats;

    public ServidorProyecto() {
        usuariosEnLinea = new HashSet<>();
        clientes = new CopyOnWriteArrayList<>();
        usuariosActivosEnChats = new HashMap<>();
    }

    public void iniciar() {
        crearVentanaUsuarios();

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PUERTO);
                System.out.println("Servidor iniciado, esperando conexiones...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Cliente conectado desde: " + socket.getInetAddress());
                    System.out.println("Total de usuarios en línea: " + usuariosEnLinea.size());

                    ManejadorClienteProyecto manejador = new ManejadorClienteProyecto(socket, this);
                    clientes.add(manejador);
                    new Thread(() -> {
                        try {
                            manejador.manejarMensajes();
                        } catch (IOException e) {
                            System.out.println("Error al manejar cliente: " + e.getMessage());
                            clientes.remove(manejador);
                        }
                    }).start();
                }
            } catch (IOException e) {
                System.out.println("Error en el servidor: " + e.getMessage());
            } finally {
                try {
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el servidor: " + e.getMessage());
                }
            }
        }).start();
    }

    private void crearVentanaUsuarios() {
        frame = new JFrame("Usuarios Registrados y Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        String[] columnas = {"ID", "Nombre de Usuario", "Contraseña", "En línea"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaUsuarios = new JTable(modeloTabla);
        JScrollPane scrollPaneTabla = new JScrollPane(tablaUsuarios);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea(10, 50);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPaneChat = new JScrollPane(chatArea);
        chatPanel.add(new JLabel("Historial de Chat:"), BorderLayout.NORTH);
        chatPanel.add(scrollPaneChat, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneTabla, chatPanel);
        splitPane.setDividerLocation(300);
        frame.add(splitPane, BorderLayout.CENTER);

        actualizarTablaUsuarios();

        frame.setVisible(true);
    }

    public synchronized void actualizarTablaUsuarios() {
        modeloTabla.setRowCount(0);

        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<String> fila = new Vector<>();
                String nombreUsuario = rs.getString("nombre_usuario");
                fila.add(String.valueOf(rs.getInt("id")));
                fila.add(nombreUsuario);
                fila.add(rs.getString("contrasena"));
                fila.add(usuariosEnLinea.contains(nombreUsuario) ? "Sí" : "No");
                modeloTabla.addRow(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public synchronized void agregarUsuarioEnLinea(String nombreUsuario) {
        usuariosEnLinea.add(nombreUsuario);
        System.out.println("Usuario en línea agregado: " + nombreUsuario + ". Total: " + usuariosEnLinea.size());
        actualizarTablaUsuarios();
        difundirMensaje("USER_CONNECTED:" + nombreUsuario, null);
    }

    public synchronized void removerUsuarioEnLinea(String nombreUsuario) {
        usuariosEnLinea.remove(nombreUsuario);
        System.out.println("Usuario en línea removido: " + nombreUsuario + ". Total: " + usuariosEnLinea.size());
        actualizarTablaUsuarios();
        difundirMensaje("USER_DISCONNECTED:" + nombreUsuario, null);
    }

    public synchronized boolean estaUsuarioEnLinea(String nombreUsuario) {
        return usuariosEnLinea.contains(nombreUsuario);
    }

    public synchronized Set<String> getUsuariosEnLinea() {
        return new HashSet<>(usuariosEnLinea);
    }

    public synchronized void difundirMensaje(String mensaje, ManejadorClienteProyecto emisor) {
        SwingUtilities.invokeLater(() -> {
            if (!mensaje.startsWith("USER_")) {
                chatArea.append(mensaje + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });

        for (ManejadorClienteProyecto cliente : clientes) {
            if (cliente != emisor) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }

    public synchronized void removerCliente(ManejadorClienteProyecto manejador) {
        clientes.remove(manejador);
    }

    public synchronized void agregarUsuarioActivoEnChat(String emisor, String receptor, String usuario) {
        String claveChat = generarClaveChat(emisor, receptor);
        usuariosActivosEnChats.computeIfAbsent(claveChat, k -> new HashSet<>()).add(usuario);
        System.out.println("Usuario " + usuario + " activo en chat " + claveChat);
    }

    public synchronized void removerUsuarioActivoEnChat(String emisor, String receptor, String usuario) {
        String claveChat = generarClaveChat(emisor, receptor);
        Set<String> usuariosActivos = usuariosActivosEnChats.get(claveChat);
        if (usuariosActivos != null) {
            usuariosActivos.remove(usuario);
            System.out.println("Usuario " + usuario + " dejó el chat " + claveChat);
            if (usuariosActivos.isEmpty()) {
                desactivarChat(emisor, receptor);
                usuariosActivosEnChats.remove(claveChat);
            }
        }
    }

    private String generarClaveChat(String emisor, String receptor) {
        String[] usuarios = new String[]{emisor, receptor};
        java.util.Arrays.sort(usuarios);
        return usuarios[0] + ":" + usuarios[1];
    }

    private void desactivarChat(String emisor, String receptor) {
        String url = "jdbc:mysql://localhost:3306/proyectoavanzada?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE mensajes_privados SET activo = FALSE WHERE (usuario_emisor = ? AND usuario_receptor = ?) OR (usuario_emisor = ? AND usuario_receptor = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emisor);
            stmt.setString(2, receptor);
            stmt.setString(3, receptor);
            stmt.setString(4, emisor);
            int filasAfectadas = stmt.executeUpdate();
            System.out.println("Chat entre " + emisor + " y " + receptor + " desactivado. Filas afectadas: " + filasAfectadas);
        } catch (SQLException e) {
            System.out.println("Error al desactivar chat: " + e.getMessage());
        }
    }

    public synchronized CopyOnWriteArrayList<ManejadorClienteProyecto> getClientes() {
        return clientes;
    }

    public static void main(String[] args) {
        ServidorProyecto servidor = new ServidorProyecto();
        servidor.iniciar();
    }
}