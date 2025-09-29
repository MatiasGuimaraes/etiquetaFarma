package br.com.farmaetiquetas.app;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PosologiaForm extends JFrame {

    private final JTextField pacienteField = new JTextField();
    private final JTextArea posologiaArea = new JTextArea(5, 20);
    private final JTextField saidaField = new JTextField("C:/Etiquetas");

    public PosologiaForm() {
        super("Gerador de Etiqueta - Posologia");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(480, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Paciente:"), c);
        c.gridx = 1; c.weightx = 1.0; form.add(pacienteField, c);
        y++;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Posologia:"), c);
        c.gridx = 1;
        JScrollPane scroll = new JScrollPane(posologiaArea);
        form.add(scroll, c);
        y++;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Saída:"), c);
        c.gridx = 1; form.add(saidaField, c);

        JButton gerar = new JButton("Gerar PDF");
        gerar.addActionListener(e -> gerar());

        add(form, BorderLayout.CENTER);
        add(gerar, BorderLayout.SOUTH);
    }

    private void gerar() {
        try {
            String paciente = pacienteField.getText().trim();
            String posologia = posologiaArea.getText().trim();
            String caminhoSaida = saidaField.getText().trim() + "/posologia_" + paciente + ".pdf";

            if (paciente.isEmpty() || posologia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return;
            }

            new File(saidaField.getText().trim()).mkdirs();
            PdfLabelGenerator.generateEtiquetaPosologia(paciente, posologia, caminhoSaida);

            // === Abre o PDF automaticamente ===
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(caminhoSaida));
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "PDF gerado, mas não foi possível abrir automaticamente.");
            }

            JOptionPane.showMessageDialog(this, "PDF gerado com sucesso!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void open() {
        new PosologiaForm().setVisible(true);
    }
}
