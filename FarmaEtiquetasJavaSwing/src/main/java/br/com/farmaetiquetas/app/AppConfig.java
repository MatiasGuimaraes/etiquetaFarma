package br.com.farmaetiquetas.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";

    // --- BANCO DE DADOS (Atualizado com dados do TesteConexao) ---
    public String dbHost = "host";
    public String dbPorta = "porta";
    public String dbBanco = "banco";
    public String dbUsuario = "usuario";
    public String dbSenha = "senha";

    // Caminho de saída
    public String caminhoSaida = "C:/Etiquetas";

    // Impressora (Mantido para compatibilidade com o código anterior)
    public String nomeImpressora = "Nome_Da_Impressora_Aqui";

    /**
     * Carrega as configurações do arquivo config.properties
     */
    public void carregar() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            // Se existirem valores no arquivo, eles sobrescrevem os padrões acima
            dbHost = prop.getProperty("dbHost", dbHost);
            dbPorta = prop.getProperty("dbPorta", dbPorta);
            dbBanco = prop.getProperty("dbBanco", dbBanco);
            dbUsuario = prop.getProperty("dbUsuario", dbUsuario);
            dbSenha = prop.getProperty("dbSenha", dbSenha);
            caminhoSaida = prop.getProperty("caminhoSaida", caminhoSaida);
            nomeImpressora = prop.getProperty("nomeImpressora", nomeImpressora);

            System.out.println("✅ Configurações carregadas de " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível carregar " + CONFIG_FILE + ". Usando valores padrão (hardcoded).");
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
            prop.setProperty("nomeImpressora", nomeImpressora);

            prop.store(output, "Configurações do FarmaEtiquetas");
            System.out.println("💾 Configurações salvas em " + CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
