package br.com.farmaetiquetas.app;

import javax.swing.*;
import java.awt.*;

public class MainMenu {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FarmaEtiquetas - Menu");
            // aumento pra caber melhor os botoes
            frame.setSize(400, 250);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


            frame.setLayout(new GridLayout(3, 1, 10, 10));
            frame.setLocationRelativeTo(null);

            JButton posologiaBtn = new JButton("Gerar Etiqueta - Posologia");
            posologiaBtn.setBackground(new Color(0,102,204));
            posologiaBtn.setForeground(Color.WHITE);
            posologiaBtn.addActionListener(e -> PosologiaForm.open());

            JButton pedidoBtn = new JButton("Gerar Etiqueta - Pedido");
            pedidoBtn.setBackground(new Color(0,102,204));
            pedidoBtn.setForeground(Color.WHITE);
            pedidoBtn.addActionListener(e -> PedidoForm.open());

            JButton consultaFrete = new JButton("Consultar Frete / Endereço");
            consultaFrete.setBackground(new Color(0,153,76));
            consultaFrete.setForeground(Color.WHITE);
            consultaFrete.setFont(new Font("SansSerif", Font.BOLD, 12));
            consultaFrete.addActionListener(e -> FreteForm.open());

            frame.add(posologiaBtn);
            frame.add(pedidoBtn);
            frame.add(consultaFrete);
            frame.setVisible(true);
        });
    }
}