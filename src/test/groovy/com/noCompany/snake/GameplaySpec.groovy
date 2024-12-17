package com.noCompany.snake

import spock.lang.Specification

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent

class GameplaySpec extends Specification {

    Gameplay gameplay

    def setup() {
        def gridDimension = new Dimension(9, 9)
        def window = new Rectangle( 500, 300)
        def gameField = new Rectangle( 300, 10)
        gameplay = new Gameplay(gridDimension, 10, window, gameField)
        gameplay.delay = 1
    }

    def "should initialize snake with default properties"() {
        given:
            def gameplay = gameplay

        expect:
            gameplay.snake.size() == 3
            gameplay.direction == Direction.RIGHT
            gameplay.moves == 0
            gameplay.scorePerLevel == 0
            gameplay.level == 1
    }

    def "should change direction on valid key press"() {
        given:
            def gameplay = gameplay
            gameplay.pause = false

        when:
            def event = event(KeyEvent.VK_W)
            gameplay.keyPressed(event)
            gameplay.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed"))

        then:
            gameplay.direction == Direction.UP
            gameplay.moves == 1
    }

    def "should not allow opposite direction change"() {
        given:
            gameplay.direction = Direction.RIGHT
            gameplay.pause = false
            gameplay.delay = 10

        when:
            def firstEvent = event(KeyEvent.VK_UP)
            gameplay.keyPressed(firstEvent)
            gameplay.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed"))

            sleep(1)
            def secondEvent = event(KeyEvent.VK_LEFT)
            gameplay.keyPressed(secondEvent)
            gameplay.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed"))


        then:
            gameplay.direction == Direction.UP
            gameplay.moves == 1
    }

    def "should allow opposite direction change when intermediate steps are performed slower than the clock step"() {
        given:
            gameplay.direction = Direction.RIGHT
            gameplay.pause = false
            gameplay.delay = 1

        when:
            def firstEvent = event(KeyEvent.VK_UP)
            gameplay.keyPressed(firstEvent)
            gameplay.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed"))

            sleep(10)
            def secondEvent = event(KeyEvent.VK_LEFT)
            gameplay.keyPressed(secondEvent)
            gameplay.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed"))


        then:
            gameplay.direction == Direction.LEFT
            gameplay.moves == 2
    }

    def "should pause and resume game on key press"() {
        given:
            gameplay.pause = false

        when:
            def startEvent = event(KeyEvent.VK_SPACE)
            gameplay.keyPressed(startEvent)

        then:
            gameplay.pause == true

        when:
            def pauseEvent = event(KeyEvent.VK_P)
            gameplay.keyPressed(pauseEvent)

        then:
            gameplay.pause == false
    }

    def "should restart game on ENTER key press"() {
        given:
            gameplay.moves = 10
            gameplay.scorePerLevel = 5
            gameplay.direction = Direction.DOWN
            gameplay.gameOver = true

        when:
            def event = event(KeyEvent.VK_ENTER)
            gameplay.keyPressed(event)

        then:
            gameplay.moves == 0
            gameplay.scorePerLevel == 0
            gameplay.scorePerGame == 0
            gameplay.level == 1
            gameplay.direction == Direction.RIGHT
    }

    def "should generate random enemy position not overlapping snake"() {
        when:
            def enemyPosition = gameplay.getNewTargetPosition()

        then:
            !gameplay.snake.find { it == enemyPosition }
    }

    static def event(int keyEvent) {
        return new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyEvent, KeyEvent.getKeyText(keyEvent) as char)
    }
}
