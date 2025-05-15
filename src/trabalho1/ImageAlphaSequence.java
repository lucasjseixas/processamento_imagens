package trabalho1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

	public class ImageAlphaSequence {
	    public static void main(String[] args) {
	        try {
	            // Carregar imagem original
	            BufferedImage original = ImageIO.read(new File("C:\\Users\\lucas\\eclipse-workspace\\Trabalho1_PDI\\src\\trabalho1\\imagem.png"));

	            // Definir valores de alpha para a sequência (0 = preto, 1 = normal)
	            float[] alphas = {0.0f, 0.33f, 0.66f, 1.0f};

	            for (int i = 0; i < alphas.length; i++) {
	                BufferedImage newImage = applyAlpha(original, alphas[i]);
	                File output = new File("output_" + i + ".png");
	                ImageIO.write(newImage, "png", output);
	            }

	            System.out.println("Sequência criada com sucesso!");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private static BufferedImage applyAlpha(BufferedImage original, float alphaFactor) {
	        int width = original.getWidth();
	        int height = original.getHeight();

	        // Criar nova imagem com canal alfa
	        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	        Graphics2D g = newImage.createGraphics();
	        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFactor));
	        g.drawImage(original, 0, 0, null);
	        g.dispose();

	        return newImage;
	    }
	}
	
