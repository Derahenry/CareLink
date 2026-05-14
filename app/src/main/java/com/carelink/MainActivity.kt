package com.carelink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.carelink.navigation.AppNavGraph
import com.carelink.ui.theme.CareLinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareLinkTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}