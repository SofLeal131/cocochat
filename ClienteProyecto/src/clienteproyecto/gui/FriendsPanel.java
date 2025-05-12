package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FriendsPanel extends JPanel {
    private final DefaultListModel<String> listModel;
    private final JList<String> friendsList;
    private final Map<String, String> nicknames;
    private final Map<String, String> usernameToDisplay;
    private BiConsumer<String, String> onSetNickname;
    private Consumer<String> onFriendSelected;
    private Consumer<Void> onBack;

    public FriendsPanel() {
        setLayout(new BorderLayout(10, 10));
        nicknames = new HashMap<>();
        usernameToDisplay = new HashMap<>();

        // Label
        JLabel lblFriends = new JLabel("Amigos:");
        add(lblFriends, BorderLayout.NORTH);

        // List of friends
        listModel = new DefaultListModel<>();
        friendsList = new JList<>(listModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsList.setCellRenderer(new FriendCellRenderer());
        JScrollPane scrollPane = new JScrollPane(friendsList);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel with Start Chat, Nickname, and Back buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton chatButton = new JButton("Iniciar Chat");
        chatButton.addActionListener(e -> {
            String selectedDisplay = friendsList.getSelectedValue();
            if (selectedDisplay == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un amigo.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Find the username corresponding to the displayed nickname
            String username = usernameToDisplay.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(selectedDisplay))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(selectedDisplay);
            if (onFriendSelected != null) {
                onFriendSelected.accept(username);
            }
        });
        buttonPanel.add(chatButton);

        JButton nicknameButton = new JButton("Cambiar Apodo");
        nicknameButton.addActionListener(e -> {
            String selectedDisplay = friendsList.getSelectedValue();
            if (selectedDisplay == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un amigo.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Find the username corresponding to the displayed nickname
            String username = usernameToDisplay.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(selectedDisplay))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(selectedDisplay);
            showNicknameModal(username);
        });
        buttonPanel.add(nicknameButton);

        JButton backButton = new JButton("Volver");
        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.accept(null);
            }
        });
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showNicknameModal(String username) {
        String currentNickname = nicknames.getOrDefault(username, "");
        
        // Create a modal dialog
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog nicknameDialog;
        if (window instanceof Frame) {
            nicknameDialog = new JDialog((Frame) window, "Cambiar Apodo", true);
        } else {
            nicknameDialog = new JDialog((Frame) null, "Cambiar Apodo", true);
            nicknameDialog.setLocationRelativeTo(this);
        }
        nicknameDialog.setLayout(new BorderLayout(10, 10));
        nicknameDialog.setSize(300, 150);
        nicknameDialog.setLocationRelativeTo(this);

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel label = new JLabel("Ingresa un apodo para " + username + ":");
        JTextField nicknameField = new JTextField(currentNickname, 20);
        inputPanel.add(label);
        inputPanel.add(nicknameField);
        nicknameDialog.add(inputPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            String newNickname = nicknameField.getText().trim();
            if (newNickname.isEmpty()) {
                JOptionPane.showMessageDialog(nicknameDialog, "El apodo no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (onSetNickname != null) {
                onSetNickname.accept(username, newNickname);
            }
            nicknameDialog.dispose();
        });

        cancelButton.addActionListener(e -> nicknameDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        nicknameDialog.add(buttonPanel, BorderLayout.SOUTH);

        nicknameDialog.setVisible(true);
    }

    public void updateFriends(String[] friends, Map<String, String> nicknameMap) {
        System.out.println("Actualizando amigos: " + Arrays.toString(friends) + ", apodos: " + nicknameMap);
        listModel.clear();
        nicknames.clear();
        usernameToDisplay.clear();
        nicknames.putAll(nicknameMap);
        for (String friend : friends) {
            String display = nicknames.containsKey(friend) ? nicknames.get(friend) : friend;
            System.out.println("Mostrando amigo: " + friend + " como: " + display);
            listModel.addElement(display);
            usernameToDisplay.put(friend, display);
            friendsList.repaint(); // Forzar actualización de la UI
        }
    }

    public void setOnSetNickname(BiConsumer<String, String> onSetNickname) {
        this.onSetNickname = onSetNickname;
    }

    public void setOnFriendSelected(Consumer<String> onFriendSelected) {
        this.onFriendSelected = onFriendSelected;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }

    // Custom cell renderer for friend list
    private class FriendCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(value.toString());
            return label;
        }
    }
}