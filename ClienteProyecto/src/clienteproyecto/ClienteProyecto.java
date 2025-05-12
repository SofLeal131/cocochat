package clienteproyecto;

import clienteproyecto.controller.ClientController;
import clienteproyecto.gui.MainFrame;
import clienteproyecto.network.ServerConnection;

public class ClienteProyecto {
    public static void main(String[] args) {
        // Crear componentes principales
        ServerConnection serverConnection = new ServerConnection();
        MainFrame mainFrame = new MainFrame();
        ClientController controller = new ClientController(mainFrame, serverConnection);
        
        // Set controller in mainFrame after creation
        mainFrame.setController(controller);

        // Iniciar la aplicación
        controller.start();
    }
}