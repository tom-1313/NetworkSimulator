/***************
 * App
 * Author: Christian Duncan
 * Spring 21: CSC340
 * 
 * This is the Main GUI interface to the Network Simulator
 * It allows selecting various options as well as
 * visualizing the network and monitoring statistics on the
 * network.
 ***************/
import java.awt.*;        // import statements to make necessary classes available
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
    
public class App extends JFrame {
    /**
     * The main entry point that sets up the window and basic functionality
     */
    public static void main(String[] args) {
        App frame = new App();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(true);
        frame.setVisible(true);
    }

    private VisPanel visPane;
    
    /* Constructor: Sets up the initial look-and-feel */
    public App() {
        JLabel label;  // Temporary variable for a label
        JButton button; // Temporary variable for a button

        // Set up the initial size and layout of the frame
        // For this we will keep it to a simple BoxLayout
        setLocation(100, 100);
        setPreferredSize(new Dimension(800, 800));
        setTitle("CSC340 Basic Network Simulator");
        Container mainPane = getContentPane();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.setPreferredSize(new Dimension(1000, 500));

        // Create the Visualization Panel
        visPane = new VisPanel();
        mainPane.add(visPane);
        
        // Create the buttons at bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // Drop down list (Flood, Distance Vector, Link State)
        JLabel routerLabel = new JLabel("Router:");
        String[] choices = { "Flood", "Distance Vector", "Link State" };
        JComboBox<String> routerBox = new JComboBox<String>(choices);
        buttonPanel.add(routerBox);
        
        // Run/Reset network button
        Action runAction = new AbstractAction("Run") {
                public void actionPerformed(ActionEvent e) {
                }
            };
        runAction.putValue(Action.SHORT_DESCRIPTION, "Start the network running.");
        JButton runButton = new JButton(runAction);
        runButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        buttonPanel.add(runButton);
        
        // JTextField: Packets/Second
        JTextField pps = new JTextField("100");
        JLabel ppsLabel = new JLabel("Packets/sec");
        buttonPanel.add(pps);
        buttonPanel.add(ppsLabel);
        mainPane.add(buttonPanel);

        // Setup the menubar
        setupMenuBar();
    }

    private void setupMenuBar() {
        JMenuBar mbar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        Action menuAction;
        menu = new JMenu("File");
        menuAction = new AbstractAction("Load Network") {
                JFileChooser chooser = null;
                public void actionPerformed(ActionEvent e) {
                    if (chooser == null) {
                        chooser = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Networks (GQU)", "gqu");
                        chooser.setFileFilter(filter);
                    }
                    int returnVal = chooser.showOpenDialog(visPane);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        System.out.println("You chose to open this file: " +
                                           chooser.getSelectedFile().getName());
                    }
                }
            };
        menuAction.putValue(Action.SHORT_DESCRIPTION, "Load a network to simulate");
        menuItem = new JMenuItem(menuAction);
        menu.add(menuItem);
        mbar.add(menu);
        
        menu = new JMenu("Monitor");
        mbar.add(menu);
        setJMenuBar(mbar);
    }
    
    public class VisPanel extends JPanel {
        Graphics2D g2;
        
        public VisPanel() {
            setPreferredSize(new Dimension(1000,1000) ); // Set size of drawing area, in pixels.
        }

        /* Used for drawing the network */
        protected void paintComponent(Graphics g) {
            g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new Color(200, 200, 220));
            g2.fillRect(0, 0, getWidth(), getHeight());
            setupViewport(-100, 100, -100, 100);

            drawNetwork();
        }

        /**
         * Set up the viewport so dimensions of window are in range (left to right) and (bottom to top)
         * ... roughly, aspect ratio is still preserved.
         * @param left left edge of drawing area
         * @param right right edge of drawing area
         * @param bottom bottom edge of drawing area
         * @param top top edge of drawing area
         */
        private void setupViewport(double left, double right, double bottom, double top) {
            // Get width and height in pixels of panel.
            int width = getWidth();  
            int height = getHeight();

            // Correct viewport dimensions to preserve aspect ratio
            double panelAspect = Math.abs((double)height / width);
            double viewAspect = Math.abs(( bottom-top ) / ( right-left ));
            if (panelAspect > viewAspect) {
                // Expand the viewport vertically.
                double padding = (bottom-top)*(panelAspect/viewAspect - 1)/2;
                bottom += padding;
                top -= padding;
            }
            else { 
                // Expand the viewport horizontally
                double padding = (right-left)*(viewAspect/panelAspect - 1)/2;
                right += padding;
                left -= padding;
            }

            g2.scale(width/(right-left), height/(bottom-top));
            g2.translate(-left, -top);
        }

        private void drawNetwork() {
            drawNode(0, 0, 5);
        }
        
        private void drawNode(double cx, double cy, double radius) {
            g2.setPaint(Color.BLUE);
            double diam = radius*2;
            g2.fill(new Ellipse2D.Double(cx-radius, cy-radius, diam, diam));
        }
    }
}

