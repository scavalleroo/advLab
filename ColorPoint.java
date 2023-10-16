import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorPoint extends Point {
    private Color color;
    private List<ChangeListener> changeListeners = new ArrayList<>();

    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
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

    public void setColor(Color color) {
        this.color = color;
        this.fireChangeListeners();
    }

    public Color getColor() {
        return color;
    }

    public boolean isBreakPoint() {
        return this.x == PhotoModel.BREAK_POINT.x && this.y == PhotoModel.BREAK_POINT.y;
    }
}
