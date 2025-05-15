package trabalho1;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class VisualizadorPGM {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> iniciar());
    }

    private static void iniciar() {	
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo PGM (P5)");
        int resultado = fileChooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                BufferedImage imagem = lerPGM(arquivo);
                exibirImagem(imagem);
                perguntarSalvar(imagem);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo PGM:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private static BufferedImage lerPGM(File arquivo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            // Ler magic number (P5)
            String magic = raf.readLine();
            if (magic == null || !magic.startsWith("P5")) {
                throw new IOException("Formato não suportado. Esperado P5, encontrado: " + magic);
            }

            // Ler largura e altura (pulando comentários)
            String[] dimensoes = lerDimensoes(raf);
            int largura = Integer.parseInt(dimensoes[0]);
            int altura = Integer.parseInt(dimensoes[1]);

            // Ler valor máximo
            String maxValLine = raf.readLine();
            int maxVal = Integer.parseInt(maxValLine.trim());
            if (maxVal > 255) {
                throw new IOException("Somente PGM com maxVal <= 255 são suportados.");
            }

            // Posição atual após o cabeçalho
            long posDados = raf.getFilePointer();

            // Verificar tamanho do arquivo
            long tamanhoEsperado = largura * altura;
            if (arquivo.length() - posDados < tamanhoEsperado) {
                throw new IOException(String.format(
                    "Arquivo incompleto. Esperados %d bytes, disponíveis %d bytes.",
                    tamanhoEsperado, arquivo.length() - posDados
                ));
            }

            // Ler dados da imagem
            byte[] dados = new byte[(int) tamanhoEsperado];
            raf.readFully(dados);

            BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_GRAY);
            img.getRaster().setDataElements(0, 0, largura, altura, dados);
            return img;
        }
    }

    private static String[] lerDimensoes(RandomAccessFile raf) throws IOException {
        String linha;
        do {
            linha = raf.readLine();
            if (linha == null) throw new IOException("Fim do arquivo inesperado");
            if (linha.startsWith("#")) continue;
            
            String[] tokens = linha.split("\\s+");
            if (tokens.length >= 2) {
                return new String[]{tokens[0], tokens[1]};
            }
            
            // Combina com próxima linha se necessário
            String proximaLinha = raf.readLine();
            if (proximaLinha == null) throw new IOException("Dimensões incompletas");
            linha = linha + " " + proximaLinha;
        } while (true);
    }

    private static void exibirImagem(BufferedImage img) {
        JFrame frame = new JFrame("Visualizador PGM (P5)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icone = new ImageIcon(img);
        JLabel label = new JLabel(icone);
        frame.getContentPane().add(new JScrollPane(label), BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void perguntarSalvar(BufferedImage img) {
        int opcao = JOptionPane.showConfirmDialog(
            null,
            "Deseja exportar a imagem para PNG/JPG/BMP/GIF?",
            "Exportar",
            JOptionPane.YES_NO_OPTION
        );
        if (opcao == JOptionPane.YES_OPTION) {
            salvarImagem(img);
        }
    }

    private static void salvarImagem(BufferedImage img) {
    // Caixa de diálogo para escolher o formato
    String[] formatos = {"PNG", "JPEG", "BMP", "GIF"};
    String formatoEscolhido = (String) JOptionPane.showInputDialog(
        null,
        "Escolha o formato de saída:",
        "Exportar Imagem",
        JOptionPane.QUESTION_MESSAGE,
        null,
        formatos,
        formatos[0] // Padrão: PNG
    );

    if (formatoEscolhido == null) return; // Usuário cancelou

    // Mapeia a escolha para extensões e formatos do ImageIO
    String extensao = formatoEscolhido.toLowerCase();
    String formato;
    switch (formatoEscolhido) {
        case "JPEG": formato = "jpg"; break;
        default:     formato = extensao; break;
    }

    // Diálogo para escolher local/nome do arquivo
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Salvar como " + formatoEscolhido);
    fileChooser.setSelectedFile(new File("imagem." + extensao));
    
    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        File arquivo = fileChooser.getSelectedFile();
        try {
            if (ImageIO.write(img, formato, arquivo)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Imagem salva como " + formatoEscolhido + ":\n" + arquivo.getAbsolutePath(),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Erro: formato " + formatoEscolhido + " não suportado.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Falha ao salvar:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
}