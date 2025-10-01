package br.com.farmaetiquetas.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class PedidoForm extends JFrame {

    private final JTextField pedidoField = new JTextField();
    private final JTextField pacienteField = new JTextField();
    private final JTextField idadeField = new JTextField();
    private final JTextField saidaField = new JTextField("C:/Etiquetas");

    public PedidoForm() {
        super("Gerador de Etiqueta - Pedido (BD)");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(520, 260);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Número do Pedido:"), c);
        c.gridx = 1; c.weightx = 1.0; form.add(pedidoField, c);
        y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Nome do Paciente:"), c);
        c.gridx = 1; form.add(pacienteField, c);
        y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Idade do Paciente:"), c);
        c.gridx = 1; form.add(idadeField, c);
        y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Saída (pasta):"), c);
        c.gridx = 1; form.add(saidaField, c);
        y++;

        JButton gerar = new JButton("Buscar no BD e Gerar Etiqueta");
        gerar.addActionListener(this::onGerar);
        add(form, BorderLayout.CENTER);
        add(gerar, BorderLayout.SOUTH);
    }

    private void onGerar(ActionEvent e) {
        String numPedido = pedidoField.getText().trim();
        String paciente = pacienteField.getText().trim();
        String idade = idadeField.getText().trim();
        String pastaSaida = saidaField.getText().trim();

        if (numPedido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o número do pedido.");
            return;
        }
        if (paciente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o nome do paciente.");
            return;
        }
        if (idade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite a idade do paciente.");
            return;
        }

        // Carrega configuração do DB
        AppConfig cfg = new AppConfig();
        cfg.carregar(); // garante que dbHost/dbPorta/dbBanco/dbUsuario/dbSenha estejam preenchidos

        String url = "jdbc:postgresql://" + cfg.dbHost + ":" + cfg.dbPorta + "/" + cfg.dbBanco;

        try (Connection conn = DriverManager.getConnection(url, cfg.dbUsuario, cfg.dbSenha)) {

            // 1) Busca cliente e vendedor na cadcvend - CORRIGIDO: num_nota::text
            String codCliente = null;
            String codVendedor = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT cod_cliente, cod_vendedor FROM cadcvend WHERE num_nota::text = ?")) {
                ps.setString(1, numPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        codCliente = rs.getString("cod_cliente");
                        codVendedor = rs.getString("cod_vendedor");
                    } else {
                        JOptionPane.showMessageDialog(this, "Pedido não encontrado (cadcvend).");
                        return;
                    }
                }
            }

            // 2) Busca dados do cliente em cadclien - CORRIGIDO: cod_cliente::text
            String nomCliente = "";
            String numCnpj = "";
            String RG = "";
            String endereco = "";
            String telefone = "";
            String emissor = "";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT nom_cliente, num_cnpj, num_ident, end_cliente, num_endereco, bai_cliente, cid_cliente, num_celular, org_emisconj, est_cliente, cep_cliente " +
                            "FROM cadclien WHERE cod_cliente::text = ?")) {
                ps.setString(1, codCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nomCliente = rs.getString("nom_cliente");
                        numCnpj = rs.getString("num_cnpj");
                        RG = rs.getString("num_ident");
                        String e1 = rs.getString("end_cliente");
                        String e2 = rs.getString("num_endereco");
                        String e3 = rs.getString("bai_cliente");
                        String cidade = rs.getString("cid_cliente");
                        String estado = rs.getString("est_cliente");
                        String cep = rs.getString("cep_cliente");
                        endereco = String.format("%s, %s, %s - %s-%s %s",
                                safe(e1), safe(e2), safe(e3), safe(cidade), safe(estado), safe(cep));
                        telefone = rs.getString("num_celular");
                        emissor = rs.getString("org_emisconj");
                    } else {
                        JOptionPane.showMessageDialog(this, "Cliente não encontrado (cadclien).");
                        return;
                    }
                }
            }

            // 3) Produtos do pedido (cadivend)
            List<String> medicamentos = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT cod_reduzido, qtd_produto FROM cadivend WHERE num_nota = ? AND (flg_excluido = '' OR flg_excluido IS NULL)")) {
                ps.setString(1, numPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String codReduz = rs.getString("cod_reduzido");
                        String qtdStr = rs.getString("qtd_produto");
                        // buscar nome do produto e cod_laborat em cadprodu (filtrando grupos)
                        try (PreparedStatement ps2 = conn.prepareStatement(
                                "SELECT nom_produto, cod_laborat FROM cadprodu WHERE cod_reduzido::text = ? AND cod_grupo IN (86,98,99,111,102)")) { // CORRIGIDO: CASTING
                            ps2.setString(1, codReduz);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    String nomeProd = rs2.getString("nom_produto");
                                    String codLab = rs2.getString("cod_laborat");
                                    String nomeLab = "LAB. DESCONHECIDO";
                                    try (PreparedStatement ps3 = conn.prepareStatement(
                                            "SELECT nom_laborat FROM cadlabor WHERE cod_laborat::text = ?")) { // CORRIGIDO: CASTING
                                        ps3.setString(1, codLab);
                                        try (ResultSet rs3 = ps3.executeQuery()) {
                                            if (rs3.next()) {
                                                nomeLab = rs3.getString("nom_laborat");
                                            }
                                        }
                                    }

                                    // AJUSTE DE FORMATAÇÃO DE QUANTIDADE (Trata inteiros e decimais)
                                    String qtdFmt = "1"; // Default value
                                    if (qtdStr != null && !qtdStr.trim().isEmpty()) {
                                        try {
                                            double qtdDouble = Double.parseDouble(qtdStr);
                                            // Verifica se é um número inteiro (ex: 2.0)
                                            if (qtdDouble == Math.floor(qtdDouble)) {
                                                // Formata como inteiro
                                                qtdFmt = String.valueOf((int) qtdDouble);
                                            } else {
                                                // Número fracionário (ex: 2.5), formata com precisão decimal
                                                DecimalFormat df = new DecimalFormat("#.##");
                                                qtdFmt = df.format(qtdDouble);
                                            }
                                        } catch (NumberFormatException ex) {
                                            // Se não for um número válido, usa a string original
                                            qtdFmt = qtdStr;
                                        }
                                    }

                                    String linhaMed = String.format("%sx - %s (%s)_____________", qtdFmt, safe(nomeProd), safe(nomeLab));
                                    medicamentos.add(linhaMed);
                                }
                            }
                        } // ps2 closed
                    }
                }
            }

            // 4) Nome do atendente (cadusuar) - CORRIGIDO: cod_usuario::text
            String nomeAtendente = "";
            if (codVendedor != null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT nom_apelido FROM cadusuar WHERE cod_usuario::text = ?")) { // CORRIGIDO: CASTING
                    ps.setString(1, codVendedor);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) nomeAtendente = rs.getString("nom_apelido");
                    }
                }
            }

            // --- chama o gerador (mantendo layout semelhante ao Python) ---
            new File(pastaSaida).mkdirs();
            String arquivoGerado = PdfLabelGenerator.generateEtiquetaProduto(
                    numPedido, nomCliente, numCnpj, endereco, RG, telefone, paciente, idade,
                    medicamentos, nomeAtendente, emissor, pastaSaida
            );

            // abre automaticamente
            File pdfFile = new File(arquivoGerado);
            if (Desktop.isDesktopSupported() && pdfFile.exists()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (Exception exOpen) {
                    JOptionPane.showMessageDialog(this, "Etiqueta gerada, mas não foi possível abrir automaticamente.\nArquivo: " + pdfFile.getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Etiqueta gerada em: " + pdfFile.getAbsolutePath());
            }

            JOptionPane.showMessageDialog(this, "Etiqueta de produto gerada com sucesso!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao buscar dados / gerar etiqueta:\n" + ex.getMessage());
        }
    }

    private static String safe(String s) {

        return s == null ? "" : s;
    }

    public static void open() {
        new PedidoForm().setVisible(true);
    }
}