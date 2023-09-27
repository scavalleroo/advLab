import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PhotoUI {
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private Point insertionPoint;
    private boolean mousePressed = false;
    private boolean isTyping = false;
    private boolean isDrawing = false;
    private final int NOT_SET = -1;
    private int currentTextIndex = NOT_SET;
    private int cursorPosition = NOT_SET;
    private double scaleX;
    private double scaleY;
    private int imageWidth;
    private int imageHeight;
    int x0;
    int y0;

    public PhotoUI(){

    }

    public void paint(Graphics g, PhotoComponent c) {
        c.setLayout(null);

        Image image = c.getModel().getImage();
        computeImageScaling(image, c.getWidth(), c.getHeight());

        if(!c.getModel().isFlipped()) {
            // Show the picture
            g.drawImage(image, x0, y0, imageWidth, imageHeight, null);
        } else {
            // Draw the background of the canvas
            g.setColor(Color.WHITE);
            g.fillRect(x0, y0, imageWidth, imageHeight);

            // Draw the strokes
            drawLines(g, c);

            // Draw the text
            drawText(g, c);
            c.invalidate();
        }

        // remove the old listeners
        if(mouseAdapter != null && keyAdapter != null) {
            c.removeMouseListener(mouseAdapter);
            c.removeMouseMotionListener(mouseAdapter);
            c.removeKeyListener(keyAdapter);
        }

        mouseAdapter = new MouseAdapter() {
            // Takes care of flipping the image
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (isPointInArea(e.getPoint())) {
                        c.getModel().setFlipped(!c.getModel().isFlipped());
                        c.repaint();
                    }
                }
            }

            // Takes care of handling the creation of a new text
            @Override
            public void mousePressed(MouseEvent e) {
                if(c.getModel().isFlipped()) {
                    if (isTyping) {
                        isTyping = false;
                        // Removing the "|" cursor before publishing
                        if(currentTextIndex != NOT_SET) {
                            String text = c.getModel().getAnnotations().get(currentTextIndex).getText();
                            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                            c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                            cursorPosition = NOT_SET;
                        }
                    }

                    // Check if the insert point is inside the drawing area
                    if (isPointInArea(e.getPoint())) {
                        // Setting the point where the click happen as the insert point
                        insertionPoint = new Point((int) (e.getX() / scaleX), (int) (e.getY()/scaleY));
                        FontMetrics font = g.getFontMetrics();
                        List<Annotation> annotations = c.getModel().getAnnotations();
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
                if (isPointInArea(e.getPoint())) {
                    c.getModel().getDrawingPoints().add(PhotoModel.BREAK_POINT);
                    c.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPointInArea(e.getPoint())) {
                    isDrawing = true;
                    Point p = new Point((int) (e.getX() / scaleX), (int) (e.getY()/scaleY));
                    c.getModel().addPoint(p);
                    c.repaint();
                }
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(mousePressed && !isDrawing) {
                    if(currentTextIndex != NOT_SET) {
                        String text = c.getModel().getAnnotations().get(currentTextIndex).getText();
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            if(cursorPosition > 0 && !text.substring(0, cursorPosition).isBlank()) {
                                text = text.substring(0, cursorPosition - 1) + "|" + text.charAt(cursorPosition - 1) + text.substring(cursorPosition + 1);
                                c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition--;
                            }
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            if (cursorPosition < (text.length() - 1)) {
                                text = text.substring(0, cursorPosition) + text.charAt(cursorPosition + 1) + "|" + text.substring(cursorPosition + 2);
                                c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition++;
                            }
                        }
                    }
                    if(!isTyping) {
                        createNewAnnotation(e, c);
                        isTyping = true;
                    }
                    editAnnotation(e, c);
                    c.repaint();
                }
            }
        };

        // Add listeners
        c.addMouseListener(mouseAdapter);
        c.addMouseMotionListener(mouseAdapter);
        c.addKeyListener(keyAdapter);

        c.setFocusable(true);
        c.requestFocusInWindow();
    }

    private void editAnnotation(KeyEvent e, PhotoComponent c) {
        String text = c.getModel().getAnnotations().get(currentTextIndex).getText();
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
        c.getModel().getAnnotations().get(currentTextIndex).setText(text);
    }

    private void createNewAnnotation(KeyEvent e, PhotoComponent c) {
        if(currentTextIndex == NOT_SET) {
            String text = (e.getKeyChar() + "").concat("|");
            c.getModel().addAnnotation(new Annotation(text, insertionPoint));
            currentTextIndex = c.getModel().getAnnotations().size() - 1;
            cursorPosition = 1;
        }
    }

    private void computeImageScaling(Image image, int width, int height) {
        // Calculate the scaling factors to fit the image within the component
        scaleX = (double) width / image.getWidth(null);
        scaleY = (double) height / image.getHeight(null);

        // Use the minimum scaling factor to ensure the entire image fits
        double scale = Math.min(scaleX, scaleY);

        // Calculate the new width and height based on the scale
        imageWidth = (int) (image.getWidth(null) * scale);
        imageHeight = (int) (image.getHeight(null) * scale);

        // Calculate the position to center the image within the component
        x0 = (width - imageWidth) / 2;
        y0 = (height - imageHeight) / 2;
    }

    private void setCurrentEditingText(List<Annotation> annotations, FontMetrics font) {
        // Check if the click was over one of the text
        // If the area of the click is inside the area of text then we are editing the currentTextIndex string
        for (int j = 0; j < annotations.size() && currentTextIndex == NOT_SET; j++) {
            int y = (int) (annotations.get(j).getY() * scaleY);

            int i = 0; // index of the char in the word
            char[] word = annotations.get(j).getText().toCharArray();

            String printedWord = ""; // word to be printed
            int startX = (int)(annotations.get(j).getX() * scaleX); // x0 coordinate of the word

            while(i < word.length) {
                printedWord += word[i];
                int endX = startX + font.stringWidth(printedWord);
                if(!isPointInArea(new Point(endX, y))) {
                    printedWord = word[i] + "";
                    y += font.getHeight() + 1;
                }
                if(hasClickedOverTheText(font.getHeight(), startX, endX, y, j)) {
                    currentTextIndex = j;
                    break;
                }
                i++;
            }

            int endX = startX + font.stringWidth(printedWord);
            if(hasClickedOverTheText(font.getHeight(), startX, endX, y, j)) {
                currentTextIndex = j;
            }
        }
        if(currentTextIndex != NOT_SET) {
            annotations.get(currentTextIndex).setText(annotations.get(currentTextIndex).getText().concat("|"));
            cursorPosition = annotations.get(currentTextIndex).getText().length() - 1;
        }
    }

    private boolean hasClickedOverTheText(int fontHeight, int startX, int endX, int y, int j) {
        return ((int)(insertionPoint.getX() * scaleX) >= startX && (int)(insertionPoint.getX() * scaleX) <= endX && (int)(insertionPoint.getY() * scaleY) + fontHeight >= y && (int)(insertionPoint.getY() * scaleY) <= y);
    }

    private void drawText(Graphics g, PhotoComponent c) {
        g.setFont(new Font("Ariel", Font.PLAIN, 20));
        if(!c.getModel().getAnnotations().isEmpty()) {
            FontMetrics font = g.getFontMetrics();
            // Print every word taking care of creating a new line if the word is too long
            for (int j = 0; j < c.getModel().getAnnotations().size(); j++) {
                Annotation annotation = c.getModel().getAnnotations().get(j);

                // Different style for the text that is being edited
                if(j == currentTextIndex) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.BLUE);
                }

                int y = (int) (annotation.getY() * scaleY);
                int i = 0; // index of the char in the word

                char[] word = annotation.getText().toCharArray();
                String printedWord = "";
                int startX = (int) (annotation.getX() * scaleX);
                // Build the longest string possible before creating a new line
                while(i < word.length && y < y0 + imageHeight) {
                    printedWord += word[i];
                    int endX = startX + font.stringWidth(printedWord);
                    // If the word overflow the size of the image then print word that can fit in that space and move the
                    // y coordinate to create a new line
                    if(!isPointInArea(new Point(endX, y))) {
                        g.drawString(printedWord.substring(0, printedWord.length() - 1), startX, y);
                        printedWord = word[i] + "";
                        y += font.getHeight() + 1;
                    }
                    i++;
                }
                if(y < y0 + imageHeight) {
                    g.drawString(printedWord, startX, y);
                }
            }
        }
    }

    private void drawLines(Graphics g, PhotoComponent c) {
        if(!c.getModel().getDrawingPoints().isEmpty()) {
            g.setColor(Color.BLUE);
            Point start = c.getModel().getDrawingPoints().get(0);
            for (Point point: c.getModel().getDrawingPoints()) {
                if(point != PhotoModel.BREAK_POINT && start != PhotoModel.BREAK_POINT) {
                    // Assign the relative coordinate to the point that is going to be printed
                    // In this way rescaling the image also rescale the lines
                    Point drawingPoint = new Point((int)(point.x * scaleX), (int)(point.y * scaleY));
                    // Check if the new point is inside the picture area
                    if (isPointInArea(drawingPoint)) {
                        g.drawLine((int)(start.x * scaleX), (int)(start.y * scaleY), drawingPoint.x, drawingPoint.y);
                    }
                }
                start = point;
            }
        }
    }

    private boolean isPointInArea(Point p) {
        return p.getX() >= x0 && p.getX() <= x0 + imageWidth &&
                p.getY() >= y0 && p.getY() <= y0 + imageHeight;
    }
}
