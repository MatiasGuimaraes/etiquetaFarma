package br.com.farmaetiquetas.app;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FreteService {

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT    = 30_000;

    private final AppConfig config;

    public FreteService(AppConfig config) {
        this.config = config;
    }

    public JSONObject calcularFrete(String cep, boolean convenio) throws Exception {
        String endpoint = "/api/frete/calcular?cep=" + cep + "&convenio=" + convenio;
        return chamarApi(endpoint);
    }

    public JSONObject buscarEndereco(String cep) throws Exception {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() != 8) throw new IllegalArgumentException("CEP invalido!");

        URL url = new URL("https://viacep.com.br/ws/" + cepLimpo + "/json/");
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(CONNECT_TIMEOUT);
        conexao.setReadTimeout(READ_TIMEOUT);

        if (conexao.getResponseCode() != 200)
            throw new Exception("Erro ao buscar CEP: " + conexao.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) content.append(line);
        in.close();

        JSONObject json = new JSONObject(content.toString());
        if (json.has("erro")) throw new Exception("CEP nao encontrado.");
        return json;
    }

    private JSONObject chamarApi(String endpoint) throws Exception {
        if (config.apiUrl == null || config.apiUrl.trim().isEmpty())
            throw new Exception("URL da API nao configurada. Abra Configuracoes e preencha.");
        if (config.apiKey == null || config.apiKey.trim().isEmpty())
            throw new Exception("API Key nao configurada. Abra Configuracoes e preencha.");

        URL url = new URL(config.apiUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("X-API-KEY", config.apiKey);

        int status = conn.getResponseCode();
        if (status == 401) throw new Exception("Acesso negado. Verifique a API Key nas configuracoes.");
        if (status != 200) throw new Exception("Erro na API: " + status);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        return new JSONObject(sb.toString());
    }
}