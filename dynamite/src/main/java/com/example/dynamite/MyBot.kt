package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import kotlin.math.absoluteValue
import kotlin.random.Random

class MyBot : Bot {
    private var gamestate: Gamestate = Gamestate()

    private var myDynamites = 100
    private var theirDynamites = 100

    private var moves: MutableMap<Pair<List<Pair<Move, Move>>, Move>, Double> = HashMap<Pair<List<Pair<Move, Move>>, Move>, Double>()

    private fun randomDecide(nom: Int, den: Int): Boolean {
        if (nom >= den)
            return true

        var list = mutableListOf<Int>()
        for (i in 0..nom)
            list.add(0)
        for (i in 0..(den - nom))
            list.add(1)

        return (list.shuffled().first() == 0)
    }

    private fun randomStandardMove(): Move {
        when(listOf<Int>(0, 1, 2).shuffled().first()) {
            0 -> return Move.R
            1 -> return Move.S
            2 -> return Move.P
        }
        return Move.R
    }

    private fun randomMove(): Move {
        if (myDynamites < 1)
            return randomStandardMove()

        val rounds = gamestate.rounds.size - 1
        if (gamestate.rounds.size >= 3)
            if (gamestate.rounds[rounds].p1 == Move.D && gamestate.rounds[rounds - 1].p1 == Move.D && gamestate.rounds[rounds - 2].p1 == Move.D)
                if (randomDecide(1, 2))
                    return randomStandardMove()

        if (gamestate.rounds.size >= 1800)
            if (randomDecide(1, 3))
                return Move.D

        if (randomDecide(myDynamites, 90))
            return Move.D
        return randomStandardMove()
    }



    private fun lastKmoves(k: Int, from: Int): List<Pair<Move, Move>> {
        if (gamestate.rounds.size - 1 < from || from < 0 || k > from + 1)
            return listOf()

        var result = mutableListOf<Pair<Move, Move>>()
        for (i in 0 until k)
            result.add(Pair(gamestate.rounds[from - i].p1, gamestate.rounds[from - i].p2))
        return result
    }



    private fun chooseMove(memory: Int): Move {
        val round = gamestate.rounds.size - 1

        if (memory < 1)
            return randomMove()

        var countCases: MutableMap<Move, Double> = HashMap<Move, Double>()
        var total = 0.0
        for (move in Move.values()) {
            countCases.put(move, moves.getOrDefault(Pair(lastKmoves(memory, round), move), 0.0) + 1)
            total += countCases.getOrDefault(move, 1.0)
        }
        if (theirDynamites <= 5) {
            total -= countCases.getOrDefault(Move.D, 1.0)
            countCases.put(Move.D, 0.0)
        }
        if (total.absoluteValue < 0.3)
            total = 1.0


        //history analysis
        var chances: MutableMap<Move, Double> = HashMap<Move, Double>()
        var mostLikelyMove = Move.R
        var mostLikelyMoveProb: Double = 0.0
        for (move in Move.values()) {
            chances.put(move, countCases.getOrDefault(move, 0.0) / total)
            if (chances.getOrDefault(move, 0.0) > mostLikelyMoveProb) {
                mostLikelyMoveProb = chances.getOrDefault(move, 0.0)
                mostLikelyMove = move
            }
        }

        //recursion or random - too little information
        if (total < 10)
            chooseMove(memory - 1)

        var randomNominator = 1.0
        var randomDenominator = 10.0

        if (total < 20.1)
            randomNominator *= 2

        randomDenominator *= mostLikelyMoveProb
        var list = mutableListOf<Int>()
        for (i in 0..(randomNominator * 10).toInt())
            list.add(0)
        for (i in 0..((randomDenominator - randomNominator) * 10).toInt())
            list.add(1)

        if (list.shuffled().first() == 0)
            return randomMove()

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
    }



    override fun makeMove(gamestate: Gamestate): Move {
        this.gamestate = gamestate
        if (gamestate.rounds.size > 0) {
            if (gamestate.rounds[gamestate.rounds.size - 1].p2 == Move.D)
                theirDynamites --
            if (gamestate.rounds[gamestate.rounds.size - 1].p1 == Move.D)
                myDynamites --
        }

        if (gamestate.rounds.size < 3)
            return randomStandardMove()

        //update memory
        val round = gamestate.rounds.size - 1
        val previousMove = gamestate.rounds[round].p2
        for (k in 1 until 4)
            if (round > k - 1) {
                val key = Pair(lastKmoves(k, round - 1), previousMove)
                moves.put(key, moves.getOrDefault(key, 0.0) + 1)
        }

        if (gamestate.rounds.size < 10) {
            if (randomDecide(1, 3))
                return randomMove()
            return randomStandardMove()
        }

        //choose move
        return chooseMove(3)
    }

    init {
        myDynamites = 100
        theirDynamites = 100
    }
}