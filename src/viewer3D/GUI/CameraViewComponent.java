package viewer3D.GUI;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import viewer3D.GraphicsEngine.Polygon;

/**
 * A Component that is responsible for drawing a set of screen-space polygons given by a camera object
 * @author Arik Dicks
 */
public class CameraViewComponent extends JComponent {
    private Polygon[] polygons;
    private java.awt.Polygon[] polygons2D;
    private ArrayList<Line2D> lines;
    private double width;
    private double height;
    private String[] data;
    private LineBorder border;

    /**
     * Constructs a CameraViewComponent with a given width and height, and a set of screen-space polygons 
     * @param polygons A set of screen-space polygons
     * @param width The width of this component
     * @param height The height of this component
     */
    public CameraViewComponent(Polygon[] polygons, double width, double height) {
        //super.setFocusable(true);
        this.width = width;
        this.height = height;
        this.polygons = polygons;
        polygons2D = new java.awt.Polygon[polygons.length];
        lines = new ArrayList<>();
        border = new LineBorder(Color.BLACK, 2, false);
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
            //if (polygons[i].getIsVisible()) {
                //System.out.println("test");
                Color faceColor = polygons[i].getFaceColor();
                if (faceColor != null) {
                    g2.setColor(faceColor);
                    g2.fill(polygons2D[i]);
                }
            //}
        }
        
        // Draw Polygon Edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < polygons2D.length; i++) {
            //if (polygons[i].getIsVisible()) {
                Color edgeColor = polygons[i].getEdgeColor();
                if (edgeColor != null) {
                    g2.setColor(edgeColor);
                    g2.draw(polygons2D[i]);
                }
            //}
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
}
