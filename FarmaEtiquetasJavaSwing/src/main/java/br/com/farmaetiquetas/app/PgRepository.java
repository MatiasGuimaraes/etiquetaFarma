package br.com.farmaetiquetas.app;

import java.util.ArrayList;
import java.util.List;

public class PgRepository {

    public static class PedidoData {
        public String cliente;
        public String rg;
        public String cnpj;
        public String telefone;
        public String endereco;
        public String atendente;
        public List<Medicamento> medicamentos = new ArrayList<>();
    }

    public static class Medicamento {
        public String qtd;
        public String desc;
        public String laboratorio;

        public Medicamento(String qtd, String desc, String laboratorio) {
            this.qtd = qtd;
            this.desc = desc;
            this.laboratorio = laboratorio;
        }
    }
}