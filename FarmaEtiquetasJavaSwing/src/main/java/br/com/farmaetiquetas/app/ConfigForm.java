package br.com.farmaetiquetas.app;

import javax.swing.*;
import java.awt.*;

public class ConfigForm extends JFrame {

    private final JTextField hostField = new JTextField();
    private final JTextField portaField = new JTextField();
    private final JTextField usuarioField = new JTextField();
    private final JPasswordField senhaField = new JPasswordField();

    private final AppConfig config;

    public ConfigForm(AppConfig config) {
        super("Configurações do Banco de Dados");
        this.config = config;

        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        c.gridx = 0; c.gridy = y; add(new JLabel("Host:"), c);
        c.gridx = 1; c.weightx = 1.0; add(hostField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Porta:"), c);
        c.gridx = 1; add(portaField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Usuário:"), c);
        c.gridx = 1; add(usuarioField, c);

        y++;
        c.gridx = 0; c.gridy = y; add(new JLabel("Senha:"), c);
        c.gridx = 1; add(senhaField, c);

        JButton salvar = new JButton("Salvar");
        salvar.addActionListener(e -> salvarConfig());

        c.gridy++; c.gridx = 1;
        add(salvar, c);

        carregarCampos();
    }

    private void carregarCampos() {
        hostField.setText(config.dbHost);
        portaField.setText(config.dbPorta);
        usuarioField.setText(config.dbUsuario);
        senhaField.setText(config.dbSenha);
    }

    private void salvarConfig() {
        config.dbHost = hostField.getText().trim();
        config.dbPorta = portaField.getText().trim();
        config.dbUsuario = usuarioField.getText().trim();
        config.dbSenha = new String(senhaField.getPassword());
        config.salvar();

        JOptionPane.showMessageDialog(this, "Configurações salvas com sucesso!");
        dispose();
    }

    public static void open(AppConfig config) {
        new ConfigForm(config).setVisible(true);
    }
}