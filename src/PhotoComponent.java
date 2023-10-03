import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PhotoComponent extends JComponent {

    private PhotoModel model;
    private PhotoUI ui;
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private Point insertionPoint;
    private boolean mousePressed = false;
    private boolean isTyping = false;
    private boolean isDrawing = false;
    private final int NOT_SET = -1;

    private int currentTextIndex = NOT_SET;
    private int cursorPosition = NOT_SET;

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

        // remove the old listeners
        if(mouseAdapter != null && keyAdapter != null) {
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseAdapter);
            removeKeyListener(keyAdapter);
        }

        mouseAdapter = new MouseAdapter() {
            // Takes care of flipping the image
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (ui.isPointInArea(e.getPoint())) {
                        model.setFlipped(!model.isFlipped());
                        repaint();
                    }
                }
            }

            // Takes care of handling the creation of a new text
            @Override
            public void mousePressed(MouseEvent e) {
                if(model.isFlipped()) {
                    if (isTyping) {
                        isTyping = false;
                        // Removing the "|" cursor before publishing
                        if(currentTextIndex != NOT_SET) {
                            String text = model.getAnnotations().get(currentTextIndex).getText();
                            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                            model.getAnnotations().get(currentTextIndex).setText(text);
                            cursorPosition = NOT_SET;
                        }
                    }

                    // Check if the insert point is inside the drawing area
                    if (ui.isPointInArea(e.getPoint())) {
                        // Setting the point where the click happen as the insert point
                        insertionPoint = new Point((int)((e.getX() - ui.getX0()) / ui.getScaleX()), (int)((e.getY() - ui.getY0()) / ui.getScaleY()));
                        FontMetrics font = g.getFontMetrics();
                        List<Annotation> annotations = model.getAnnotations();
                        // Resetting the current text that is being edited
                        currentTextIndex = NOT_SET;
                        // Check if the click happen over a text that is being printed
                        setCurrentEditingText(annotations, font);
                        mousePressed = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                if (ui.isPointInArea(e.getPoint())) {
                    model.getDrawingPoints().add(PhotoModel.BREAK_POINT);
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (ui.isPointInArea(e.getPoint())) {
                    isDrawing = true;
                    Point p = new Point((int)((e.getX() - ui.getX0()) / ui.getScaleX()), (int)((e.getY() - ui.getY0()) / ui.getScaleY()));
                    model.addPoint(p);
                    repaint();
                }
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(mousePressed && !isDrawing) {
                    if(currentTextIndex != NOT_SET) {
                        String text = model.getAnnotations().get(currentTextIndex).getText();
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            if(cursorPosition > 0 && !text.substring(0, cursorPosition).isBlank()) {
                                text = text.substring(0, cursorPosition - 1) + "|" + text.charAt(cursorPosition - 1) + text.substring(cursorPosition + 1);
                                model.getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition--;
                            }
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            if (cursorPosition < (text.length() - 1)) {
                                text = text.substring(0, cursorPosition) + text.charAt(cursorPosition + 1) + "|" + text.substring(cursorPosition + 2);
                                model.getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition++;
                            }
                        }
                    }
                    if(!isTyping) {
                        createNewAnnotation(e);
                        isTyping = true;
                    }
                    editAnnotation(e);
                    repaint();
                }
            }
        };

        // Add listeners
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addKeyListener(keyAdapter);

        setFocusable(true);
        requestFocusInWindow();
    }

    private void setCurrentEditingText(List<Annotation> annotations, FontMetrics font) {
        // Check if the click was over one of the text
        // If the area of the click is inside the area of text then we are editing the currentTextIndex string
        for (int j = 0; j < annotations.size() && currentTextIndex == NOT_SET; j++) {
            int y = ((int)(annotations.get(j).getY() * ui.getScaleY()) + ui.getY0());

            int i = 0; // index of the char in the word
            char[] word = annotations.get(j).getText().toCharArray();

            String printedWord = ""; // word to be printed
            int startX = ((int)(annotations.get(j).getX() * ui.getScaleX()) + ui.getX0()); // x0 coordinate of the word

            while(i < word.length) {
                printedWord += word[i];
                int endX = startX + font.stringWidth(printedWord);
                if(!ui.isPointInArea(new Point(endX, y))) {
                    printedWord = word[i] + "";
                    y += font.getHeight() + 1;
                }
                if(hasClickedOverTheText(font.getHeight(), startX, endX, y)) {
                    currentTextIndex = j;
                    break;
                }
                i++;
            }

            int endX = startX + font.stringWidth(printedWord);
            if(hasClickedOverTheText(font.getHeight(), startX, endX, y)) {
                currentTextIndex = j;
            }
        }
        if(currentTextIndex != NOT_SET) {
            annotations.get(currentTextIndex).setText(annotations.get(currentTextIndex).getText().concat("|"));
            cursorPosition = annotations.get(currentTextIndex).getText().length() - 1;
        }
    }

    private void editAnnotation(KeyEvent e) {
        String text = model.getAnnotations().get(currentTextIndex).getText();
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if(!text.substring(0, cursorPosition).isEmpty()) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
            }
        } else {
            if(Character.isDefined(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) {
                String cursor = "|";
                text = text.substring(0, cursorPosition) +
                        e.getKeyChar() + cursor +
                        text.substring(cursorPosition + 1);
                cursorPosition++;
            }
        }
        model.getAnnotations().get(currentTextIndex).setText(text);
    }

    private void createNewAnnotation(KeyEvent e) {
        if(currentTextIndex == NOT_SET) {
            String text = (e.getKeyChar() + "").concat("|");
            model.addAnnotation(new Annotation(text, insertionPoint));
            currentTextIndex = model.getAnnotations().size() - 1;
            cursorPosition = 1;
        }
    }

    private boolean hasClickedOverTheText(int fontHeight, int startX, int endX, int y) {
        return ((int)((insertionPoint.getX() * ui.getScaleX()) + ui.getX0()) >= startX
                && (int)((insertionPoint.getX() * ui.getScaleX()) + ui.getX0()) <= endX
                && (int)((insertionPoint.getY() * ui.getScaleY()) + ui.getY0()) + fontHeight >= y
                && (int)((insertionPoint.getY() * ui.getScaleY()) + ui.getY0()) <= y);
    }

    public PhotoModel getModel() {
        return model;
    }

    public int getCurrentTextIndex() {
        return currentTextIndex;
    }
}
