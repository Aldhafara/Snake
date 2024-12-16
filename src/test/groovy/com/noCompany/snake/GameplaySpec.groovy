package com.noCompany.snake

import spock.lang.Specification

import java.awt.*
import java.awt.event.KeyEvent

class GameplaySpec extends Specification {

    Gameplay gameplay

    def setup() {
        def gridDimension = new Dimension(9, 9)
        def window = new Rectangle( 500, 300)
        def gameField = new Rectangle( 300, 10)
        gameplay = new Gameplay(gridDimension, 10, window, gameField)
    }

    def "should initialize snake with default properties"() {
        given:
            def gameplay = gameplay

        expect:
            gameplay.length == 3
            gameplay.direction == Direction.RIGHT
            gameplay.moves == 0
            gameplay.scorePerLevel == 0
            gameplay.level == 1
    }

    def "should change direction on valid key press"() {
        given:
            def gameplay = gameplay

        when:
            def event = event(KeyEvent.VK_W)
            gameplay.keyPressed(event)

        then:
            gameplay.direction == Direction.UP
            gameplay.moves == 1
    }

    def "should not allow opposite direction change"() {
        given:
            gameplay.direction = Direction.RIGHT

        when:
            def event = event(KeyEvent.VK_LEFT)
            gameplay.keyPressed(event)

        then:
            gameplay.direction == Direction.RIGHT
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
        given:
            def gameplay = gameplay

        when:
            def enemyPosition = gameplay.getNewTargetPosition()

        then:
            !(0..<gameplay.length).any { gameplay.snake[it].equals(enemyPosition) }
    }

    static def event(int keyEvent) {
        return new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyEvent, KeyEvent.getKeyText(keyEvent) as char)
    }
}
