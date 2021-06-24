package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import kotlin.random.Random

class MyBot : Bot {
    private var myDynamites = 100
    private var theirDynamites = 100

    private var moves: MutableMap<Triple<Pair<Move, Move>, Pair<Move, Move>, Move>, Int> = HashMap<Triple<Pair<Move, Move>, Pair<Move, Move>, Move>, Int>()

    private fun randomStandardMove(): Move {
        when(Random.nextInt(3)) {
            0 -> return Move.R
            1 -> return Move.S
            2 -> return Move.P
        }
        return Move.R
    }

    private fun randomMove(): Move {
//        println("randomMove")
        if (Random.nextInt(70) < myDynamites)
            return Move.D
        return randomStandardMove()
    }

    override fun makeMove(gamestate: Gamestate): Move {
        if (gamestate.rounds.size < 3) {
            return Move.W
        }

        //update memory
        val last = gamestate.rounds.size - 1
        val previousMove = Triple(
            Pair(gamestate.rounds[last - 2].p1, gamestate.rounds[last - 2].p2),
            Pair(gamestate.rounds[last - 1].p1, gamestate.rounds[last - 1].p2),
            gamestate.rounds[last].p2
        )
        moves.put(previousMove, moves.getOrDefault(previousMove, 0) + 1)

        if (gamestate.rounds.size > 0) {
            if (gamestate.rounds[gamestate.rounds.size - 1].p2 == Move.D)
                theirDynamites --
            if (gamestate.rounds[gamestate.rounds.size - 1].p1 == Move.D)
                myDynamites --
        }

        //count cases and probability
        val last1Move = Pair(gamestate.rounds[last - 1].p1, gamestate.rounds[last - 1].p2)
        val lastMove = Pair(gamestate.rounds[last].p1, gamestate.rounds[last].p2)
        var countCases: MutableMap<Move, Int> = HashMap<Move, Int>()
        var total = 0
        for (move in Move.values()) {
            countCases.put(move, moves.getOrDefault(Triple(last1Move, lastMove, move), 0) + 1)
            total += countCases.getOrDefault(move, 0)
        }
        if (theirDynamites <= 0) {
            total -= countCases.getOrDefault(Move.D, 0)
            countCases.put(Move.D, 0)
        }

        //decide on move

        //history analysis
        var chances: MutableMap<Move, Int> = HashMap<Move, Int>()
        var mostLikelyMove = Move.W
        var mostLikelyMoveProb = 0
        for (move in Move.values()) {
            chances.put(move, countCases.getOrDefault(move, 0) + 1)
            if (chances.getOrDefault(move, 0) > mostLikelyMoveProb) {
                mostLikelyMoveProb = chances.getOrDefault(move, 0)
                mostLikelyMove = move
            }
        }


        //random
        var randomNominator = 1
        var randomDenominator = 10

        if (total < 10)
            randomNominator *= 2

        randomDenominator *= mostLikelyMoveProb

        if (Random.nextInt(randomDenominator) < randomNominator) {
            return randomMove()
        }

        //history move
        when(mostLikelyMove) {
            Move.D ->
                return Move.W
            Move.W ->
                return randomStandardMove()
            Move.S ->
                return Move.R
            Move.R ->
                return Move.P
            Move.P ->
                return Move.S
        }

//        if (myDynamites > 0) {
//            myDynamites --
//            return Move.D
//        }
//
//        val random = Random.nextInt(0, 3)
//        when(random) {
//            0 -> return Move.R
//            1 -> return Move.P
//            2 -> return Move.S
//        }
//        return Move.W
    }

    init {
        println("Started new match")
    }
}