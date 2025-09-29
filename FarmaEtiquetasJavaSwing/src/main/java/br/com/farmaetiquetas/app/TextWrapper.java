package br.com.farmaetiquetas.app.util;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para quebrar texto em várias linhas respeitando largura máxima no PDF.
 */
public class TextWrapper {

    public static List<String> wrap(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float width = font.getStringWidth(testLine) / 1000f * fontSize;
            if (width > maxWidth) {
                // quebra a linha
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
