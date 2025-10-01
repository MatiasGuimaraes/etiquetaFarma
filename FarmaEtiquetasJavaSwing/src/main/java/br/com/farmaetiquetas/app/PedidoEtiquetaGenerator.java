package br.com.farmaetiquetas.app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PedidoEtiquetaGenerator {

    private static final float MM = 2.8346457f;
    private static final float PAGE_W = 100 * MM;
    private static final float PAGE_H = 50 * MM;

    public static File generate(String numPedido, String paciente, String idade,
                                PgRepository.PedidoData data, AppConfig cfg) throws IOException {
        File outDir = new File(cfg.caminhoSaida);
        outDir.mkdirs();
        String nome = "etiqueta_pedido_" + numPedido + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        File out = new File(outDir, nome);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // borda
                cs.setStrokingColor(0);
                cs.addRect(0, 0, PAGE_W, PAGE_H);
                cs.stroke();

                // logo
                PDImageXObject img = loadLogo(doc);
                float yTop = PAGE_H - 10;
                if (img != null) {
                    float maxW = 25 * MM, maxH = 10 * MM;
                    float scale = Math.min(maxW / img.getWidth(), maxH / img.getHeight());
                    float drawW = img.getWidth() * scale;
                    float drawH = img.getHeight() * scale;
                    cs.drawImage(img, 10, yTop - drawH, drawW, drawH);
                }

                float[] lineY = { PAGE_H - 15 };
                float left = 10;

                // conteúdo
                drawLine(cs, "Pedido: " + numPedido + "  " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), lineY, left);

                drawLine(cs, "FARMÁCIA MODELO  -  TANEMIL FARMA LTDA  -  02.893.507/0001-47", lineY, left);
                drawLine(cs, "AV. REPÚBLICA DO LÍBANO, 1620, ST. OESTE, GOIÂNIA - GO, 74.115-030", lineY, left);
                drawLine(cs, "", lineY, left);

                drawLine(cs, "COMPRADOR: " + safe(data.cliente) +
                        " RG: " + safe(data.rg) +
                        "  CPF: " + safe(data.cnpj) +
                        " TEL: " + safe(data.telefone), lineY, left);

                drawLine(cs, safe(data.endereco), lineY, left);

                drawLine(cs, "PACIENTE: " + (paciente == null ? "" : paciente.toUpperCase()) +
                        "  IDADE: " + (idade == null ? "" : idade), lineY, left);

                for (var m : data.medicamentos) {
                    int qtdInt;
                    try { qtdInt = (int) Float.parseFloat(m.qtd); } catch (Exception e) { qtdInt = 0; }
                    String linha = qtdInt + "x - " + m.desc + " (" + m.laboratorio + ")_____________";
                    while (linha.length() > 80) {
                        drawLine(cs, linha.substring(0, 80), lineY, left);
                        linha = "    " + linha.substring(80);
                    }
                    drawLine(cs, linha, lineY, left);
                }

                drawLine(cs, "", lineY, left);
                drawLine(cs, "_____________________                  _____________________", lineY, left);
                drawLine(cs, " FARMACÊUTICO(A)                                " + safe(data.atendente), lineY, left);
                drawLine(cs, "", lineY, left);
                drawLine(cs, "É VEDADA A DEVOLUÇÃO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLAÇÃO VIGENTE.", lineY, left);
                drawLine(cs, "", lineY, left);
                drawLine(cs, "___________________________________________", lineY, left);
                drawLine(cs, safe(data.cliente), lineY, left);
            }

            doc.save(out);

            // abre no navegador
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(out.toURI());
            }
        }

        return out;
    }

    private static void drawLine(PDPageContentStream cs, String txt, float[] lineY, float left) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 6);
        cs.newLineAtOffset(left, lineY[0]);
        cs.showText(txt);
        cs.endText();
        lineY[0] -= 7;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static PDImageXObject loadLogo(PDDocument doc) {
        try {
            var is = PedidoEtiquetaGenerator.class.getResourceAsStream("/logo.jpg");
            if (is != null) {
                return PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo");
            }
            File f = new File("resources/logo.jpg");
            if (f.exists()) return PDImageXObject.createFromFile(f.getAbsolutePath(), doc);
            f = new File("logo.jpg");
            if (f.exists()) return PDImageXObject.createFromFile(f.getAbsolutePath(), doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}