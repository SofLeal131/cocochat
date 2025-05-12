package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChangePasswordPanel extends JPanel {
    private final JTextField txtUsuarioCambiar;
    private final JPasswordField txtNuevaContrasena;
    private BiConsumer<String, String> onChangePassword;
    private Consumer<Void> onBack;

    public ChangePasswordPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblUsuarioCambiar = new JLabel("Nombre de usuario:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblUsuarioCambiar, gbc);

        txtUsuarioCambiar = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtUsuarioCambiar, gbc);

        JLabel lblNuevaContrasena = new JLabel("Nueva contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblNuevaContrasena, gbc);

        txtNuevaContrasena = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtNuevaContrasena, gbc);

        JButton btnCambiarContrasena = new JButton("Cambiar contraseña");
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(btnCambiarContrasena, gbc);

        JButton btnVolverCambiar = new JButton("Volver");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(btnVolverCambiar, gbc);

        btnCambiarContrasena.addActionListener(e -> {
            if (onChangePassword != null) {
                onChangePassword.accept(txtUsuarioCambiar.getText(), new String(txtNuevaContrasena.getPassword()));
            }
        });

        btnVolverCambiar.addActionListener(e -> {
            if (onBack != null) onBack.accept(null);
        });
    }

    public void clearFields() {
        txtUsuarioCambiar.setText("");
        txtNuevaContrasena.setText("");
    }

    public void setOnChangePassword(BiConsumer<String, String> onChangePassword) {
        this.onChangePassword = onChangePassword;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }
}