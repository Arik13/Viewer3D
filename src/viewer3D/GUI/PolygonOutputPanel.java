package viewer3D.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import viewer3D.GraphicsEngine.Polygon;

/**
 * A JPanel that provides a table interface for viewing data about a set of Polygons
 * @author Arik Dicks
 */
public class PolygonOutputPanel extends JPanel {
    private boolean changedSelection;
    private int selectedPolygonIndex;
    private Polygon[] polygons;
    private final JLabel titleLabel;
    private JScrollPane shapesScrollPane;
    private JScrollPane polygonsScrollPane;
    private JTable shapesTable;
    private JTable polygonsTable;
    private String[][][] allShapesData;
    private String[][] shapesData;
    private String[][] polygonsData;
    private final String[] shapesColumnsNames = {"Shapes"};
    private final String[] polygonsColumnsNames = {"Polygons", "Vertex1", "Vertex2", "Vertex3", "Normal"};
    private final int SHAPES_NUM_OF_ROWS = 5;
    private final int POLYGONS_NUM_OF_ROWS = 5;

    /**
     * Constructs the output panel with a title 
     * @param title The title of this panel
     */
    public PolygonOutputPanel(String title) {
        this.selectedPolygonIndex = 0;
        this.changedSelection = false;
        super.setLayout(new BorderLayout());
        this.titleLabel = new JLabel(title);
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
//        titleLabel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.BLACK, 2, false) ,new EmptyBorder(10, 10, 10, 10)));
//        super.add(titleLabel, BorderLayout.NORTH);
    }

    /**
     * Sets the polygons of the polygon output panel to the given polygons, and updates
     * this panels tables with the polygons data
     * @param polygons A set of polygons
     */
    public void setPolygons(Polygon[] polygons) {   
        this.polygons = polygons;
        getPolygonData();
        if (shapesTable == null) {
            initShapesTable();
            initPolygonsTable();
            updatePolygonsTable();
            shapesTable.setRowSelectionInterval(0, 0);
        } else {
            updatePolygonsTable();
        }
    }

