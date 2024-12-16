package com.noCompany.snake;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class App {

    private static final String PATH = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private static final int TITLE_BAR_HEIGHT = 38;
    private static final int OUTSIDE_BORDER = 25;
    private static final int SCOREBOARD_HEIGHT = 125;
    private static final int SEPARATION_LINE_HEIGHT = 10;
    private static final Dimension GRID_DIMENSION = new Dimension(15, 11);
    private static final int GRID_SIZE_IN_PIXELS = new ImageIcon(PATH + "body.png").getIconWidth();

    public static void main(String[] args) {
        Dimension gameDimensions = calculateGameDimensions();
        JFrame frame = createGameWindow(gameDimensions);
        frame.setVisible(true);
    }

    private static Dimension calculateGameDimensions() {
        int gameFieldWidth = GRID_DIMENSION.width * GRID_SIZE_IN_PIXELS;
        int gameFieldHeight = GRID_DIMENSION.height * GRID_SIZE_IN_PIXELS;
        int gameFieldVerticalDisplacement = OUTSIDE_BORDER + SCOREBOARD_HEIGHT + SEPARATION_LINE_HEIGHT;

        int windowWidth = gameFieldWidth + 2 * OUTSIDE_BORDER;
        int windowHeight = gameFieldHeight + OUTSIDE_BORDER + gameFieldVerticalDisplacement + TITLE_BAR_HEIGHT;

        return new Dimension(windowWidth, windowHeight);
    }

    private static JFrame createGameWindow(Dimension gameDimensions) {
        int windowWidth = gameDimensions.width;
        int windowHeight = gameDimensions.height;

        Gameplay gameplay = new Gameplay(
                GRID_DIMENSION,
                TITLE_BAR_HEIGHT,
                new Rectangle(0, 0, windowWidth, windowHeight),
                new Rectangle(
                        OUTSIDE_BORDER,
                        OUTSIDE_BORDER + SCOREBOARD_HEIGHT + SEPARATION_LINE_HEIGHT,
                        GRID_DIMENSION.width * GRID_SIZE_IN_PIXELS,
                        GRID_DIMENSION.height * GRID_SIZE_IN_PIXELS
                )
        );

        JFrame frame = new JFrame("Snake");
        frame.setIconImage(new ImageIcon(PATH + "icon.png").getImage());
        frame.setBounds(
                (int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth() - windowWidth) / 2),
                (int) ((Toolkit.getDefaultToolkit().getScreenSize().getHeight() - windowHeight) / 2),
                windowWidth + 12,
                windowHeight
        );
        frame.setBackground(Color.DARK_GRAY);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gameplay);

        return frame;
    }
}
