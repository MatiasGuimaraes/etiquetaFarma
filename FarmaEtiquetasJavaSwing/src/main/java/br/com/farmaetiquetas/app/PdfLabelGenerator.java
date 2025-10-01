package br.com.farmaetiquetas.app;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfLabelGenerator {

    // --- POSOLOGIA (MANTIDO EXATAMENTE COMO VOCÊ ENVIOU) ---
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

        // LOGO (classpath)
        try {
            java.net.URL logoUrl = PdfLabelGenerator.class.getResource("/logo.jpg");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(90f, 30f);
                logo.setAbsolutePosition(xLeft, yTop - 30f);
                document.add(logo);
            } else {
                System.out.println("⚠️ Logo não encontrada em resources (esperado /logo.jpg)");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao carregar logo: " + e.getMessage());
        }

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

    // --- PRODUTO (REFEITO COM TUDO EM NEGRITO E COMPRESSÃO GARANTIDA) ---
    public static String generateEtiquetaProduto(String numPedido,
                                                 String cliente, String cnpjCliente, String endereco,
                                                 String RG, String telefone, String paciente, String idade,
                                                 List<String> medicamentos, String atendente, String emissor,
                                                 String caminhoSaida) throws Exception {

        String nomeArquivo = "etiqueta_pedido_" + numPedido + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        Rectangle pageSize = new Rectangle(100f * 2.83f, 50f * 2.83f); // ~100x50 mm
        float margin = 8f;
        Document document = new Document(pageSize, margin, margin, margin, margin);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        float contentWidth = pageSize.getWidth() - (margin * 2);
        float contentHeight = pageSize.getHeight() - (margin * 2);

        // Usa uma fonte em negrito como base
        Font font = new Font(Font.FontFamily.HELVETICA, 6, Font.BOLD, BaseColor.BLACK);
        float leading = 7f;

        // Constrói a tabela com todo o conteúdo
        PdfPTable table = buildContentTable(font, leading, numPedido, cliente, cnpjCliente, endereco, RG, telefone, paciente, idade, medicamentos, atendente);

        // Define a largura total da tabela ANTES de qualquer outra operação.
        table.setTotalWidth(contentWidth);

        // Mede a altura real que a tabela precisa
        float tableHeight = table.getTotalHeight();

        // Calcula o fator de compressão
        float scale = 1.0f;
        if (tableHeight > contentHeight) {
            scale = contentHeight / tableHeight;
        }

        // Cria um "desenho" (template) com a altura exata do conteúdo
        PdfTemplate template = cb.createTemplate(contentWidth, tableHeight);

        // Renderiza a tabela nesse desenho (agora com a largura já definida)
        table.writeSelectedRows(0, -1, 0, tableHeight, template);

        // Adiciona o desenho ao PDF, aplicando a compressão vertical (scale)
        float yPos = margin;
        if (scale < 1.0f) { // Se estiver a comprimir, alinha ao topo para evitar margem extra
            yPos = margin + (contentHeight - tableHeight * scale);
        }
        cb.addTemplate(template, 1, 0, 0, scale, margin, yPos);

        document.close();
        return arquivoSaida;
    }

    // Função que constrói a tabela de conteúdo
    private static PdfPTable buildContentTable(Font font, float leading, String numPedido, String cliente, String cnpjCliente, String endereco, String RG, String telefone, String paciente, String idade, List<String> medicamentos, String atendente) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        table.addCell(createCell("Pedido: " + numPedido + "  " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), font, leading));
        table.addCell(createCell("FARMÁCIA MODELO  -  TANEMIL FARMA LTDA  -  02.893.507/0001-47", font, leading));
        table.addCell(createCell("AV. REPÚBLICA DO LÍBANO, 1620, ST. OESTE, GOIÂNIA - GO, 74.115-030", font, leading));
        table.addCell(createCell(" ", font, leading / 2));

        String compradorLine = String.format("COMPRADOR: %s  RG: %s  CNPJ/CPF: %s  TEL: %s",
                safe(cliente), safe(RG), safe(cnpjCliente), safe(telefone));
        table.addCell(createCell(compradorLine, font, leading));
        table.addCell(createCell(safe(endereco), font, leading));
        table.addCell(createCell("PACIENTE: " + safe(paciente).toUpperCase() + "  IDADE: " + safe(idade), font, leading));

        for (String med : medicamentos) {
            table.addCell(createCell(med, font, leading));
        }

        table.addCell(createCell(" ", font, leading / 2));
        table.addCell(createCell("_____________________            _____________________", font, leading));
        table.addCell(createCell(" FARMACÊUTICO(A)                       " + safe(atendente), font, leading));
        table.addCell(createCell(" ", font, leading / 2));
        table.addCell(createCell("É VEDADA A DEVOLUÇÃO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLAÇÃO VIGENTE.", font, leading));
        table.addCell(createCell(" ", font, leading / 2));
        table.addCell(createCell("___________________________________________", font, leading));
        table.addCell(createCell(safe(cliente), font, leading));

        return table;
    }

    // Helper para criar células de tabela
    private static PdfPCell createCell(String text, Font font, float leading) {
        Paragraph p = new Paragraph(text, font);
        p.setLeading(leading);
        PdfPCell cell = new PdfPCell(p);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}