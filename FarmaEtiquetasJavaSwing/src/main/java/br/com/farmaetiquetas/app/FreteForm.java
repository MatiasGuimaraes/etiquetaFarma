package br.com.farmaetiquetas.app;

import org.json.JSONObject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FreteForm extends JFrame {

    private final Color COR_FUNDO    = new Color(245, 245, 250);
    private final Color COR_PRIMARIA = new Color(0, 102, 204);
    private final Color COR_SUCESSO  = new Color(40, 167, 69);
    private final Color COR_ERRO     = new Color(220, 53, 69);
    private final Font  FONTE_GRANDE  = new Font("Segoe UI", Font.BOLD, 28);
    private final Font  FONTE_MEDIA   = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font  FONTE_PEQUENA = new Font("Segoe UI", Font.PLAIN, 13);

    private JTextField txtCep;
    private JLabel lblLogradouro, lblBairro, lblCidade, lblComplemento;
    private JLabel lblValorFrete, lblDistancia;
    private JCheckBox chkConvenio;
    private JButton btnBuscar;

    private final FreteService service;

    public FreteForm(AppConfig config) {
        super("Consulta de Entregas");
        this.service = new FreteService(config);
        configurarJanela();
        construirInterface();
    }

    private void configurarJanela() {
        // Aumentei levemente a altura de 540 para 570 para acomodar o novo link
        setSize(480, 570);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);
    }

    private void construirInterface() {
        JPanel panelTopo = new JPanel(new GridBagLayout());
        panelTopo.setBackground(Color.WHITE);
        panelTopo.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Calculo de Frete");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelTopo.add(lblTitulo, gbc);

        txtCep = new JTextField();
        txtCep.setFont(new Font("Monospaced", Font.BOLD, 22));
        txtCep.setHorizontalAlignment(JTextField.CENTER);
        txtCep.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)));
        txtCep.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buscar();
            }
        });
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panelTopo.add(txtCep, gbc);

        btnBuscar = new JButton("BUSCAR");
        btnBuscar.setBackground(COR_PRIMARIA);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.setOpaque(true);
        btnBuscar.addActionListener(e -> buscar());
        gbc.gridx = 1; gbc.weightx = 0.3;
        btnBuscar.setPreferredSize(new Dimension(100, 40));
        panelTopo.add(btnBuscar, gbc);

        add(panelTopo, BorderLayout.NORTH);

        JPanel panelDados = new JPanel();
        panelDados.setLayout(new BoxLayout(panelDados, BoxLayout.Y_AXIS));
        panelDados.setBackground(COR_FUNDO);
        panelDados.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel cardEndereco = new JPanel(new GridLayout(4, 1, 5, 5));
        cardEndereco.setBackground(Color.WHITE);
        cardEndereco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 15, 15, 15)));

        lblLogradouro  = criarLabelInfo("Endereco: -");
        lblBairro      = criarLabelInfo("Bairro: -");
        lblCidade      = criarLabelInfo("Cidade: -");
        lblComplemento = criarLabelInfo("Obs: -");

        cardEndereco.add(lblLogradouro);
        cardEndereco.add(lblBairro);
        cardEndereco.add(lblCidade);
        cardEndereco.add(lblComplemento);

        panelDados.add(cardEndereco);
        add(panelDados, BorderLayout.CENTER);

        JPanel panelRodape = new JPanel(new BorderLayout(10, 10));
        panelRodape.setBackground(Color.WHITE);
        panelRodape.setBorder(new EmptyBorder(15, 20, 20, 20));

        chkConvenio = new JCheckBox("Cliente Convenio (Frete Gratis)");
        chkConvenio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkConvenio.setForeground(COR_PRIMARIA);
        chkConvenio.setBackground(Color.WHITE);
        chkConvenio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkConvenio.addActionListener(e -> recalcularApenasValor());

        lblDistancia = new JLabel("Distancia: -", SwingConstants.CENTER);
        lblDistancia.setFont(FONTE_PEQUENA);
        lblDistancia.setForeground(Color.GRAY);

        lblValorFrete = new JLabel("R$ 0,00", SwingConstants.CENTER);
        lblValorFrete.setFont(FONTE_GRANDE);
        lblValorFrete.setForeground(Color.GRAY);
        lblValorFrete.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JPanel panelValores = new JPanel(new GridLayout(2, 1, 0, 2));
        panelValores.setBackground(Color.WHITE);
        panelValores.add(lblDistancia);
        panelValores.add(lblValorFrete);

        // --- INÍCIO DA ADIÇÃO DO LINK PARA A TABELA ---
        JLabel lblExcecoes = new JLabel("📋 Ver Clientes com Taxa Diferenciada", SwingConstants.CENTER);
        lblExcecoes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExcecoes.setForeground(COR_PRIMARIA);
        lblExcecoes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblExcecoes.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        lblExcecoes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { abrirTabelaExcecoes(); }
        });
        // --- FIM DA ADIÇÃO DO LINK ---

        panelRodape.add(chkConvenio, BorderLayout.NORTH);
        panelRodape.add(panelValores, BorderLayout.CENTER);
        panelRodape.add(lblExcecoes, BorderLayout.SOUTH); // Link adicionado no fundo do rodapé

        add(panelRodape, BorderLayout.SOUTH);
    }

    private JLabel criarLabelInfo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONTE_MEDIA);
        lbl.setForeground(Color.DARK_GRAY);
        return lbl;
    }

    private void buscar() {
        String cep = txtCep.getText().trim();
        if (cep.length() < 8) {
            JOptionPane.showMessageDialog(this, "CEP invalido. Digite apenas numeros.");
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("...");
        lblDistancia.setText("Calculando...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                JSONObject endereco   = service.buscarEndereco(cep);
                JSONObject freteResp  = service.calcularFrete(cep, chkConvenio.isSelected());

                Double valor     = freteResp.isNull("valor") ? null : freteResp.getDouble("valor");
                double distancia = freteResp.optDouble("distancia", 0.0);

                SwingUtilities.invokeLater(() -> {
                    lblLogradouro.setText("Rua: " + endereco.optString("logradouro", "-"));
                    lblBairro.setText("Bairro: " + endereco.optString("bairro", "-"));
                    lblCidade.setText("Local: " + endereco.optString("localidade", "") + " / " + endereco.optString("uf", ""));

                    if (endereco.has("complemento") && !endereco.getString("complemento").isEmpty())
                        lblComplemento.setText("Comp: " + endereco.getString("complemento"));
                    else
                        lblComplemento.setText("");

                    lblDistancia.setText(distancia > 0 ? String.format("Distancia: %.1f km", distancia) : "Distancia: -");
                    atualizarDisplayValor(valor);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
                    limparResultados();
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnBuscar.setEnabled(true);
                    btnBuscar.setText("BUSCAR");
                    setCursor(Cursor.getDefaultCursor());
                });
            }
        }).start();
    }

    private void recalcularApenasValor() {
        String cep = txtCep.getText().trim();
        if (cep.length() < 8) return;

        new Thread(() -> {
            try {
                JSONObject freteResp = service.calcularFrete(cep, chkConvenio.isSelected());
                Double valor     = freteResp.isNull("valor") ? null : freteResp.getDouble("valor");
                double distancia = freteResp.optDouble("distancia", 0.0);

                SwingUtilities.invokeLater(() -> {
                    if (distancia > 0) lblDistancia.setText(String.format("Distancia: %.1f km", distancia));
                    atualizarDisplayValor(valor);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erro ao recalcular: " + ex.getMessage()));
            }
        }).start();
    }

    private void atualizarDisplayValor(Double valor) {
        if (valor != null) {
            lblValorFrete.setText(String.format("R$ %.2f", valor));
            lblValorFrete.setForeground(valor == 0.0 ? COR_PRIMARIA : COR_SUCESSO);
        } else {
            if (chkConvenio.isSelected()) {
                lblValorFrete.setText("R$ 0,00");
                lblValorFrete.setForeground(COR_PRIMARIA);
            } else {
                lblValorFrete.setText("A Combinar / Fora da Faixa");
                lblValorFrete.setForeground(COR_ERRO);
            }
        }
    }

    private void limparResultados() {
        lblLogradouro.setText("Endereco: -");
        lblBairro.setText("Bairro: -");
        lblCidade.setText("Cidade: -");
        lblComplemento.setText("Obs: -");
        lblDistancia.setText("Distancia: -");
        lblValorFrete.setText("R$ 0,00");
        lblValorFrete.setForeground(Color.GRAY);
    }

    // --- NOVO MÉTODO: GERA A TABELA POP-UP ---
    private void abrirTabelaExcecoes() {
        JDialog dialog = new JDialog(this, "Clientes com Taxa Diferenciada", true);
        dialog.setSize(550, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COR_FUNDO);

        String[] colunas = {"Código", "Cliente", "Taxa"};
        Object[][] dados = {
                {"207184", "ASMIGO ASS DE SAUDE MENTAL INF DE GOIAS", "R$ 0,00"},
                {"202001", "THALIS GCM COMPUTADORES", "R$ 5,00"},
                {"205971", "CLINICA CLARE (LEONARDO PRESTES)", "R$ 0,00"},
                {"1039",   "EDILBERTO REZENDE", "R$ 0,00"}
        };

        JTable table = new JTable(dados, colunas);
        table.setRowHeight(28);
        table.setFont(FONTE_PEQUENA);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(COR_PRIMARIA);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setEnabled(false); // Impede edição

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel panelAviso = new JPanel(new BorderLayout());
        panelAviso.setBackground(COR_FUNDO);
        panelAviso.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JLabel lblAviso = new JLabel("<html><b>⚠ Regra Especial (CEP 74672-020) e CEP 74075-050 a taxa é R$ 0,00(SOMENTE SE FOR CONVÊNIO):</b><br>" +
                "Funcionário ou Paciente que estiver na Asmigo, a taxa é: <b>R$ 5,00</b>");
        lblAviso.setFont(FONTE_PEQUENA);
        lblAviso.setForeground(Color.DARK_GRAY);

        panelAviso.add(lblAviso, BorderLayout.CENTER);
        dialog.add(panelAviso, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public static void open(AppConfig config) {
        SwingUtilities.invokeLater(() -> new FreteForm(config).setVisible(true));
    }
}