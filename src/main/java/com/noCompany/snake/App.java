package com.noCompany.snake;

import javax.swing.JFrame;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        JFrame obj = new JFrame();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        int windowWidth = 905;
        int windowHeight = 700;

        Gameplay gameplay = new Gameplay(windowWidth,windowHeight);

        obj.setBounds((int) ((width-windowWidth)/2),(int)((height-windowHeight)/2),windowWidth,windowHeight);
        obj.setBackground(new Color(Color.DARK_GRAY.getRGB()));
        obj.setResizable(false);
        obj.setVisible(true);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(gameplay);
    }
}
