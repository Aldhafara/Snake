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
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Gameplay extends JPanel implements KeyListener, ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Gameplay.class);

    private final static Map<Integer, Direction> KEY_DIRECTION_MAP = Map.of(
            KeyEvent.VK_RIGHT, Direction.RIGHT,
            KeyEvent.VK_D, Direction.RIGHT,
            KeyEvent.VK_LEFT, Direction.LEFT,
            KeyEvent.VK_A, Direction.LEFT,
            KeyEvent.VK_UP, Direction.UP,
            KeyEvent.VK_W, Direction.UP,
            KeyEvent.VK_DOWN, Direction.DOWN,
            KeyEvent.VK_S, Direction.DOWN
    );
    private final static String PATH = "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private final static ImageIcon TARGET_IMAGE = new ImageIcon(PATH + "apple.png");
    private final static ImageIcon BODY = new ImageIcon(PATH + "body.png");
    private final static ImageIcon TAIL_MIDDLE = new ImageIcon(PATH + "tail.png");
    private final static ImageIcon TAIL_END = new ImageIcon(PATH + "tail2.png");
    private final static ImageIcon TITLE_IMAGE = new ImageIcon(PATH + "title.png");
    private final static int GRID_SIZE = BODY.getIconWidth();
    private final static int MAX_LEVEL = 9;
    private final static int STARTING_LENGTH = 3;
    private final int OUTSIDE_BORDER_WIDTH;
    private final LinkedList<Point> snake = new LinkedList<>();

    private final Random random = new Random();
    private final Rectangle gameField;
    private final Dimension playingFieldDimensions;
    private final int gameFieldVerticalDisplacement;
    private final int titleBarHeight;
    private final Timer timer;

    private final int width;
    private final int height;
    private boolean pause = false;
    private boolean gameOver = false;
    private boolean biteHimself = false;
    private Point targetPosition;
    private Direction direction = Direction.RIGHT;
    private Direction lastDirectionTyped;
    private Direction lastDirectionExecuted;
    private final ImageIcon snakeFace = new ImageIcon(PATH + "mr.png");
    private int moves, scorePerLevel, scorePerGame, maxScore = 0;
    private int delay = 437;
    private int level = 1;
    private boolean playFanfare = false;

    public Gameplay(Dimension gridDimension, int titleBarHeight, Rectangle window, Rectangle gameField) {
        this.titleBarHeight = titleBarHeight;
        this.gameFieldVerticalDisplacement = gameField.y;
        this.OUTSIDE_BORDER_WIDTH = gameField.x;
        this.playingFieldDimensions = gridDimension;
        this.width = window.width;
        this.height = window.height;
        this.gameField = gameField;

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
            drawSnakeBodyPart(g, snakeFace, snake.getLast());
        }

        drawSoreBoard(g);
        drawTitleBox(g);
        drawGameField(g);
        drawSnake(g);
        drawCounters(g);

        drawSnakeBodyPart(g, TARGET_IMAGE, targetPosition);

        g.setColor(Color.GRAY);

        for (int i = 1; i < snake.size(); i++) {
            if (biteHimself) {
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

    private void incrementCounters() {
        if (scorePerGame > maxScore) {
            maxScore = scorePerGame;
        }

        if(snake.size() == playingFieldDimensions.width * playingFieldDimensions.height){
            delay = (int) (delay / 1.5);
            level++;
            moves = 0;
            scorePerLevel = 0;
            direction = Direction.RIGHT;
            initializeSnake();
        }

        scorePerLevel++;
        scorePerGame++;
        if (snake.size() < gameField.height * gameField.width) {
            targetPosition = getNewTargetPosition();
        }
    }

    private boolean targetReached() {
        return (targetPosition.x == snake.getLast().x) && (targetPosition.y == snake.getLast().y);
    }

    private void drawPauseMessage(Graphics g) {
        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "PAUSE", gameField.width, gameField.height, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "Press SPACE to unpause", gameField.width, gameField.height, font, 5);
    }

    private void drawEndMessage(Graphics g) {
        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "YOU BEAT THE GAME", gameField.width, gameField.height, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "YOUR SCORE IS " + scorePerGame, gameField.width, gameField.height, font, 5);
        drawCenteredString(g, "Press ENTER to try again", gameField.width, gameField.height, font, 25);
    }

    private void drawEndGameMessage(Graphics g) {
        direction = null;

        Font font = new Font("arial", Font.BOLD, 50);
        g.setFont(font);
        drawCenteredString(g, "GAME OVER", gameField.width, gameField.height, font, -45);

        font = new Font("arial", Font.BOLD, 20);
        g.setFont(font);
        drawCenteredString(g, "Press ENTER to RESTART", gameField.width, gameField.height, font, 5);

        //TODO wait until the user presses ENTER
    }

    private void drawSnake(Graphics g) {
        int lastIndex = snake.size() - 1;

        for (int i = 0; i < snake.size(); i++) {
            Point point = snake.get(i);

            if (i == 0) {
                drawSnakeBodyPart(g, TAIL_END, point);
            } else if (i == 1) {
                drawSnakeBodyPart(g, TAIL_MIDDLE, point);
            } else if (i == lastIndex) {
                ImageIcon snakeFace = determineSnakeFace(direction);
                drawSnakeBodyPart(g, snakeFace, point);
            } else {
                drawSnakeBodyPart(g, BODY, point);
            }
        }
    }

    private ImageIcon determineSnakeFace(Direction direction) {
        if (direction == null) {
            return new ImageIcon(PATH + "mr.png");
        }
        return switch (direction) {
            case RIGHT -> new ImageIcon(PATH + "mr.png");
            case LEFT -> new ImageIcon(PATH + "ml.png");
            case UP -> new ImageIcon(PATH + "mu.png");
            case DOWN -> new ImageIcon(PATH + "md.png");
            default -> new ImageIcon(PATH + "mr.png");
        };
    }

    private void drawSnakeBodyPart(Graphics g, ImageIcon imageIcon, Point position) {
        imageIcon.paintIcon(this, g, position.x * GRID_SIZE + OUTSIDE_BORDER_WIDTH, position.y * GRID_SIZE + gameFieldVerticalDisplacement);
    }

    private void drawGameField(Graphics g) {
        drawRectangle(g, new Color(184, 203, 87), new Rectangle(OUTSIDE_BORDER_WIDTH, gameFieldVerticalDisplacement, gameField.width, gameField.height));
    }

    private void drawSoreBoard(Graphics g) {
        drawRectangle(g, new Color(162, 183, 56), new Rectangle(OUTSIDE_BORDER_WIDTH, OUTSIDE_BORDER_WIDTH, gameField.width, 125));
    }

    private void drawTitleBox(Graphics g) {
        if (gameField.width >= 374) {
            TITLE_IMAGE.paintIcon(this, g, (width - TITLE_IMAGE.getIconWidth()) / 2, gameFieldVerticalDisplacement - TITLE_IMAGE.getIconHeight() - 15);
        }
    }

    private void drawCounters(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("arial", Font.BOLD, 12));

        //RIGHT SIDE
        g.drawString("SCORE:  " + scorePerLevel, width - 120, titleBarHeight + 20);
        g.drawString("LENGTH: " + snake.size(), width - 120, titleBarHeight + 40);

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
        int textPositionX = GRID_SIZE + (rectangleWidth - metrics.stringWidth(text)) / 2;
        int textPositionY = 3 * GRID_SIZE + ((rectangleHeight - metrics.getHeight()) / 2) + metrics.getAscent() + yShift;

        g.setFont(font);
        g.drawString(text, textPositionX, textPositionY);
    }

    private Point getNewTargetPosition() {
        Point target = new Point((random.nextInt(playingFieldDimensions.width)), (random.nextInt((playingFieldDimensions.height))));

        for (int i = 0; i < snake.size(); i++) {
            if (snake.contains(target)) {
                target = getNewTargetPosition();
            }
        }
        return target;
    }

    private void initializeSnake() {
        snake.clear();
        for (int i = 0; i < STARTING_LENGTH; i++) {
            snake.add(new Point(2 + i, playingFieldDimensions.height == 1 ? 0 : 1));
        }
    }

    public void actionPerformed(ActionEvent e) {

        timer.setDelay(delay);
        timer.start();

        if (!pause) {

            if (direction == Direction.RIGHT) {
                int possibleX = snake.getLast().x + 1;
                if (possibleX >= gameField.width / GRID_SIZE) {
                    possibleX = 0;
                }
                Point nextStep = new Point(possibleX, snake.getLast().y);
                if (!snake.contains(nextStep) || snake.getFirst().equals(nextStep)) {
                    snake.add(nextStep);
                    if (!targetReached()) {
                        snake.removeFirst();
                    } else {
                        incrementCounters();
                    }
                } else {
                    biteHimself = true;
                }
            }

            if (direction == Direction.LEFT) {
                int possibleX = snake.getLast().x - 1;
                if (possibleX < 0) {
                    possibleX = gameField.width / GRID_SIZE - 1;
                }
                Point nextStep = new Point(possibleX, snake.getLast().y);
                if (!snake.contains(nextStep) || snake.getFirst().equals(nextStep)) {
                    snake.add(nextStep);
                    if (!targetReached()) {
                        snake.removeFirst();
                    } else {
                        incrementCounters();
                    }
                } else {
                    biteHimself = true;
                }
            }

            if (direction == Direction.DOWN) {
                int possibleY = snake.getLast().y + 1;
                if (possibleY > playingFieldDimensions.height - 1) {
                    possibleY = 0;
                }
                Point nextStep = new Point(snake.getLast().x, possibleY);
                if (!snake.contains(nextStep) || snake.getFirst().equals(nextStep)) {
                    snake.add(nextStep);
                    if (!targetReached()) {
                        snake.removeFirst();
                    } else {
                        incrementCounters();
                    }
                } else {
                    biteHimself = true;
                }
            }

            if (direction == Direction.UP) {
                int possibleY = snake.getLast().y - 1;
                if (possibleY < 0) {
                    possibleY = playingFieldDimensions.height - 1;
                }
                Point nextStep = new Point(snake.getLast().x, possibleY);
                if (!snake.contains(nextStep) || snake.getFirst().equals(nextStep)) {
                    snake.add(nextStep);
                    if (!targetReached()) {
                        snake.removeFirst();
                    } else {
                        incrementCounters();
                    }
                } else {
                    biteHimself = true;
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
            initializeSnake();
            moves = 0;
            scorePerLevel = 0;
            scorePerGame = 0;
            delay = 437;
            level = 1;
            direction = Direction.RIGHT;
            gameOver = false;
        } else {
            Direction newDirection = KEY_DIRECTION_MAP.get(e.getKeyCode());
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
