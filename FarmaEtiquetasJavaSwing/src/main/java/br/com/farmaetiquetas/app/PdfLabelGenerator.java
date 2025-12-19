package br.com.farmaetiquetas.app;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.print.*;
import java.io.FileInputStream;

public class PdfLabelGenerator {

    // --- POSOLOGIA (MANTIDO) ---
    public static String generateEtiquetaPosologia(String paciente, String posologia, String caminhoSaida) throws Exception {
        String nomeArquivo = "etiqueta_posologia_" + paciente.replaceAll("\\s+", "_") + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        Rectangle pageSize = new Rectangle(100f * 2.83f, 50f * 2.83f); // ~100x50mm
        Document document = new Document(pageSize, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        float xLeft = 10f;
        float yTop = pageSize.getHeight() - 10f;

        try {
            java.net.URL logoUrl = PdfLabelGenerator.class.getResource("/logo.jpg");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(90f, 30f);
                logo.setAbsolutePosition(xLeft, yTop - 30f);
                document.add(logo);
            }
        } catch (Exception e) { /* Ignora */ }

        float textX = xLeft + 95f;
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Tel: 3229-1966 / 3214-1666", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)),
                textX, yTop - 15f, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("www.FarmaciaModelo.com.br", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)),
                textX, yTop - 25f, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Paciente: " + paciente, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)),
                xLeft, yTop - 40f, 0);

        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font posFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        float maxWidth = pageSize.getWidth() - 2f * xLeft;
        float yPos = yTop - 60f;

        Phrase posologiaLinha = new Phrase();
        posologiaLinha.add(new Chunk("Posologia: ", tituloFont));
        posologiaLinha.add(new Chunk(posologia, posFont));

        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(posologiaLinha, xLeft, 20f, xLeft + maxWidth, yPos, 12f, Element.ALIGN_LEFT);
        ct.go();

        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase(data, new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY)),
                pageSize.getWidth() - 10f, 8f, 0);

        document.close();
        return arquivoSaida;
    }

    // --- PRODUTO / PEDIDO ---
    public static String generateEtiquetaProduto(String numPedido,
                                                 String cliente, String cnpjCliente, String endereco,
                                                 String RG, String telefone, String paciente, String idade,
                                                 List<String> medicamentos, String atendente, String emissor,
                                                 String caminhoSaida) throws Exception {

        String nomeArquivo = "etiqueta_pedido_" + numPedido + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        // Tamanho da etiqueta
        Rectangle pageSize = new Rectangle(100f * 2.83f, 50f * 2.83f);
        float margin = 8f;
        Document document = new Document(pageSize, margin, margin, margin, margin);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        float contentWidth = pageSize.getWidth() - (margin * 2);

        // Fontes
        Font fontBold = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
        Font fontSmall = new Font(Font.FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);

        // Tabela Principal (1 coluna)
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setTotalWidth(contentWidth);

        // 1. CABEÇALHO
        addCell(mainTable, "Pedido: " + numPedido + "  " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), fontBold, Element.ALIGN_LEFT);
        addCell(mainTable, "FARMÁCIA MODELO  -  TANEMIL FARMA LTDA  -  02.893.507/0001-47", fontNormal, Element.ALIGN_LEFT);
        addCell(mainTable, "AV. REPÚBLICA DO LÍBANO, 1620, ST. OESTE, GOIÂNIA - GO, 74.115-030", fontNormal, Element.ALIGN_LEFT);
        
        addEmptyCell(mainTable, 4f);

        // 2. DADOS DO CLIENTE
        String line1 = String.format("COMPRADOR: %s  RG: %s  CNPJ/CPF: %s  TEL: %s",
                safe(cliente), safe(RG), safe(cnpjCliente), safe(telefone));
        addCell(mainTable, line1, fontNormal, Element.ALIGN_LEFT);
        addCell(mainTable, safe(endereco), fontNormal, Element.ALIGN_LEFT);
        
        String linePac = "PACIENTE: " + safe(paciente).toUpperCase() + "  IDADE: " + safe(idade);
        addCell(mainTable, linePac, fontBold, Element.ALIGN_LEFT);

        addEmptyCell(mainTable, 4f);

        // 3. MEDICAMENTOS
        for (String med : medicamentos) {
            addCell(mainTable, med, fontBold, Element.ALIGN_LEFT);
        }

        // Espaço antes das assinaturas
        addEmptyCell(mainTable, 12f);

        // 4. ASSINATURAS (Farmacêutico, Atendente e Cliente - LADO A LADO)
        // 5 colunas: [Linha] [Esp] [Linha] [Esp] [Linha]
        PdfPTable sigTable = new PdfPTable(5);
        // Larguras: 32% para cada assinatura (mais curtas para caberem) e 2% de intervalo
        sigTable.setWidths(new float[]{32f, 2f, 32f, 2f, 32f});
        sigTable.setWidthPercentage(100);

        // Configuração do Traço Gráfico (LineSeparator)
        PdfPCell cellLine = new PdfPCell();
        cellLine.setBorder(Rectangle.NO_BORDER);
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(0.5f);
        ls.setPercentage(100);
        ls.setLineColor(BaseColor.BLACK);
        cellLine.addElement(new Chunk(ls));

        // Célula de Espaço
        PdfPCell cellSpace = new PdfPCell(new Phrase(" "));
        cellSpace.setBorder(Rectangle.NO_BORDER);

        // --- LINHA DE CIMA: TRAÇOS ---
        sigTable.addCell(cellLine);                 // Traço Farmacêutico
        sigTable.addCell(cellSpace);                // Espaço
        sigTable.addCell(new PdfPCell(cellLine));   // Traço Atendente
        sigTable.addCell(cellSpace);                // Espaço
        sigTable.addCell(new PdfPCell(cellLine));   // Traço Cliente

        // --- LINHA DE BAIXO: NOMES ---
        addCell(sigTable, "FARMACÊUTICO(A)", fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, " ", fontNormal, Element.ALIGN_CENTER);
        // Limita tamanho do nome do atendente se necessário
        addCell(sigTable, safe(atendente), fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, " ", fontNormal, Element.ALIGN_CENTER);
        // AQUI ESTÁ A ALTERAÇÃO: APENAS PRIMEIRO NOME DO CLIENTE
        addCell(sigTable, primeiroNome(safe(cliente)), fontNormal, Element.ALIGN_CENTER);

        // Adiciona bloco de assinaturas na tabela principal
        PdfPCell sigCell = new PdfPCell(sigTable);
        sigCell.setBorder(Rectangle.NO_BORDER);
        mainTable.addCell(sigCell);

        addEmptyCell(mainTable, 8f);

        // 5. AVISO LEGAL (Rodapé)
        addCell(mainTable, "É VEDADA A DEVOLUÇÃO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLAÇÃO VIGENTE.", fontSmall, Element.ALIGN_CENTER);

        // --- RENDERIZAÇÃO E ESCALA ---
        float tableHeight = mainTable.calculateHeights();
        float availableHeight = pageSize.getHeight() - (margin * 2);

        if (tableHeight > availableHeight) {
            float scale = availableHeight / tableHeight;
            PdfContentByte canvas = writer.getDirectContent();
            PdfTemplate template = canvas.createTemplate(pageSize.getWidth() - (margin * 2), tableHeight);
            
            mainTable.writeSelectedRows(0, -1, 0, tableHeight, template);
            
            // Adiciona o template escalonado
            canvas.addTemplate(template, scale, 0, 0, scale, margin, margin); 
        } else {
            document.add(mainTable);
        }

        document.close();
        return arquivoSaida;
    }

    // --- Helpers ---
    private static void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingBottom(2f);
        table.addCell(cell);
    }

    private static void addEmptyCell(PdfPTable table, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        table.addCell(cell);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
    
    // Extrai apenas o primeiro nome
    private static String primeiroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        return nome.trim().split("\\s+")[0];
    }

    // --- IMPRESSÃO ---
    public static void imprimirArquivo(String caminhoArquivo, String nomeImpressora) {
        try {
            if (nomeImpressora == null || nomeImpressora.trim().isEmpty() || nomeImpressora.equalsIgnoreCase("Nome_Da_Impressora_Aqui")) {
                JOptionPane.showMessageDialog(null, "⚠️ Nome da impressora não configurado!\nAbrindo arquivo manualmente...");
                java.awt.Desktop.getDesktop().open(new File(caminhoArquivo));
                return;
            }
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService impressoraSelecionada = null;
            for (PrintService ps : services) {
                if (ps.getName().equalsIgnoreCase(nomeImpressora)) {
                    impressoraSelecionada = ps;
                    break;
                }
            }
            if (impressoraSelecionada == null) {
                JOptionPane.showMessageDialog(null, "❌ Impressora não encontrada: " + nomeImpressora);
                java.awt.Desktop.getDesktop().open(new File(caminhoArquivo));
                return;
            }
            DocPrintJob job = impressoraSelecionada.createPrintJob();
            try (FileInputStream fis = new FileInputStream(caminhoArquivo)) {
                Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
                job.print(doc, null);
                JOptionPane.showMessageDialog(null, "✅ Enviado para impressora: " + nomeImpressora);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao imprimir: " + e.getMessage());
        }
    }
}
