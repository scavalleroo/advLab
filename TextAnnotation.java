import fr.lri.swingstates.canvas.CStateMachine;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TextAnnotation {
    private Point insertionPoint;
    private String text;
    private boolean hasCursor = false;
    private List<ChangeListener> changeListeners = new ArrayList<>();
    private Color color;

    public TextAnnotation(String text, Point insertionPoint, Color color) {
        this.text = text;
        this.insertionPoint = insertionPoint;
        this.color = color;
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        for(ChangeListener listener: changeListeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
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
        this.fireChangeListeners();
    }

    public void setOrigin(Point origin) {
        this.insertionPoint = origin;
        this.fireChangeListeners();
    }

    public void setHasCursor(boolean hasCursor) {
        this.hasCursor = hasCursor;
    }

    public boolean hasCursor() {
        return hasCursor;
    }

    public Point getOrigin() {
        return insertionPoint;
    }

    public boolean isAnnotationInPoint(Point click, Point origin, double scaleX, double scaleY, FontMetrics font, Function<Point, Boolean> isPointInArea) {
        int y = (int) ((insertionPoint.y * scaleY) + origin.y);
        int i = 0; // index of the char in the word
        char[] word = text.toCharArray();
        String printedWord = ""; // word to be printed
        int startX = (int) ((insertionPoint.x * scaleX) + origin.x); // x0 coordinate of the word
        int endX = startX + font.stringWidth(Arrays.toString(word));
        System.out.println("Start X before: " + startX);
        System.out.println("End X before: " + endX);
        if(!isPointInArea.apply(new Point(endX, y))) {
            while(i < word.length) {
                printedWord += word[i];
                endX = startX + font.stringWidth(printedWord);
                if(!isPointInArea.apply(new Point(endX, y))) {
                    printedWord = word[i] + "";
                    y += font.getHeight() + 1;
                }
                if (isPointOverText(click, origin, scaleX, scaleY, font, y, startX, endX)) return true;
                i++;
            }
        } else {
            printedWord = Arrays.toString(word);
        }
        endX = startX + font.stringWidth(printedWord);
        return isPointOverText(click, origin, scaleX, scaleY, font, y, startX, endX);
    }

    private boolean isPointOverText(Point click, Point origin, double scaleX, double scaleY, FontMetrics font, int y, int startX, int endX) {
        return (int) click.getX() >= startX
                && (int) click.getX() <= endX
                && (int) click.getY() + font.getHeight() >= y
                && (int) click.getY() + origin.y <= y;
    }




    public void draw(Graphics g, Point origin, double scaleX, double scaleY, int imageHeight, Function<Point, Boolean> isPointInArea) {
        g.setFont(new Font("Ariel", Font.PLAIN, 20));
        FontMetrics font = g.getFontMetrics();
        // Different style for the text that is being edited
        if(hasCursor) {
            g.setColor(Color.GRAY);
        } else {
            g.setColor(this.color);
        }

        int y = (int)((insertionPoint.y * scaleY) + origin.y);
        int i = 0; // index of the char in the word

        char[] word = text.toCharArray();
        String printedWord = "";
        int startX = (int)((insertionPoint.x * scaleX) + origin.x);
        // Build the longest string possible before creating a new line
        while(i < word.length && y < origin.y + imageHeight) {
            printedWord += word[i];
            int endX = startX + font.stringWidth(printedWord);
            // If the word overflow the size of the image then print word that can fit in that space and move the
            // y coordinate to create a new line
            if(!isPointInArea.apply(new Point(endX, y))) {
                // -1 because otherwise the text will go out of the Canvas
                g.drawString(printedWord.substring(0, printedWord.length() - 1), startX, y);
                printedWord = word[i] + "";
                y += font.getHeight() + 1;
            }
            i++;
        }
        if(y < origin.y + imageHeight) {
            g.drawString(printedWord, startX, y);
        }
    }

    public void setColor(Color color) {
        this.color = color;
        this.fireChangeListeners();
    }
}


