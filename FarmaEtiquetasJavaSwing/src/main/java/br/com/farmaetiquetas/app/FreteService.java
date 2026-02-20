package br.com.farmaetiquetas.app;

import org.json.JSONObject;
import javax.swing.JOptionPane; // Importante para as janelas de aviso
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FreteService {

    private static class FaixaCep {
        long inicio;
        long fim;
        double valor;
        String convenio;

        public FaixaCep(long inicio, long fim, double valor, String convenio) {
            this.inicio = inicio;
            this.fim = fim;
            this.valor = valor;
            this.convenio = convenio;
        }

        public long getTamanho() {
            return fim - inicio;
        }
    }

    private final List<FaixaCep> faixas;

    public FreteService() {
        faixas = new ArrayList<>();
        carregarFaixasDoArquivo();
    }

    private void carregarFaixasDoArquivo() {
        String nomeArquivo = "FaixaCEP.csv";
        // Tenta achar na pasta do programa (onde o .jar está)
        File file = new File(System.getProperty("user.dir"), nomeArquivo);

        // Fallback: Se não achar, tenta no caminho antigo (Desenvolvimento)
        if (!file.exists()) {
            file = new File("D:\\Projetos Gerais\\ProjetosFarma\\etiquetaFarma\\etiquetaFarma\\FaixaCEP.csv");
        }

        if (!file.exists()) {
            JOptionPane.showMessageDialog(null,
                    "ERRO CRÍTICO:\nO arquivo 'FaixaCEP.csv' não foi encontrado!\n" +
                            "O sistema procurou em: " + System.getProperty("user.dir"));
            return;
        }

        int linhasLidas = 0;
        int linhasComErro = 0;
        String ultimoErro = "";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String linha;
            boolean cabecalho = true;

            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                // Remove aspas que envolvem a linha inteira (Formato da sua imagem)
                // Ex: "74000,74999,""R$ 5,00"",", -> vira -> 74000,74999,""R$ 5,00"",",
                String linhaLimpa = linha.trim();
                if (linhaLimpa.startsWith("\"") && linhaLimpa.endsWith("\"")) {
                    linhaLimpa = linhaLimpa.substring(1, linhaLimpa.length() - 1);
                }

                // Pula cabeçalho DEPOIS de limpar as aspas
                if (cabecalho) {
                    if (linhaLimpa.toUpperCase().contains("CEP") || linhaLimpa.toUpperCase().contains("TAXA")) {
                        cabecalho = false;
                        continue;
                    }
                }

                try {
                    // SEPARAÇÃO ROBUSTA (Ignora regex complexo e vai na força bruta para garantir)
                    // Remove R$, aspas duplas e espaços de TUDO para facilitar a leitura dos números
                    String linhaSemLixo = linhaLimpa.replace("\"", "").replace("R$", "").replace(" ", "");
                    // Agora temos algo como: 74001959,74063010,5,00,CONVENIO

                    String[] partes = linhaSemLixo.split(",");

                    if (partes.length >= 3) {
                        // Parte 0: CEP Inicio
                        String cepIniStr = partes[0].replaceAll("\\D", "");
                        // Parte 1: CEP Fim
                        String cepFimStr = partes[1].replaceAll("\\D", "");

                        // O preço pode ter sido quebrado em 2 partes por causa da vírgula (ex: 5,00 vira "5" e "00")
                        String valorStr;
                        int indiceProximaColuna;

                        // Verifica se a parte 3 é centavos (ex: "00")
                        if (partes.length > 3 && partes[3].matches("\\d{2}")) {
                            // É formato brasileiro: 5,00
                            valorStr = partes[2] + "." + partes[3]; // Reconstrói 5.00
                            indiceProximaColuna = 4;
                        } else {
                            // É formato americano ou inteiro: 5.00 ou 5
                            valorStr = partes[2].replace(",", ".");
                            indiceProximaColuna = 3;
                        }

                        // Convênio
                        String convenio = "";
                        if (partes.length > indiceProximaColuna) {
                            convenio = partes[indiceProximaColuna].toUpperCase();
                        }

                        if (!cepIniStr.isEmpty() && !cepFimStr.isEmpty()) {
                            long inicio = Long.parseLong(cepIniStr);
                            long fim = Long.parseLong(cepFimStr);
                            double valor = Double.parseDouble(valorStr);

                            faixas.add(new FaixaCep(inicio, fim, valor, convenio));
                            linhasLidas++;
                        }
                    }
                } catch (Exception e) {
                    linhasComErro++;
                    ultimoErro = "Linha: " + linha + " | Erro: " + e.getMessage();
                    System.out.println(ultimoErro);
                }
            }

            faixas.sort(Comparator.comparingLong(FaixaCep::getTamanho));

            // --- DIAGNÓSTICO (Remova depois que funcionar) ---
            if (linhasLidas == 0) {
                JOptionPane.showMessageDialog(null,
                        "ALERTA: O arquivo foi encontrado, mas NENHUMA linha foi lida corretamente.\n" +
                                "Verifique o formato!\nÚltimo erro: " + ultimoErro);
            } else {
                // Se quiser confirmar que carregou, descomente a linha abaixo:
                // JOptionPane.showMessageDialog(null, "Sucesso! Carregadas " + linhasLidas + " faixas de frete.");
            }
            // -------------------------------------------------

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao ler arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Double calcularFrete(String cepString, boolean isCheckboxMarcada) {
        String cepLimpo = cepString.replaceAll("\\D", "");
        if (cepLimpo.isEmpty()) return null;

        long cepNumerico = Long.parseLong(cepLimpo);

        for (FaixaCep faixa : faixas) {
            if (cepNumerico >= faixa.inicio && cepNumerico <= faixa.fim) {
                boolean isFaixaDeConvenio = faixa.convenio != null && !faixa.convenio.trim().isEmpty();

                if (isFaixaDeConvenio) {
                    if (isCheckboxMarcada) {
                        return 0.0;
                    } else {
                        continue; // Pula essa faixa restrita e procura a próxima
                    }
                }
                return faixa.valor;
            }
        }
        return null; // Fora de todas as faixas
    }

    public JSONObject buscarEndereco(String cep) throws Exception {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() != 8) throw new IllegalArgumentException("CEP INVÁLIDO!");

        URL url = new URL("https://viacep.com.br/ws/" + cepLimpo + "/json/");
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(5000);

        if (conexao.getResponseCode() != 200) {
            throw new Exception("Erro de conexão: " + conexao.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        JSONObject json = new JSONObject(content.toString());
        if (json.has("erro")) throw new Exception("CEP não encontrado na base de dados.");

        return json;
    }
}