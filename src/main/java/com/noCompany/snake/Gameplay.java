package com.noCompany.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Random;

public class Gameplay extends JPanel implements KeyListener, ActionListener {

    private String path = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private int step;
    private Point[] position = new Point[750];
    private Random random = new Random();

    private int width,height;

    private boolean pause = false;

    private Point enemyPosition = new Point((random.nextInt(34) + 1) * 25, (random.nextInt(21) + 3) * 25);

    private Direction direction;
    private Direction lastDirection;

    private ImageIcon snakeFace;
    private ImageIcon enemyImage = new ImageIcon(path + "apple.png");

    private int length = 3;
    private int moves = 0;
    private int score = 0;

    private Timer timer;
    private int delay = 437;
    private int level = 1;

    private ImageIcon body = new ImageIcon(path + "body.png");
    private ImageIcon titleImage = new ImageIcon(path + "title.png");

    Gameplay(int wwidth, int wheight) {

        width=wwidth;
        height=wheight;

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {


        if (moves == 0) {
            snakeFace = new ImageIcon(path + "mr.png");
            step = snakeFace.getIconWidth();

            position[0] = new Point(4 * step, 4 * step);
            position[1] = new Point(3 * step, 4 * step);
            position[2] = new Point(2 * step, 4 * step);

            snakeFace.paintIcon(this, g, position[0].x, position[0].y);
        }

        //NAGŁÓWEK
        g.setColor(Color.WHITE);
        g.drawRect(24, 10, 851, 55);

        titleImage.paintIcon(this, g, 25, 11);

        //'liczniki'
        g.setColor(Color.WHITE);
        g.setFont(new Font("arial", Font.BOLD, 12));
        g.drawString("SCORE:  " + score, 780, 30);
        g.drawString("LENGTH: " + length, 780, 50);
        g.drawString("LEVEL:  " + level, 60, 30);
        g.drawString("Delay:  " + delay, 60, 50);

        //POLE GRY
        g.setColor(Color.WHITE);
        g.drawRect(step - 1, 3 * step - 1, 851, 23 * step + 1);

        //g.setColor(new Color(159, 197, 68));
        g.setColor(new Color(184, 203, 87));
        g.fillRect(step, 3 * step, 850, 23 * step);



        for (int i = 0; i < length; i++) {

            if ((i == 0 && direction == Direction.RIGHT) || (i == 0 && direction == null)) {
                snakeFace = new ImageIcon(path + "mr.png");
                snakeFace.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i == 0 && direction == Direction.LEFT) {
                snakeFace = new ImageIcon(path + "ml.png");
                snakeFace.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i == 0 && direction == Direction.UP) {
                snakeFace = new ImageIcon(path + "mu.png");
                snakeFace.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i == 0 && direction == Direction.DOWN) {
                snakeFace = new ImageIcon(path + "md.png");
                snakeFace.paintIcon(this, g, position[i].x, position[i].y);
            }

            if (i != 0) {
                body.paintIcon(this, g, position[i].x, position[i].y);
            }

        }


        if ((enemyPosition.x == position[0].x) && (enemyPosition.y == position[0].y)) {
            score ++;
            length++;
            enemyPosition = randPosition();


            //if (score % 20 == 0 && score != 0){
            if (score == 779){
                delay = (int) (delay/1.5);
                level++;
            }
        }
        enemyImage.paintIcon(this, g, enemyPosition.x, enemyPosition.y);

        for (int i = 1; i < length; i++) {
            if ((position[0].x == position[i].x) && (position[0].y == position[i].y)) {
                direction = null;

                g.setColor(Color.WHITE);
                g.setFont(new Font("arial", Font.BOLD, 50));
                g.drawString("GAME OVER", 300, 300);

                g.setFont(new Font("arial", Font.BOLD, 20));
                g.drawString("Press ENTER to RESTART", 350, 320);
            }
        }

        if (pause) {
            g.setColor(Color.WHITE);

            //drawCenteredString(g, "Tekst w środku", , );

            g.setFont(new Font("arial", Font.BOLD, 50));
            g.drawString("PAUSE", 300, 300);

            g.setFont(new Font("arial", Font.BOLD, 20));
            g.drawString("Press SPACE", 350, 350);

        }

        if (level==9){
            direction = null;

            g.setColor(Color.WHITE);
            g.setFont(new Font("arial", Font.BOLD, 50));
            g.drawString("YOU BEAT THE GAME", 300, 300);

            g.setFont(new Font("arial", Font.BOLD, 20));
            g.drawString("YOUR SCORE IS " + score, 350, 320);
            g.drawString("Press ENTER to try again", 300, 340);
        }

        g.dispose();
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    private Point randPosition() {
        Point point = new Point((random.nextInt(34) + 1) * 25, (random.nextInt(21) + 3) * 25);

        for (int i = 0; i <length ; i++) {
            if (point.x==position[i].x && point.y==position[i].y)
                point= randPosition();
        }
        return point;
    }

    public void actionPerformed(ActionEvent e) {

        timer.setDelay(delay);
        timer.start();

        if (!pause) {

            if (direction == Direction.RIGHT) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].x = position[i].x + step;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }


                    if (position[i].x > 850) {            // Co się stanie gdy wąż dotrze do prawej krawędzi?
                        position[i].x = step;             // Pojawi się z lewej strony
                    }
                }
            }

            if (direction == Direction.LEFT) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].x = position[i].x - step;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }

                    if (position[i].x < step) {           // Co się stanie gdy wąż dotrze do lewej krawędzi?
                        position[i].x = 850;              // Pojawi się z prawej strony
                    }
                }
            }

            if (direction == Direction.DOWN) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].y = position[i].y + step;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }

                    if (position[i].y > 625) {            // Co się stanie gdy wąż dotrze do dolnej krawędzi?
                        position[i].y = step * 3;         // Pojawi się z góry
                    }
                }
            }

            if (direction == Direction.UP) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].y = position[i].y - step;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }
                    if (position[i].y < 3 * step) {      // Co się stanie gdy wąż dotrze do górnej krawędzi?
                        position[i].y = 625;             // Pojawi się z dołu
                    }
                }
            }
        }
        repaint();
    }

    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_P && !pause) {
            pause = true;
            lastDirection = direction;

        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && pause) {
            pause = false;
            direction = lastDirection;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            length = 3;
            moves = 0;
            score = 0;
            delay = 437;
            level = 1;
            snakeFace = new ImageIcon(path + "mr.png");
            direction = Direction.RIGHT;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            if (direction != Direction.LEFT) {
                moves++;
                direction = Direction.RIGHT;
                //System.out.println("->   "+"\u2192");
            }
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            if (direction != Direction.RIGHT) {
                moves++;
                direction = Direction.LEFT;
                //System.out.println("<-   "+"\u2190");
            }
        if (e.getKeyCode() == KeyEvent.VK_UP)
            if (direction != Direction.DOWN) {
                moves++;
                direction = Direction.UP;
                //System.out.println(" /\\   "+"\u2191");
            }
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            if (direction != Direction.UP) {
                moves++;
                direction = Direction.DOWN;
                //System.out.println(" \\/   "+"\u2193");
            }

    }

    public void keyReleased(KeyEvent e) {

    }
}
