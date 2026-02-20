package br.com.farmaetiquetas.app;

import org.json.JSONObject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FreteForm extends JFrame {

    // Cores do Tema
    private final Color COR_FUNDO = new Color(245, 245, 250);
    private final Color COR_PRIMARIA = new Color(0, 102, 204);
    private final Color COR_SUCESSO = new Color(40, 167, 69);
    private final Color COR_ERRO = new Color(220, 53, 69);
    private final Font FONTE_GRANDE = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FONTE_MEDIA = new Font("Segoe UI", Font.PLAIN, 16);

    private JTextField txtCep;
    private JLabel lblLogradouro, lblBairro, lblCidade, lblComplemento;
    private JLabel lblValorFrete;
    private JCheckBox chkConvenio;
    private JButton btnBuscar;
    private FreteService service;

    public FreteForm() {
        super("Consulta de Entregas");
        this.service = new FreteService();
        configurarJanela();
        construirInterface();
    }

    private void configurarJanela() {
        setSize(480, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);
    }

    private void construirInterface() {
        // --- TOPO: Título e Busca ---
        JPanel panelTopo = new JPanel(new GridBagLayout());
        panelTopo.setBackground(Color.WHITE);
        panelTopo.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Cálculo de Frete");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelTopo.add(lblTitulo, gbc);

        // Input CEP
        txtCep = new JTextField();
        txtCep.setFont(new Font("Monospaced", Font.BOLD, 22));
        txtCep.setHorizontalAlignment(JTextField.CENTER);
        txtCep.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        // Ação: Pressionar ENTER busca
        txtCep.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buscar();
            }
        });

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panelTopo.add(txtCep, gbc);

        // Botão Buscar
        btnBuscar = new JButton("BUSCAR");
        btnBuscar.setBackground(COR_PRIMARIA);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscar());

        gbc.gridx = 1; gbc.weightx = 0.3;
        btnBuscar.setPreferredSize(new Dimension(100, 40));
        panelTopo.add(btnBuscar, gbc);

        add(panelTopo, BorderLayout.NORTH);

        // --- CENTRO: Dados do Endereço ---
        JPanel panelDados = new JPanel();
        panelDados.setLayout(new BoxLayout(panelDados, BoxLayout.Y_AXIS));
        panelDados.setBackground(COR_FUNDO);
        panelDados.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Container visual para o endereço
        JPanel cardEndereco = new JPanel(new GridLayout(4, 1, 5, 5));
        cardEndereco.setBackground(Color.WHITE);
        cardEndereco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        lblLogradouro = criarLabelInfo("Endereço: -");
        lblBairro = criarLabelInfo("Bairro: -");
        lblCidade = criarLabelInfo("Cidade: -");
        lblComplemento = criarLabelInfo("Obs: -");

        cardEndereco.add(lblLogradouro);
        cardEndereco.add(lblBairro);
        cardEndereco.add(lblCidade);
        cardEndereco.add(lblComplemento);

        panelDados.add(cardEndereco);
        add(panelDados, BorderLayout.CENTER);

        // --- RODAPÉ: Valor e Opções ---
        JPanel panelRodape = new JPanel(new BorderLayout(10, 10));
        panelRodape.setBackground(Color.WHITE);
        panelRodape.setBorder(new EmptyBorder(20, 20, 20, 20));

        chkConvenio = new JCheckBox("Cliente Convênio (Frete Grátis)");
        chkConvenio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkConvenio.setForeground(COR_PRIMARIA);
        chkConvenio.setBackground(Color.WHITE);
        chkConvenio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkConvenio.addActionListener(e -> recalcularApenasValor());

        lblValorFrete = new JLabel("R$ 0,00", SwingConstants.CENTER);
        lblValorFrete.setFont(FONTE_GRANDE);
        lblValorFrete.setForeground(Color.GRAY);
        lblValorFrete.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panelRodape.add(chkConvenio, BorderLayout.NORTH);
        panelRodape.add(lblValorFrete, BorderLayout.CENTER);

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
            JOptionPane.showMessageDialog(this, "CEP inválido. Digite apenas números.");
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                // 1. Busca visual (ViaCEP)
                JSONObject json = service.buscarEndereco(cep);

                // 2. Cálculo financeiro (CSV)
                Double valorCalculado = service.calcularFrete(cep, chkConvenio.isSelected());

                SwingUtilities.invokeLater(() -> {
                    lblLogradouro.setText("Rua: " + json.optString("logradouro", "-"));
                    lblBairro.setText("Bairro: " + json.optString("bairro", "-"));
                    lblCidade.setText("Local: " + json.optString("localidade", "") + " / " + json.optString("uf", ""));

                    if(json.has("complemento") && !json.getString("complemento").isEmpty()){
                        lblComplemento.setText("Comp: " + json.getString("complemento"));
                    } else {
                        lblComplemento.setText("");
                    }

                    atualizarDisplayValor(valorCalculado);
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
        if (cep.length() >= 8) {
            Double valor = service.calcularFrete(cep, chkConvenio.isSelected());
            atualizarDisplayValor(valor);
        }
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
        lblLogradouro.setText("Endereço: -");
        lblBairro.setText("Bairro: -");
        lblCidade.setText("Cidade: -");
        lblValorFrete.setText("R$ 0,00");
        lblValorFrete.setForeground(Color.GRAY);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new FreteForm().setVisible(true));
    }
}