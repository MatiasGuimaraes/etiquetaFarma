package br.com.farmaetiquetas.app;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfLabelGenerator {

    // --- POSOLOGIA (mantido como está; carrega logo do classpath /logo.jpg) ---
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
                logo.setAbsolutePosition(xLeft, yTop - logo.getScaledHeight());
                document.add(logo);
            } else {
                System.out.println("⚠️ Logo não encontrada em resources (esperado /logo.jpg)");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao carregar logo: " + e.getMessage());
        }

        // TELEFONE e SITE (ao lado da logo)
        float textX = xLeft + 95f;
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Tel: 3229-1966 / 3214-1666", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)),
                textX, yTop - 5f, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("www.FarmaciaModelo.com.br", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)),
                textX, yTop - 18f, 0);

        // PACIENTE
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Paciente: " + paciente, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)),
                xLeft, yTop - 40f, 0);

        // POSOLOGIA: título + texto NA MESMA LINHA (quebra automática se necessário)
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font posFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        float maxWidth = pageSize.getWidth() - 2f * xLeft;
        float yPos = yTop - 60f;

        Phrase posologiaLinha = new Phrase();
        posologiaLinha.add(new Chunk("Posologia: ", tituloFont));
        posologiaLinha.add(new Chunk(posologia, posFont));

        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(posologiaLinha, xLeft, yPos - 20f, xLeft + maxWidth, yPos, 12f, Element.ALIGN_LEFT);
        ct.go();

        // RODAPÉ (data/hora)
        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase(data, new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY)),
                pageSize.getWidth() - 10f, 8f, 0);

        document.close();
        return arquivoSaida;
    }

    // --- PRODUTO (nova implementação que replica a lógica do seu script Python) ---
    /**
     * Gera etiqueta de produto no layout semelhante ao Python (reportlab).
     * medicamentos: lista de linhas já formatadas (ex: "2x - Dipirona 1g (LAB)")
     */
    public static String generateEtiquetaProduto(String numPedido,
                                                 String cliente, String cnpjCliente, String endereco,
                                                 String RG, String telefone, String paciente, String idade,
                                                 List<String> medicamentos, String atendente, String emissor,
                                                 String caminhoSaida) throws Exception {

        String nomeArquivo = "etiqueta_pedido_" + numPedido + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String arquivoSaida = caminhoSaida + File.separator + nomeArquivo;

        Rectangle pageSize = new Rectangle(100f * 2.83f, 50f * 2.83f); // ~100x50 mm
        Document document = new Document(pageSize, 6, 6, 6, 6); // margens pequenas
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoSaida));
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        // desenha borda (opcional, como no reportlab)
        cb.rectangle(2f, 2f, pageSize.getWidth() - 4f, pageSize.getHeight() - 4f);
        cb.stroke();

        // fonte pequena conforme Python (Helvetica 6)
        Font f = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.BLACK);

        float x = 8f;
        float yStart = pageSize.getHeight() - 12f;
        float[] y = new float[]{ yStart }; // mutable container

        // helper para nova página
        Runnable newPage = () -> {
            document.newPage();
            cb.rectangle(2f, 2f, pageSize.getWidth() - 4f, pageSize.getHeight() - 4f);
            cb.stroke();
            y[0] = yStart;
        };

        // helper para desenhar linha com quebra de página
        java.util.function.Consumer<String> drawLine = (text) -> {
            if (y[0] < 12f) {
                newPage.run();
            }
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(text, f), x, y[0], 0);
            y[0] -= 8f;
        };

        // Cabeçalho
        drawLine.accept("Pedido: " + numPedido + "  " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        drawLine.accept("FARMÁCIA MODELO  -  TANEMIL FARMA LTDA  -  02.893.507/0001-47");
        drawLine.accept("AV. REPÚBLICA DO LÍBANO, 1620, ST. OESTE, GOIÂNIA - GO, 74.115-030");
        drawLine.accept("");

        // Comprador / dados
        String compradorLine = String.format("COMPRADOR: %s  RG: %s  CPF: %s  TEL: %s",
                safe(cliente), safe(RG), safe(cnpjCliente), safe(telefone));
        // quebra manual se muito longa (similar ao Python)
        if (compradorLine.length() <= 120) {
            drawLine.accept(compradorLine);
        } else {
            // quebra por 100 chars com indent
            int max = 100;
            String tmp = compradorLine;
            while (tmp.length() > max) {
                drawLine.accept(tmp.substring(0, max));
                tmp = "    " + tmp.substring(max);
            }
            if (!tmp.trim().isEmpty()) drawLine.accept(tmp);
        }
        drawLine.accept(safe(endereco));

        // Paciente
        drawLine.accept("PACIENTE: " + safe(paciente).toUpperCase() + "  IDADE: " + safe(idade));

        // Medicamentos (cada linha, com quebra se necessário)
        for (String med : medicamentos) {
            String linha = med;
            // format qtd as integer-like if possible already in string by caller
            // quebra por 80 caracteres em caso de excesso (mantendo comportamento similar ao Python)
            int max = 80;
            String resto = linha;
            while (resto.length() > max) {
                drawLine.accept(resto.substring(0, max));
                resto = "    " + resto.substring(max);
            }
            if (!resto.trim().isEmpty()) drawLine.accept(resto);
        }

        drawLine.accept("");
        drawLine.accept("_____________________                  _____________________");
        drawLine.accept(" FARMACÊUTICO(A)                                " + safe(atendente));
        drawLine.accept("");
        drawLine.accept("É VEDADA A DEVOLUÇÃO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLAÇÃO VIGENTE.");
        drawLine.accept("");
        drawLine.accept("___________________________________________");
        drawLine.accept(safe(cliente));

        document.close();
        return arquivoSaida;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
