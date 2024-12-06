package com.example.arcrun

import android.content.Context
import android.content.Intent
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigation(private val context: Context) {

    fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.findViewById<View>(R.id.nav1).setOnClickListener {
            val intent = Intent(context, WelcomeActivity::class.java)
            context.startActivity(intent)
        }

    }
}