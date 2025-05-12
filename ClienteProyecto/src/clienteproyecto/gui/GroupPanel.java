package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public class GroupPanel extends JPanel {
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JButton createGroupButton;
    private final JButton leaveGroupButton;
    private final JButton backButton;
    private final JList<String> membersList;
    private final DefaultListModel<String> membersModel;
    private final JButton inviteButton;
    private final JList<String> groupsList;
    private final DefaultListModel<String> groupsListModel;
    private final JLabel memberCountLabel;
    private String currentGroupId;
    
    private final JDialog createGroupDialog;
    private final JTextField groupNameField;
    private final JList<String> onlineUsersList;
    private final DefaultListModel<String> onlineUsersModel;
    private final JButton dialogCreateButton;
  
    private BiConsumer<String, String[]> onCreateGroupConfirmed;
    private final JList<String> invitationsList;
    private final DefaultListModel<String> invitationsModel;
    private final JButton acceptInviteButton;
    private final JButton rejectInviteButton;
    private JPanel contentPanel; 

    public GroupPanel() {
        setLayout(new BorderLayout());
        
        chatArea = new JTextArea();
        messageField = new JTextField();
        sendButton = new JButton("Enviar");
        createGroupButton = new JButton("Crear Grupo");
        leaveGroupButton = new JButton("Salir del Grupo");
        backButton = new JButton("Volver");
        membersModel = new DefaultListModel<>();
        membersList = new JList<>(membersModel);
        inviteButton = new JButton("Invitar Usuario");
        groupsListModel = new DefaultListModel<>();
        groupsList = new JList<>(groupsListModel);
        memberCountLabel = new JLabel("Miembros: 0");
        
        onlineUsersModel = new DefaultListModel<>();
        onlineUsersList = new JList<>(onlineUsersModel);
        onlineUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        createGroupDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Crear Grupo", true);
        groupNameField = new JTextField(20);
        dialogCreateButton = new JButton("Crear");

        invitationsModel = new DefaultListModel<>();
        invitationsList = new JList<>(invitationsModel);
        acceptInviteButton = new JButton("Aceptar");
        rejectInviteButton = new JButton("Rechazar");

        setupDialog();
        setupMainPanel();
    }

    private void setupDialog() {
        createGroupDialog.setSize(400, 400);
        createGroupDialog.setLocationRelativeTo(null);
        
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("Nombre del Grupo:"), BorderLayout.WEST);
        namePanel.add(groupNameField, BorderLayout.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(onlineUsersList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Usuarios en línea"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancelar");
        buttonPanel.add(dialogCreateButton);
        buttonPanel.add(cancelButton);
        
        dialogPanel.add(namePanel, BorderLayout.NORTH);
        dialogPanel.add(scrollPane, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        createGroupDialog.add(dialogPanel);
        
        cancelButton.addActionListener(e -> createGroupDialog.setVisible(false));
    }

    private void setupMainPanel() {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        
        topPanel.add(createGroupButton);
        add(topPanel, BorderLayout.NORTH);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.setBorder(BorderFactory.createTitledBorder("Grupos"));
        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsPanel.add(new JScrollPane(groupsList), BorderLayout.CENTER);
        leftPanel.add(groupsPanel, BorderLayout.CENTER);
        
        createGroupButton.addActionListener(e -> {
            String groupName = JOptionPane.showInputDialog(this, 
                "Ingrese el nombre del grupo:", 
                "Crear Grupo", 
                JOptionPane.PLAIN_MESSAGE);
                
            if (groupName != null && !groupName.trim().isEmpty()) {
                JDialog selectUsersDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Seleccionar Usuarios", true);
                selectUsersDialog.setLayout(new BorderLayout());
                selectUsersDialog.setSize(300, 400);
                selectUsersDialog.setLocationRelativeTo(this);
                
                JList<String> userSelectionList = new JList<>(onlineUsersModel);
                userSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                
                JLabel instructionLabel = new JLabel("Seleccione al menos 2 usuarios (Ctrl+clic para selección múltiple)");
                instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                JButton confirmButton = new JButton("Crear Grupo");
                confirmButton.addActionListener(ev -> {
                    List<String> selectedUsers = userSelectionList.getSelectedValuesList();
                    if (selectedUsers.size() < 2) {
                        JOptionPane.showMessageDialog(selectUsersDialog,
                            "Seleccione al menos 2 usuarios",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (onCreateGroupConfirmed != null) {
                        onCreateGroupConfirmed.accept(groupName.trim(), selectedUsers.toArray(new String[0]));
                    }
                    selectUsersDialog.dispose();
                });
                
                selectUsersDialog.add(instructionLabel, BorderLayout.NORTH);
                selectUsersDialog.add(new JScrollPane(userSelectionList), BorderLayout.CENTER);
                selectUsersDialog.add(confirmButton, BorderLayout.SOUTH);
                
                selectUsersDialog.setVisible(true);
            }
        });
        
        contentPanel = new JPanel(new BorderLayout());
        
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JPanel membersPanel = new JPanel(new BorderLayout());
        membersPanel.setBorder(BorderFactory.createTitledBorder("Miembros"));
        membersPanel.add(new JScrollPane(membersList), BorderLayout.CENTER);
        membersPanel.add(memberCountLabel, BorderLayout.NORTH);
        
        JPanel memberButtonsPanel = new JPanel(new FlowLayout());
        memberButtonsPanel.add(inviteButton);
        memberButtonsPanel.add(leaveGroupButton);
        membersPanel.add(memberButtonsPanel, BorderLayout.SOUTH);
        
        contentPanel.add(chatPanel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
        contentPanel.setVisible(false);
        
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(contentPanel);
        
        // Update invitations panel setup
        JPanel invitationsPanel = new JPanel(new BorderLayout());
        invitationsPanel.setBorder(BorderFactory.createTitledBorder("Invitaciones Pendientes"));
        
        // Make sure invitationsList uses invitationsModel
        invitationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invitationsPanel.add(new JScrollPane(invitationsList), BorderLayout.CENTER);
        
        JPanel inviteButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inviteButtonsPanel.add(acceptInviteButton);
        inviteButtonsPanel.add(rejectInviteButton);
        invitationsPanel.add(inviteButtonsPanel, BorderLayout.SOUTH);
        
        // Add invitations panel to main layout
        leftPanel.add(invitationsPanel, BorderLayout.SOUTH);
        
        add(mainSplit, BorderLayout.CENTER);
        
        groupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = groupsList.getSelectedValue();
                if (selected != null) {
                    String groupId = selected.split(" - ")[0].trim();
                    setCurrentGroupId(groupId);
                    contentPanel.setVisible(true);
                    revalidate();
                    repaint();
                } else {
                    contentPanel.setVisible(false);
                }
            }
        });
    }

    public JButton getCreateGroupButton() { 
        return createGroupButton; 
    }

    public JButton getLeaveGroupButton() { 
        return leaveGroupButton; 
    }

    public JButton getInviteButton() { 
        return inviteButton; 
    }

    public JButton getSendButton() { 
        return sendButton; 
    }

    public JTextField getGroupNameField() { 
        return groupNameField; 
    }

    public JTextField getMessageField() { 
        return messageField; 
    }

    public JTextArea getChatArea() { 
        return chatArea; 
    }

    public JList<String> getMembersList() { 
        return membersList; 
    }

    public DefaultListModel<String> getMembersModel() { 
        return membersModel; 
    }

    public String getCurrentGroupId() {
        return currentGroupId;
    }

    public void setCurrentGroupId(String groupId) {
        this.currentGroupId = groupId;
    }

    public void updateGroupList(String[] groups) {
        membersModel.clear();
        for (String group : groups) {
            membersModel.addElement(group);
        }
    }

    public void setOnBack(Consumer<Void> onBack) {
        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.accept(null);
            }
        });
    }
    
    public JButton getBackButton() {
        return backButton;
    }

    public void updateGroupsList(String[] groups) {
        SwingUtilities.invokeLater(() -> {
            groupsListModel.clear();
            if (groups != null) {
                for (String group : groups) {
                    groupsListModel.addElement(group);
                }
            }
        });
    }

    public void updateMembersCount(int count) {
        memberCountLabel.setText("Miembros: " + count + "/3 (mínimo)");
        memberCountLabel.setForeground(count < 3 ? Color.RED : Color.BLACK);
    }

    public JList<String> getGroupsList() {
        return groupsList;
    }

    public void updateMembersList(String[] members) {
        membersModel.clear();
        for (String member : members) {
            membersModel.addElement(member);
        }
        updateMembersCount(members.length);
    }

    public void setOnCreateGroup(Consumer<String> handler) {
        createGroupButton.addActionListener(e -> {
            String groupName = JOptionPane.showInputDialog("Nombre del grupo:");
            if (groupName != null && !groupName.trim().isEmpty()) {
                handler.accept(groupName.trim());
            }
        });
    }

    public void setOnBack(Runnable handler) {
        backButton.addActionListener(e -> handler.run());
    }

    public void loadGroupMembers(String groupId) {
        membersModel.clear();
        setCurrentGroupId(groupId);
    }

    public void setGroupSelectionListener(Consumer<String> handler) {
        groupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = groupsList.getSelectedValue();
                if (selected != null) {
                    String groupId = selected.split(" - ")[0].trim();
                    handler.accept(groupId);
                }
            }
        });
    }

    public void setInviteButton(Consumer<String> handler) {
        inviteButton.addActionListener(e -> {
            if (currentGroupId != null) {
                String invitedUser = JOptionPane.showInputDialog("Ingrese el nombre de usuario a invitar:");
                if (invitedUser != null && !invitedUser.trim().isEmpty()) {
                    handler.accept(invitedUser.trim());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un grupo primero.");
            }
        });
    }

    public void setLeaveButton(Consumer<String> handler) {
        leaveGroupButton.addActionListener(e -> {
            if (currentGroupId != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro que desea salir del grupo?",
                    "Confirmar salida",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    handler.accept(currentGroupId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un grupo primero.");
            }
        });
    }

    public void updateInvitationsList(String[] invitations) {
        SwingUtilities.invokeLater(() -> {
            invitationsModel.clear();
            if (invitations != null && invitations.length > 0) {
                for (String invitation : invitations) {
                    invitationsModel.addElement(invitation);
                }
                System.out.println("Added " + invitations.length + " invitations");
            }
        });
    }

    public void setOnAcceptInvite(Consumer<String> handler) {
        acceptInviteButton.addActionListener(e -> {
            String selected = invitationsList.getSelectedValue();
            if (selected != null) {
                String groupId = selected.split(" - ")[0].trim();
                handler.accept(groupId);
            }
        });
    }

    public void setOnRejectInvite(Consumer<String> handler) {
        rejectInviteButton.addActionListener(e -> {
            String selected = invitationsList.getSelectedValue();
            if (selected != null) {
                String groupId = selected.split(" - ")[0].trim();
                handler.accept(groupId);
            }
        });
    }

    public void updateOnlineUsers(String[] users) {
        SwingUtilities.invokeLater(() -> {
            onlineUsersModel.clear();
            for (String user : users) {
                onlineUsersModel.addElement(user);
            }
            System.out.println("GroupPanel: Updated online users list with " + users.length + " users");
        });
    }

    public void showCreateGroupDialog() {
        createGroupDialog.setVisible(true);
    }

    public String[] getSelectedUsers() {
        return onlineUsersList.getSelectedValuesList().toArray(new String[0]);
    }

    public void setCreateGroupButton(Consumer<Void> handler) {
        createGroupButton.addActionListener(e -> {
            String[] selectedUsers = getSelectedUsers();
            if (selectedUsers.length < 2) {
                JOptionPane.showMessageDialog(this, 
                    "Debes seleccionar al menos 2 usuarios para crear un grupo",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            handler.accept(null);
        });
    }

    public String getGroupName() {
        return groupNameField.getText().trim();
    }

    public void setOnCreateGroupConfirmed(BiConsumer<String, String[]> handler) {
        this.onCreateGroupConfirmed = handler; // Store the handler
        
        createGroupButton.addActionListener(e -> {
            String groupName = JOptionPane.showInputDialog(this, 
                "Ingrese el nombre del grupo:", 
                "Crear Grupo", 
                JOptionPane.PLAIN_MESSAGE);
                
            if (groupName != null && !groupName.trim().isEmpty()) {
                JDialog selectUsersDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Seleccionar Usuarios", true);
                selectUsersDialog.setLayout(new BorderLayout());
                selectUsersDialog.setSize(300, 400);
                selectUsersDialog.setLocationRelativeTo(this);
                
                JList<String> userSelectionList = new JList<>(onlineUsersModel);
                userSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                
                JLabel instructionLabel = new JLabel("Seleccione al menos 2 usuarios (Ctrl+clic)");
                instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                JButton confirmButton = new JButton("Crear");
                confirmButton.addActionListener(ev -> {
                    List<String> selectedUsers = userSelectionList.getSelectedValuesList();
                    if (selectedUsers.size() < 2) {
                        JOptionPane.showMessageDialog(selectUsersDialog,
                            "Seleccione al menos 2 usuarios",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Call the stored handler with group name and selected users
                    onCreateGroupConfirmed.accept(groupName.trim(), selectedUsers.toArray(new String[0]));
                    selectUsersDialog.dispose();
                });
                
                selectUsersDialog.add(instructionLabel, BorderLayout.NORTH);
                selectUsersDialog.add(new JScrollPane(userSelectionList), BorderLayout.CENTER);
                selectUsersDialog.add(confirmButton, BorderLayout.SOUTH);
                
                selectUsersDialog.setVisible(true);
            }
        });
    }

    public JList<String> getOnlineUsersList() {
        return onlineUsersList;
    }
}