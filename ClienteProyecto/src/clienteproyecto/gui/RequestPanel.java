package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RequestPanel extends JPanel {
    private final DefaultListModel<String> requestListModel;
    private final JList<String> requestList;
    private BiConsumer<String, String> onAcceptRequest;
    private BiConsumer<String, String> onRejectRequest;
    private Consumer<Void> onBack;

    public RequestPanel() {
        setLayout(new BorderLayout(10, 10));

        // Label
        JLabel lblRequests = new JLabel("Solicitudes de Amistad Pendientes:");
        add(lblRequests, BorderLayout.NORTH);

        // List of pending requests
        requestListModel = new DefaultListModel<>();
        requestList = new JList<>(requestListModel);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(requestList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton acceptButton = new JButton("Aceptar");
        JButton rejectButton = new JButton("Rechazar");
        JButton backButton = new JButton("Volver");

        acceptButton.addActionListener(e -> {
            String selectedSender = requestList.getSelectedValue();
            if (selectedSender != null && onAcceptRequest != null) {
                onAcceptRequest.accept(selectedSender, null);
            } else if (selectedSender == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una solicitud.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        rejectButton.addActionListener(e -> {
            String selectedSender = requestList.getSelectedValue();
            if (selectedSender != null && onRejectRequest != null) {
                onRejectRequest.accept(selectedSender, null);
            } else if (selectedSender == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una solicitud.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.accept(null);
            }
        });

        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateRequests(String[] senders) {
        requestListModel.clear();
        for (String sender : senders) {
            requestListModel.addElement(sender);
        }
    }

    public void setOnAcceptRequest(BiConsumer<String, String> onAcceptRequest) {
        this.onAcceptRequest = onAcceptRequest;
    }

    public void setOnRejectRequest(BiConsumer<String, String> onRejectRequest) {
        this.onRejectRequest = onRejectRequest;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }
}