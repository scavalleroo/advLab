import java.awt.*;

public class Annotation {
    private Point insertionPoint;
    private String text;


    public Annotation(String text, Point insertionPoint) {
        this.text = text;
        this.insertionPoint = insertionPoint;
    }

    public int getX() {
        return (int) insertionPoint.getX();
    }

    public int getY() {
        return (int) insertionPoint.getY();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
