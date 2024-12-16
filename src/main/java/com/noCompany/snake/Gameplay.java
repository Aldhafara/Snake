package com.noCompany.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Gameplay extends JPanel implements KeyListener, ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Gameplay.class);

    private final static String PATH = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private final static int GRID_SIZE = new ImageIcon(PATH + "body.png").getIconWidth();
    private final static int MAX_LEVEL = 9;
    private final int OUTSIDE_BORDER_WIDTH;
    private final Point[] snake = new Point[750];
    private final ArrayList<Point> newSnake = new ArrayList<>();

    private final Random random = new Random();
    private final int gameFieldWidth;
    private final int gameFieldHeight;
    private final Dimension playingFieldDimensions;
    private final int gameFieldVerticalDisplacement;
    private final int titleBarHeight;
    private final Timer timer;
    private final ImageIcon targetImage = new ImageIcon(PATH + "apple.png");
    private final ImageIcon body = new ImageIcon(PATH + "body.png");
    private final ImageIcon tailMiddle = new ImageIcon(PATH + "tail.png");
    private final ImageIcon tailEnd = new ImageIcon(PATH + "tail2.png");
    private final ImageIcon titleImage = new ImageIcon(PATH + "title.png");
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
    private final int width;
    private final int height;
    private boolean pause = false;
    private boolean gameOver = false;
    private Point targetPosition;
    private Direction direction = Direction.RIGHT;
    private Direction lastDirectionTyped;
    private Direction lastDirectionExecuted;
    private ImageIcon snakeFace = new ImageIcon(PATH + "mr.png");
    private int length = 3;
    private int moves, scorePerLevel, scorePerGame, maxScore = 0;
    private int delay = 437;
    private int level = 1;
    private int X, Y;
    private boolean playFanfare = false;

    public Gameplay(Dimension gridDimension, int titleBarHeight, Rectangle window, Rectangle gameField) {
        this.titleBarHeight = titleBarHeight;
        this.gameFieldVerticalDisplacement = gameField.y;
        this.OUTSIDE_BORDER_WIDTH = gameField.x;
        this.playingFieldDimensions = gridDimension;
        this.width = window.width;
        this.height = window.height;
        this.gameFieldWidth = gameField.width;
        this.gameFieldHeight = gameField.height;

        initializeSnake();

        targetPosition = getNewTargetPosition();

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {

        if (moves == 0) {
            initializeSnake();
            drawSnakeBodyPart(g, snakeFace, snake[0]);
        }

        drawSoreBoard(g);
        drawTitleBox(g);
        drawGameField(g);

        X = GRID_SIZE;
        Y = 3 * GRID_SIZE;

        if (targetReached()) {
            if (scorePerGame > maxScore) {
                maxScore = scorePerGame;
            }

            length++;

            if (length == ((gameFieldHeight * gameFieldWidth) / (GRID_SIZE * GRID_SIZE))) {
                delay = (int) (delay / 1.5);
                level++;
                length = 3;
                moves = 0;
                scorePerLevel = 0;
                direction = Direction.RIGHT;
                initializeSnake();
            }

            scorePerLevel++;
            scorePerGame++;
            targetPosition = getNewTargetPosition();
        }

        drawSnake(g);
        drawCounters(g);

        drawSnakeBodyPart(g, targetImage, targetPosition);

        g.setColor(Color.GRAY);

        for (int i = 1; i < length; i++) {
            if ((snake[0].x == snake[i].x) && (snake[0].y == snake[i].y)) {
                drawEndGameMessage(g);
                gameOver = true;
            }
        }

        if (pause) {
            drawPauseMessage(g);
        }
        if (level == MAX_LEVEL) {
            drawEndMessage(g);

            direction = null;
            pause = true;
            gameOver = true;
            playTune();
        }

        g.dispose();
    }

    private boolean targetReached() {
        return (targetPosition.x == snake[0].x) && (targetPosition.y == snake[0].y);
    }

    private void drawPauseMessage(Graphics g) {
        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "PAUSE", gameFieldWidth, gameFieldHeight, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "Press SPACE to unpause", gameFieldWidth, gameFieldHeight, font, 5);
    }

    private void drawEndMessage(Graphics g) {
        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "YOU BEAT THE GAME", gameFieldWidth, gameFieldHeight, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "YOUR SCORE IS " + scorePerGame, gameFieldWidth, gameFieldHeight, font, 5);
        drawCenteredString(g, "Press ENTER to try again", gameFieldWidth, gameFieldHeight, font, 25);
    }

    private void drawEndGameMessage(Graphics g) {
        direction = null;

        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "GAME OVER", gameFieldWidth, gameFieldHeight, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "Press ENTER to RESTART", gameFieldWidth, gameFieldHeight, font, 5);

        //TODO wait until the user presses ENTER
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < length; i++) {

            if (i == 0) {
                if (direction == null) {
                    snakeFace = new ImageIcon(PATH + "mr.png");
                } else {
                    switch (direction) {
                        case RIGHT -> snakeFace = new ImageIcon(PATH + "mr.png");
                        case LEFT -> snakeFace = new ImageIcon(PATH + "ml.png");
                        case UP -> snakeFace = new ImageIcon(PATH + "mu.png");
                        case DOWN -> snakeFace = new ImageIcon(PATH + "md.png");
                        default -> snakeFace = new ImageIcon(PATH + "mr.png");
                    }
                }
                lastDirectionExecuted = lastDirectionTyped;

                drawSnakeBodyPart(g, snakeFace, snake[i]);
            }

            if (i == length - 1) {
                drawSnakeBodyPart(g, tailEnd, snake[i]);
            }
            if (i == length - 2) {
                drawSnakeBodyPart(g, tailMiddle, snake[i]);
            }
            if (i != 0 && i < length - 2) {
                drawSnakeBodyPart(g, body, snake[i]);
            }
        }
    }

    private void drawSnakeBodyPart(Graphics g, ImageIcon imageIcon, Point position) {
        imageIcon.paintIcon(this, g, position.x * GRID_SIZE, position.y * GRID_SIZE + gameFieldVerticalDisplacement);
    }

    private void drawGameField(Graphics g) {
        drawRectangle(g, new Color(184, 203, 87), new Rectangle(OUTSIDE_BORDER_WIDTH, gameFieldVerticalDisplacement, gameFieldWidth, gameFieldHeight));
    }

    private void drawSoreBoard(Graphics g) {
        drawRectangle(g, new Color(162, 183, 56), new Rectangle(OUTSIDE_BORDER_WIDTH, OUTSIDE_BORDER_WIDTH, gameFieldWidth, 75));
    }

    private void drawTitleBox(Graphics g) {
        if (gameFieldWidth >= 360) {
            titleImage.paintIcon(this, g, (width - titleImage.getIconWidth()) / 2, gameFieldVerticalDisplacement - titleImage.getIconHeight() - 15);
        }
    }

    private void drawCounters(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("arial", Font.BOLD, 12));

        //RIGHT SIDE
        g.drawString("SCORE:  " + scorePerLevel, width - 120, titleBarHeight + 20);
        g.drawString("LENGTH: " + length, width - 120, titleBarHeight + 40);

        //LEFT SIDE
        g.drawString("LEVEL:  " + level, 60, titleBarHeight + 20);
        g.drawString("RECORD:  " + maxScore, 60, titleBarHeight + 40);
    }

    private void drawRectangle(Graphics g, Color color, Rectangle rectangle) {
        g.setColor(Color.WHITE);
        g.drawRect(rectangle.x - 1, rectangle.y - 1, rectangle.width + 1, rectangle.height + 1);
        g.setColor(color);
        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    private void playTune() {

        Thread thread = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        new File(PATH + "fanfare.wav"));
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

    private Point getNewTargetPosition() {
        Point point = new Point((random.nextInt(playingFieldDimensions.width) + 1), (random.nextInt((playingFieldDimensions.height)) ));

        for (int i = 0; i < length; i++) {
            if (point.x == snake[i].x && point.y == snake[i].y) {
                point = getNewTargetPosition();
            }
        }
        return point;
    }

    private void initializeSnake() {
        for (int i = 0; i < length; i++) {
            snake[i] = new Point(4 - i ,playingFieldDimensions.height == 1 ? 0 : 1);
        }
    }

    public void actionPerformed(ActionEvent e) {

        timer.setDelay(delay);
        timer.start();

        if (!pause) {

            if (direction == Direction.RIGHT) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        snake[i] = new Point(snake[i - 1].x, snake[i - 1].y);
                    else if (i == 0)
                        snake[i].x = snake[i].x + 1;
                    else {
                        snake[i].x = snake[i - 1].x;
                        snake[i].y = snake[i - 1].y;
                    }

                    if (snake[i].x > gameFieldWidth/GRID_SIZE) {          // Co się stanie gdy wąż dotrze do prawej krawędzi?
                        snake[i].x = 1;                                   // Pojawi się z lewej strony
                    }
                }
            }

            if (direction == Direction.LEFT) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        snake[i] = new Point(snake[i - 1].x, snake[i - 1].y);
                    else if (i == 0)
                        snake[i].x = snake[i].x - 1;
                    else {
                        snake[i].x = snake[i - 1].x;
                        snake[i].y = snake[i - 1].y;
                    }

                    if (snake[i].x < 1) {                                 // Co się stanie gdy wąż dotrze do lewej krawędzi?
                        snake[i].x = gameFieldWidth/GRID_SIZE;            // Pojawi się z prawej strony
                    }
                }
            }

            if (direction == Direction.DOWN) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        snake[i] = new Point(snake[i - 1].x, snake[i - 1].y);
                    else if (i == 0)
                        snake[i].y = snake[i].y + 1;
                    else {
                        snake[i].x = snake[i - 1].x;
                        snake[i].y = snake[i - 1].y;
                    }

                    if (snake[i].y > playingFieldDimensions.height - 1) { // Co się stanie gdy wąż dotrze do dolnej krawędzi?
                        snake[i].y = 0;                                   // Pojawi się z góry
                    }
                }
            }

            if (direction == Direction.UP) {
                for (int i = length; i >= 0; i--) {
                    if (i == length)
                        snake[i] = new Point(snake[i - 1].x, snake[i - 1].y);
                    else if (i == 0)
                        snake[i].y = snake[i].y - 1;
                    else {
                        snake[i].x = snake[i - 1].x;
                        snake[i].y = snake[i - 1].y;
                    }
                    if (snake[i].y < 0) {                                 // Co się stanie gdy wąż dotrze do górnej krawędzi?
                        snake[i].y = playingFieldDimensions.height - 1;   // Pojawi się z dołu
                    }
                }

            }
        }
        repaint();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (pause) {
                direction = lastDirectionTyped;
                pause = false;
            } else {
                lastDirectionTyped = direction;
                pause = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
            length = 3;
            initializeSnake();
            moves = 0;
            scorePerLevel = 0;
            scorePerGame = 0;
            delay = 437;
            level = 1;
            direction = Direction.RIGHT;
            gameOver = false;
        } else {
            Direction newDirection = keyDirectionMap.get(e.getKeyCode());
            if (newDirection != null && direction != opposite(newDirection) && !pause) {
                moves++;
                lastDirectionTyped = direction;
                direction = newDirection;
                logger.debug("Direction changed to: {}", direction);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public Direction opposite(Direction dir) {
        return switch (dir) {
            case RIGHT -> Direction.LEFT;
            case LEFT -> Direction.RIGHT;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
        };
    }
}
