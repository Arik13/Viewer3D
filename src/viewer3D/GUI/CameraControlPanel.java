package viewer3D.GUI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import viewer3D.GraphicsEngine.Camera;

/**
 * A JPanel that provides a GUI interface to the control methods of a camera
 * @author Arik Dicks
 */
public final class CameraControlPanel extends JPanel {
    private final Camera camera;
    private boolean wasCameraChanged;
    private int textFieldWidth = 5;
    
    // Camera Speed Components
    private JLabel speedLabel;
    private JSlider speedSlider;
    
    // Camera Position Components
    private JLabel positionLabel;
    private JTextField xPositionField;
    private JTextField yPositionField;
    private JTextField zPositionField;
    
    // Camera Direction Components
    private JLabel directionLabel;
    private JTextField xDirectionField;
    private JTextField yDirectionField;
    private JTextField zDirectionField;

    /**
     * Initializes and adds to itself a slider for camera speed as well as text-fields for camera position and camera direction
     * @param camera
     */
    public CameraControlPanel(Camera camera) {
        this.camera = camera;
        initSpeedComponents();
        initPositionComponents();
        initDirectionComponents();
        addComponentsToPanel();
        update();
    }

    /**
     * Refreshes the displayed information content of the panel
     */
    public void update() {
        speedSlider.setValue((int)(camera.getSpeed()));
        
        xPositionField.setText("" + String.format("%.2f", camera.getXPosition()));
        yPositionField.setText("" + String.format("%.2f", camera.getYPosition()));
        zPositionField.setText("" + String.format("%.2f", camera.getZPosition()));
        
        xDirectionField.setText("" + String.format("%.2f", camera.getXDirection()));
        yDirectionField.setText("" + String.format("%.2f", camera.getYDirection()));
        zDirectionField.setText("" + String.format("%.2f", camera.getZDirection()));
    }

    /**
     * Returns true if the control panel has changed the camera settings since this method was last called
     * @return
     */
    public boolean getWasCameraChanged() {
        if (wasCameraChanged) {
            wasCameraChanged = false;
            return true;
        }
        return false;
    }
    /**
     * Initializes the jslider which controls the camera speed to have a range of 1 - 10, with tick-spacings of 1
     */
    private void initSpeedComponents() {
        speedLabel = new JLabel("Speed");
        speedSlider = new JSlider();
        speedSlider.setMaximum(100);
        speedSlider.setMinimum(1);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.addChangeListener(new SpeedSliderListener());
    }
    /** 
     * Initializes the fields responsible for each dimension of the cameras position
     */
    private void initPositionComponents() {
        positionLabel = new JLabel("Position");
        xPositionField = new JTextField(textFieldWidth);
        yPositionField = new JTextField(textFieldWidth);
        zPositionField = new JTextField(textFieldWidth);
        
        PositionFieldActionListener positionFieldListener = new PositionFieldActionListener();
        xPositionField.addActionListener(positionFieldListener);
        yPositionField.addActionListener(positionFieldListener);
        zPositionField.addActionListener(positionFieldListener);
        
        xPositionField.addFocusListener(new TextFieldFocusListener(xPositionField));
        yPositionField.addFocusListener(new TextFieldFocusListener(yPositionField));
        zPositionField.addFocusListener(new TextFieldFocusListener(zPositionField));
    }
    /** 
     * Initializes the fields responsible for each dimension of the cameras direction
     */
    private void initDirectionComponents() {
        directionLabel = new JLabel("Direction");
        xDirectionField = new JTextField(textFieldWidth);
        yDirectionField = new JTextField(textFieldWidth);
        zDirectionField = new JTextField(textFieldWidth);
        
        DirectionFieldActionListener directionFieldListener = new DirectionFieldActionListener();
        xDirectionField.addActionListener(directionFieldListener);
        yDirectionField.addActionListener(directionFieldListener);
        zDirectionField.addActionListener(directionFieldListener);
        
        xDirectionField.addFocusListener(new TextFieldFocusListener(xDirectionField));
        yDirectionField.addFocusListener(new TextFieldFocusListener(yDirectionField));
        zDirectionField.addFocusListener(new TextFieldFocusListener(zDirectionField));
    }
    /**
     * Adds all components to the parent panel with a gridbaglayout
     */
    private void addComponentsToPanel() {
        JPanel positionFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        positionFieldsPanel.add(xPositionField);
        positionFieldsPanel.add(yPositionField);
        positionFieldsPanel.add(zPositionField);
        
        JPanel directionFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        directionFieldsPanel.add(xDirectionField);
        directionFieldsPanel.add(yDirectionField);
        directionFieldsPanel.add(zDirectionField);

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = 1;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1;
        c.weighty = 0.0;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        this.add(speedLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        c.gridy = 0;
        this.add(speedSlider, c);

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        this.add(positionLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        c.gridy = 1;
        this.add(positionFieldsPanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 2;
        this.add(directionLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        c.gridy = 2;
        this.add(directionFieldsPanel, c);
    }
    /** 
     * Changes the camera speed to reflect the slider when the slider is changed
     */
    private class SpeedSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            camera.setSpeed(speedSlider.getValue());
        }
    }
    /**
     * Attempts to parse the camera position fields and set the cameras position equal to them, and then updates the fields
     */
    private class PositionFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double x = Double.parseDouble(xPositionField.getText());
                double y = Double.parseDouble(yPositionField.getText());
                double z = Double.parseDouble(zPositionField.getText());
                camera.setPosition(x, y, z);
                camera.observe();
                wasCameraChanged = true;
            } catch(NumberFormatException a) {}
            update();
        }
    }
    /**
     * Attempts to parse the camera direction fields and set the cameras position equal to them, and then updates the fields
     */
    private class DirectionFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double x = Double.parseDouble(xPositionField.getText());
                double y = Double.parseDouble(yPositionField.getText());
                double z = Double.parseDouble(zPositionField.getText());
                camera.setDirection(x, y, z);
                camera.observe();
                wasCameraChanged = true;
            } catch(NumberFormatException a) {}
            update();
        }
    }
    /**
     * Selects the given fields text when it gains focus
     */
    private class TextFieldFocusListener implements FocusListener {
        private JTextField textField;
        public TextFieldFocusListener(JTextField textField) {
            this.textField = textField;
        }
        @Override
        public void focusGained(FocusEvent e) {
            textField.selectAll();
        }
        @Override
        public void focusLost(FocusEvent e) {

        }
    }
}