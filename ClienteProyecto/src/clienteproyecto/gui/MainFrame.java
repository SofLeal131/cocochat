package clienteproyecto.gui;

import clienteproyecto.controller.ClientController;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private GroupPanel groupPanel;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final MainPanel initialPanel;
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final ChangePasswordPanel changePasswordPanel;
    private final HomePanel homePanel;
    private final ChatPanel chatPanel;
    private final FriendsPanel friendsPanel;
    private final RequestPanel requestPanel;
    private ClientController controller;
    private Map<String, JPanel> panels;

    public MainFrame() {
        super("Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        panels = new HashMap<>();
        
        initialPanel = new MainPanel();
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();
        changePasswordPanel = new ChangePasswordPanel();
        homePanel = new HomePanel();
        chatPanel = new ChatPanel(); 
        friendsPanel = new FriendsPanel();
        requestPanel = new RequestPanel();
        groupPanel = new GroupPanel();

        mainPanel.add(initialPanel, "Main");
        mainPanel.add(loginPanel, "InicioSesion");
        mainPanel.add(registerPanel, "Registro");
        mainPanel.add(changePasswordPanel, "CambiarContrasena");
        mainPanel.add(homePanel, "Home");
        mainPanel.add(chatPanel, "Chat");        
        mainPanel.add(friendsPanel, "Friends");
        mainPanel.add(requestPanel, "Requests");
        mainPanel.add(groupPanel, "Group");

        setContentPane(mainPanel);
    }

    public MainPanel getMainPanel() {
        return initialPanel;
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public ChangePasswordPanel getChangePasswordPanel() {
        return changePasswordPanel;
    }

    public HomePanel getHomePanel() {
        return homePanel;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public FriendsPanel getFriendsPanel() {
        return friendsPanel;
    }

    public RequestPanel getRequestPanel() {
        return requestPanel;
    }

    public GroupPanel getGroupPanel() {
        return groupPanel;
    }

    public void setController(ClientController controller) {
        this.controller = controller;
        
        groupPanel.getCreateGroupButton().addActionListener(e -> {
            String groupName = groupPanel.getGroupNameField().getText().trim();
            if (!groupName.isEmpty()) {
                controller.createGroup(groupName);
                groupPanel.getGroupNameField().setText("");
            } 
        });

        groupPanel.getInviteButton().addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Ingrese el nombre de usuario a invitar:");
            if (username != null && !username.trim().isEmpty()) {
                String currentGroupId = groupPanel.getCurrentGroupId();
                if (currentGroupId != null) {
                    controller.inviteToGroup(currentGroupId, username.trim());
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Por favor seleccione un grupo primero", "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        groupPanel.getLeaveGroupButton().addActionListener(e -> {
            String currentGroupId = groupPanel.getCurrentGroupId();
            if (currentGroupId != null) {
                controller.leaveGroup(currentGroupId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Por favor seleccione un grupo primero", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void showPanel(String panelName) {
        System.out.println("Switching to panel: " + panelName);
        cardLayout.show(mainPanel, panelName);
        revalidate();
        repaint();
    }

    public void display() {
        setVisible(true);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

   
    
    private void setupGroupPanel() {
        groupPanel = new GroupPanel();
        
        panels.put("Group", groupPanel);
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", 
                JOptionPane.ERROR_MESSAGE));
    }
}