package viewer3D;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JComponent;
import javax.swing.JFrame;
import viewer3D.Math.Vector;


public class VectorDrawer2D extends JFrame {
    int width = 400;
    int height = 400;
    DrawerComponent drawer;
    Vector[][] vectorGrid;
    Vector[] vertices;
    public VectorDrawer2D(Vector[][] vectorGrid, Vector[] vertices) {
        
        drawer = new DrawerComponent();
        drawer.setPreferredSize(new Dimension(width+20, height+20));
        drawer.setSize(new Dimension(width+20, height+20));
        this.vectorGrid = vectorGrid;
        this.vertices = vertices;
        normalize(vectorGrid);
        normalize(vertices);
        super.setPreferredSize(new Dimension(width+20, height+20));
        super.setSize(new Dimension(width+20, height+100));
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
                System.out.println("" + vertices[0] + ":" + vertices[1] + ":" + vertices[2]);
                System.out.println(vectorGrid[i][j]);
                System.out.println(isInTriangle(vectorGrid[i][j]));
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
            g2.draw(new Line2D.Double(
                    vertices[0].getPoint().getX()*width, 
                    vertices[0].getPoint().getY()*width, 
                    vertices[1].getPoint().getX()*height,
                    vertices[1].getPoint().getY()*height));
            g2.draw(new Line2D.Double(
                    vertices[1].getPoint().getX()*width, 
                    vertices[1].getPoint().getY()*width, 
                    vertices[2].getPoint().getX()*height,
                    vertices[2].getPoint().getY()*height));
            g2.draw(new Line2D.Double(
                    vertices[0].getPoint().getX()*width, 
                    vertices[0].getPoint().getY()*width, 
                    vertices[2].getPoint().getX()*height,
                    vertices[2].getPoint().getY()*height));
            for (int i = 0; i < vectorGrid.length; i++) {
                for (int j = 0; j < vectorGrid[0].length; j++) {
                    g2.fillOval(
                            (int)Math.round((vectorGrid[i][j].getComponent(0))*width)/4, 
                            (int)Math.round((vectorGrid[i][j].getComponent(1))*height)/4, 
                            6, 
                            6);
                }
            }
        }
    }
}
