package viewer3D.GUI;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import viewer3D.GraphicsEngine.Camera;

/**
 * A Component that is responsible for drawing a set of screen-space polygons given by a camera object
 * @author Arik Dicks
 */
public class CameraViewComponent extends JComponent {
    private Camera camera;
    private String[] data;
    private LineBorder border;
    private int mouseSensitivity;
    private boolean wasUpdated;
    private Robot cursorSetter;
    private BufferedImage image;
    /**
     * Constructs a CameraViewComponent with a given width and height, and a set of screen-space polygons 
     * @param camera
     */
    public CameraViewComponent(Camera camera) {
        super.setCursor(super.getToolkit().createCustomCursor(
                   new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ),
                   new Point(),
                   null));
        this.camera = camera;
        border = new LineBorder(Color.BLACK, 2, false);
        mouseSensitivity = 5;
        try {
            cursorSetter = new Robot();
        } catch (AWTException ex) {}
        cursorSetter.mouseMove(300, 300);
        super.addMouseMotionListener(new CursorPositionListener());
    }
    
    /**
     * Draws the polygon faces, edges, camera data and border
     * @param g The graphics object that is drawn on
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(image, 0, 0, null);

        // Draw Camera Data
        FontMetrics metrics = g2.getFontMetrics(this.getFont());
        g2.setColor(Color.WHITE);
        int fontHeight = metrics.getHeight();
        for (int i = 0; i < data.length; i++) {
            g2.drawString(data[i], 10, (i+1)*fontHeight + border.getThickness());
        }
    }
    
    /**
     * Sets the image of this view
     * @param image A BufferedImage
     */
    public void updateImage(BufferedImage image) {
        this.image = image;
        repaint();
    }
    /**
     * Updates the data of the camera
     * @param data Camera data: (frame-rate, position, direction etc.)
     */
    public void updateData(String[] data) {
        this.data = data;
    } 
    public boolean wasUpdated() {
        if (wasUpdated) {
            wasUpdated = false;
            return true;
        }
        return false;
    }
    private int getCenterX() {
        return super.getWidth()/2;
    }
    private int getCenterY() {
        return super.getHeight()/2;
    }
    private int getComponentWidth() {
        return super.getWidth();
    }
    private int getComponentHeight() {
        return super.getHeight();
    }
    private class CursorPositionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {
            int deltaX = Math.abs(e.getX()-getCenterX());
            int deltaY = Math.abs(e.getY()-getCenterY());
            double xRatio = deltaX/(double)getComponentWidth();
            double yRatio = deltaY/(double)getComponentHeight();
            int xAngle = (int)Math.round((xRatio*mouseSensitivity)*45);
            int yAngle = (int)Math.round((yRatio*mouseSensitivity)*45);
            int deltaTheta = 0;
            int deltaPhi = 0;
            
            if (e.getX() < getCenterX()) {
                deltaTheta = -xAngle;
                wasUpdated = true;
            } else if (e.getX() > getCenterX())  {
                deltaTheta = xAngle;
                wasUpdated = true;
            }
            if (e.getY() < getCenterY()) {
                deltaPhi = yAngle;
                wasUpdated = true;
            } else if (e.getY() > getCenterY())  {
                deltaPhi = -yAngle;
                wasUpdated = true;
            }
            if (wasUpdated) {
                camera.rotate(deltaTheta, deltaPhi);
                cursorSetter.mouseMove(getCenterX()+5, getCenterY()+50);
            }
        }
    }
}
