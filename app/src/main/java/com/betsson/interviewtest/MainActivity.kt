package com.betsson.interviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    val bets = arrayListOf<Bet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler_view).adapter = ItemAdapter(getItemsFromNetwork())
        findViewById<RecyclerView>(R.id.recycler_view).layoutManager = LinearLayoutManager(this)
    }

    // for the scope of this exercise, we will just return a hardcoded list of items,
    // but imagine they are coming from a network call
    fun getItemsFromNetwork(): ArrayList<Bet> {
        bets.add(Bet("Winning team", 10, 20, "https://i.imgur.com/mx66SBD.jpeg"))
        bets.add(Bet("Total score", 2, 0, "https://i.imgur.com/VnPRqcv.jpeg"))
        bets.add(Bet("Player performance", 5, 7, "https://i.imgur.com/Urpc00H.jpeg"))
        bets.add(Bet("First goal scorer", 0, 80, "https://i.imgur.com/Wy94Tt7.jpeg"))
        bets.add(Bet("Number of fouls", 5, 49, "https://i.imgur.com/NMLpcKj.jpeg"))
        bets.add(Bet("Corner kicks", 3, 6, "https://i.imgur.com/TiJ8y5l.jpeg"))
        return bets
    }

    fun calculateOdds() {
        for (i in bets.indices) {
            if (bets[i].type != "Total score" && bets[i].type != "Number of fouls") {
                if (bets[i].odds > 0) {
                    if (bets[i].type != "First goal scorer") {
                        bets[i].odds = bets[i].odds - 1
                    }
                }
            } else {
                if (bets[i].odds < 50) {
                    bets[i].odds = bets[i].odds + 1

                    if (bets[i].type == "Number of fouls") {
                        if (bets[i].sellIn < 11) {
                            if (bets[i].odds < 50) {
                                bets[i].odds = bets[i].odds + 1
                            }
                        }

                        if (bets[i].sellIn < 6) {
                            if (bets[i].odds < 50) {
                                bets[i].odds = bets[i].odds + 1
                            }
                        }
                    }
                }
            }

            if (bets[i].type != "First goal scorer") {
                bets[i].sellIn = bets[i].sellIn - 1
            }

            if (bets[i].sellIn < 0) {
                if (bets[i].type != "Total score") {
                    if (bets[i].type != "Number of fouls") {
                        if (bets[i].odds > 0) {
                            if (bets[i].type != "First goal scorer") {
                                bets[i].odds = bets[i].odds - 1
                            }
                        }
                    } else {
                        bets[i].odds = bets[i].odds - bets[i].odds
                    }
                } else {
                    if (bets[i].odds < 50) {
                        bets[i].odds = bets[i].odds + 1
                    }
                }
            }
        }
    }
}