package br.com.farmaetiquetas.app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainMenu {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ex) { ex.printStackTrace(); }
        }

        AppConfig config = new AppConfig();
        config.carregar();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FarmaEtiquetas");
            frame.setSize(480, 460);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(235, 238, 245));

            // Header azul
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(26, 82, 160));
            header.setBorder(new EmptyBorder(18, 24, 18, 24));

            JLabel titulo = new JLabel("FarmaEtiquetas");
            titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
            titulo.setForeground(Color.WHITE);

            JLabel sub = new JLabel("Sistema de gestao de etiquetas");
            sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
            sub.setForeground(new Color(180, 210, 255));

            JPanel headerTxt = new JPanel(new GridLayout(2, 1, 0, 3));
            headerTxt.setOpaque(false);
            headerTxt.add(titulo);
            headerTxt.add(sub);
            header.add(headerTxt, BorderLayout.CENTER);
            root.add(header, BorderLayout.NORTH);

            // Botoes
            JPanel panel = new JPanel(new GridLayout(4, 1, 0, 12));
            panel.setBackground(new Color(235, 238, 245));
            panel.setBorder(new EmptyBorder(22, 22, 22, 22));

            MenuBtn posBtn = new MenuBtn("Gerar Etiqueta - Posologia", "Nova posologia",
                    new Color(26, 82, 160), new Color(20, 65, 135), MenuBtn.PILL);
            posBtn.addActionListener(e -> PosologiaForm.open());

            MenuBtn pedBtn = new MenuBtn("Gerar Etiqueta - Pedido", "Novo pedido",
                    new Color(22, 120, 80), new Color(17, 95, 63), MenuBtn.CLIPBOARD);
            pedBtn.addActionListener(e -> PedidoForm.open(config));

            MenuBtn freBtn = new MenuBtn("Consultar Frete / Endereco", "Calcular frete",
                    new Color(50, 160, 70), new Color(38, 130, 55), MenuBtn.TRUCK);
            freBtn.addActionListener(e -> FreteForm.open(config));

            MenuBtn cfgBtn = new MenuBtn("Configuracoes", "Preferencias do sistema",
                    new Color(110, 118, 135), new Color(88, 95, 110), MenuBtn.GEAR);
            cfgBtn.addActionListener(e -> ConfigForm.open(config));

            panel.add(posBtn);
            panel.add(pedBtn);
            panel.add(freBtn);
            panel.add(cfgBtn);

            root.add(panel, BorderLayout.CENTER);
            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }

    // Botao de menu com icone vetorial
    public static class MenuBtn extends JButton {
        public static final int PILL = 0, CLIPBOARD = 1, TRUCK = 2, GEAR = 3;

        private final String titulo, sub;
        private final Color base, hov;
        private final int icon;
        private boolean ov = false;

        public MenuBtn(String titulo, String sub, Color base, Color hov, int icon) {
            this.titulo = titulo; this.sub = sub;
            this.base = base; this.hov = hov; this.icon = icon;
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 72));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { ov = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { ov = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(new Color(0, 0, 0, 22));
            g2.fill(new RoundRectangle2D.Float(2, 4, w - 3, h - 4, 14, 14));

            g2.setColor(ov ? hov : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 3, 14, 14));

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new RoundRectangle2D.Float(0, 0, 64, h - 3, 14, 14));
            g2.setColor(ov ? hov : base);
            g2.fill(new Rectangle(48, 0, 18, h - 3));

            g2.setColor(new Color(255, 255, 255, 210));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g2, icon, 12, (h - 3) / 2 - 13, 26, 26);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            g2.drawString(titulo, 74, h / 2 - 2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(sub, 74, h / 2 + 14);

            g2.dispose();
        }

        private void drawIcon(Graphics2D g2, int t, int x, int y, int w, int h) {
            int cx = x + w / 2, cy = y + h / 2;
            switch (t) {
                case PILL -> {
                    g2.drawOval(cx - 10, cy - 5, 20, 10);
                    g2.drawLine(cx, cy - 5, cx, cy + 5);
                    g2.drawLine(cx - 4, cy - 12, cx + 4, cy - 12);
                    g2.drawLine(cx, cy - 15, cx, cy - 9);
                }
                case CLIPBOARD -> {
                    g2.drawRoundRect(cx - 9, cy - 10, 18, 20, 3, 3);
                    g2.drawRoundRect(cx - 4, cy - 13, 8, 5, 2, 2);
                    g2.drawLine(cx - 5, cy - 3, cx + 5, cy - 3);
                    g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
                    g2.drawLine(cx - 5, cy + 7, cx + 2, cy + 7);
                }
                case TRUCK -> {
                    g2.drawRect(cx - 10, cy - 8, 12, 11);
                    g2.drawRect(cx + 2, cy - 6, 8, 9);
                    g2.drawRect(cx + 3, cy - 5, 5, 4);
                    g2.fillOval(cx - 7, cy + 2, 5, 5);
                    g2.fillOval(cx + 4, cy + 2, 5, 5);
                }
                case GEAR -> {
                    int r = 5;
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * 2 * i / 8;
                        g2.drawLine((int)(cx + Math.cos(a) * (r+1)), (int)(cy + Math.sin(a) * (r+1)),
                                (int)(cx + Math.cos(a) * (r+5)), (int)(cy + Math.sin(a) * (r+5)));
                    }
                    g2.fillOval(cx - 2, cy - 2, 4, 4);
                }
            }
        }
    }
}