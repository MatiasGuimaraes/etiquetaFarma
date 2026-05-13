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

    // Dimensoes da etiqueta em pontos (100x50mm)
    private static final float PAGE_W = 100f * 2.83f;
    private static final float PAGE_H = 50f  * 2.83f;
    private static final float MARGIN = 8f;

    // --- POSOLOGIA ---
    public static String generateEtiquetaPosologia(String paciente, String posologia, String caminhoSaida) throws Exception {
        String nomeArquivo = "etiqueta_posologia_" + paciente.replaceAll("\\s+", "_") + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        Rectangle pageSize = new Rectangle(PAGE_W, PAGE_H);
        Document document = new Document(pageSize, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        float xLeft = 10f;
        float yTop  = PAGE_H - 10f;

        try {
            java.net.URL logoUrl = PdfLabelGenerator.class.getResource("/logo.jpg");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(90f, 30f);
                logo.setAbsolutePosition(xLeft, yTop - 30f);
                document.add(logo);
            }
        } catch (Exception e) {}

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
        Font posFont    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        float maxWidth  = PAGE_W - 2f * xLeft;
        float yPos      = yTop - 60f;

        Phrase posologiaLinha = new Phrase();
        posologiaLinha.add(new Chunk("Posologia: ", tituloFont));
        posologiaLinha.add(new Chunk(posologia, posFont));

        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(posologiaLinha, xLeft, 20f, xLeft + maxWidth, yPos, 12f, Element.ALIGN_LEFT);
        ct.go();

        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase(data, new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY)),
                PAGE_W - 10f, 8f, 0);

        document.close();
        return arquivoSaida;
    }

    // --- PRODUTO / PEDIDO ---
    public static String generateEtiquetaProduto(String numPedido,
                                                 String cliente, String cnpjCliente, String endereco,
                                                 String RG, String telefone, String paciente, String idade,
                                                 List<String> medicamentos, String atendente, String emissor,
                                                 String caminhoSaida,
                                                 boolean isEntrega) throws Exception {

        String nomeArquivo = "etiqueta_pedido_" + numPedido + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        // Largura reservada para o barcode na lateral (apenas se for entrega)
        float barcodeAreaW = isEntrega ? 26f : 0f;
        float barcodeGap   = isEntrega ? 3f  : 0f;

        // Largura util do conteudo principal
        float contentW = PAGE_W - (MARGIN * 2) - barcodeAreaW - barcodeGap;
        float contentH = PAGE_H - (MARGIN * 2);

        Rectangle pageSize = new Rectangle(PAGE_W, PAGE_H);

        // Documento com margens normais — o barcode sera posicionado manualmente
        Document document = new Document(pageSize, MARGIN, MARGIN, MARGIN, MARGIN);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        // -------------------------------------------------------
        // CODIGO DE BARRAS NA LATERAL DIREITA (70% da altura)
        // -------------------------------------------------------
        if (isEntrega) {
            float barcodeLength = PAGE_H * 0.70f; // comprimento = 70% da altura da pagina
            float barHeight     = barcodeAreaW - 2f;

            Barcode128 barcode = new Barcode128();
            barcode.setCode(numPedido);
            barcode.setCodeType(Barcode128.CODE128);
            barcode.setFont(null);
            barcode.setBarHeight(barHeight);
            barcode.setX(0.8f); // largura de cada barra

            // Cria template na orientacao horizontal e depois rotaciona 90 graus
            PdfTemplate tmpl = cb.createTemplate(barcodeLength, barcodeAreaW);
            barcode.placeBarcode(tmpl, BaseColor.BLACK, BaseColor.BLACK);

            // Posicao do barcode: canto direito, centralizado verticalmente
            float barcodeX = PAGE_W - MARGIN - barcodeAreaW;
            float barcodeY = (PAGE_H - barcodeLength) / 2f;

            // Rotacao 90 graus horario
            cb.addTemplate(tmpl, 0, 1, -1, 0,
                    barcodeX + barcodeAreaW,
                    barcodeY);

            // Linha separadora fina
            cb.saveState();
            cb.setLineWidth(0.4f);
            cb.setColorStroke(new GrayColor(0.75f));
            cb.moveTo(barcodeX - 2f, MARGIN);
            cb.lineTo(barcodeX - 2f, PAGE_H - MARGIN);
            cb.stroke();
            cb.restoreState();
        }

        // -------------------------------------------------------
        // CONTEUDO PRINCIPAL (usando ColumnText para controle preciso)
        // -------------------------------------------------------
        Font fontBold   = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD,   BaseColor.BLACK);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
        Font fontSmall  = new Font(Font.FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);

        // Monta o conteudo em uma tabela
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setTotalWidth(contentW);

        // Cabecalho
        String headerTexto = "Pedido: " + numPedido + "  " +
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        addCell(mainTable, headerTexto, fontBold, Element.ALIGN_LEFT);

        // Dados da farmacia
        addCell(mainTable, "FARMACIA MODELO  -  TANEMIL FARMA LTDA  -  02.893.507/0001-47", fontNormal, Element.ALIGN_LEFT);
        addCell(mainTable, "AV. REPUBLICA DO LIBANO, 1620, ST. OESTE, GOIANIA - GO, 74.115-030", fontNormal, Element.ALIGN_LEFT);

        addEmptyCell(mainTable, 3f);

        // Dados do cliente
        String line1 = String.format("COMPRADOR: %s  RG: %s  CNPJ/CPF: %s  TEL: %s",
                safe(cliente), safe(RG), safe(cnpjCliente), safe(telefone));
        addCell(mainTable, line1, fontNormal, Element.ALIGN_LEFT);
        addCell(mainTable, safe(endereco), fontNormal, Element.ALIGN_LEFT);

        // Linha divisoria
        addLinhaDivisoria(mainTable);

        // Paciente
        addCell(mainTable, "PACIENTE: " + safe(paciente).toUpperCase() + "  IDADE: " + safe(idade), fontBold, Element.ALIGN_LEFT);

        addEmptyCell(mainTable, 3f);

        // Medicamentos
        for (String med : medicamentos) {
            addCell(mainTable, med, fontBold, Element.ALIGN_LEFT);
        }

        addEmptyCell(mainTable, 10f);

        // Assinaturas
        PdfPTable sigTable = new PdfPTable(5);
        sigTable.setWidths(new float[]{32f, 2f, 32f, 2f, 32f});
        sigTable.setWidthPercentage(100);

        PdfPCell cellLine = new PdfPCell();
        cellLine.setBorder(Rectangle.NO_BORDER);
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(0.5f);
        ls.setPercentage(100);
        ls.setLineColor(BaseColor.BLACK);
        cellLine.addElement(new Chunk(ls));

        PdfPCell cellSpace = new PdfPCell(new Phrase(" "));
        cellSpace.setBorder(Rectangle.NO_BORDER);

        sigTable.addCell(cellLine);
        sigTable.addCell(cellSpace);
        sigTable.addCell(new PdfPCell(cellLine));
        sigTable.addCell(cellSpace);
        sigTable.addCell(new PdfPCell(cellLine));

        addCell(sigTable, "FARMACEUTICO(A)", fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, " ",              fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, safe(atendente),  fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, " ",              fontNormal, Element.ALIGN_CENTER);
        addCell(sigTable, primeiroNome(safe(cliente)), fontNormal, Element.ALIGN_CENTER);

        PdfPCell sigCell = new PdfPCell(sigTable);
        sigCell.setBorder(Rectangle.NO_BORDER);
        mainTable.addCell(sigCell);

        addEmptyCell(mainTable, 6f);

        // Aviso legal
        addCell(mainTable, "E VEDADA A DEVOLUCAO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLACAO VIGENTE.", fontSmall, Element.ALIGN_CENTER);

        // Renderiza a tabela diretamente no canvas na posicao correta
        // sem escalonamento — a tabela e desenhada em tamanho real
        float tableHeight = mainTable.calculateHeights();

        // Se o conteudo for maior que o disponivel, escala apenas o suficiente
        if (tableHeight > contentH) {
            float scale = contentH / tableHeight;
            PdfTemplate template = cb.createTemplate(contentW, tableHeight);
            mainTable.writeSelectedRows(0, -1, 0, tableHeight, template);
            cb.addTemplate(template, scale, 0, 0, scale, MARGIN, MARGIN);
        } else {
            // Posiciona o conteudo no topo da area util
            float yStart = PAGE_H - MARGIN;
            mainTable.writeSelectedRows(0, -1, MARGIN, yStart, cb);
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

    private static void addLinhaDivisoria(PdfPTable table) {
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(1.2f);
        ls.setPercentage(100);
        ls.setLineColor(BaseColor.BLACK);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(3f);
        cell.addElement(new Chunk(ls));
        table.addCell(cell);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String primeiroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        return nome.trim().split("\\s+")[0];
    }

    // --- IMPRESSAO ---
    public static void imprimirArquivo(String caminhoArquivo, String nomeImpressora) {
        try {
            if (nomeImpressora == null || nomeImpressora.trim().isEmpty() ||
                    nomeImpressora.equalsIgnoreCase("Nome_Da_Impressora_Aqui")) {
                JOptionPane.showMessageDialog(null, "Nome da impressora nao configurado!\nAbrindo arquivo manualmente...");
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
                JOptionPane.showMessageDialog(null, "Impressora nao encontrada: " + nomeImpressora);
                java.awt.Desktop.getDesktop().open(new File(caminhoArquivo));
                return;
            }
            DocPrintJob job = impressoraSelecionada.createPrintJob();
            try (FileInputStream fis = new FileInputStream(caminhoArquivo)) {
                Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
                job.print(doc, null);
                JOptionPane.showMessageDialog(null, "Enviado para impressora: " + nomeImpressora);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao imprimir: " + e.getMessage());
        }
    }
}