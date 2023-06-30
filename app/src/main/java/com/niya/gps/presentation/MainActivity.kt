package com.niya.gps.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.niya.gps.R
import com.niya.gps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomNav()
    }


    private fun bottomNav() {
        binding.bNView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.id_home -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.homeFragment)
                }
                R.id.id_settings -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.settingsFragment)
                }
                R.id.id_tracks -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.tracksFragment)
                }
            }
            true
        }
    }

}
