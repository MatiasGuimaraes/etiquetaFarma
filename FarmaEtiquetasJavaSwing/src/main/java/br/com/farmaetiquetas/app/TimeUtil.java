package br.com.farmaetiquetas.app.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário para lidar com data/hora em nomes de arquivos.
 */
public class TimeUtil {

    public static String nowFileSafe() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
