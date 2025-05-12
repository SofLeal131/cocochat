package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MainPanel extends JPanel {
    private Consumer<Void> onRegister;
    private Consumer<Void> onLogin;

    public MainPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton btnMostrarRegistro = new JButton("Ir a Registro");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(btnMostrarRegistro, gbc);

        JButton btnMostrarInicioSesion = new JButton("Ir a Inicio de Sesión");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(btnMostrarInicioSesion, gbc);

        btnMostrarRegistro.addActionListener(e -> {
            if (onRegister != null) onRegister.accept(null);
        });

        btnMostrarInicioSesion.addActionListener(e -> {
            if (onLogin != null) onLogin.accept(null);
        });
    }

    public void setOnRegister(Consumer<Void> onRegister) {
        this.onRegister = onRegister;
    }

    public void setOnLogin(Consumer<Void> onLogin) {
        this.onLogin = onLogin;
    }
}