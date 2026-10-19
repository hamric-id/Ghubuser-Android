package com.hamric.ghubuser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hamric.core.network.BuildConfig
import com.hamric.feature.detail_user.DetailUserScreen
import com.hamric.feature.users.UsersScreen
import com.hamric.ghubuser.ui.theme.GhubuserTheme

class MainActivity : ComponentActivity() {
    private val notifPermissionLauncer = //for chucker purpose on android 13++
        registerForActivityResult(ActivityResultContracts.RequestPermission()){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhubuserTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "users") {
                    composable("users") { UsersScreen(navController) }
                    composable("detail/{login}") { backStackEntry ->
                        val login = backStackEntry.arguments?.getString("login").orEmpty()
                        DetailUserScreen(login)
                    }
                }
            }
        }

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) notifPermissionLauncer.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}