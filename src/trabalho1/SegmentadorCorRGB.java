package trabalho1;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SegmentadorCorRGB {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SegmentadorCorRGB::iniciarSegmentacao);
    }

    private static void iniciarSegmentacao() {
        // --- 1. Obtenção da Cor Desejada e Tolerância do Usuário ---
        int rDesejado, gDesejado, bDesejado, tolerancia;

        try {
            String rStr = JOptionPane.showInputDialog(null, "Digite o valor de Vermelho (R, 0-255):", "Cor Desejada", JOptionPane.QUESTION_MESSAGE);
            if (rStr == null) return; // Usuário cancelou
            rDesejado = Integer.parseInt(rStr);

            String gStr = JOptionPane.showInputDialog(null, "Digite o valor de Verde (G, 0-255):", "Cor Desejada", JOptionPane.QUESTION_MESSAGE);
            if (gStr == null) return;
            gDesejado = Integer.parseInt(gStr);

            String bStr = JOptionPane.showInputDialog(null, "Digite o valor de Azul (B, 0-255):", "Cor Desejada", JOptionPane.QUESTION_MESSAGE);
            if (bStr == null) return;
            bDesejado = Integer.parseInt(bStr);

            String tolStr = JOptionPane.showInputDialog(null, "Digite o valor de tolerância (ex: 20-50):", "Tolerância", JOptionPane.QUESTION_MESSAGE);
            if (tolStr == null) return;
            tolerancia = Integer.parseInt(tolStr);

            if (rDesejado < 0 || rDesejado > 255 || gDesejado < 0 || gDesejado > 255 || bDesejado < 0 || bDesejado > 255 || tolerancia < 0) {
                JOptionPane.showMessageDialog(null, "Valores inválidos inseridos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor, insira números válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- 2. Carregamento da Imagem ---
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione a imagem RGB");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "png", "bmp", "jpeg", "gif"));
        int retorno = fileChooser.showOpenDialog(null);

        if (retorno != JFileChooser.APPROVE_OPTION) {
            System.out.println("Nenhuma imagem selecionada.");
            return;
        }

        File arquivoImagem = fileChooser.getSelectedFile();
        BufferedImage imagemOriginal;
        try {
            imagemOriginal = ImageIO.read(arquivoImagem);
            if (imagemOriginal == null) {
                JOptionPane.showMessageDialog(null, "Não foi possível carregar a imagem selecionada.", "Erro de Imagem", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo de imagem: " + e.getMessage(), "Erro de I/O", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // --- 3. Criação da Imagem de Saída ---
        int largura = imagemOriginal.getWidth();
        int altura = imagemOriginal.getHeight();
        BufferedImage imagemSegmentada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB); // Garante que a imagem de saída seja RGB

        // Cor preta
        Color corPreta = Color.BLACK;
        int rgbPreto = corPreta.getRGB();

        // --- 4 & 5. Iteração e Processamento dos Pixels ---
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rgbPixelOriginal = imagemOriginal.getRGB(x, y);
                Color corPixelOriginal = new Color(rgbPixelOriginal, true); // 'true' para considerar alpha se houver

                int rPixel = corPixelOriginal.getRed();
                int gPixel = corPixelOriginal.getGreen();
                int bPixel = corPixelOriginal.getBlue();

                // Condição de Correspondência com tolerância
                boolean rMatch = Math.abs(rPixel - rDesejado) <= tolerancia;
                boolean gMatch = Math.abs(gPixel - gDesejado) <= tolerancia;
                boolean bMatch = Math.abs(bPixel - bDesejado) <= tolerancia;

                if (rMatch && gMatch && bMatch) {
                    imagemSegmentada.setRGB(x, y, rgbPixelOriginal); // Mantém a cor original
                } else {
                    imagemSegmentada.setRGB(x, y, rgbPreto); // Define como preto
                }
            }
        }

        // --- 6. Exibição ou Salvamento da Imagem Segmentada ---
        exibirImagens(imagemOriginal, imagemSegmentada,
                String.format("Cor Alvo: R%d G%d B%d (Tol: %d)", rDesejado, gDesejado, bDesejado, tolerancia));

        int opcaoSalvar = JOptionPane.showConfirmDialog(null, "Deseja salvar a imagem segmentada?", "Salvar Imagem", JOptionPane.YES_NO_OPTION);
        if (opcaoSalvar == JOptionPane.YES_OPTION) {
            salvarImagem(imagemSegmentada);
        }
    }

    private static void exibirImagens(BufferedImage imgOriginal, BufferedImage imgSegmentada, String tituloSegmentada) {
        JFrame frame = new JFrame("Segmentação de Imagem por Cor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2)); // Layout para mostrar lado a lado

        ImageIcon iconeOriginal = new ImageIcon(imgOriginal.getScaledInstance(400, -1, Image.SCALE_SMOOTH));
        JLabel labelOriginal = new JLabel(iconeOriginal);
        labelOriginal.setBorder(BorderFactory.createTitledBorder("Imagem Original"));

        ImageIcon iconeSegmentada = new ImageIcon(imgSegmentada.getScaledInstance(400, -1, Image.SCALE_SMOOTH));
        JLabel labelSegmentada = new JLabel(iconeSegmentada);
        labelSegmentada.setBorder(BorderFactory.createTitledBorder("Segmentada: " + tituloSegmentada));

        frame.add(labelOriginal);
        frame.add(labelSegmentada);

        frame.pack();
        frame.setLocationRelativeTo(null); // Centralizar na tela
        frame.setVisible(true);
    }

    private static void salvarImagem(BufferedImage imagem) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar imagem segmentada como...");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Image", "png"));
        fileChooser.setSelectedFile(new File("imagem_segmentada.png")); // Sugestão de nome

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = fileChooser.getSelectedFile();
            try {
                String nomeArquivo = arquivoParaSalvar.getName();
                String extensao = "png"; // Padrão
                if (nomeArquivo.contains(".")) {
                    extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
                } else {
                    // Se não houver extensão, adiciona .png
                    arquivoParaSalvar = new File(arquivoParaSalvar.getAbsolutePath() + ".png");
                }

                ImageIO.write(imagem, extensao, arquivoParaSalvar);
                JOptionPane.showMessageDialog(null, "Imagem salva com sucesso em:\n" + arquivoParaSalvar.getAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao salvar a imagem: " + e.getMessage(), "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
