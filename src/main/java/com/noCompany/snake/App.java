package com.noCompany.snake;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class App {
    public static void main(String[] args) {
        JFrame obj = new JFrame();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenSizeWidth = screenSize.getWidth();
        double screenSizeHeight = screenSize.getHeight();

        String path = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
        int step = new ImageIcon(path + "body.png").getIconWidth();

        //Point size = new Point(9,1);     //good test values
        Point size = new Point(23,11); // x >= 8, y >= 5

        int gamefieldWidth = size.x*step;
        int gamefieldHeight = size.y*step;
        int windowWidth = gamefieldWidth + 55;
        int windowHeight = gamefieldHeight + 125;

        Gameplay gameplay = new Gameplay(size,windowWidth,windowHeight, gamefieldWidth,gamefieldHeight,step);

        obj.setTitle("Snake");
        obj.setIconImage(new ImageIcon(path+"icon.png").getImage());
        obj.setBounds((int) ((screenSizeWidth-windowWidth)/2),(int)((screenSizeHeight-windowHeight)/2),windowWidth,windowHeight);
        obj.setBackground(new Color(Color.DARK_GRAY.getRGB()));
        obj.setResizable(false);
        obj.setVisible(true);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(gameplay);
    }
}
