package viewer3D.Math;

/**
 *
 * @author Arik Dicks
 */
public class Plane {
    private Vector pointVector;
    private Vector normalVector;

    /**
     *
     * @param pointVector
     * @param normalVector
     */
    public Plane (Vector pointVector, Vector normalVector) {
        this.pointVector = pointVector;
        this.normalVector = normalVector;
    }

    /**
     *
     * @param pointVector1
     * @param pointVector2
     * @param planeVector
     */
    public Plane (Vector pointVector1, Vector pointVector2, Vector planeVector) {
        this.pointVector = pointVector1;
    }

    /**
     *
     * @return
     */
    public Vector getPointVector() {
        return pointVector;
    }

    /**
     *
     * @return
     */
    public Vector getNormalVector() {
        return normalVector;
    }
    /*
     * Let N be the normalVector    (the vector normal to the plane)
     * Let P be the pointVector     (a position vector that determines some known point on the plane)
     * Let S be the startingVector  (a position vector from which a line intersecting the plane will be created)
     * Let D be the directionVector (the vector denoting the direction of the intersection point from the startingVector position)
     * Let c be the scalingConstant (the scalar that scales Q to intersect the plane, if Q were to begin from the startingVector)
     * 
     * Intersection Equation
     *     N•(P-S)
     * c = -------
     *       N•D
     * IntersectionVector = S + c*D
     */
    /**
     * @param startingVector
     * @param directionVector
     * @return 
    */
    public Vector getIntersectingVector(Vector startingVector, Vector directionVector) {
        double scalingConstant = (normalVector.dot(pointVector.subtract(startingVector)))/(normalVector.dot(directionVector));
//        System.out.println("p - s: " + pointVector.subtract(startingVector));
//        System.out.println("n•(p - s): " + normalVector.dot(pointVector.subtract(startingVector)));
//        System.out.println("n•d: " +normalVector.dot(directionVector));
//        System.out.println();
//        System.out.println("N: " + normalVector);
//        System.out.println("P: " + pointVector);
//        System.out.println("S: " + startingVector);
//        System.out.println("D: " + directionVector);
//        System.out.println("c: " + scalingConstant);
//        System.out.println("I: " + startingVector.add(directionVector.multiply(scalingConstant)));
//        System.out.println();
        if (scalingConstant == Double.POSITIVE_INFINITY || scalingConstant == Double.NEGATIVE_INFINITY) {
            return startingVector.add(directionVector);
        }
        if (scalingConstant < 0) {
            return startingVector.add(directionVector.multiply(1/(-1*scalingConstant)));
        }
        return startingVector.add(directionVector.multiply(scalingConstant));
    }
    public Vector getIntersectingVector(Vector positionVector) {
        double scalingConstant = (pointVector.dot(normalVector))/(positionVector.dot(normalVector));
        if (scalingConstant == Double.POSITIVE_INFINITY || scalingConstant == Double.NEGATIVE_INFINITY) {
            //System.out.println(positionVector);
            return positionVector;
            //return new Vector(new double[] {0, 0, 0});
        }
        if (scalingConstant < 0) {
            return positionVector.multiply(1/(-1*scalingConstant));
        }
        return positionVector.multiply(scalingConstant);
    }
    public Vector lineIntersection(Vector directionVector) {
        if (normalVector.dot(directionVector) == 0) {
            return null;
        }
        double t = (normalVector.dot(pointVector) - normalVector.dot(directionVector)) / normalVector.dot(directionVector);
        return directionVector.add(directionVector.multiply(t));
    }

    /**
     * 
     * @param width
     * @param height
     * @param vertices
     * @return
     */
    public Vector[][] getGridOfVectors(int width, int height, Vector[] vertices) {
        Vector[][] gridVectors = new Vector[height][width];
        for (int i = 0; i < gridVectors.length; i++) {
        //for (int i = gridVectors.length-1; i >= 0 ; i--) {
            double y = 2*((double)(i)/(height-1))-1;
            for (int j = 0; j < gridVectors[0].length; j++) {    
                double x = 2*((double)j/(width-1))-1;
                gridVectors[i][j] = new Vector(new double[]{x, y, 1});
            }
        }
        return gridVectors;
    }
}