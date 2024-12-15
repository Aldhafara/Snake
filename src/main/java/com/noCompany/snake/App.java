package com.noCompany.snake;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class App {

    private final static String PATH = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private final static int GRID_SIZE_IN_PIXELS = new ImageIcon(PATH + "body.png").getIconWidth();

    public static void main(String[] args) {

        int titleBarHeight = 38;

        Dimension gridDimension = new Dimension(23,11);     // x >= 8, y >= 5
        int outsideBorder = 25;
        int scoreBoardHeight = 75;
        int separationLineUnderScoreBoardHeight = 10;

        int gameFieldWidth = gridDimension.width * GRID_SIZE_IN_PIXELS;
        int gameFieldHeight = gridDimension.height * GRID_SIZE_IN_PIXELS;

        int gameFieldVerticalDisplacement = outsideBorder + scoreBoardHeight + separationLineUnderScoreBoardHeight ;
        int windowWidth = gameFieldWidth + 2 * outsideBorder;
        int windowHeight = gameFieldHeight + outsideBorder + gameFieldVerticalDisplacement + titleBarHeight;

        Gameplay gameplay = new Gameplay(
                gridDimension,
                titleBarHeight,
                new Rectangle(0, 0, windowWidth, windowHeight),
                new Rectangle(outsideBorder, gameFieldVerticalDisplacement, gameFieldWidth, gameFieldHeight)
        );

        JFrame frame = new JFrame("Snake");
        frame.setIconImage(new ImageIcon(PATH + "icon.png").getImage());
        frame.setBounds(
                (int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth() - windowWidth) / 2),
                (int) ((Toolkit.getDefaultToolkit().getScreenSize().getHeight() - windowHeight) / 2),
                windowWidth +10, windowHeight
        );
        frame.setBackground(Color.DARK_GRAY);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gameplay);
        frame.setVisible(true);
    }
}
