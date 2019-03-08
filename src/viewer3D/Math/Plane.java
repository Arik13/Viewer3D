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
            return positionVector;
        }
        if (scalingConstant < 0) {
            return positionVector.multiply(1/(-1*scalingConstant));
        }
        return positionVector.multiply(scalingConstant);
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

//        gridVectors[0][0] = vertices[0];
//        gridVectors[0][width-1] = vertices[1];
//        gridVectors[height-1][0] = vertices[2];
//        gridVectors[height-1][width-1] = vertices[3];
//        calcColMiddleVector(gridVectors, 0, width-1, 0);
//        calcColMiddleVector(gridVectors, 0, width-1, height-1);
//        for (int i = 0; i < gridVectors.length; i++) {
//            calcRowMiddleVector(gridVectors[i], 0, width-1);
//        }
//        return gridVectors;
    }

    /**
     *
     * @param vectorArray
     * @param lo
     * @param hi
     */
    public void calcRowMiddleVector(Vector[] vectorArray, int lo, int hi) {
        if (hi==lo+1)
            return;
        int mid = (hi-lo)/2 + lo;
        vectorArray[mid] = vectorArray[lo].add((vectorArray[hi].subtract(vectorArray[lo])).multiply(0.5));
        calcRowMiddleVector(vectorArray, lo, mid);
        calcRowMiddleVector(vectorArray, mid, hi);
    }

    /**
     *
     * @param vectorArray
     * @param lo
     * @param hi
     * @param col
     */
    public void calcColMiddleVector(Vector[][] vectorArray, int lo, int hi, int col) {
        if (hi==lo+1)
            return;
        int mid = (hi-lo)/2 + lo;
        vectorArray[mid][col] = vectorArray[lo][col].add((vectorArray[hi][col].subtract(vectorArray[lo][col])).multiply(0.5));
        calcColMiddleVector(vectorArray, lo, mid, col);
        calcColMiddleVector(vectorArray, mid, hi, col);
    }
}
