package com.example.progetto

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.progetto.components.AlertOrder
import com.example.progetto.components.AlertPermission
import com.example.progetto.components.AlertProfile
import com.example.progetto.components.ErrorScreen
import com.example.progetto.components.Home
import com.example.progetto.components.HomeScreen
import com.example.progetto.components.LoadingScreen
import com.example.progetto.components.MenuDetail
import com.example.progetto.components.MenuList
import com.example.progetto.components.MyOrder
import com.example.progetto.components.Profile
import com.example.progetto.viewmodel.UserRepository
import com.example.progetto.viewmodel.UserViewModel
import com.example.progetto.model.DBController
import com.example.progetto.viewmodel.DBRepository
import com.example.progetto.viewmodel.LocationRepository
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val myDb = DBController(this)
        val userRepository = UserRepository(myDb)
        val dbRepository = DBRepository(myDb)
        val locationRepository = LocationRepository()
        userViewModel = UserViewModel(userRepository, dbRepository, locationRepository)

        setContent {
            MyApp(userViewModel)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onPause")
        lifecycleScope.launch {
            userViewModel.onPauseScreenLogic()
        }
    }

    /*override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        lifecycleScope.launch {
            userViewModel.onResumeScreenLogic()
        }
    }*/
}

@Composable
fun MyApp(userViewModel: UserViewModel) {
    val currentScreen by userViewModel.currentScreen.collectAsState()

    val isInitialized by userViewModel.isInitialized.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("MainActivity" ,"Actitvity Mounted")
        userViewModel.checkFirstLaunch()
    }

    LaunchedEffect(Unit) {
        Log.d("MainActivity","checkOnResumeLaunch")
        userViewModel.checkOnResumeLaunch()
    }

    if (isInitialized) {
        when (currentScreen) {
            "Home" -> Home(userViewModel)
            "AlertPermission" -> AlertPermission(userViewModel)
            "Profile" -> Profile(userViewModel)
            "AlertProfile" -> AlertProfile(userViewModel)
            "MenuList" -> MenuList(userViewModel)
            "MenuDetail" -> MenuDetail(userViewModel)
            "MyOrder" -> MyOrder(userViewModel)
            "AlertOrder" -> AlertOrder(userViewModel)
            else -> {
                ErrorScreen(userViewModel)
            }
        }
    } else {
        LoadingScreen()
    }
}

