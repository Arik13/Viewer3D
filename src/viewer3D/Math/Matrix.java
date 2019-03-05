package viewer3D.Math;

/**
 * Supports matrix multiplication and 2D & 3D rotation matrices
 * @author Arik Dicks
 */
public class Matrix {
    double[][] matrix;

    /**
     * Constructs a matrix with the given rows and columns with default values of 0
     * @param rows The rows of this matrix
     * @param columns The columns of this matrix
     */
    public Matrix(int rows, int columns) {
        matrix = new double[rows][columns];
    }

    /**
     * Constructs a matrix from the given 2D array of doubles
     * @param matrix A 2D array of doubles
     */
    public Matrix(double[][] matrix) {
        this.matrix = matrix;
    }

    /**
     * Constructs a new matrix with the same contents of another matrix
     * @param matrix The parent matrix
     */
    public Matrix(Matrix matrix) {
        this.matrix = matrix.matrix.clone();
    }

    /**
     * *NOT IMPLEMENTED*
     * @param otherMatrix
     * @return
     */
    public Matrix multiply(Matrix otherMatrix) {
        Matrix newMatrix = new Matrix(matrix.length, otherMatrix.matrix[0].length);
        // Loop through new matrix
        for (int i = 0; i < newMatrix.matrix.length; i++) {
            for (int j = 0; j < newMatrix.matrix[0].length; j++) {
                // Loop through first matrix and 2nd matrix
                //System.out.println(newMatrix);
                for (int k = 0; k < matrix.length; k++) {
                    //System.out.println("Cell " + i + ":" + j + "(" + newMatrix.matrix[i][j] + ")" +" += " + matrix[i][k] + "*" + otherMatrix.matrix[k][i]);
                    newMatrix.matrix[i][j] += matrix[i][k]*otherMatrix.matrix[k][j];
                    //System.out.println(newMatrix.matrix[i][j]);
                    
                }
                //newMatrix.matrix[i][j] += matrix[i][j]*otherMatrix.matrix[j][i];
            }
        }
        return newMatrix;
    }

    /**
     * *NOT IMPLEMENTED*
     * @param vector
     * @return
     */
    public Vector multiply(Vector vector) {
        return new Vector(0);
    }

    /**
     * Returns the number of rows in this matrix
     * @return the number of rows in this matrix
     */
    public int getNumOfRows() {
        return matrix.length;
    }

    /**
     * Returns the number of columns in this matrix
     * @return the number of columns in this matrix
     */
    public int getNumOfColumns() {
        return matrix[0].length;
    }

    /**
     * Returns the element at the given row and column
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @return the element at the given row and column
     */
    public double getElement(int rowIndex, int columnIndex) {
        return matrix[rowIndex][columnIndex];
    }

    /**
     * Returns the 2D rotation matrix for the given angle
     * @param theta The given angle
     * @return the 2D rotation matrix for the given angle
     */
    public static Matrix get2DXRotationMatrix(double theta) {
        double[][] xRotationMatrix = new double[2][2];
        xRotationMatrix[0][0] = Math.cos(theta);
        xRotationMatrix[0][1] = -Math.sin(theta);
        xRotationMatrix[1][0] = Math.sin(theta);
        xRotationMatrix[1][1] = Math.cos(theta);
        return new Matrix(xRotationMatrix);
    }

    /**
     * Returns the 3D rotation matrix around the x axis for the given angle
     * @param theta The given angle
     * @return the 3D rotation matrix around the x axis for the given angle
     */
    public static Matrix get3DXRotationMatrix(double theta) {
        double[][] xRotationMatrix = new double[3][3];
        xRotationMatrix[0][0] = 1;
        xRotationMatrix[0][1] = 0;
        xRotationMatrix[0][2] = 0;
        xRotationMatrix[1][0] = 0;
        xRotationMatrix[1][1] = Math.cos(theta);
        xRotationMatrix[1][2] = -Math.sin(theta);
        xRotationMatrix[2][0] = 0;
        xRotationMatrix[2][1] = Math.sin(theta);
        xRotationMatrix[2][2] = Math.cos(theta);
        return new Matrix(xRotationMatrix);
    }

    /**
     * Returns the 3D rotation matrix around the y axis for the given angle
     * @param theta The given angle
     * @return the 3D rotation matrix around the y axis for the given angle
     */
    public static Matrix get3DYRotationMatrix(double theta) {
        double[][] yRotationMatrix = new double[3][3];
        yRotationMatrix[0][0] = Math.cos(theta);
        yRotationMatrix[0][1] = 0;
        yRotationMatrix[0][2] = Math.sin(theta);
        yRotationMatrix[1][0] = 0;
        yRotationMatrix[1][1] = 1;
        yRotationMatrix[1][2] = 0;
        yRotationMatrix[2][0] = -Math.sin(theta);
        yRotationMatrix[2][1] = 0;
        yRotationMatrix[2][2] = Math.cos(theta);
        return new Matrix(yRotationMatrix);
    }

    /**
     * Returns the 3D rotation matrix around the z axis for the given angle
     * @param theta The given angle
     * @return the 3D rotation matrix around the z axis for the given angle
     */
    public static Matrix get3DZRotationMatrix(double theta) {
        double[][] zRotationMatrix = new double[3][3];
        zRotationMatrix[0][0] = Math.cos(theta);
        zRotationMatrix[0][1] = -Math.sin(theta);
        zRotationMatrix[0][2] = 0;
        zRotationMatrix[1][0] = Math.sin(theta);
        zRotationMatrix[1][1] = Math.cos(theta);
        zRotationMatrix[1][2] = 0;
        zRotationMatrix[2][0] = 0;
        zRotationMatrix[2][1] = 0;
        zRotationMatrix[2][2] = 1;
        return new Matrix(zRotationMatrix);
    }
    @Override
    public String toString() {
        String objStr = getClass().getName() + "\n";
        for (int i = 0; i < matrix.length; i++) {
            objStr += "{";
            for (int j = 0; j < matrix[0].length; j++) {
                objStr += matrix[i][j] + ", ";
            }
            objStr = objStr.substring(0, objStr.length()-2) + "}\n";
        }
        return objStr;
    }
}
