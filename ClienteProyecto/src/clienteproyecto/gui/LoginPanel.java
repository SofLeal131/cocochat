package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    private final JTextField txtUsuarioInicio;
    private final JPasswordField txtContrasenaInicio;
    private BiConsumer<String, String> onLogin;
    private Consumer<Void> onBack;
    private Consumer<Void> onForgotPassword;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblUsuarioInicio = new JLabel("Nombre de usuario:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblUsuarioInicio, gbc);

        txtUsuarioInicio = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtUsuarioInicio, gbc);

        JLabel lblContrasenaInicio = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblContrasenaInicio, gbc);

        txtContrasenaInicio = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtContrasenaInicio, gbc);

        JButton btnIniciarSesion = new JButton("Iniciar Sesión");
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(btnIniciarSesion, gbc);

        JButton btnVolverInicioSesion = new JButton("Volver");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(btnVolverInicioSesion, gbc);

        JButton btnOlvidasteContrasena = new JButton("Olvidaste tu contraseña?");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(btnOlvidasteContrasena, gbc);

        btnIniciarSesion.addActionListener(e -> {
            if (onLogin != null) {
                onLogin.accept(txtUsuarioInicio.getText(), new String(txtContrasenaInicio.getPassword()));
            }
        });

        btnVolverInicioSesion.addActionListener(e -> {
            if (onBack != null) onBack.accept(null);
        });

        btnOlvidasteContrasena.addActionListener(e -> {
            if (onForgotPassword != null) onForgotPassword.accept(null);
        });
    }

    public void clearFields() {
        txtUsuarioInicio.setText("");
        txtContrasenaInicio.setText("");
    }

    public void setOnLogin(BiConsumer<String, String> onLogin) {
        this.onLogin = onLogin;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }

    public void setOnForgotPassword(Consumer<Void> onForgotPassword) {
        this.onForgotPassword = onForgotPassword;
    }
}