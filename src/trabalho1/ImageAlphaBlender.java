package trabalho1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageAlphaBlender extends JFrame {

    private BufferedImage image1; // Potencialmente redimensionada
    private BufferedImage image2; // Potencialmente redimensionada
    private BufferedImage blendedImage;

    // Para informações sobre as dimensões originais
    private BufferedImage originalImage1ForInfo;
    private BufferedImage originalImage2ForInfo;

    private JLabel imageDisplayLabel;
    private JButton loadImage1Button;
    private JButton loadImage2Button;
    private JButton startAnimationButton;
    private JMenuItem startAnimationMenuItem;
    private JLabel statusLabel;

    private final int ANIMATION_STEPS = 100;
    private final int ANIMATION_DELAY = 50;

    // Dimensões de referência estabelecidas pela primeira imagem carregada
    private int referenceWidth = -1;
    private int referenceHeight = -1;

    public ImageAlphaBlender() {
        setTitle("Sobreposição de Imagens com Alpha (Redimensionamento Automático)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 650); // Aumentei um pouco a altura para o statusLabel
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        createMenuBar();

        startAnimationButton.setEnabled(false);
        if (startAnimationMenuItem != null) {
            startAnimationMenuItem.setEnabled(false);
        }
    }

    private void initComponents() {
        imageDisplayLabel = new JLabel("Carregue duas imagens...", JLabel.CENTER);
        imageDisplayLabel.setPreferredSize(new Dimension(600, 400));
        imageDisplayLabel.setBorder(BorderFactory.createEtchedBorder());

        loadImage1Button = new JButton("Carregar Imagem 1");
        loadImage1Button.addActionListener(e -> loadImage(true));

        loadImage2Button = new JButton("Carregar Imagem 2");
        loadImage2Button.addActionListener(e -> loadImage(false));

        startAnimationButton = new JButton("Iniciar Animação Alpha");
        startAnimationButton.addActionListener(e -> startAlphaBlendAnimation());

        statusLabel = new JLabel("Status: Nenhuma imagem carregada.", JLabel.CENTER);
        statusLabel.setPreferredSize(new Dimension(getWidth(), 40)); // Dar mais espaço
    }

    private void layoutComponents() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.add(loadImage1Button);
        controlPanel.add(loadImage2Button);
        controlPanel.add(startAnimationButton);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(imageDisplayLabel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem loadImg1Item = new JMenuItem("Carregar Imagem 1");
        loadImg1Item.addActionListener(e -> loadImage(true));
        JMenuItem loadImg2Item = new JMenuItem("Carregar Imagem 2");
        loadImg2Item.addActionListener(e -> loadImage(false));
        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadImg1Item);
        fileMenu.add(loadImg2Item);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu effectsMenu = new JMenu("Efeitos");
        startAnimationMenuItem = new JMenuItem("Iniciar Animação Alpha");
        startAnimationMenuItem.addActionListener(e -> startAlphaBlendAnimation());
        effectsMenu.add(startAnimationMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(effectsMenu);
        setJMenuBar(menuBar);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        if (originalImage.getWidth() == targetWidth && originalImage.getHeight() == targetHeight) {
            return originalImage; // Nenhum redimensionamento necessário
        }

        // Escolhe um tipo de imagem seguro para o redimensionamento
        int imageType = originalImage.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM || imageType == 0) {
            imageType = BufferedImage.TYPE_INT_ARGB; // Um bom tipo padrão com canal alfa
        }
        
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2d = resizedImage.createGraphics();

        // Configurações para melhor qualidade de redimensionamento
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }

    private void loadImage(boolean isLoadingImage1) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(isLoadingImage1 ? "Selecione a Imagem 1" : "Selecione a Imagem 2");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "jpg", "jpeg", "png", "bmp", "gif"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage loadedImg = ImageIO.read(selectedFile);
                if (loadedImg == null) {
                    JOptionPane.showMessageDialog(this, "Não foi possível ler o arquivo de imagem selecionado.", "Erro de Leitura", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String originalDimensionsStr = loadedImg.getWidth() + "x" + loadedImg.getHeight();
                BufferedImage imageToStore = loadedImg; // Imagem final a ser armazenada (pode ser redimensionada)

                if (isLoadingImage1) {
                    originalImage1ForInfo = loadedImg; // Guarda a original para info
                    if (referenceWidth == -1 && referenceHeight == -1) { // Primeira imagem carregada no geral
                        referenceWidth = loadedImg.getWidth();
                        referenceHeight = loadedImg.getHeight();
                        statusLabel.setText("Imagem 1 (Ref: " + referenceWidth + "x" + referenceHeight + ") carregada: " + selectedFile.getName());
                    } else { // Dimensões de referência já existem
                        if (loadedImg.getWidth() != referenceWidth || loadedImg.getHeight() != referenceHeight) {
                            imageToStore = resizeImage(loadedImg, referenceWidth, referenceHeight);
                            statusLabel.setText("Imagem 1 (" + originalDimensionsStr + ") redimensionada para " + referenceWidth + "x" + referenceHeight + ".");
                        } else {
                            statusLabel.setText("Imagem 1 (" + originalDimensionsStr + ") carregada: " + selectedFile.getName());
                        }
                    }
                    image1 = imageToStore;
                } else { // isLoadingImage2
                    originalImage2ForInfo = loadedImg; // Guarda a original para info
                    if (referenceWidth == -1 && referenceHeight == -1) { // Primeira imagem carregada no geral
                        referenceWidth = loadedImg.getWidth();
                        referenceHeight = loadedImg.getHeight();
                        statusLabel.setText("Imagem 2 (Ref: " + referenceWidth + "x" + referenceHeight + ") carregada: " + selectedFile.getName());
                    } else { // Dimensões de referência já existem
                        if (loadedImg.getWidth() != referenceWidth || loadedImg.getHeight() != referenceHeight) {
                            imageToStore = resizeImage(loadedImg, referenceWidth, referenceHeight);
                            statusLabel.setText("Imagem 2 (" + originalDimensionsStr + ") redimensionada para " + referenceWidth + "x" + referenceHeight + ".");
                        } else {
                            statusLabel.setText("Imagem 2 (" + originalDimensionsStr + ") carregada: " + selectedFile.getName());
                        }
                    }
                    image2 = imageToStore;
                }
                checkImagesAndPrepare();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar imagem: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Erro ao carregar imagem: " + selectedFile.getName());
            }
        }
    }

    private void checkImagesAndPrepare() {
        startAnimationButton.setEnabled(false); // Desabilita por padrão
        if (startAnimationMenuItem != null) startAnimationMenuItem.setEnabled(false);

        if (image1 != null && image2 != null) {
            // Ambas as imagens estão carregadas e redimensionadas para referenceWidth/Height
            statusLabel.setText("Imagens prontas para mistura (" + referenceWidth + "x" + referenceHeight + ").");
            startAnimationButton.setEnabled(true);
            if (startAnimationMenuItem != null) startAnimationMenuItem.setEnabled(true);

            blendedImage = new BufferedImage(referenceWidth, referenceHeight, BufferedImage.TYPE_INT_RGB);
            performBlending(1.0); // Exibe image2 inicialmente (alpha=1.0)

        } else if (image1 != null) {
            displaySingleImage(image1, "Imagem 1");
            String dimInfo = (originalImage1ForInfo != null && (image1.getWidth() != originalImage1ForInfo.getWidth() || image1.getHeight() != originalImage1ForInfo.getHeight())) ?
                             originalImage1ForInfo.getWidth() + "x" + originalImage1ForInfo.getHeight() + " (redim. para " + image1.getWidth() + "x" + image1.getHeight() + ")" :
                             image1.getWidth() + "x" + image1.getHeight();
            statusLabel.setText("Imagem 1 carregada: " + dimInfo + ". Aguardando Imagem 2.");
        } else if (image2 != null) {
            displaySingleImage(image2, "Imagem 2");
            String dimInfo = (originalImage2ForInfo != null && (image2.getWidth() != originalImage2ForInfo.getWidth() || image2.getHeight() != originalImage2ForInfo.getHeight())) ?
                             originalImage2ForInfo.getWidth() + "x" + originalImage2ForInfo.getHeight() + " (redim. para " + image2.getWidth() + "x" + image2.getHeight() + ")" :
                             image2.getWidth() + "x" + image2.getHeight();
            statusLabel.setText("Imagem 2 carregada: " + dimInfo + ". Aguardando Imagem 1.");
        } else {
            imageDisplayLabel.setIcon(null);
            imageDisplayLabel.setText("Carregue duas imagens...");
            statusLabel.setText("Status: Nenhuma imagem carregada.");
            // Se não há imagens, reseta as dimensões de referência para a próxima primeira carga
            referenceWidth = -1;
            referenceHeight = -1;
            originalImage1ForInfo = null;
            originalImage2ForInfo = null;
        }
    }

    private void displaySingleImage(BufferedImage img, String imageName) {
        if (img != null) {
            int labelWidth = imageDisplayLabel.getWidth() > 0 ? imageDisplayLabel.getWidth() : imageDisplayLabel.getPreferredSize().width;
            int labelHeight = imageDisplayLabel.getHeight() > 0 ? imageDisplayLabel.getHeight() : imageDisplayLabel.getPreferredSize().height;

            Image scaledImg = img.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
            imageDisplayLabel.setIcon(new ImageIcon(scaledImg));
            imageDisplayLabel.setText(null);
        }
    }

    private void performBlending(double alpha) {
        if (image1 == null || image2 == null || blendedImage == null || referenceWidth == -1) {
            return;
        }

        // As imagens já devem estar com as dimensões de referenceWidth e referenceHeight
        int width = referenceWidth;
        int height = referenceHeight;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Usar true no construtor de Color para considerar o canal alfa se presente na imagem fonte (ex: PNG)
                // embora a fórmula de mistura final não use o alfa da fonte, apenas o alfa da mistura.
                Color color1 = new Color(image1.getRGB(x, y), true);
                Color color2 = new Color(image2.getRGB(x, y), true);

                double r1 = color1.getRed();
                double g1 = color1.getGreen();
                double b1 = color1.getBlue();

                double r2 = color2.getRed();
                double g2 = color2.getGreen();
                double b2 = color2.getBlue();

                int newR = (int) (r1 * (1.0 - alpha) + r2 * alpha);
                int newG = (int) (g1 * (1.0 - alpha) + g2 * alpha);
                int newB = (int) (b1 * (1.0 - alpha) + b2 * alpha);

                newR = Math.max(0, Math.min(255, newR));
                newG = Math.max(0, Math.min(255, newG));
                newB = Math.max(0, Math.min(255, newB));

                Color blendedColor = new Color(newR, newG, newB); // Resultado é RGB opaco
                blendedImage.setRGB(x, y, blendedColor.getRGB());
            }
        }
        
        int labelWidth = imageDisplayLabel.getWidth() > 0 ? imageDisplayLabel.getWidth() : imageDisplayLabel.getPreferredSize().width;
        int labelHeight = imageDisplayLabel.getHeight() > 0 ? imageDisplayLabel.getHeight() : imageDisplayLabel.getPreferredSize().height;
        Image scaledImg = blendedImage.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
        
        imageDisplayLabel.setIcon(new ImageIcon(scaledImg));
        imageDisplayLabel.setText(null);
    }

    private void startAlphaBlendAnimation() {
        if (image1 == null || image2 == null || blendedImage == null) {
            JOptionPane.showMessageDialog(this, "Carregue duas imagens primeiro.", "Imagens Faltando", JOptionPane.WARNING_MESSAGE);
            return;
        }

        startAnimationButton.setEnabled(false);
        if (startAnimationMenuItem != null) startAnimationMenuItem.setEnabled(false);
        loadImage1Button.setEnabled(false);
        loadImage2Button.setEnabled(false);
        statusLabel.setText("Animação em progresso...");

        BlendingWorker worker = new BlendingWorker();
        worker.execute();
    }

    private class BlendingWorker extends SwingWorker<Void, Double> {
        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i <= ANIMATION_STEPS; i++) {
                double alpha = 1.0 - ((double) i / ANIMATION_STEPS);
                publish(alpha);
                Thread.sleep(ANIMATION_DELAY);
            }
            return null;
        }

        @Override
        protected void process(List<Double> chunks) {
            if (!chunks.isEmpty()) {
                double alpha = chunks.get(chunks.size() - 1);
                performBlending(alpha);
                setTitle(String.format("Sobreposição de Imagens - Alpha: %.2f", alpha));
            }
        }

        @Override
        protected void done() {
            startAnimationButton.setEnabled(true);
            if (startAnimationMenuItem != null) startAnimationMenuItem.setEnabled(true);
            loadImage1Button.setEnabled(true);
            loadImage2Button.setEnabled(true);
            statusLabel.setText("Animação concluída. Alpha = 0.0 (Imagem 1 visível)");
            setTitle("Sobreposição de Imagens com Alpha (Redimensionamento Automático)");
            performBlending(0.0); // Garante o estado final
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageAlphaBlender blenderApp = new ImageAlphaBlender();
            blenderApp.setVisible(true);
        });
    }
}
