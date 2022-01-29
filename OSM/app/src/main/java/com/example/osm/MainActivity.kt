package com.example.osm


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private val online :Fragment = OnlineFragment()
    private val offline : Fragment = OfflineFragment()
    private val settings :Fragment = SettingFragment()
    private val fragmentManager = supportFragmentManager
    private var activeFragment = online
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * Initialize the bottom navigation view
         * create bottom navigation view object
         */
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //val navController = findNavController(R.id.nav_fragment)
        //bottomNavigationView.setupWithNavController(navController)


        fragmentManager.beginTransaction().apply {
            add(R.id.container, online)
            add(R.id.container, offline).hide(offline)
            add(R.id.container, settings).hide(settings)
        }.commit()
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.onlineFragment -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(online).commit()
                    activeFragment = online
                    true
                }
                R.id.offlineFragment -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(offline).commit()
                    activeFragment = offline
                    true
                }
                R.id.settingFragment -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(settings).commit()
                    activeFragment = settings
                    true
                }
                else -> false
            }
        }
    }


}