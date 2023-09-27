import javax.swing.*;
import java.awt.*;

public class PhotoComponent extends JComponent {

    private PhotoModel model;
    private PhotoUI ui;

    public PhotoComponent(Image image) {
        super();
        model = new PhotoModel(image);
        ui = new PhotoUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        ui.paint(g2, this);
    }

    public PhotoModel getModel() {
        return model;
    }

    public void setModel(PhotoModel model) {
        this.model = model;
    }

    public PhotoUI getUi() {
        return ui;
    }

    public void setUi(PhotoUI ui) {
        this.ui = ui;
    }
}
