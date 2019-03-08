package viewer3D.GraphicsEngine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Texture {
    BufferedImage image;
    public Texture() {
        File textureFile = new File("Textures/Pyramid.jpg");
        try {
            image = ImageIO.read(textureFile);
        } catch (IOException ex) {
            Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Color getColor(int x, int y) {
        int[] rgbArray = new int[3];
        image.getRaster().getPixel(x, y, rgbArray);
        int colorCode = rgbArray[0] << 2 | rgbArray[1] << 2 | rgbArray[2];
        return new Color(colorCode);
    }
}
