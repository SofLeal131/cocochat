package clienteproyecto.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatPanel extends JPanel {
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton btnSend;
    private final JButton btnBack;
    private final JLabel lblChattingWith;
    private BiConsumer<String, String> onSendMessage;
    private Consumer<Void> onBack;

    public ChatPanel() {
        setLayout(new BorderLayout(10, 10));

        // Header
        lblChattingWith = new JLabel("Chateando con: ");
        add(lblChattingWith, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        btnSend = new JButton("Enviar");
        btnBack = new JButton("Volver");

        // Add action listeners
        btnSend.addActionListener(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() && onSendMessage != null) {
                String recipient = lblChattingWith.getText().replace("Chateando con: ", "");
                onSendMessage.accept(message, recipient);
                messageField.setText("");
            }
        });

        btnBack.addActionListener(e -> {
            if (onBack != null) {
                onBack.accept(null);
            }
        });

        // Layout input panel
        inputPanel.add(btnBack, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void setChattingWith(String username) {
        lblChattingWith.setText("Chateando con: " + username);
    }

    public void appendMessage(String sender, String message, String timestamp) {
        chatArea.append("[" + timestamp + "] " + sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void loadChatHistory(String history) {
        chatArea.setText("");
        if (!history.equals("Sin mensajes.")) {
            String[] messages = history.split(";");
            for (String msg : messages) {
                String[] parts = msg.split(":", 3);
                if (parts.length == 3) {
                    appendMessage(parts[0], parts[1], parts[2]);
                }
            }
        }
    }

    public void setOnSendMessage(BiConsumer<String, String> onSendMessage) {
        this.onSendMessage = onSendMessage;
    }

    public void setOnBack(Consumer<Void> onBack) {
        this.onBack = onBack;
    }
}