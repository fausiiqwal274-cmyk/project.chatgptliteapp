package com.example.chatgptliteapp.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatgptliteapp.R
import com.example.chatgptliteapp.auth.LoginActivity
import com.example.chatgptliteapp.fragment.ChatFragment
import com.example.chatgptliteapp.fragment.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 CEK LOGIN
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // 🔥 CHAT JADI DEFAULT (INI KUNCINYA)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, ChatFragment())
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ChatFragment())
                        .commit()
                    true
                }
                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
