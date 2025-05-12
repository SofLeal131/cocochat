package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegisterPanel extends JPanel {
    private final JTextField txtUsuarioRegistro;
    private final JPasswordField txtContrasenaRegistro;
    private BiConsumer<String, String> onRegister;
    private Consumer<Void> onBack;

    public RegisterPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblUsuarioRegistro = new JLabel("Nombre de usuario:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblUsuarioRegistro, gbc);

        txtUsuarioRegistro = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtUsuarioRegistro, gbc);

        JLabel lblContrasenaRegistro = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblContrasenaRegistro, gbc);

        txtContrasenaRegistro = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtContrasenaRegistro, gbc);

        JButton btnRegistrar = new JButton("Registrar");
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(btnRegistrar, gbc);

        JButton btnVolverRegistro = new JButton("Volver");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(btnVolverRegistro, gbc);

        btnRegistrar.addActionListener(e -> {
            if (onRegister != null) {
                onRegister.accept(txtUsuarioRegistro.getText(), new String(txtContrasenaRegistro.getPassword()));
            }
        });

        btnVolverRegistro.addActionListener(e -> {
            if (onBack != null) onBack.accept(null);
        });
    }

    public void clearFields() {
        txtUsuarioRegistro.setText("");
        txtContrasenaRegistro.setText("");
    }

    public void setOnRegister(BiConsumer<String, String> onRegister) {
        this.onRegister = onRegister;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }
}