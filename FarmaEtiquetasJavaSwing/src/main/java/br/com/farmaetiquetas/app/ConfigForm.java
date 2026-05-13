package br.com.farmaetiquetas.app;

import javax.swing.*;
import java.awt.*;

public class ConfigForm extends JFrame {

    private static final String SENHA_ADMIN = "farma2024";

    private final JTextField     hostField    = new JTextField();
    private final JTextField     portaField   = new JTextField();
    private final JTextField     bancoField   = new JTextField();
    private final JTextField     usuarioField = new JTextField();
    private final JPasswordField senhaField   = new JPasswordField();
    private final JTextField     apiUrlField  = new JTextField();
    private final JPasswordField apiKeyField  = new JPasswordField();

    private final AppConfig config;

    public ConfigForm(AppConfig config) {
        super("Configuracoes");
        this.config = config;

        setSize(440, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JLabel lblBanco = new JLabel("-- Banco de Dados --");
        lblBanco.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = y; c.gridwidth = 2;
        add(lblBanco, c); c.gridwidth = 1;

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Host:"), c);
        c.gridx = 1; c.weightx = 1.0; add(hostField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Porta:"), c);
        c.gridx = 1; add(portaField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Banco:"), c);
        c.gridx = 1; add(bancoField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Usuario BD:"), c);
        c.gridx = 1; add(usuarioField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Senha BD:"), c);
        c.gridx = 1; add(senhaField, c);

        y++;
        JLabel lblApi = new JLabel("-- API Spring Boot --");
        lblApi.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = y; c.gridwidth = 2;
        add(lblApi, c); c.gridwidth = 1;

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("URL da API:"), c);
        c.gridx = 1; add(apiUrlField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("API Key:"), c);
        c.gridx = 1; add(apiKeyField, c);

        y++;
        JButton salvar = new JButton("Salvar");
        salvar.addActionListener(e -> salvarConfig());
        c.gridy = y; c.gridx = 1;
        add(salvar, c);

        carregarCampos();
    }

    private void carregarCampos() {
        hostField.setText(config.dbHost);
        portaField.setText(config.dbPorta);
        bancoField.setText(config.dbBanco);
        usuarioField.setText(config.dbUsuario);
        senhaField.setText(config.dbSenha);
        apiUrlField.setText(config.apiUrl);
        apiKeyField.setText(config.apiKey);
    }

    private void salvarConfig() {
        config.dbHost    = hostField.getText().trim();
        config.dbPorta   = portaField.getText().trim();
        config.dbBanco   = bancoField.getText().trim();
        config.dbUsuario = usuarioField.getText().trim();
        config.dbSenha   = new String(senhaField.getPassword());
        config.apiUrl    = apiUrlField.getText().trim();
        config.apiKey    = new String(apiKeyField.getPassword());
        config.salvar();

        JOptionPane.showMessageDialog(this, "Configuracoes salvas com sucesso!");
        dispose();
    }

    public static void open(AppConfig config) {
        JPasswordField senhaInput = new JPasswordField();
        int opcao = JOptionPane.showConfirmDialog(
                null, senhaInput,
                "Digite a senha de administrador:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (opcao != JOptionPane.OK_OPTION) return;

        if (!new String(senhaInput.getPassword()).equals(SENHA_ADMIN)) {
            JOptionPane.showMessageDialog(null, "Senha incorreta. Acesso negado.",
                    "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new ConfigForm(config).setVisible(true);
    }
}