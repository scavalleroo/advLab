import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoModel {
    private Image image;
    private List<Annotation> annotations;
    private List<Point> drawingPoints;
    private boolean flipped;
    public static final Point BREAK_POINT = new Point(-1, -1);

    public PhotoModel(Image image) {
        this.image = image;
        this.drawingPoints = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.flipped = false;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public List<Point> getDrawingPoints() {
        return drawingPoints;
    }

    public void addPoint(Point point) {
        this.drawingPoints.add(point);
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }
}
