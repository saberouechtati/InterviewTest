package com.betsson.interviewtest

open class Bet(var type: String, var sellIn: Int, var odds: Int, var image: String) {
    override fun toString(): String {
        return this.type + ", " + this.sellIn + ", " + this.odds
    }
}