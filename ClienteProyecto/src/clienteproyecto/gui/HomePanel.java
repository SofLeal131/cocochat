package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class HomePanel extends JPanel {
    private final JList<String> userList;
    private final DefaultListModel<String> userListModel;
    private final JButton addFriendButton;
    private final JButton showFriendsButton;
    private final JButton showRequestsButton;
    private final JButton showGroupsButton; // Add this field
    private Consumer<String> onUserSelected;
    private Consumer<String> onAddFriend;
    private Consumer<Void> onShowFriends;
    private Consumer<Void> onShowRequests;
    private Consumer<Void> onShowGroups; // Add this field

    public HomePanel() {
        setLayout(new BorderLayout());

        // Lista de usuarios en línea
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                System.out.println("Usuario seleccionado en HomePanel: " + selectedUser);
                if (onUserSelected != null && selectedUser != null) {
                    onUserSelected.accept(selectedUser);
                }
            }
        });
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Usuarios en Línea"));

        // Botones
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        addFriendButton = new JButton("Agregar");
        addFriendButton.setFont(new Font("Arial", Font.BOLD, 14));
        addFriendButton.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            System.out.println("Botón Agregar clicado en HomePanel. Usuario seleccionado: " + selectedUser);
            if (selectedUser == null) {
                System.out.println("Error: No se seleccionó ningún usuario para agregar.");
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (onAddFriend != null) {
                System.out.println("Llamando onAddFriend con usuario: " + selectedUser);
                onAddFriend.accept(selectedUser);
            } else {
                System.out.println("Error: onAddFriend no está configurado.");
            }
        });

        showFriendsButton = new JButton("Ver Amigos");
        showFriendsButton.setFont(new Font("Arial", Font.BOLD, 14));
        showFriendsButton.addActionListener(e -> {
            System.out.println("Botón Ver Amigos clicado en HomePanel.");
            if (onShowFriends != null) {
                onShowFriends.accept(null);
            } else {
                System.out.println("Error: onShowFriends no está configurado.");
            }
        });

        showRequestsButton = new JButton("Ver Solicitudes");
        showRequestsButton.setFont(new Font("Arial", Font.BOLD, 14));
        showRequestsButton.addActionListener(e -> {
            System.out.println("Botón Ver Solicitudes clicado en HomePanel.");
            if (onShowRequests != null) {
                onShowRequests.accept(null);
            } else {
                System.out.println("Error: onShowRequests no está configurado.");
            }
        });

        showGroupsButton = new JButton("Grupos");
        showGroupsButton.setFont(new Font("Arial", Font.BOLD, 14));
        showGroupsButton.addActionListener(e -> {
            System.out.println("Botón Grupos presionado");
            if (onShowGroups != null) {
                onShowGroups.accept(null);
            }
        });

        buttonPanel.add(addFriendButton);
        buttonPanel.add(showFriendsButton);
        buttonPanel.add(showRequestsButton);
        buttonPanel.add(showGroupsButton); // Add groups button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(userScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }

    public void updateOnlineUsers(String[] users) {
        System.out.println("Actualizando usuarios en línea en HomePanel: " + String.join(", ", users));
        userListModel.clear();
        for (String user : users) {
            userListModel.addElement(user);
        }
        System.out.println("Lista de usuarios actualizada. Total: " + userListModel.size());
    }

    public void addUser(String username) {
        if (!userListModel.contains(username)) {
            System.out.println("Añadiendo usuario a HomePanel: " + username);
            userListModel.addElement(username);
        } else {
            System.out.println("Usuario ya está en la lista: " + username);
        }
    }

    public void removeUser(String username) {
        System.out.println("Eliminando usuario de HomePanel: " + username);
        userListModel.removeElement(username);
    }

    public void setOnUserSelected(Consumer<String> onUserSelected) {
        System.out.println("Configurando onUserSelected en HomePanel.");
        this.onUserSelected = onUserSelected;
    }

    public void setOnAddFriend(Consumer<String> onAddFriend) {
        System.out.println("Configurando onAddFriend en HomePanel.");
        this.onAddFriend = onAddFriend;
    }

    public void setOnShowFriends(Consumer<Void> onShowFriends) {
        System.out.println("Configurando onShowFriends en HomePanel.");
        this.onShowFriends = onShowFriends;
    }

    public void setOnShowRequests(Consumer<Void> onShowRequests) {
        System.out.println("Configurando onShowRequests en HomePanel.");
        this.onShowRequests = onShowRequests;
    }

    public void setOnShowGroups(Consumer<Void> onShowGroups) {
        System.out.println("Configuring onShowGroups in HomePanel.");
        this.onShowGroups = onShowGroups;
    }
}