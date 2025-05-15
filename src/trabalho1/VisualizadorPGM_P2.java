package trabalho1;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class VisualizadorPGM_P2 {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VisualizadorPGM_P2::iniciar);
    }

    private static void iniciar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo PGM (P2)");
        int resultado = fileChooser.showOpenDialog(null);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo selecionado.");
            return;
        }

        File arquivoPGM = fileChooser.getSelectedFile();

        try {
            BufferedImage imagem = lerPGM(arquivoPGM);
            exibirImagem(imagem);

            int opcaoSalvar = JOptionPane.showConfirmDialog(null, "Deseja exportar a imagem para outro formato?", "Exportar", JOptionPane.YES_NO_OPTION);
            if (opcaoSalvar == JOptionPane.YES_OPTION) {
                exportarImagem(imagem);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static BufferedImage lerPGM(File arquivo) throws IOException {
        Scanner scanner = new Scanner(arquivo);

        // 1. Verifica cabeçalho P2
        String tipo = scanner.next();
        if (!tipo.equals("P2")) {
            scanner.close();
            throw new IOException("Formato não suportado (esperado P2).");
        }

        // 2. Lê (e ignora) comentários
        String linha = scanner.nextLine();
        while (linha.startsWith("#") || linha.trim().isEmpty()) {
            linha = scanner.nextLine();
        }

        // 3. Lê largura e altura
        int largura = scanner.nextInt();
        int altura = scanner.nextInt();

        // 4. Lê valor máximo (ex: 255)
        int maxValor = scanner.nextInt();

        // 5. Cria imagem BufferedImage
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_GRAY);

        // 6. Lê pixels
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int valorPixel = scanner.nextInt();
                int valorEscala = (valorPixel * 255) / maxValor; // Normaliza para 0-255
                int rgb = new Color(valorEscala, valorEscala, valorEscala).getRGB();
                imagem.setRGB(x, y, rgb);
            }
        }

        scanner.close();
        return imagem;
    }

    private static void exibirImagem(BufferedImage imagem) {
        JFrame frame = new JFrame("Visualizador PGM (P2)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel(new ImageIcon(imagem));
        frame.getContentPane().add(new JScrollPane(label));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void exportarImagem(BufferedImage imagem) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar imagem como...");

        String[] formatos = {"png", "jpg", "bmp", "gif"};
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", formatos));

        int resultado = fileChooser.showSaveDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSaida = fileChooser.getSelectedFile();
            String nome = arquivoSaida.getName();
            String extensao = "png"; // Padrão

            // Detecta extensão escolhida
            for (String fmt : formatos) {
                if (nome.toLowerCase().endsWith("." + fmt)) {
                    extensao = fmt;
                    break;
                }
            }

            // Se não tiver extensão, adiciona .png
            if (!nome.contains(".")) {
                arquivoSaida = new File(arquivoSaida.getAbsolutePath() + ".png");
            }

            try {
                ImageIO.write(imagem, extensao, arquivoSaida);
                JOptionPane.showMessageDialog(null, "Imagem salva com sucesso: " + arquivoSaida.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}

