package com.noCompany.snake;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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

    private int width, height;
    private int gamefieldWidth, gamefieldHeight;

    private boolean pause = false;

    private Point size;

    private Point enemyPosition;

    private Direction direction;
    private Direction lastDirection;

    private ImageIcon snakeFace;
    private ImageIcon enemyImage = new ImageIcon(path + "apple.png");

    private int length;
    private int moves, scorePerLevel, scorePerGame, maxScore = 0;

    private Timer timer;
    private int delay = 437;
    private int level = 1;

    private ImageIcon body = new ImageIcon(path + "body.png");
    private ImageIcon tail = new ImageIcon(path + "tail.png");
    private ImageIcon tail2 = new ImageIcon(path + "tail2.png");
    private ImageIcon titleImage = new ImageIcon(path + "title.png");

    private int X, Y;
    private boolean playFanfare = false;

    Gameplay(Point Size, int wWidth, int wHeight, int gfwidth, int gfheight, int Step) {

        size = Size;
        width = wWidth;
        height = wHeight;
        gamefieldWidth = gfwidth;
        gamefieldHeight = gfheight;
        step = Step;

        initialPropertiesOfSnake();

        enemyPosition = randPosition();

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {


        if (moves == 0) {
            initialPropertiesOfSnake();
            snakeFace.paintIcon(this, g, position[0].x, position[0].y);
        }

        //NAGŁÓWEK
        g.setColor(Color.WHITE);
        g.drawRect(24, 10, gamefieldWidth + 1, 55);
        g.setColor(new Color(162, 183, 56));
        g.fillRect(25, 11, gamefieldWidth, 54);

        if (gamefieldWidth >= 360) {
            titleImage.paintIcon(this, g, 25 + (gamefieldWidth - titleImage.getIconWidth()) / 2, 11);
        }

        //POLE GRY
        g.setColor(Color.WHITE);
        g.drawRect(step - 1, 3 * step - 1, gamefieldWidth + 1, gamefieldHeight + 1);
        g.setColor(new Color(184, 203, 87));
        X = step;
        Y = 3 * step;
        g.fillRect(X, Y, gamefieldWidth, gamefieldHeight);

        if ((enemyPosition.x == position[0].x) && (enemyPosition.y == position[0].y)) {
            scorePerLevel++;
            scorePerGame++;
            if (scorePerGame > maxScore)
                maxScore = scorePerGame;
            length++;

            if (length == ((gamefieldHeight * gamefieldWidth) / (step * step))) {
                delay = (int) (delay / 1.5);
                level++;
                length = 3;
                moves = 0;
                scorePerLevel = 0;
                direction = Direction.RIGHT;
                initialPropertiesOfSnake();
            }

            enemyPosition = randPosition();
        }

        for (int i = 0; i < length; i++) {

            if (i == 0) {
                if ((direction == Direction.RIGHT) || (direction == null))
                    snakeFace = new ImageIcon(path + "mr.png");
                if (direction == Direction.LEFT)
                    snakeFace = new ImageIcon(path + "ml.png");
                if (direction == Direction.UP)
                    snakeFace = new ImageIcon(path + "mu.png");
                if (direction == Direction.DOWN)
                    snakeFace = new ImageIcon(path + "md.png");

                snakeFace.paintIcon(this, g, position[i].x, position[i].y);
            }

            if (i == length - 1) {
                tail2.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i == length - 2) {
                tail.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i != 0 && i < length - 2) {
                body.paintIcon(this, g, position[i].x, position[i].y);
            }

        }

        //'liczniki'
        g.setColor(Color.WHITE);
        g.setFont(new Font("arial", Font.BOLD, 12));
        g.drawString("SCORE:  " + scorePerLevel, gamefieldWidth - 70, 30);
        g.drawString("LENGTH: " + length, gamefieldWidth - 70, 50);
        g.drawString("LEVEL:  " + level, 60, 30);
        g.drawString("RECORD:  " + maxScore, 60, 50);

        enemyImage.paintIcon(this, g, enemyPosition.x, enemyPosition.y);

        g.setColor(Color.GRAY);

        for (int i = 1; i < length; i++) {
            if ((position[0].x == position[i].x) && (position[0].y == position[i].y)) {
                direction = null;

                Font font = new Font("arial", Font.BOLD, 50);
                g.setFont(font);
                drawCenteredString(g, "GAME OVER", gamefieldWidth, gamefieldHeight, font, -45);

                font = new Font("arial", Font.BOLD, 20);
                g.setFont(font);
                drawCenteredString(g, "Press ENTER to RESTART", gamefieldWidth, gamefieldHeight, font, 5);

                //TODO wait until the user presses ENTER
            }
        }

        if (pause) {
            Font font = new Font("arial", Font.BOLD, 50);
            g.setFont(font);
            drawCenteredString(g, "PAUSE", gamefieldWidth, gamefieldHeight, font, -45);

            font = new Font("arial", Font.BOLD, 20);
            g.setFont(font);
            drawCenteredString(g, "Press SPACE to unpause", gamefieldWidth, gamefieldHeight, font, 5);

            //TODO wait until the user presses SPACE
        }

        if (level == 9) {
            direction = null;

            Font font = new Font("arial", Font.BOLD, 50);
            g.setFont(font);
            drawCenteredString(g, "YOU BEAT THE GAME", gamefieldWidth, gamefieldHeight, font, -45);

            font = new Font("arial", Font.BOLD, 20);
            g.setFont(font);
            drawCenteredString(g, "YOUR SCORE IS " + scorePerGame, gamefieldWidth, gamefieldHeight, font, 5);
            drawCenteredString(g, "Press ENTER to try again", gamefieldWidth, gamefieldHeight, font, 25);

            pause = true;
            playTune();

            //TODO wait until the user presses ENTER
        }

        g.dispose();
    }

    private void playTune() {

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            new File(path+"fanfare.wav"));
                    clip.open(inputStream);
                    clip.start();
                    playFanfare=true;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        if (!playFanfare)
            thread.start();
        while (thread.isAlive())
            pause=true;

        pause = false;
    }

    private void drawCenteredString(Graphics g, String text, int rectangleWidth, int rectangleHeight, Font font, int yShift) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = X + (rectangleWidth - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = Y + ((rectangleHeight - metrics.getHeight()) / 2) + metrics.getAscent() + yShift;
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    private Point randPosition() {
        Point point = new Point((random.nextInt(size.x) + 1) * step, (random.nextInt((size.y)) + 3) * step);

        for (int i = 0; i < length; i++) {
            if (point.x == position[i].x && point.y == position[i].y)
                point = randPosition();
        }
        return point;
    }

    private void initialPropertiesOfSnake() {

        snakeFace = new ImageIcon(path + "mr.png");
        if (size.y == 1) {
            position[0] = new Point(4 * step, 3 * step);
            position[1] = new Point(3 * step, 3 * step);
            position[2] = new Point(2 * step, 3 * step);
            length = 3;
        } else {
            position[0] = new Point(4 * step, 4 * step);
            position[1] = new Point(3 * step, 4 * step);
            position[2] = new Point(2 * step, 4 * step);
            length = 3;
        }
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


                    if (position[i].x > gamefieldWidth) {            // Co się stanie gdy wąż dotrze do prawej krawędzi?
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
                        position[i].x = gamefieldWidth;              // Pojawi się z prawej strony
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

                    if (position[i].y > gamefieldHeight + 50) {            // Co się stanie gdy wąż dotrze do dolnej krawędzi?
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
                        position[i].y = gamefieldHeight + 50;             // Pojawi się z dołu
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
            initialPropertiesOfSnake();
            moves = 0;
            scorePerLevel = 0;
            scorePerGame = 0;
            delay = 437;
            level = 1;
            direction = Direction.RIGHT;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D)
            if (direction != Direction.LEFT) {
                moves++;
                lastDirection = direction;
                direction = Direction.RIGHT;
                //System.out.println("->   "+"\u2192");
            }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A)
            if (direction != Direction.RIGHT) {
                moves++;
                lastDirection = direction;
                direction = Direction.LEFT;
                //System.out.println("<-   "+"\u2190");
            }
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W)
            if (direction != Direction.DOWN) {
                moves++;
                lastDirection = direction;
                direction = Direction.UP;
                //System.out.println(" /\\   "+"\u2191");
            }
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S)
            if (direction != Direction.UP) {
                moves++;
                lastDirection = direction;
                direction = Direction.DOWN;
                //System.out.println(" \\/   "+"\u2193");
            }

    }

    public void keyReleased(KeyEvent e) {

    }
}
