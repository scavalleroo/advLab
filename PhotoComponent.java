import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhotoComponent extends JComponent {

    private final PhotoModel model;
    private final PhotoUI ui;
    private boolean flipped;
    private Color selectedColor; // Store the selected color

    public PhotoComponent(Image image) {
        super();
        model = new PhotoModel(image);
        model.addChangeListener(e -> repaint());
        ui = new PhotoUI(this);
        flipped = false;

        selectedColor = Color.BLACK;
        // Create a toolbar and add a button for selecting color
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton colorButton = new JButton("Pen color");
        toolbar.add(colorButton);

        // Add an ActionListener to the color button
        colorButton.addActionListener(e -> selectColor());

        // Add the toolbar to the component
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
    }

    // Method to open the JColorChooser
    private void selectColor() {
        Color newColor = JColorChooser.showDialog(this, "Select Color", selectedColor);
        if (newColor != null) {
            selectedColor = newColor;
            ui.updateColorsOfItemsSelected(model, newColor);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        // Set the selected color
        g2.setColor(selectedColor);

        ui.paint(g2, this);

        setFocusable(true);
        requestFocusInWindow();
    }

    public PhotoModel getModel() {
        return model;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        repaint();
    }

    public Color getColor() {
        return selectedColor;
    }
}
