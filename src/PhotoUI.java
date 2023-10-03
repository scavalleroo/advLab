import java.awt.*;

public class PhotoUI {
    private int imageWidth;
    private int imageHeight;
    int x0;
    int y0;
    double scaleX;
    double scaleY;
    double scale;

    public PhotoUI(){}

    public void paint(Graphics2D g, PhotoComponent c) {
        computeImageScaling(c);

        if(!c.getModel().isFlipped()) {
            // Show the picture
            Image image = c.getModel().getImage();
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
        this.x0 = x0;
        this.y0 = y0;
    }

    private void drawText(Graphics g, PhotoComponent c) {
        g.setFont(new Font("Ariel", Font.PLAIN, 20));
        if(!c.getModel().getAnnotations().isEmpty()) {
            FontMetrics font = g.getFontMetrics();
            // Print every word taking care of creating a new line if the word is too long
            for (int j = 0; j < c.getModel().getAnnotations().size(); j++) {
                Annotation annotation = c.getModel().getAnnotations().get(j);

                // Different style for the text that is being edited
                if(j == c.getCurrentTextIndex()) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.BLUE);
                }

                int y = (int)((annotation.getY() * scaleY) + y0);
                int i = 0; // index of the char in the word

                char[] word = annotation.getText().toCharArray();
                String printedWord = "";
                int startX = (int)((annotation.getX() * scaleX) + x0);
                // Build the longest string possible before creating a new line
                while(i < word.length && y < y0 + imageHeight) {
                    printedWord += word[i];
                    int endX = startX + font.stringWidth(printedWord);
                    // If the word overflow the size of the image then print word that can fit in that space and move the
                    // y coordinate to create a new line
                    if(!isPointInArea(new Point(endX, y))) {
                        // -1 because otherwise the text will go out of the Canvas
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
                    Point drawingPoint = new Point((int)((point.x  * scaleX) + x0), (int)((point.y * scaleY) + y0));
                    // Check if the new point is inside the picture area
                    if (isPointInArea(drawingPoint)) {
                        g.drawLine((int)((start.x * scaleX) + x0), (int)((start.y * scaleY) + y0), drawingPoint.x, drawingPoint.y);
                    }
                }
                start = point;
            }
        }
    }

    public boolean isPointInArea(Point p) {
        return p.getX() >= x0 && p.getX() <= x0 + imageWidth &&
                p.getY() >= y0 && p.getY() <= y0 + imageHeight;
    }
    public int getX0() {
        return x0;
    }

    public int getY0() {
        return y0;
    }

    public double getScaleX() {
        return scaleX;
    }
    public double getScaleY() {
        return scaleY;
    }
}
