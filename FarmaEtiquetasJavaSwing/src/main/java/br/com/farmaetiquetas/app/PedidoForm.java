package br.com.farmaetiquetas.app;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PedidoForm extends JFrame {

    private static final Color AZUL       = new Color(26, 82, 160);
    private static final Color AZUL_HOVER = new Color(20, 65, 135);
    private static final Color FUNDO      = new Color(240, 242, 247);
    private static final Color CAMPO_BG   = new Color(248, 249, 252);
    private static final Color BORDA      = new Color(210, 215, 225);
    private static final Color LABEL_COR  = new Color(60, 70, 90);

    private final CampoTexto pedidoField   = new CampoTexto();
    private final CampoTexto pacienteField = new CampoTexto();
    private final CampoTexto idadeField    = new CampoTexto();
    private final CampoTexto saidaField    = new CampoTexto();

    private final AppConfig config;

    public PedidoForm(AppConfig config) {
        super("Gerador de Etiqueta - Pedido");
        this.config = config;

        saidaField.setText("C:/Etiquetas");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(500, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(FUNDO);
        setLayout(new BorderLayout());

        // Header azul
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lblTitulo = new JLabel("GERADOR DE ETIQUETA - PEDIDO");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Busca dados do pedido via API");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(180, 210, 255));

        JPanel headerTxt = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTxt.setOpaque(false);
        headerTxt.add(lblTitulo);
        headerTxt.add(lblSub);

        // Icone prancheta
        JPanel iconeHeader = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(cx - 18, cy - 18, 36, 36);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(cx - 9, cy - 10, 18, 20, 3, 3);
                g2.drawRoundRect(cx - 4, cy - 13, 8, 5, 2, 2);
                g2.drawLine(cx - 5, cy - 3, cx + 5, cy - 3);
                g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
                g2.drawLine(cx - 5, cy + 7, cx + 2, cy + 7);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(48, 48); }
        };
        iconeHeader.setOpaque(false);
        header.add(headerTxt, BorderLayout.CENTER);
        header.add(iconeHeader, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Card branco arredondado
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
        card.add(label("Numero do Pedido:"), c);
        c.gridy = 1; c.insets = new Insets(0, 0, 14, 0);
        card.add(pedidoField, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Nome do Paciente:"), c);
        c.gridy = 3; c.insets = new Insets(0, 0, 14, 0);
        card.add(pacienteField, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Idade do Paciente:"), c);
        c.gridy = 5; c.insets = new Insets(0, 0, 14, 0);
        card.add(idadeField, c);

        c.gridy = 6; c.insets = new Insets(0, 0, 5, 0);
        card.add(label("Pasta de Saida:"), c);
        c.gridy = 7; c.insets = new Insets(0, 0, 22, 0);
        card.add(saidaField, c);

        BotaoRound btnGerar = new BotaoRound("BUSCAR NA API E GERAR ETIQUETA", AZUL, AZUL_HOVER);
        btnGerar.addActionListener(this::onGerar);
        c.gridy = 8; c.insets = new Insets(0, 0, 10, 0);
        card.add(btnGerar, c);

        // Link configuracoes
        JLabel lblConfig = new JLabel("Gerenciar Configuracoes", SwingConstants.CENTER);
        lblConfig.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblConfig.setForeground(new Color(100, 120, 160));
        lblConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { ConfigForm.open(config); }
            public void mouseEntered(java.awt.event.MouseEvent e) { lblConfig.setForeground(AZUL); }
            public void mouseExited(java.awt.event.MouseEvent e)  { lblConfig.setForeground(new Color(100, 120, 160)); }
        });
        c.gridy = 9; c.insets = new Insets(0, 0, 0, 0);
        card.add(lblConfig, c);

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

    private void onGerar(ActionEvent e) {
        String numPedido  = pedidoField.getText().trim();
        String paciente   = pacienteField.getText().trim();
        String idade      = idadeField.getText().trim();
        String pastaSaida = saidaField.getText().trim();

        if (numPedido.isEmpty()) { JOptionPane.showMessageDialog(this, "Digite o numero do pedido."); return; }
        if (paciente.isEmpty())  { JOptionPane.showMessageDialog(this, "Digite o nome do paciente."); return; }
        if (idade.isEmpty())     { JOptionPane.showMessageDialog(this, "Digite a idade do paciente."); return; }

        if (config.apiUrl == null || config.apiUrl.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "URL da API nao configurada."); return;
        }
        if (config.apiKey == null || config.apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "API Key nao configurada."); return;
        }

        try {
            // 1. FAZ O GET: Busca os dados do pedido no banco de dados
            URL urlGet = new URL(config.apiUrl + "/api/pedidos/buscar/" + numPedido);
            HttpURLConnection conexaoGet = (HttpURLConnection) urlGet.openConnection();
            conexaoGet.setRequestMethod("GET");
            conexaoGet.setRequestProperty("Accept", "application/json");
            conexaoGet.setRequestProperty("X-API-KEY", config.apiKey);
            conexaoGet.setConnectTimeout(10000);
            conexaoGet.setReadTimeout(30000);

            int statusGet = conexaoGet.getResponseCode();
            if (statusGet == 401) { JOptionPane.showMessageDialog(this, "Acesso negado. Verifique a API Key."); return; }
            if (statusGet == 404) { JOptionPane.showMessageDialog(this, "Pedido nao encontrado no banco de dados."); return; }
            if (statusGet != 200) { JOptionPane.showMessageDialog(this, "Erro na API. Codigo: " + statusGet); return; }

            // Lê o JSON que a API devolveu
            BufferedReader in = new BufferedReader(new InputStreamReader(conexaoGet.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();

            // 2. INJETA OS DADOS DA TELA NO JSON
            JSONObject json = new JSONObject(content.toString());
            json.put("paciente", paciente); // Adiciona o nome que foi digitado
            json.put("idade", idade);       // Adiciona a idade que foi digitada

            json.put("numero", numPedido);

            // 3. FAZ O POST: Manda o JSON completo de volta pedindo o PDF
            URL urlPost = new URL(config.apiUrl + "/api/pedidos/gerar-etiqueta");
            HttpURLConnection conexaoPost = (HttpURLConnection) urlPost.openConnection();
            conexaoPost.setRequestMethod("POST");
            conexaoPost.setRequestProperty("Content-Type", "application/json");
            conexaoPost.setRequestProperty("Accept", "application/pdf");
            conexaoPost.setRequestProperty("X-API-KEY", config.apiKey);
            conexaoPost.setDoOutput(true);

            // Escreve o JSON no corpo da requisição
            try (java.io.OutputStream os = conexaoPost.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }

            // 4. RECEBE O PDF E SALVA NA MÁQUINA
            int statusPost = conexaoPost.getResponseCode();
            if (statusPost == 200) {
                new File(pastaSaida).mkdirs();
                File pdfFile = new File(pastaSaida, "etiqueta_pedido_" + numPedido + ".pdf");

                // Faz o download dos bytes e salva como arquivo físico
                try (java.io.InputStream is = conexaoPost.getInputStream();
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                // Abre o PDF na tela!
                if (Desktop.isDesktopSupported() && pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF na API. Codigo: " + statusPost);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro de Conexão: " + ex.getMessage());
        }
    }

    // Campo texto arredondado — modificado para resolver esmagamento no macOS
    static class CampoTexto extends JPanel {
        private final JTextField field = new JTextField();
        private boolean foc = false;

        CampoTexto() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(0, 44));

            // O segredo para o Mac não espremer o texto:
            field.setOpaque(false);
            field.setBackground(new Color(0, 0, 0, 0)); // Força a transparência absoluta no macOS

            // Margens verticais aplicadas para centralizar o texto e evitar o corte
            field.setBorder(new EmptyBorder(10, 12, 10, 12));

            field.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Fonte ajustada
            field.setForeground(new Color(30, 40, 60));
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { foc = true;  repaint(); }
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
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }

        public String getText() { return field.getText(); }
        public void setText(String t) { field.setText(t); }
    }

    // Botao arredondado reutilizavel
    public static class BotaoRound extends JButton {
        private final Color corBase;
        private final Color corHover;
        private boolean hover = false;

        public BotaoRound(String texto, Color corBase, Color corHover) {
            super(texto);
            this.corBase  = corBase;
            this.corHover = corHover;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 46));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? corHover : corBase);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static void open(AppConfig config) {
        new PedidoForm(config).setVisible(true);
    }
}