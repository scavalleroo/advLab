import fr.lri.swingstates.canvas.CStateMachine;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PhotoModel {
    private Image image;
    private List<TextAnnotation> textAnnotations = new ArrayList<>();
    private List<ColorPoint> drawingPoints = new ArrayList<>();
    public static final Point BREAK_POINT = new Point(-1, -1);

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public PhotoModel(Image image) {
        this.image = image;
        this.drawingPoints = new ArrayList<>();
        this.textAnnotations = new ArrayList<>();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        fireChangeListeners();
    }

    public List<TextAnnotation> getAnnotations() {
        return Collections.unmodifiableList(textAnnotations);
    }

    public void addAnnotation(TextAnnotation textAnnotation) {
        textAnnotation.addChangeListener(e -> fireChangeListeners());
        this.textAnnotations.add(textAnnotation);
        this.fireChangeListeners();
    }

    public List<Point> getDrawingPoints() {
        return Collections.unmodifiableList(drawingPoints);
    }

    public void addPoint(Point point, Color color) {
        ColorPoint cp = new ColorPoint(point.x, point.y, color);
        cp.addChangeListener(e -> fireChangeListeners());
        this.drawingPoints.add(cp);
        fireChangeListeners();
    }

    public int getSelectedTextAnnotation(Point click, Point origin, double scaleX, double scaleY, FontMetrics font, Function<Point, Boolean> isPointInArea) {
        for(int i = 0; i < textAnnotations.size(); i++) {
            if(textAnnotations.get(i).isAnnotationInPoint(click, origin, scaleX, scaleY, font, isPointInArea)) {
                System.out.println("Hit");
                return i;
            } else {
                System.out.println("Miss");
            }
        }
        return PhotoUI.NOT_SET;
    }

    public void drawText(Graphics g, Point origin, double scaleX, double scaleY, int imageHeight, Function<Point, Boolean> isPointInArea) {
        for(TextAnnotation annotation: textAnnotations) {
            annotation.draw(g, origin, scaleX, scaleY, imageHeight, isPointInArea);
        }
    }

    public int getSelectedLine(Point click, Point origin, double scaleX, double scaleY, int offsetHit, Function<Point, Boolean> isPointInArea) {
        int lineIndex = 0;
        for(int i = 0; i < drawingPoints.size() - 1; i++) {
            ColorPoint p = drawingPoints.get(i);
            if(p.isBreakPoint()) {
                lineIndex++;
            } else {
                if(!drawingPoints.get(i+1).isBreakPoint()) {
                    Point startPoint = new Point((int) (p.x * scaleX + origin.x), (int) (p.y * scaleY + origin.y));
                    p = drawingPoints.get(i+1);
                    Point endPoint = new Point((int) (p.x * scaleX + origin.x), (int) (p.y * scaleY + origin.y));
                    if(isClickInRangePoint(click, startPoint, endPoint, offsetHit)) {
                        return lineIndex;
                    }
                }
            }
        }
        return PhotoUI.NOT_SET;
    }

    public void moveLineAt(int index, Point click) {
        int lineIndex = 0;
        for(int i = 0; i < drawingPoints.size(); i++) {
            ColorPoint p = drawingPoints.get(i);
            if (lineIndex == index) {
                double minDistance = calculateDistance(p, click);
                ColorPoint closedPoint = p;
                int firstIndex = i;
                while(i < drawingPoints.size() && !p.isBreakPoint()) {
                    double newDistance = calculateDistance(p, click);
                    if(newDistance < minDistance) {
                        minDistance = newDistance;
                        closedPoint = p;
                    }
                    i++;
                    p = drawingPoints.get(i);
                }
                int lastIndex = i;
                movePoints(firstIndex, lastIndex, closedPoint, click);
                fireChangeListeners();
                return;
            }
            if (p.isBreakPoint()) {
                lineIndex++;
            }
        }
    }

    private void movePoints(int startIndex, int endIndex, Point referencePoint, Point click) {
        int deltaX = click.x - referencePoint.x;
        int deltaY = click.y - referencePoint.y;

        for(int i = startIndex; i < endIndex; i++) {
            drawingPoints.get(i).x = drawingPoints.get(i).x + deltaX;
            drawingPoints.get(i).y = drawingPoints.get(i).y + deltaY;
        }
    }

    public static double calculateDistance(Point point1, Point point2) {
        int x1 = point1.x;
        int y1 = point1.y;
        int x2 = point2.x;
        int y2 = point2.y;

        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public boolean isClickInRangePoint(Point click, Point lineStart, Point lineEnd, int offsetHit) {
        // Calculate the bounding box around the line with an offset
        int minX = Math.min(lineStart.x, lineEnd.x) - offsetHit;
        int maxX = Math.max(lineStart.x, lineEnd.x) + offsetHit;
        int minY = Math.min(lineStart.y, lineEnd.y) - offsetHit;
        int maxY = Math.max(lineStart.y, lineEnd.y) + offsetHit;

        // Check if the click point is within the bounding box
        return click.x >= minX && click.x <= maxX && click.y >= minY && click.y <= maxY;
    }

    public void drawLines(Graphics g, Point origin, double scaleX, double scaleY, int indexSelected, Function<Point, Boolean> isPointInArea) {
        int index = 0;
        if(!drawingPoints.isEmpty()) {
            ColorPoint start = drawingPoints.get(0);
            for (ColorPoint point: drawingPoints) {
                if(point.isBreakPoint()) {
                    index++;
                } else if(!start.isBreakPoint()) {
                    // Assign the relative coordinate to the point that is going to be printed
                    // In this way rescaling the image also rescale the lines
                    Point drawingPoint = new Point((int)((point.x  * scaleX) + origin.x), (int)((point.y * scaleY) + origin.y));
                    // Check if the new point is inside the picture area
                    if (isPointInArea.apply(drawingPoint)) {
                        g.setColor(index == indexSelected ? Color.GRAY : point.getColor());
                        g.drawLine((int)((start.x * scaleX) + origin.x), (int)((start.y * scaleY) + origin.y), drawingPoint.x, drawingPoint.y);
                    }
                }
                start = point;
            }
        }
    }

    public void updateColorOfTextAt(int index, Color newColor) {
        this.textAnnotations.get(index).setColor(newColor);
        fireChangeListeners();
    }

    public void setColorOfLineAt(int index, Color newColor) {
        int lineIndex = 0;
        int i = 0;
        while (i < drawingPoints.size()) {
            ColorPoint p = drawingPoints.get(i);
            if (lineIndex == index) {
                if (!p.isBreakPoint()) {
                    p.setColor(newColor);
                } else {
                    break;
                }
            }
            if (p.isBreakPoint()) {
                lineIndex++;
            }
            i++;
        }
        fireChangeListeners();
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        for (ChangeListener listener: changeListeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
