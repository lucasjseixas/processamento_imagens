package trabalho1;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class EditorDeImagens extends JFrame implements ActionListener {

    // Variáveis para armazenar as imagens
    private BufferedImage imagemOriginal;  // Armazena a imagem original carregada
    private BufferedImage imagemEditada;   // Armazena a versão editada da imagem
    private JLabel areaImagem;             // Componente para exibir a imagem na interface
    private JFileChooser seletorDeArquivos; // Componente para selecionar arquivos

    // Método Construtor
    public EditorDeImagens() {
        super("Editor de Imagens");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        areaImagem = new JLabel();
        add(new JScrollPane(areaImagem), BorderLayout.CENTER);

        JMenuBar barraDeMenu = new JMenuBar();

        JMenu menuArquivo = new JMenu("Arquivo");
        
        // Item para carregar imagem
        JMenuItem opcaoCarregar = new JMenuItem("Carregar Imagem");
        opcaoCarregar.setActionCommand("carregar");
        opcaoCarregar.addActionListener(this);
        menuArquivo.add(opcaoCarregar);

        // Item para salvar imagem
        JMenuItem opcaoSalvar = new JMenuItem("Salvar Imagem");
        opcaoSalvar.setActionCommand("salvar");
        opcaoSalvar.addActionListener(this);
        opcaoSalvar.setEnabled(false);
        menuArquivo.add(opcaoSalvar);

        barraDeMenu.add(menuArquivo);

        // Menu Efeitos
        JMenu menuEfeitos = new JMenu("Efeitos");

        // Item para resetar a imagem (voltar ao original)
        JMenuItem opcaoResetar = new JMenuItem("Resetar Imagem");
        opcaoResetar.setActionCommand("resetar");
        opcaoResetar.addActionListener(this);
        opcaoResetar.setEnabled(false);
        menuEfeitos.add(opcaoResetar);

        // Item para aplicar efeito de redemoinho (swirl)
        JMenuItem opcaoRedemoinho = new JMenuItem("Efeito Swirl");
        opcaoRedemoinho.setActionCommand("redemoinho");
        opcaoRedemoinho.addActionListener(this);
        opcaoRedemoinho.setEnabled(false);
        menuEfeitos.add(opcaoRedemoinho);

        // Item para aplicar efeito de pixelização
        JMenuItem opcaoPixelizar = new JMenuItem("Pixelize");
        opcaoPixelizar.setActionCommand("pixelizar");
        opcaoPixelizar.addActionListener(this);
        opcaoPixelizar.setEnabled(false);
        menuEfeitos.add(opcaoPixelizar);

        barraDeMenu.add(menuEfeitos);

        setJMenuBar(barraDeMenu);

        seletorDeArquivos = new JFileChooser();

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EditorDeImagens::new);
    }

    // Tratar eventos dos menus
    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        switch (comando) {
            case "carregar":
                carregarImagem();
                break;
            case "salvar":
                salvarImagem();
                break;
            case "resetar":
                resetarImagem();
                break;
            case "redemoinho":
                aplicarRedemoinho();
                break;
            case "pixelizar":
                aplicarPixelizacao();
                break;
        }
    }

    // Método para carregar uma imagem
    private void carregarImagem() {
        int resultado = seletorDeArquivos.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = seletorDeArquivos.getSelectedFile();
            try {
                imagemOriginal = ImageIO.read(arquivo);
                if (imagemOriginal != null) {
                    imagemEditada = copiarImagem(imagemOriginal);
                    atualizarExibicaoImagem();
                    habilitarEdicao(true);
                } else {
                    mostrarErro("Formato de imagem não suportado.");
                }
            } catch (IOException ex) {
                mostrarErro("Erro ao carregar a imagem.");
            }
        }
    }

    // Método para salvar a imagem editada
    private void salvarImagem() {
        int resultado = seletorDeArquivos.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = seletorDeArquivos.getSelectedFile();
            try {
                String caminho = arquivo.getAbsolutePath();
                // Extrai a extensão do arquivo (jpg, png, etc.)
                String extensao = caminho.substring(caminho.lastIndexOf('.') + 1);
                // Salva a imagem com o formato correspondente
                ImageIO.write(imagemEditada, extensao, arquivo);
                JOptionPane.showMessageDialog(this, "Imagem salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                mostrarErro("Erro ao salvar a imagem.");
            }
        }
    }

    // Método para resetar a imagem
    private void resetarImagem() {
        if (imagemOriginal != null) {
            // Cria uma nova cópia da imagem original
            imagemEditada = copiarImagem(imagemOriginal);
            atualizarExibicaoImagem();
        }
    }

    // Método para aplicar efeito de swirl na imagem
    private void aplicarRedemoinho() {
        if (imagemEditada == null) return;

        int largura = imagemEditada.getWidth();
        int altura = imagemEditada.getHeight();
        BufferedImage novaImagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        // Calcula o centro da imagem
        double centroX = largura / 2.0;
        double centroY = altura / 2.0;
        double raioMaximo = centroX * centroX + centroY * centroY;
        double intensidade = 5.0;  // Controla a força do efeito

        // Percorre todos os pixels da imagem
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                double dx = x - centroX;
                double dy = y - centroY;
                double raioAtual = dx * dx + dy * dy;

                // Aplica o efeito apenas dentro do raio máximo
                if (raioAtual < raioMaximo) {
                    // Calcula o ângulo de distorção
                    double angulo = Math.atan2(dy, dx) + intensidade * (1 - Math.sqrt(raioAtual) / Math.sqrt(raioMaximo));
                    // Calcula as novas coordenadas do pixel
                    int novoX = (int) (centroX + Math.cos(angulo) * Math.sqrt(raioAtual));
                    int novoY = (int) (centroY + Math.sin(angulo) * Math.sqrt(raioAtual));

                    // Verifica se as novas coordenadas estão dentro dos limites da imagem
                    if (novoX >= 0 && novoX < largura && novoY >= 0 && novoY < altura) {
                        novaImagem.setRGB(x, y, imagemEditada.getRGB(novoX, novoY));
                    } else {
                        novaImagem.setRGB(x, y, imagemEditada.getRGB(x, y));
                    }
                } else {
                    // Mantém o pixel original se estiver fora do raio máximo
                    novaImagem.setRGB(x, y, imagemEditada.getRGB(x, y));
                }
            }
        }

        imagemEditada = novaImagem;
        atualizarExibicaoImagem();
    }

    // Método para aplicar efeito de pixelização na imagem
    private void aplicarPixelizacao() {
        if (imagemEditada == null) return;

        int tamanhoBloco = 8;  // Tamanho do bloco de pixels que será uniformizado
        int largura = imagemEditada.getWidth();
        int altura = imagemEditada.getHeight();

        // Percorre a imagem em blocos
        for (int y = 0; y < altura; y += tamanhoBloco) {
            for (int x = 0; x < largura; x += tamanhoBloco) {
                // Calcula os limites do bloco atual
                int fimX = Math.min(x + tamanhoBloco, largura);
                int fimY = Math.min(y + tamanhoBloco, altura);

                // Variáveis para calcular a média das cores
                int somaR = 0, somaG = 0, somaB = 0, total = 0;

                // Percorre todos os pixels do bloco atual
                for (int i = y; i < fimY; i++) {
                    for (int j = x; j < fimX; j++) {
                        Color cor = new Color(imagemEditada.getRGB(j, i));
                        somaR += cor.getRed();
                        somaG += cor.getGreen();
                        somaB += cor.getBlue();
                        total++;
                    }
                }

                if (total > 0) {
                    // Calcula a cor média do bloco
                    int mediaR = somaR / total;
                    int mediaG = somaG / total;
                    int mediaB = somaB / total;
                    int corMedia = new Color(mediaR, mediaG, mediaB).getRGB();

                    // Aplica a cor média a todos os pixels do bloco
                    for (int i = y; i < fimY; i++) {
                        for (int j = x; j < fimX; j++) {
                            imagemEditada.setRGB(j, i, corMedia);
                        }
                    }
                }
            }
        }

        atualizarExibicaoImagem();
    }

    // Método para atualizar a exibição da imagem na interface
    private void atualizarExibicaoImagem() {
        if (imagemEditada != null) {
            areaImagem.setIcon(new ImageIcon(imagemEditada));
            areaImagem.revalidate();
        } else {
            areaImagem.setIcon(null);
        }
    }

    // Método auxiliar para criar uma cópia de uma imagem
    private BufferedImage copiarImagem(BufferedImage original) {
        BufferedImage copia = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics g = copia.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copia;
    }

    private void habilitarEdicao(boolean habilitar) {
        JMenuBar barra = getJMenuBar();

        for (int i = 0; i < barra.getMenuCount(); i++) {
            JMenu menu = barra.getMenu(i);
            
            if (menu != null && menu.getText().equals("Efeitos")) {
                for (int j = 0; j < menu.getItemCount(); j++) {
                    JMenuItem item = menu.getItem(j);
                    if (item != null) item.setEnabled(habilitar);
                }
            }

            if (menu != null && menu.getText().equals("Arquivo")) {
                for (int j = 0; j < menu.getItemCount(); j++) {
                    JMenuItem item = menu.getItem(j);
                    if (item != null && item.getText().equals("Salvar Imagem")) {
                        item.setEnabled(habilitar);
                    }
                }
            }
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}