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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final static int GRID_SIZE = BODY.getIconWidth();
    private final static ImageIcon TAIL_MIDDLE = new ImageIcon(PATH + "tail.png");
    private final static ImageIcon TAIL_END = new ImageIcon(PATH + "tail2.png");
    private final static ImageIcon TITLE_IMAGE = new ImageIcon(PATH + "title.png");
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
    private final ImageIcon snakeFace = new ImageIcon(PATH + "mr.png");
    private boolean pause = true;
    private boolean gameOver = false;
    private boolean biteHimself = false;
    private Point targetPosition;
    private Direction direction = Direction.RIGHT;
    private Direction lastDirectionExecuted = Direction.RIGHT;
    private int moves, scorePerLevel, scorePerGame, maxScore = 0;
    private int delay = 437;
    private int level = 1;
    private final AtomicBoolean fanfareIsPlaying = new AtomicBoolean(false);

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

        if (biteHimself) {
            drawEndGameMessage(g);
            gameOver = true;
        }

        if (pause && level < MAX_LEVEL) {
            drawPauseMessage(g);
        }
        if (level == MAX_LEVEL) {
            drawEndMessage(g);
            playTune();
        }

        g.dispose();
    }

    private void incrementCounters() {
        if (snake.size() == playingFieldDimensions.width * playingFieldDimensions.height) {
            delay = (int) (delay / 1.5);
            level++;
            moves = 0;
            scorePerLevel = 0;
            direction = Direction.RIGHT;
            lastDirectionExecuted = Direction.RIGHT;
            initializeSnake();
            pause = true;
        }
        scorePerLevel++;
        scorePerGame++;
        if (scorePerGame >= maxScore) {
            maxScore = scorePerGame;
        }
        if (snake.size() < gameField.height * gameField.width) {
            targetPosition = getNewTargetPosition();
        }
    }

    private boolean targetReached() {
        return (targetPosition.x == snake.getLast().x) && (targetPosition.y == snake.getLast().y);
    }

    private void drawMessage(Graphics g, String[] messages, int[] offsets) {
        if (messages.length != offsets.length) {
            throw new IllegalArgumentException("Messages and offsets arrays must have the same length.");
        }

        for (int i = 0; i < messages.length; i++) {
            Font font = i == 0 ? new Font("arial", Font.BOLD, 50) : new Font("arial", Font.BOLD, 20);
            g.setFont(font);
            drawCenteredString(g, messages[i], gameField.width, gameField.height, font, offsets[i]);
        }
    }

    private void drawCenteredString(Graphics g, String text, int rectangleWidth, int rectangleHeight, Font font, int yShift) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textPositionX = GRID_SIZE + (rectangleWidth - metrics.stringWidth(text)) / 2;
        int textPositionY = height / 3 + ((rectangleHeight - metrics.getHeight()) / 2) + metrics.getAscent() + yShift;

        g.setFont(font);
        g.setColor(Color.BLACK);
        int outlineSize = 2;
        for (int dx = -outlineSize; dx <= outlineSize; dx++) {
            for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, textPositionX + dx, textPositionY + dy);
                }
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(text, textPositionX, textPositionY);
    }

    private void drawPauseMessage(Graphics g) {
        drawMessage(g,
                new String[]{"PAUSE", "Press SPACE to unpause"},
                new int[]{-45, 5}
        );
    }

    private void drawEndMessage(Graphics g) {
        drawMessage(g,
                new String[]{"YOU BEAT THE GAME", "YOUR SCORE IS " + scorePerGame, "Press ENTER to try again"},
                new int[]{-45, 5, 25}
        );
    }

    private void drawEndGameMessage(Graphics g) {
        direction = null;
        drawMessage(g,
                new String[]{"GAME OVER", "Press ENTER to RESTART"},
                new int[]{-45, 5}
        );
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
        if (!fanfareIsPlaying.compareAndSet(false, true)) {
            return;
        }

        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(PATH + "fanfare.wav"));

                clip.open(inputStream);
                clip.start();
                do {
                    Thread.sleep(100);
                } while (clip.isRunning());

                clip.close();
                inputStream.close();
            } catch (Exception e) {
                logger.error("Error playing tune: {}", e.getMessage());
            } finally {
                fanfareIsPlaying.set(false);
            }
        }).start();
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

        if (!pause && !gameOver) {
            moveSnake();
        }
        repaint();
    }

    private void moveSnake() {
        Point nextStep = getNextStep(snake.getLast());

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
        moves++;
    }

    private Point getNextStep(Point head) {
        int nextX = head.x;
        int nextY = head.y;

        switch (direction) {
            case RIGHT -> nextX = (head.x + 1) % (gameField.width / GRID_SIZE);
            case LEFT -> nextX = (head.x - 1 + gameField.width / GRID_SIZE) % (gameField.width / GRID_SIZE);
            case DOWN -> nextY = (head.y + 1) % playingFieldDimensions.height;
            case UP -> nextY = (head.y - 1 + playingFieldDimensions.height) % playingFieldDimensions.height;
        }
        lastDirectionExecuted = direction;
        return new Point(nextX, nextY);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_SPACE) {
            togglePause();
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER && (gameOver || level == MAX_LEVEL)) {
            restartGame();
        } else {
            Direction newDirection = KEY_DIRECTION_MAP.get(e.getKeyCode());
            if (newDirection != null && lastDirectionExecuted != opposite(newDirection) && !pause) {
                lastDirectionExecuted = direction;
                direction = newDirection;
                logger.debug("Direction changed to: {}", direction);
            }
        }
    }

    private void restartGame() {
        initializeSnake();
        moves = 0;
        scorePerLevel = 0;
        scorePerGame = 0;
        delay = 437;
        level = 1;
        direction = Direction.RIGHT;
        lastDirectionExecuted = Direction.RIGHT;
        gameOver = false;
        biteHimself = false;
        pause = true;
    }

    private void togglePause() {
        if (pause) {
            direction = lastDirectionExecuted;
            pause = false;
        } else {
            lastDirectionExecuted = direction;
            pause = true;
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
