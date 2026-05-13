package br.com.farmaetiquetas.app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class PosologiaForm extends JFrame {

    private static final Color AZUL       = new Color(26, 82, 160);
    private static final Color AZUL_HOVER = new Color(20, 65, 135);
    private static final Color FUNDO      = new Color(240, 242, 247);
    private static final Color CAMPO_BG   = new Color(248, 249, 252);
    private static final Color BORDA      = new Color(210, 215, 225);
    private static final Color LABEL_COR  = new Color(60, 70, 90);

    private final CampoTexto pacienteField = new CampoTexto();
    private final AreaTexto  posologiaArea = new AreaTexto();
    private final CampoTexto saidaField    = new CampoTexto();

    public PosologiaForm() {
        super("Gerador de Etiqueta - Posologia");
        saidaField.setText("C:/Etiquetas");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // AJUSTE: Altura aumentada de 480 para 530 para dar respiro aos campos maiores
        setSize(500, 530);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(FUNDO);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lblTitulo = new JLabel("GERADOR DE ETIQUETA - POSOLOGIA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Etiqueta de posologia do paciente");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(180, 210, 255));

        JPanel headerTxt = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTxt.setOpaque(false);
        headerTxt.add(lblTitulo);
        headerTxt.add(lblSub);

        // Icone pilula
        JPanel iconeHeader = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.setColor(new Color(255,255,255,50));
                g2.fillOval(cx-18, cy-18, 36, 36);
                g2.setColor(new Color(255,255,255,200));
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Pilula
                g2.drawOval(cx-10, cy-5, 20, 10);
                g2.drawLine(cx, cy-5, cx, cy+5);
                // Cruz
                g2.drawLine(cx-3, cy-13, cx+3, cy-13);
                g2.drawLine(cx, cy-16, cx, cy-10);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(48, 48); }
        };
        iconeHeader.setOpaque(false);
        header.add(headerTxt, BorderLayout.CENTER);
        header.add(iconeHeader, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Paciente:"), c);

        c.gridy = 1; c.insets = new Insets(0, 0, 14, 0);
        card.add(pacienteField, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Posologia:"), c);

        c.gridy = 3; c.insets = new Insets(0, 0, 14, 0);
        c.fill = GridBagConstraints.BOTH; c.weighty = 1.0;
        card.add(posologiaArea, c);
        c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0;

        c.gridy = 4; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Pasta de Saida:"), c);

        c.gridy = 5; c.insets = new Insets(0, 0, 22, 0);
        card.add(saidaField, c);

        PedidoForm.BotaoRound btnGerar = new PedidoForm.BotaoRound("GERAR PDF", AZUL, AZUL_HOVER);
        btnGerar.addActionListener(e -> gerar());
        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        card.add(btnGerar, c);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FUNDO);
        wrapper.setBorder(new EmptyBorder(18, 18, 18, 18));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(LABEL_COR);
        return l;
    }

    private void gerar() {
        String paciente  = pacienteField.getText().trim();
        String posologia = posologiaArea.getText().trim();
        String pasta     = saidaField.getText().trim();

        if (paciente.isEmpty() || posologia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
            return;
        }

        try {
            new File(pasta).mkdirs();
            String arq = PdfLabelGenerator.generateEtiquetaPosologia(paciente, posologia, pasta);
            File pdf = new File(arq);
            if (Desktop.isDesktopSupported() && pdf.exists()) {
                try { Desktop.getDesktop().open(pdf); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Arquivo: " + pdf.getAbsolutePath()); }
            }
            JOptionPane.showMessageDialog(this, "PDF gerado com sucesso!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Campo texto simples arredondado
    static class CampoTexto extends JPanel {
        private final JTextField field = new JTextField();
        private boolean foc = false;

        CampoTexto() {
            setLayout(new BorderLayout());
            setOpaque(false);
            // AJUSTE: Altura de 44 para 50
            setPreferredSize(new Dimension(0, 50));
            field.setOpaque(false);
            // AJUSTE: Adicionado 4px de respiro vertical (margem interna)
            field.setBorder(new EmptyBorder(4, 12, 4, 12));
            // AJUSTE: Fonte de 13 para 15
            field.setFont(new Font("SansSerif", Font.PLAIN, 15));
            field.setForeground(new Color(30, 40, 60));
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { foc = true; repaint(); }
                public void focusLost(FocusEvent e)   { foc = false; repaint(); }
            });
            add(field, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CAMPO_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            g2.setColor(foc ? AZUL : BORDA);
            g2.setStroke(new BasicStroke(foc ? 1.8f : 1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }

        public String getText() { return field.getText(); }
        public void setText(String t) { field.setText(t); }
    }

    // Area de texto arredondada
    static class AreaTexto extends JPanel {
        private final JTextArea area = new JTextArea(5, 20);
        private boolean foc = false;

        AreaTexto() {
            setLayout(new BorderLayout());
            setOpaque(false);
            // AJUSTE: Altura de 100 para 120, para compensar o aumento da fonte
            setPreferredSize(new Dimension(0, 120));
            area.setOpaque(false);
            // AJUSTE: Fonte de 13 para 15
            area.setFont(new Font("SansSerif", Font.PLAIN, 15));
            area.setForeground(new Color(30, 40, 60));
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            // AJUSTE: Leve aumento na margem vertical
            area.setBorder(new EmptyBorder(10, 12, 10, 12));
            area.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { foc = true; repaint(); }
                public void focusLost(FocusEvent e)   { foc = false; repaint(); }
            });
            add(area, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CAMPO_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            g2.setColor(foc ? AZUL : BORDA);
            g2.setStroke(new BasicStroke(foc ? 1.8f : 1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }

        public String getText() { return area.getText(); }
    }

    public static void open() {
        new PosologiaForm().setVisible(true);
    }
}