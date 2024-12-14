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
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gameplay extends JPanel implements KeyListener, ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Gameplay.class);


    private final String path = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private final int gridSizeInPixels;
    private final Point[] position = new Point[750];

    private final Random random = new Random();

    private int width, height;
    private final int gameFieldWidth;
    private final int gameFieldHeight;
    private final Point size;
    private final Timer timer;
    private final ImageIcon enemyImage = new ImageIcon(path + "apple.png");
    private final ImageIcon body = new ImageIcon(path + "body.png");
    private final ImageIcon tailMiddle = new ImageIcon(path + "tail.png");
    private final ImageIcon tailEnd = new ImageIcon(path + "tail2.png");
    private final ImageIcon titleImage = new ImageIcon(path + "title.png");
    private boolean pause = false;
    private Point enemyPosition;
    private Direction direction;
    private Direction lastDirection;
    private ImageIcon snakeFace;
    private int length;
    private int moves, scorePerLevel, scorePerGame, maxScore = 0;
    private int delay = 437;
    private int level = 1;
    private int X, Y;
    private boolean playFanfare = false;
    private final Map<Integer, Direction> keyDirectionMap = Map.of(
            KeyEvent.VK_RIGHT, Direction.RIGHT,
            KeyEvent.VK_D, Direction.RIGHT,
            KeyEvent.VK_LEFT, Direction.LEFT,
            KeyEvent.VK_A, Direction.LEFT,
            KeyEvent.VK_UP, Direction.UP,
            KeyEvent.VK_W, Direction.UP,
            KeyEvent.VK_DOWN, Direction.DOWN,
            KeyEvent.VK_S, Direction.DOWN
    );

    Gameplay(Point Size, int wWidth, int wHeight, int gameFieldWidth, int gameFieldHeight, int gridSizeInPixels) {

        size = Size;
        width = wWidth;
        height = wHeight;
        this.gameFieldWidth = gameFieldWidth;
        this.gameFieldHeight = gameFieldHeight;
        this.gridSizeInPixels = gridSizeInPixels;

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
        g.drawRect(24, 10, gameFieldWidth + 1, 55);
        g.setColor(new Color(162, 183, 56));
        g.fillRect(25, 11, gameFieldWidth, 54);

        if (gameFieldWidth >= 360) {
            titleImage.paintIcon(this, g, 25 + (gameFieldWidth - titleImage.getIconWidth()) / 2, 11);
        }

        //POLE GRY
        g.setColor(Color.WHITE);
        g.drawRect(gridSizeInPixels - 1, 3 * gridSizeInPixels - 1, gameFieldWidth + 1, gameFieldHeight + 1);
        g.setColor(new Color(184, 203, 87));
        X = gridSizeInPixels;
        Y = 3 * gridSizeInPixels;
        g.fillRect(X, Y, gameFieldWidth, gameFieldHeight);

        if ((enemyPosition.x == position[0].x) && (enemyPosition.y == position[0].y)) {
            scorePerLevel++;
            scorePerGame++;
            if (scorePerGame > maxScore)
                maxScore = scorePerGame;
            length++;

            if (length == ((gameFieldHeight * gameFieldWidth) / (gridSizeInPixels * gridSizeInPixels))) {
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
                tailEnd.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i == length - 2) {
                tailMiddle.paintIcon(this, g, position[i].x, position[i].y);
            }
            if (i != 0 && i < length - 2) {
                body.paintIcon(this, g, position[i].x, position[i].y);
            }

        }

        //'liczniki'
        g.setColor(Color.WHITE);
        g.setFont(new Font("arial", Font.BOLD, 12));
        g.drawString("SCORE:  " + scorePerLevel, gameFieldWidth - 70, 30);
        g.drawString("LENGTH: " + length, gameFieldWidth - 70, 50);
        g.drawString("LEVEL:  " + level, 60, 30);
        g.drawString("RECORD:  " + maxScore, 60, 50);

        enemyImage.paintIcon(this, g, enemyPosition.x, enemyPosition.y);

        g.setColor(Color.GRAY);

        for (int i = 1; i < length; i++) {
            if ((position[0].x == position[i].x) && (position[0].y == position[i].y)) {
                direction = null;

                Font font = new Font("arial", Font.BOLD, 50);
                g.setFont(font);
                drawCenteredString(g, "GAME OVER", gameFieldWidth, gameFieldHeight, font, -45);

                font = new Font("arial", Font.BOLD, 20);
                g.setFont(font);
                drawCenteredString(g, "Press ENTER to RESTART", gameFieldWidth, gameFieldHeight, font, 5);

                //TODO wait until the user presses ENTER
            }
        }

        if (pause) {
            Font font = new Font("arial", Font.BOLD, 50);
            g.setFont(font);
            drawCenteredString(g, "PAUSE", gameFieldWidth, gameFieldHeight, font, -45);

            font = new Font("arial", Font.BOLD, 20);
            g.setFont(font);
            drawCenteredString(g, "Press SPACE to unpause", gameFieldWidth, gameFieldHeight, font, 5);

            //TODO wait until the user presses SPACE
        }

        if (level == 9) {
            direction = null;

            Font font = new Font("arial", Font.BOLD, 50);
            g.setFont(font);
            drawCenteredString(g, "YOU BEAT THE GAME", gameFieldWidth, gameFieldHeight, font, -45);

            font = new Font("arial", Font.BOLD, 20);
            g.setFont(font);
            drawCenteredString(g, "YOUR SCORE IS " + scorePerGame, gameFieldWidth, gameFieldHeight, font, 5);
            drawCenteredString(g, "Press ENTER to try again", gameFieldWidth, gameFieldHeight, font, 25);

            pause = true;
            playTune();

            //TODO wait until the user presses ENTER
        }

        g.dispose();
    }

    private void playTune() {

        Thread thread = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        new File(path + "fanfare.wav"));
                clip.open(inputStream);
                clip.start();
                playFanfare = true;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
        if (!playFanfare)
            thread.start();
        while (thread.isAlive())
            pause = true;

        pause = false;
    }

    private void drawCenteredString(Graphics g, String text, int rectangleWidth, int rectangleHeight, Font font, int yShift) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textPositionX = X + (rectangleWidth - metrics.stringWidth(text)) / 2;
        int textPositionY = Y + ((rectangleHeight - metrics.getHeight()) / 2) + metrics.getAscent() + yShift;

        g.setFont(font);
        g.drawString(text, textPositionX, textPositionY);
    }

    private Point randPosition() {
        Point point = new Point((random.nextInt(size.x) + 1) * gridSizeInPixels, (random.nextInt((size.y)) + 3) * gridSizeInPixels);

        for (int i = 0; i < length; i++) {
            if (point.x == position[i].x && point.y == position[i].y)
                point = randPosition();
        }
        return point;
    }

    private void initialPropertiesOfSnake() {

        snakeFace = new ImageIcon(path + "mr.png");
        if (size.y == 1) {
            position[0] = new Point(4 * gridSizeInPixels, 3 * gridSizeInPixels);
            position[1] = new Point(3 * gridSizeInPixels, 3 * gridSizeInPixels);
            position[2] = new Point(2 * gridSizeInPixels, 3 * gridSizeInPixels);
            length = 3;
        } else {
            position[0] = new Point(4 * gridSizeInPixels, 4 * gridSizeInPixels);
            position[1] = new Point(3 * gridSizeInPixels, 4 * gridSizeInPixels);
            position[2] = new Point(2 * gridSizeInPixels, 4 * gridSizeInPixels);
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
                        position[i].x = position[i].x + gridSizeInPixels;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }

                    if (position[i].x > gameFieldWidth) {             // Co się stanie gdy wąż dotrze do prawej krawędzi?
                        position[i].x = gridSizeInPixels;             // Pojawi się z lewej strony
                    }
                }
            }

            if (direction == Direction.LEFT) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].x = position[i].x - gridSizeInPixels;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }

                    if (position[i].x < gridSizeInPixels) {           // Co się stanie gdy wąż dotrze do lewej krawędzi?
                        position[i].x = gameFieldWidth;               // Pojawi się z prawej strony
                    }
                }
            }

            if (direction == Direction.DOWN) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].y = position[i].y + gridSizeInPixels;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }

                    if (position[i].y > gameFieldHeight + 50) {       // Co się stanie gdy wąż dotrze do dolnej krawędzi?
                        position[i].y = gridSizeInPixels * 3;         // Pojawi się z góry
                    }
                }
            }

            if (direction == Direction.UP) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        position[i] = new Point(position[i - 1].x, position[i - 1].y);
                    else if (i == 0)
                        position[i].y = position[i].y - gridSizeInPixels;
                    else {
                        position[i].x = position[i - 1].x;
                        position[i].y = position[i - 1].y;
                    }
                    if (position[i].y < 3 * gridSizeInPixels) {       // Co się stanie gdy wąż dotrze do górnej krawędzi?
                        position[i].y = gameFieldHeight + 50;         // Pojawi się z dołu
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
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE && pause) {
            pause = false;
            direction = lastDirection;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            initialPropertiesOfSnake();
            moves = 0;
            scorePerLevel = 0;
            scorePerGame = 0;
            delay = 437;
            level = 1;
            direction = Direction.RIGHT;
        } else {
            Direction newDirection = keyDirectionMap.get(e.getKeyCode());
            if (newDirection != null && direction != opposite(newDirection)) {
                moves++;
                lastDirection = direction;
                direction = newDirection;
                logger.debug("Direction changed to: {}", direction);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    private Direction opposite(Direction dir) {
        return switch (dir) {
            case RIGHT -> Direction.LEFT;
            case LEFT -> Direction.RIGHT;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
        };
    }
}