    /**
     * Returns true if a new polygon has been selected since this method was last called
     * @return True if a new polygon has been selected since this method was last called
     */
    public boolean changedSelection() {
        if (changedSelection) {
            changedSelection = false;
            return true;
        }
        return false;
    }
    /**
     * Initializes the shapes table with a scroll pane and a row height of 20, and adds it to this panel
     */
    private void initShapesTable() {
        shapesTable = new JTable(new DefaultTableModel(shapesData, shapesColumnsNames)) {
            @Override
            public boolean isCellEditable(int row,int column) {return false;}
        };
        shapesTable.setGridColor(Color.BLACK);
        shapesTable.setRowHeight(20);
        Dimension shapesScrollPaneSize = new Dimension(
                shapesTable.getColumnModel().getTotalColumnWidth(),
                shapesTable.getRowHeight() * shapesData.length);
        shapesTable.setPreferredScrollableViewportSize(shapesScrollPaneSize);
        shapesScrollPane = new JScrollPane(shapesTable);
        shapesScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
        shapesTable.getSelectionModel().addListSelectionListener(new ShapeSelectionListener());
        super.add(shapesScrollPane, BorderLayout.NORTH);
    }
    /**
     * Initializes the polygons table with a scroll pane and a row height of 20, and adds it to this panel
     */
    private void initPolygonsTable() {
        polygonsTable = new JTable(new DefaultTableModel(polygonsData, polygonsColumnsNames)) {
            @Override
            public boolean isCellEditable(int row,int column) {return false;}
        };
        polygonsTable.setGridColor(Color.BLACK);
        polygonsTable.setRowHeight(20);
        Dimension polygonsScrollPaneSize = new Dimension(
                polygonsTable.getColumnModel().getTotalColumnWidth(),
                polygonsTable.getRowHeight() * POLYGONS_NUM_OF_ROWS);
        polygonsTable.setPreferredScrollableViewportSize(polygonsScrollPaneSize);
        polygonsScrollPane = new JScrollPane(polygonsTable);
        polygonsScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
        polygonsTable.getSelectionModel().addListSelectionListener(new PolygonsSelectionListener());
        super.add(polygonsScrollPane, BorderLayout.CENTER);
    }
    /**
     * Extracts the shape and polygon data from the polygons array, and makes them available for the table updaters
     */
    private void getPolygonData() {
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<String> shapeIDsList = new ArrayList<>();
        String shapeName = null;
        String[][] polygonData = new String[polygons.length][0];
        
//__________________________________________________________________________________________________
        // Get an array of the names of every shape 
        // Also, get an array of the data of every polygon
        for (int i = 0; i < polygons.length; i++) {
            String[] data = {
                polygons[i].getPolygonID(), 
                polygons[i].getVertex(0).toString(), 
                polygons[i].getVertex(1).toString(), 
                polygons[i].getVertex(2).toString(),
                polygons[i].getNormal().toString()
            };
            String newShapeName = polygons[i].getShapeID();
            
            // If the current shape is different from the last one, start a new datalist for the new shape
            if (shapeName == null || !shapeName.equals(newShapeName)) {
                indices.add(i);
                shapeIDsList.add(newShapeName);
            }
            shapeName = newShapeName;
            polygonData[i] = data;
        }
        indices.add(polygons.length);
        
//__________________________________________________________________________________________________
        // Split the data of every polygon into seperate arrays, one for every shape
        allShapesData = new String[shapeIDsList.size()][0][0];
        for (int i = 1; i < indices.size(); i++) {
            int firstIndex = indices.get(i-1);
            int secondIndex = indices.get(i);
            String[][] shapeData = new String[secondIndex-firstIndex][];
            
            for (int j = 0; j < shapeData.length; j++) {
                shapeData[j] = polygonData[firstIndex+j];
            }
            allShapesData[i-1] = shapeData;
        }
//__________________________________________________________________________________________________
        // Transfer shapeID's from an arraylist to 2D array, for use by shapeTable
        shapesData = new String[shapeIDsList.size()][1];
        for (int i = 0; i < shapesData.length; i++) {
            for (int j = 0; j < shapesData[0].length; j++) {
                shapesData[i][0] = shapeIDsList.get(i);
            }
        }
    }
    /**
     * Gets the selected shape, accesses that shapes polygon data and puts in the polygons table
     */
    private void updatePolygonsTable() {
        int shapesSelectedRow = shapesTable.getSelectedRow();
        int polygonsSelectedRow = polygonsTable.getSelectedRow();
        if (shapesSelectedRow == -1) {
            shapesSelectedRow = 0;
        }
        polygonsData = allShapesData[shapesSelectedRow];
        addPolygonsData(polygonsData);
        if (polygonsSelectedRow >= polygonsData.length || polygonsSelectedRow == -1) {
            polygonsSelectedRow = 0;
        }
        
        polygonsTable.setRowSelectionInterval(polygonsSelectedRow, polygonsSelectedRow);
    }
    /**
     * Clears the polygons table and fills it with the given data
     * @param rowData 
     */
    private void addPolygonsData(String[][] rowData) {
        //clearPolygonsTable();
        ((DefaultTableModel)polygonsTable.getModel()).setRowCount(0);
        for (int i = 0; i < rowData.length; i++) {
            for (int j = 0; j < rowData[0].length; j++) {
                if (polygonsTable.getRowCount() < rowData.length) {
                    DefaultTableModel model = (DefaultTableModel) polygonsTable.getModel();
                    String[] newRow = (new String[rowData[0].length]);
                    model.addRow(newRow);
                }
                polygonsTable.getModel().setValueAt(rowData[i][j], i, j);
            }
        }
    }
    /**
     * A debug helper method that prints information about the given 2D array of strings
     * @param array2D 
     */
    private void print2DArray(String[][] array2D) {
        for (int i = 0; i < array2D.length; i++) {
            for (int j = 0; j < array2D[i].length; j++) {
                System.out.print(array2D[i][j] + " ");
            }
            System.out.println();
        }
    }
    /**
     * A debug helper method that prints information about the given 3D array of strings
     * @param array2D 
     */
    private void print3DArray(String[][][] array3D) {
        for (int i = 0; i < array3D.length; i++) {
            for (int j = 0; j < array3D[i].length; j++) {
                for (int k = 0; k < array3D[i][j].length; k++) {
                    System.out.print(array3D[i][j][k] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
    /**
     * Responsible for updating the polygons table when a new shape is selected
     */
    private class ShapeSelectionListener implements ListSelectionListener{
        @Override
        /**
         * Updates the polygons table and selects the first polygon
         */
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                updatePolygonsTable();
                polygonsTable.setRowSelectionInterval(0, 0);
                
            }
        }
    }
    /**
     * Responsible for setting the selected polygon to be highlighted, which will 
     * cause any polygon drawer classes to draw the polygon with its selection color
     * rather than its default color
     */
    private class PolygonsSelectionListener implements ListSelectionListener{
        @Override
        /** 
         * Sets the selected polygon to be selected
         */
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && polygonsTable.getSelectedRow() != -1 && shapesTable.getSelectedRow() != -1) {
                int shapeSelection = shapesTable.getSelectedRow();
                int polygonsSelection = polygonsTable.getSelectedRow();
                for (int i = 0; i < shapeSelection; i++) {
                    polygonsSelection += allShapesData[i].length;
                }
                if (polygonsSelection != selectedPolygonIndex) {
                    changedSelection = true;
                    polygons[selectedPolygonIndex].deselect();
                }
                selectedPolygonIndex = polygonsSelection;
                polygons[polygonsSelection].select();
            }
        }
    }
}