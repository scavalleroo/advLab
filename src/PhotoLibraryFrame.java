import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

// PhotoLibraryFrame class extends JFrame to create the main application window
public class PhotoLibraryFrame extends JFrame {
    static String TITLE_FRAME = "Photo Library";
    private final String IMPLEMENTATION_MISSING_MESSAGE = "Action not implemented yet";
    private final int WIDTH = 600;
    private final int HEIGHT = 400;
    private final int MIN_WIDTH = 400;
    private final int MIN_HEIGHT = 200;
    JMenu fileMenu, viewMenu;

    // File menu options
    JMenuBar menuBar;
    JMenuItem fmImport, fmDelete, fmQuit;

    // View menu options
    ButtonGroup btnGroupView;
    JRadioButtonMenuItem vmPhotoViewer, vmBrowser;

    JPanel mainPanel;
    JLabel statusBar;
    JToolBar toolBar;
    JToggleButton tbPeople, tbPlaces, tbSchool;
    PhotoComponent photoComponent;

    // Constructor for PhotoLibrary class
    public PhotoLibraryFrame() {
        super(TITLE_FRAME);
        this.createMenuBar();    // Create the menu bar
        this.createMainPanel();  // Create the main panel
        this.createStatusBar();  // Create the status bar
        this.createToolBar();    // Create the toolbar
        this.setFrameParameters(); // Set parameters for the frame
    }

    // Creates the menu bar for the application
    private void createMenuBar() {
        menuBar = new JMenuBar(); // Create the menu bar
        fileMenu = new JMenu("File"); // Create the "File" menu
        viewMenu = new JMenu("View"); // Create the "View" menu

        // Creation and addition of menu items for the "File" menu
        fileMenu.add(fmImport = new JMenuItem("Import"));
        fileMenu.add(fmDelete = new JMenuItem("Delete"));
        fileMenu.add(fmQuit = new JMenuItem("Quit"));

        // Add action listeners for "Import," "Delete," and "Quit" menu items
        fmImport.addActionListener(e -> this.importFile());
        fmQuit.addActionListener(e -> this.quitApplication());
        fmDelete.addActionListener(e -> this.setStatusMessage(IMPLEMENTATION_MISSING_MESSAGE));

        // Creation and addition of menu items for the "View" menu
        btnGroupView = new ButtonGroup();
        vmPhotoViewer = new JRadioButtonMenuItem("Photo Viewer", true);
        vmBrowser = new JRadioButtonMenuItem("Browser", false);
        btnGroupView.add(vmPhotoViewer);
        btnGroupView.add(vmBrowser);
        viewMenu.add(vmPhotoViewer);
        viewMenu.add(vmBrowser);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        this.setJMenuBar(menuBar); // Set the menu bar for the frame
    }

    // Creates the main panel for the application
    private void createMainPanel() {
        mainPanel = new JPanel(); // Create the main panel
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.LIGHT_GRAY);
        try {
            photoComponent = new PhotoComponent(ImageIO.read(new File("src/icons/landscape.jpeg")));
            //photoComponent.setPreferredSize(new Dimension(600, 400));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        mainPanel.add(photoComponent);
        this.add(mainPanel, BorderLayout.CENTER); // Add the main panel to the frame's center
        this.pack();
    }

    // Creates the status bar at the bottom of the application window
    private void createStatusBar() {
        statusBar = new JLabel(); // Create the status bar
        // Aligning the text of the status bar to the center
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setVerticalAlignment(JLabel.CENTER);
        statusBar.setFont(new Font("Arial", Font.PLAIN, 16));
        this.add(statusBar, BorderLayout.SOUTH); // Add the status bar to the frame's bottom
        statusBar.setPreferredSize(new Dimension(statusBar.getWidth(), 30));
    }

    private void createToolBar() {
        toolBar = new JToolBar(SwingConstants.VERTICAL); // Create a vertical toolbar
        toolBar.setBorder(new EmptyBorder(10, 5, 0, 20)); // Set border for the toolbar

        JPanel panel = new JPanel(new GridBagLayout()); // Create a panel for toolbar elements
        Icon[] icons = new Icon[]{
                new ImageIcon("src/icons/people.png"),
                new ImageIcon("src/icons/places.png"),
                new ImageIcon("src/icons/school.png")
        };
        String[] buttonNames = {"People", "Places", "School"};

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Create and add toggle buttons for filtering images
        // TODO set toggle button
        for (int i = 0; i < icons.length; i++) {
            JToggleButton button = new JToggleButton(buttonNames[i], icons[i]);
            String buttonName = buttonNames[i];
            button.addItemListener(e -> filterImage(buttonName, e.getStateChange() == ItemEvent.SELECTED));
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            panel.add(button, gbc);
            gbc.gridy++;
        }

        toolBar.add(panel);
        toolBar.setFloatable(false); // Make the toolbar non-floating
        add(toolBar, BorderLayout.WEST); // Add the toolbar to the frame's west side
    }

    // Sets the parameters for the application window
    private void setFrameParameters() {
        this.setSize(WIDTH, HEIGHT);
        this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.setVisible(true);
        // Centre the frame on the screen
        this.setLocationRelativeTo(null);
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser(); // Create a file chooser
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        chooser.setFileFilter(imageFilter); // Set a file filter for image files
        int returnVal = chooser.showOpenDialog(this); // Show the file chooser dialog
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            this.setStatusMessage(IMPLEMENTATION_MISSING_MESSAGE);
        }
    }

    private void setStatusMessage(String message) {
        this.statusBar.setText(message); // Set the status bar message
    }

    private void quitApplication() {
        // Code reference https://stackoverflow.com/a/1235994
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); // Quit the application
    }

    private void filterImage(String filter, Boolean isActive) {
        this.setStatusMessage(isActive ? filter : ""); // Set the status bar message based on filter status
    }

}