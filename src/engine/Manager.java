package engine;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * JFrame acting as the main class; handles the engine
 */
class Manager extends JFrame {
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    private static final double DEFAULT_FRAME_RATE = 60.0;

    private final Engine engine;

    // Constructors

    Manager() {
        engine = new Engine(DEFAULT_FRAME_RATE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                engine.stop();
            }
        });
    }

    // Initialization

    private void initUI() {
        setTitle("3D Graphics Engine");

        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(SCREEN_SIZE.width / 2, SCREEN_SIZE.height / 2);
        setMinimumSize(new Dimension(SCREEN_SIZE.width / 4, SCREEN_SIZE.height / 4));
        setLocationRelativeTo(null);
        setUndecorated(true);
        setExtendedState(MAXIMIZED_BOTH);
    }

    private void createAndShowGUI() {
        initUI();
        add(engine);
        setVisible(true);

        if (getExtendedState() == MAXIMIZED_BOTH) {
            engine.setSize(getWidth(), getHeight());
        }
    }

    // Run

    private void start() {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            engine.start();
        });
    }

    public static void main(String[] args) {
        Manager manager = new Manager();
        manager.start();
    }

}
