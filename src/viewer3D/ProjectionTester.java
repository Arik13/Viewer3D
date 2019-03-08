package viewer3D;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JComponent;
import javax.swing.JFrame;
import viewer3D.GraphicsEngine.ProjectedPolygon;
import viewer3D.Math.Vector;


public class ProjectionTester extends JFrame {
    int width = 500;
    int height = 500;
    DrawerComponent drawer;
    Vector[][] vectorGrid;
    Vector[] vertices;
    public ProjectionTester(Vector[][] vectorGrid, ProjectedPolygon polygon) {
        vertices = polygon.getVertices();
        drawer = new DrawerComponent();
        drawer.setPreferredSize(new Dimension(width, height));
        drawer.setSize(new Dimension(width, height));
        this.vectorGrid = vectorGrid;
        this.vertices = vertices;
        //normalize(vectorGrid);
        //normalize(vertices);
        super.setPreferredSize(new Dimension(width, height));
        super.setSize(new Dimension(width, height));
        super.setVisible(true);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.add(drawer);
        //super.pack();
    }
    public boolean isInTriangle(Vector pointVector) {
        double x = pointVector.getComponent(0);
        double y = pointVector.getComponent(1);
        double x1 = vertices[0].getComponent(0);
        double y1 = vertices[0].getComponent(1);
        double x2 = vertices[1].getComponent(0);
        double y2 = vertices[1].getComponent(1);
        double x3 = vertices[2].getComponent(0);
        double y3 = vertices[2].getComponent(1);
        
        double denominator = ((y2 - y3)*(x1 - x3) + (x3 - x2)*(y1 - y3));
        double a = ((y2 - y3)*(x - x3) + (x3 - x2)*(y - y3))/denominator;
        double b = ((y3 - y1)*(x - x3) + (x1 - x3)*(y - y3))/denominator;
        double c = 1 - a - b;
        return 0 <= a && a <= 1 && 0 <= b && b <= 1 && 0 <= c && c <= 1;
    }
    public void normalize(Vector[][] vectorGrid) {
        for (int i = 0; i < vectorGrid.length; i++) {
            for (int j = 0; j < vectorGrid[0].length; j++) {
//                System.out.println("Vertices: " + vertices[0] + ":" + vertices[1] + ":" + vertices[2]);
//                System.out.println("ProjectionPoints: " + vectorGrid[i][j]);
                //System.out.println(isInTriangle(vectorGrid[i][j]));
            }
        }
    }
    public void normalize(Vector[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = vertices[i].getUnitVector();
        }
    }
    private class DrawerComponent extends JComponent {
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            Line2D l1 = new Line2D.Double(
                    vertices[0].getComponent(0)*width, 
                    vertices[0].getComponent(1)*height, 
                    vertices[1].getComponent(0)*width,
                    vertices[1].getComponent(1)*height);
            Line2D l2 = new Line2D.Double(
                    vertices[1].getComponent(0)*width, 
                    vertices[1].getComponent(1)*height, 
                    vertices[2].getComponent(0)*width,
                    vertices[2].getComponent(1)*height);
            Line2D l3 = new Line2D.Double(
                    vertices[0].getComponent(0)*width, 
                    vertices[0].getComponent(1)*height, 
                    vertices[2].getComponent(0)*width,
                    vertices[2].getComponent(1)*height);
//            System.out.println("Line1: " + vertices[0] + " to " + vertices[1]);
//            System.out.println("Line2: " + vertices[1] + " to " + vertices[2]);
//            System.out.println("Line3: " + vertices[0] + " to " + vertices[2]);
//            System.out.println();
            g2.draw(l1);
            g2.draw(l2);
            g2.draw(l3);
            for (int i = 0; i < vectorGrid.length; i++) {
                for (int j = 0; j < vectorGrid[0].length; j++) {
                    g2.fillOval((int)Math.round((vectorGrid[i][j].getComponent(0))*width), 
                            (int)Math.round((vectorGrid[i][j].getComponent(1))*height), 
                            2, 
                            2);
                }
            }
        }
    }
}
