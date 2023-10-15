import javax.swing.*;
import java.awt.*;

public class PhotoComponent extends JComponent {

    private final PhotoModel model;
    private final PhotoUI ui;
    private boolean flipped;

    public PhotoComponent(Image image) {
        super();
        model = new PhotoModel(image);
        model.addChangeListener(e -> repaint());
        ui = new PhotoUI(this);
        flipped = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

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
}
