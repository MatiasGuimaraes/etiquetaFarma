package br.com.farmaetiquetas.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {
        // Use os mesmos dados do seu AppConfig
        String dbHost = "192.168.15.201";
        String dbPorta = "5432";
        String dbBanco = "sgfpod1";
        String dbUsuario = "consulta";
        String dbSenha = "consultoria123";

        String url = "jdbc:postgresql://" + dbHost + ":" + dbPorta + "/" + dbBanco;

        System.out.println("Tentando conectar ao banco de dados...");
        System.out.println("URL de conexão: " + url);

        try {
            // Passo 1: Forçar o carregamento do driver
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver do PostgreSQL foi carregado com sucesso.");

            // Passo 2: Tentar obter a conexão
            Connection conn = DriverManager.getConnection(url, dbUsuario, dbSenha);

            System.out.println("✅ CONEXÃO REALIZADA COM SUCESSO!");
            conn.close(); // Fecha a conexão

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERRO CRÍTICO: O driver do PostgreSQL não foi encontrado no Classpath. O problema é na configuração do projeto/IntelliJ.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ ERRO DE SQL: Não foi possível conectar. Verifique os seguintes pontos:");
            System.err.println("1. O IP e a porta estão corretos?");
            System.err.println("2. O nome do banco de dados está correto?");
            System.err.println("3. O usuário e a senha estão corretos?");
            System.err.println("4. Há um firewall bloqueando a conexão?");
            e.printStackTrace();
        }
    }
}