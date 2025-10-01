package br.com.farmaetiquetas.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";

    // banco de dados
    public String dbHost = "192.168.15.201";
    public String dbPorta = "5432";
    public String dbBanco = "sgfpod1";
    public String dbUsuario = "consulta";
    public String dbSenha = "consultoria123";

    // Caminho de saída
    public String caminhoSaida = "C:/Etiquetas";

    /**
     * Carrega as configurações do arquivo config.properties
     */
    public void carregar() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            dbHost = prop.getProperty("dbHost", dbHost);
            dbPorta = prop.getProperty("dbPorta", dbPorta);
            dbBanco = prop.getProperty("dbBanco", dbBanco);
            dbUsuario = prop.getProperty("dbUsuario", dbUsuario);
            dbSenha = prop.getProperty("dbSenha", dbSenha);
            caminhoSaida = prop.getProperty("caminhoSaida", caminhoSaida);

            System.out.println("✅ Configurações carregadas de " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível carregar " + CONFIG_FILE + ". Usando valores padrão.");
        }
    }

    /**
     * Salva as configurações atuais no arquivo config.properties
     */
    public void salvar() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();

            prop.setProperty("dbHost", dbHost);
            prop.setProperty("dbPorta", dbPorta);
            prop.setProperty("dbBanco", dbBanco);
            prop.setProperty("dbUsuario", dbUsuario);
            prop.setProperty("dbSenha", dbSenha);
            prop.setProperty("caminhoSaida", caminhoSaida);

            prop.store(output, "Configurações do FarmaEtiquetas");
            System.out.println("💾 Configurações salvas em " + CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}