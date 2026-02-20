/*
package br.com.farmaetiquetas.app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PedidoEtiquetaGenerator {

    public static void gerarEtiquetaPedido() {

        PDDocument document = new PDDocument();

        try {
            PDPage page = new PDPage(new PDRectangle(500, 720));
            document.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float y = 690;


            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("Pedido: 3300127   16/12/2025 11:53");
            cs.endText();

            y -= 14;

            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("FARMÁCIA MODELO - TANEMIL FARMA LTDA - 02.893.507/0001-47");
            cs.endText();

            y -= 14;

            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("AV. REPÚBLICA DO LÍBANO, 1620, ST. OESTE, GOIÂNIA - GO, 74.115-030");
            cs.endText();

            y -= 12;
            cs.setNonStrokingColor(0, 0, 0);
            cs.addRect(20, y, pageWidth - 40, 8);
            cs.fill();
            cs.setNonStrokingColor(0, 0, 0);

            y -= 22;



            cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("COMPRADOR: FRANCYELLE ALVES DIAS   RG:   CNPJ/CPF:   TEL:");
            cs.endText();

            y -= 14;

            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("RUA 5 SALA 601, 691, OESTE - GOIÂNIA-GO");
            cs.endText();

            y -= 18;



            cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("PACIENTE:                  IDADE:");
            cs.endText();

            y -= 18;



            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("1x - CILODEX 5ML (ALCON(9))");
            cs.endText();

            cs.setLineWidth(1f);
            cs.moveTo(240, y - 2);
            cs.lineTo(460, y - 2);
            cs.stroke();

            y -= 30;



            cs.setLineWidth(1f);

            cs.moveTo(20, y);
            cs.lineTo(180, y);
            cs.stroke();

            cs.moveTo(200, y);
            cs.lineTo(360, y);
            cs.stroke();

            cs.moveTo(380, y);
            cs.lineTo(460, y);
            cs.stroke();

            y -= 30;



            cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("FARMACÊUTICO(A)");
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(260, y);
            cs.showText("BEATRIZ");
            cs.endText();

            y -= 6;

            cs.moveTo(20, y);
            cs.lineTo(180, y);
            cs.stroke();

            cs.moveTo(240, y);
            cs.lineTo(460, y);
            cs.stroke();

            y -= 28;



            cs.setFont(PDType1Font.HELVETICA_BOLD, 8);
            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("É VEDADA A DEVOLUÇÃO DESTE(S) MEDICAMENTO(S) SEGUNDO A LEGISLAÇÃO VIGENTE.");
            cs.endText();

            y -= 26;

            cs.moveTo(20, y);
            cs.lineTo(460, y);
            cs.stroke();

            y -= 16;

            cs.beginText();
            cs.newLineAtOffset(20, y);
            cs.showText("FRANCYELLE ALVES DIAS");
            cs.endText();

            cs.close();



            String nomeArquivo = "etiqueta_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".pdf";

            File pdf = new File(nomeArquivo);
            document.save(pdf);
            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf);
            }

        } catch (Exception e) {
            try { document.close(); } catch (Exception ignored) {}

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao imprimir a etiqueta.\n\nVerifique a impressora e tente novamente.",
                    "Erro de Impressão",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

*/