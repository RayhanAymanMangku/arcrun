package com.example.arcrun

import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigation(private val context: Context) {

    fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.findViewById<LinearLayout>(R.id.nav1).setOnClickListener {
            val intent = Intent(context, WelcomeActivity::class.java)
            context.startActivity(intent)
        }
        bottomNavigationView.findViewById<LinearLayout>(R.id.nav2).setOnClickListener {
            val intent = Intent(context, TicketActivity::class.java)
            context.startActivity(intent)
        }
        bottomNavigationView.findViewById<LinearLayout>(R.id.nav3).setOnClickListener {
            val intent = Intent(context, HistoryActivity::class.java)
            context.startActivity(intent)
        }
        bottomNavigationView.findViewById<LinearLayout>(R.id.nav4).setOnClickListener {
            val intent = Intent(context, AchievementProgram::class.java)
            context.startActivity(intent)
        }

    }
}