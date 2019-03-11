package viewer3D.Polyhedrons;

import java.awt.Color;
import viewer3D.GraphicsEngine.Polygon;
import viewer3D.Math.Vector;

public class Cuboid implements Polyhedron {
    private Polygon[] polygons;
    
    public Cuboid(int x, int y, int z, int width, int height, int depth) {
        polygons = new Polygon[12];
        // North Face
        polygons[0] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y, z+depth}),
            new Vector(new double[]{x+width, y, z+depth}),
            new Vector(new double[]{x, y+height, z+depth})
        });
        polygons[1] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y+height, z+depth}),
            new Vector(new double[]{x+width, y, z+depth}),
            new Vector(new double[]{x+width, y+height, z+depth})
        });
        polygons[0].setFaceColor(Color.GRAY);
        polygons[1].setFaceColor(Color.GRAY);
        polygons[0].setPolygonID("North1");
        polygons[1].setPolygonID("North2");
        
        // West Face
        polygons[2] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y, z}),
            new Vector(new double[]{x, y, z+depth}),
            new Vector(new double[]{x, y+height, z+depth})
        });
        polygons[3] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y+height, z}),
            new Vector(new double[]{x, y, z}),
            new Vector(new double[]{x, y+height, z+depth})
        });
        polygons[2].setFaceColor(Color.DARK_GRAY);
        polygons[3].setFaceColor(Color.DARK_GRAY);
        polygons[2].setPolygonID("West1");
        polygons[3].setPolygonID("West2");
        
        // South Face
        polygons[4] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x, y, z}),
            new Vector(new double[]{x, y+height, z})
        });
        polygons[5] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x, y+height, z}),
            new Vector(new double[]{x+width, y+height, z})
        });
        polygons[4].setFaceColor(Color.GRAY);
        polygons[5].setFaceColor(Color.GRAY);
        polygons[4].setPolygonID("South1");
        polygons[5].setPolygonID("South2");
        
        // East Face
        polygons[6] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y, z+depth}),
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x+width, y+height, z+depth})
        });
        polygons[7] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x+width, y+height, z}),
            new Vector(new double[]{x+width, y+height, z+depth})
        });
        polygons[6].setFaceColor(Color.DARK_GRAY);
        polygons[7].setFaceColor(Color.DARK_GRAY);
        polygons[6].setPolygonID("East1");
        polygons[7].setPolygonID("East2");
        
        // Top Face
        polygons[8] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y+height, z}),
            new Vector(new double[]{x, y+height, z}),
            new Vector(new double[]{x, y+height, z+depth})
        });
        polygons[9] = new Polygon(new Vector[]{
            new Vector(new double[]{x+width, y+height, z}),
            new Vector(new double[]{x, y+height, z+depth}),
            new Vector(new double[]{x+width, y+height, z+depth})
        });
        polygons[8].setFaceColor(Color.WHITE);
        polygons[9].setFaceColor(Color.WHITE);
        polygons[8].setPolygonID("Top1");
        polygons[9].setPolygonID("Top2");
        
        // Bottom Face
        polygons[10] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y, z}),
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x, y, z+depth})
        });
        polygons[11] = new Polygon(new Vector[]{
            new Vector(new double[]{x, y, z+depth}),
            new Vector(new double[]{x+width, y, z}),
            new Vector(new double[]{x+width, y, z+depth})
        });
        polygons[10].setFaceColor(Color.WHITE);
        polygons[11].setFaceColor(Color.WHITE);
        polygons[10].setPolygonID("Bottom1");
        polygons[11].setPolygonID("Bottom2");
    }
    @Override
    public Polygon[] getPolygons() {
        return polygons;
    }

    @Override
    public Vector getNormal() {
        return new Vector(new double[]{0, 0, 0});
    }

    @Override
    public String getID() {
        return "Cuboid";
    }
    
}
