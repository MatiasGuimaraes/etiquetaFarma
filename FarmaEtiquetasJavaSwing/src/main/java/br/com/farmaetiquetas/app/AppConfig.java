package br.com.farmaetiquetas.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";

    public String dbHost = "";
    public String dbPorta = "5432";
    public String dbBanco = "";
    public String dbUsuario = "";
    public String dbSenha = "";
    public String apiUrl = "";
    public String apiKey = "";
    public String caminhoSaida = "C:/Etiquetas";
    public String nomeImpressora = "";

    public void carregar() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            dbHost         = prop.getProperty("dbHost",         dbHost);
            dbPorta        = prop.getProperty("dbPorta",        dbPorta);
            dbBanco        = prop.getProperty("dbBanco",        dbBanco);
            dbUsuario      = prop.getProperty("dbUsuario",      dbUsuario);
            dbSenha        = prop.getProperty("dbSenha",        dbSenha);
            apiUrl         = prop.getProperty("apiUrl",         apiUrl);
            apiKey         = prop.getProperty("apiKey",         apiKey);
            caminhoSaida   = prop.getProperty("caminhoSaida",   caminhoSaida);
            nomeImpressora = prop.getProperty("nomeImpressora", nomeImpressora);

            System.out.println("Configuracoes carregadas de " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("Nao foi possivel carregar " + CONFIG_FILE + ". Usando valores padrao.");
        }
    }

    public void salvar() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();

            prop.setProperty("dbHost",         dbHost);
            prop.setProperty("dbPorta",        dbPorta);
            prop.setProperty("dbBanco",        dbBanco);
            prop.setProperty("dbUsuario",      dbUsuario);
            prop.setProperty("dbSenha",        dbSenha);
            prop.setProperty("apiUrl",         apiUrl);
            prop.setProperty("apiKey",         apiKey);
            prop.setProperty("caminhoSaida",   caminhoSaida);
            prop.setProperty("nomeImpressora", nomeImpressora);

            prop.store(output, "Configuracoes do FarmaEtiquetas");
            System.out.println("Configuracoes salvas em " + CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}