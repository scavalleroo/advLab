import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PhotoUI {
    private int imageWidth;
    private int imageHeight;
    Point origin = new Point(0,0);
    double scaleX;
    double scaleY;
    double scale;
    private Point insertionPoint;
    private boolean mousePressed = false;
    private boolean isTyping = false;
    private boolean isDrawing = false;
    static public final int NOT_SET = -1;
    private int currentTextIndex = NOT_SET;
    private int currentLineIndex = NOT_SET;
    private int cursorPosition = NOT_SET;

    public PhotoUI(PhotoComponent c){
        addListeners(c);
    }

    public void addListeners(PhotoComponent c) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            // Takes care of flipping the image
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (isPointInArea(e.getPoint())) {
                        c.setFlipped(!c.isFlipped());
                    }
                }
            }

            // Takes care of handling the creation of a new text
            @Override
            public void mousePressed(MouseEvent e) {
                if(c.isFlipped()) {
                    if (isTyping || cursorPosition != NOT_SET) {
                        isTyping = false;
                        // Removing the "|" cursor before publishing
                        if(currentTextIndex != NOT_SET) {
                            String text = c.getModel().getAnnotations().get(currentTextIndex).getText();
                            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                            c.getModel().getAnnotations().get(currentTextIndex).setHasCursor(false);
                            c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                            cursorPosition = NOT_SET;
                        }
                    }

                    // Check if the insert point is inside the drawing area
                    if (isPointInArea(e.getPoint())) {
                        // Setting the point where the click happen as the insert point
                        insertionPoint = new Point((int)((e.getX() - origin.x) / scaleX), (int)((e.getY() - origin.y) / scaleY));
                        FontMetrics font = c.getGraphics().getFontMetrics();
                        List<TextAnnotation> textAnnotations = c.getModel().getAnnotations();
                        // Resetting the current text that is being edited
                        currentTextIndex = NOT_SET;
                        // Check if the click happen over a text that is being printed
                        setCurrentEditingText(c, e.getPoint(), textAnnotations, font);
                        setCurrentEditingLine(c, e.getPoint());

                        mousePressed = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                if (isPointInArea(e.getPoint())) {
                    c.getModel().addPoint(PhotoModel.BREAK_POINT, c.getColor());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPointInArea(e.getPoint())) {
                    Point p = new Point((int)((e.getX() - origin.x) / scaleX), (int)((e.getY() - origin.y) / scaleY));
                    if(currentTextIndex != NOT_SET) {
                        TextAnnotation annotation =  c.getModel().getAnnotations().get(currentTextIndex);
                        if(!annotation.getText().isBlank()) {
                            FontMetrics font = c.getGraphics().getFontMetrics();
                            p.x = (int)((p.x * scaleX - font.stringWidth(annotation.getText()) / 2) / scaleX);
                            c.getModel().getAnnotations().get(currentTextIndex).setOrigin(p);
                        }
                    } else if(currentLineIndex != NOT_SET) {
                        c.getModel().moveLineAt(currentLineIndex, p);
                    } else {
                        c.getModel().addPoint(p, c.getColor());
                    }
                }
            }
        };

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(mousePressed && !isDrawing) {
                    if(currentTextIndex != NOT_SET) {
                        String text = c.getModel().getAnnotations().get(currentTextIndex).getText();
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            if(cursorPosition > 0 && !text.substring(0, cursorPosition).isBlank()) {
                                text = text.substring(0, cursorPosition - 1) + "|" + text.charAt(cursorPosition - 1) + text.substring(cursorPosition + 1);
                                c.getModel().getAnnotations().get(currentTextIndex).setHasCursor(true);
                                c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition--;
                            }
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            if (cursorPosition < (text.length() - 1)) {
                                text = text.substring(0, cursorPosition) + text.charAt(cursorPosition + 1) + "|" + text.substring(cursorPosition + 2);
                                c.getModel().getAnnotations().get(currentTextIndex).setHasCursor(true);
                                c.getModel().getAnnotations().get(currentTextIndex).setText(text);
                                cursorPosition++;
                            }
                        }
                    }
                    if(!isTyping) {
                        createNewAnnotation(e, c.getModel(), c.getColor());
                        isTyping = true;
                    }
                    editAnnotation(e, c.getModel());
                }
            }
        };

        // Add listeners
        c.addMouseListener(mouseAdapter);
        c.addMouseMotionListener(mouseAdapter);
        c.addKeyListener(keyAdapter);
    }

    private void createNewAnnotation(KeyEvent e, PhotoModel model, Color color) {
        if(currentTextIndex == NOT_SET) {
            String text = (e.getKeyChar() + "").concat("|");
            model.addAnnotation(new TextAnnotation(text, insertionPoint, color));
            currentTextIndex = model.getAnnotations().size() - 1;
            cursorPosition = 1;
            model.getAnnotations().get(currentTextIndex).setHasCursor(true);
        }
    }

    private void editAnnotation(KeyEvent e, PhotoModel model) {
        String text = model.getAnnotations().get(currentTextIndex).getText();
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if(!text.substring(0, cursorPosition).isEmpty()) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                model.getAnnotations().get(currentTextIndex).setHasCursor(true);
                cursorPosition--;
            }
        } else {
            if(Character.isDefined(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) {
                String cursor = "|";
                text = text.substring(0, cursorPosition) +
                        e.getKeyChar() + cursor +
                        text.substring(cursorPosition + 1);
                cursorPosition++;
                model.getAnnotations().get(currentTextIndex).setHasCursor(true);
            }
        }
        model.getAnnotations().get(currentTextIndex).setText(text);
    }

    public void paint(Graphics2D g, PhotoComponent c) {
        computeImageScaling(c);

        Stroke stroke = new BasicStroke(6.0f); // Change 2.0f to your desired stroke size
        g.setStroke(stroke);

        Image image = c.getModel().getImage();
        g.drawImage(image, origin.x, origin.y, imageWidth, imageHeight, null);
        if(c.isFlipped()) {
            // Draw the strokes
            c.getModel().drawLines(g, origin, scaleX, scaleY, currentLineIndex, this::isPointInArea);
            // Draw the text
            c.getModel().drawText(g, origin, scaleX, scaleY, imageHeight, this::isPointInArea);
            c.invalidate();
        }
    }

    private void computeImageScaling(PhotoComponent c) {
        int componentWidth = c.getWidth();
        int componentHeight = c.getHeight();

        // Get the image dimensions
        int imageWidth = c.getModel().getImage().getWidth(null);
        int imageHeight = c.getModel().getImage().getHeight(null);

        // Calculate the scaling factors to fit the image within the component while maintaining aspect ratio
        scaleX = (double) componentWidth / imageWidth;
        scaleY = (double) componentHeight / imageHeight;
        scale = Math.min(scaleX, scaleY);

        // Calculate the new image dimensions
        int newImageWidth = (int) (imageWidth * scale);
        int newImageHeight = (int) (imageHeight * scale);

        scaleX = (double) newImageWidth / imageWidth;
        scaleY = (double) newImageHeight / imageHeight;

        // Calculate the position to center the image within the component
        int x0 = (componentWidth - newImageWidth) / 2;
        int y0 = (componentHeight - newImageHeight) / 2;

        // Update the image dimensions and position
        this.imageWidth = newImageWidth;
        this.imageHeight = newImageHeight;
        this.origin.x = x0;
        this.origin.y = y0;
    }

    private void setCurrentEditingText(PhotoComponent c, Point click, List<TextAnnotation> textAnnotations, FontMetrics font) {
        // Check if the click was over one of the text
        // If the area of the click is inside the area of text then we are editing the currentTextIndex string
        currentTextIndex = c.getModel().getSelectedTextAnnotation(click, origin, scaleX, scaleY, font, this::isPointInArea);
        if(currentTextIndex != NOT_SET) {
            if (!textAnnotations.get(currentTextIndex).hasCursor()) {
                textAnnotations.get(currentTextIndex).setText(textAnnotations.get(currentTextIndex).getText().concat("|"));
                textAnnotations.get(currentTextIndex).setHasCursor(true);
            }
            cursorPosition = textAnnotations.get(currentTextIndex).getText().length() - 1;
        }
    }


    private void setCurrentEditingLine(PhotoComponent c, Point click) {
        // Check if the click was over one of the text
        // If the area of the click is inside the area of text then we are editing the currentTextIndex string
        currentLineIndex = c.getModel().getSelectedLine(click, origin, scaleX, scaleY, 5, this::isPointInArea);
        if(currentTextIndex != NOT_SET) {
            System.out.println("Hit stroke");
        } else {
            System.out.println("Miss stroke");
        }
    }

    public boolean isPointInArea(Point p) {
        return p.getX() >= origin.x && p.getX() <= origin.x + imageWidth &&
                p.getY() >= origin.y && p.getY() <= origin.y + imageHeight;
    }

    public void updateColorsOfItemsSelected(PhotoModel model, Color newColor) {
        if(this.currentTextIndex != NOT_SET) {
            model.updateColorOfTextAt(currentTextIndex, newColor);
        }
        if(this.currentLineIndex != NOT_SET) {
            model.setColorOfLineAt(currentLineIndex, newColor);

        }
    }
}
