package viewer3D.GUI;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import viewer3D.GraphicsEngine.Camera;
import viewer3D.GraphicsEngine.Polygon;
import static viewer3D.GraphicsEngine.Rotation.*;

/**
 * A Component that is responsible for drawing a set of screen-space polygons given by a camera object
 * @author Arik Dicks
 */
public class CameraViewComponent extends JComponent {
    private Camera camera;
    private Polygon[] polygons;
    private java.awt.Polygon[] polygons2D;
    private double width;
    private double height;
    private String[] data;
    private LineBorder border;
    private int mouseSensitivity;
    private boolean wasUpdated;
    private Robot cursorSetter;
    /**
     * Constructs a CameraViewComponent with a given width and height, and a set of screen-space polygons 
     * @param camera
     * @param polygons A set of screen-space polygons
     * @param width The width of this component
     * @param height The height of this component
     */
    public CameraViewComponent(Camera camera, Polygon[] polygons, double width, double height) {
        super.setCursor(super.getToolkit().createCustomCursor(
                   new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ),
                   new Point(),
                   null));
        this.camera = camera;
        this.width = width;
        this.height = height;
        this.polygons = polygons;
        polygons2D = new java.awt.Polygon[polygons.length];
        border = new LineBorder(Color.BLACK, 2, false);
        mouseSensitivity = 5;
        try {
            cursorSetter = new Robot();
        } catch (AWTException ex) {}
        //cursorSetter.mouseMove(300, 300);
        super.addMouseMotionListener(new CursorPositionListener());
    }
    
    /**
     * Draws the polygon faces, edges, camera data and border
     * @param g The graphics object that is drawn on
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        set2DPolygons();
        
        // Draw Polygon Faces
        for (int i = 0; i < polygons2D.length; i++) {
            Color faceColor = polygons[i].getFaceColor();
            if (faceColor != null) {
                g2.setColor(faceColor);
                g2.fill(polygons2D[i]);
            }
        }
        
        // Draw Polygon Edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < polygons2D.length; i++) {
            Color edgeColor = polygons[i].getEdgeColor();
            if (edgeColor != null) {
                g2.setColor(edgeColor);
                g2.draw(polygons2D[i]);
            }
        }
        
        // Draw Camera Data
        FontMetrics metrics = g2.getFontMetrics(this.getFont());
        int fontHeight = metrics.getHeight();
        for (int i = 0; i < data.length; i++) {
            g2.drawString(data[i], 10, (i+1)*fontHeight + border.getThickness());
        }
        
        // Draw Border
        border.paintBorder(this, g, 0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Updates the data of the camera
     * @param data Camera data: (frame-rate, position, direction etc.)
     */
    public void updateData(String[] data) {
        this.data = data;
    }

    /**
     * Draws the given polygons
     * @param polygons The polygons to be drawn
     */
    public void drawPolygons(Polygon[] polygons) {
        this.polygons = polygons;
        repaint();
    }
    private void set2DPolygons() {
        for (int i = 0; i < polygons.length; i++) {
            double x1 = polygons[i].getVertex(0).getComponent(0)*width;
            double y1 = height - polygons[i].getVertex(0).getComponent(1)*height;
            double x2 = polygons[i].getVertex(1).getComponent(0)*width;
            double y2 = height - polygons[i].getVertex(1).getComponent(1)*height;
            double x3 = polygons[i].getVertex(2).getComponent(0)*width;
            double y3 = height - polygons[i].getVertex(2).getComponent(1)*height;
            
            polygons2D[i] = new java.awt.Polygon();
            polygons2D[i].addPoint((int)Math.round(x1), (int)Math.round(y1));
            polygons2D[i].addPoint((int)Math.round(x2), (int)Math.round(y2));
            polygons2D[i].addPoint((int)Math.round(x3), (int)Math.round(y3));
        }
    }
    public boolean wasUpdated() {
        if (wasUpdated) {
            wasUpdated = false;
            return true;
        }
        return false;
    }
    private int getAbsoluteCenterX() {
        return super.getLocation().x + super.getWidth()/2;
    }
    private int getAbsoluteCenterY() {
        return super.getLocation().y + super.getHeight()/2;
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
        public void mouseDragged(MouseEvent e) {

        }

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
                //camera.rotate(YAW_LEFT, xAngle);
                deltaTheta = xAngle;
                wasUpdated = true;
            } else if (e.getX() > getCenterX())  {
                //camera.rotate(YAW_RIGHT, xAngle);
                deltaTheta = -xAngle;
                wasUpdated = true;
            }
            if (e.getY() < getCenterY()) {
                //camera.rotate(PITCH_FORWARD, yAngle);
                deltaPhi = yAngle;
                wasUpdated = true;
            } else if (e.getY() > getCenterY())  {
                //camera.rotate(PITCH_BACKWARD, yAngle);
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
