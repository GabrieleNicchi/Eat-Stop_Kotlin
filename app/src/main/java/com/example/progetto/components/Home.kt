package com.example.progetto.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.viewmodel.UserViewModel
import kotlinx.coroutines.launch


@Composable
fun Home(userViewModel: UserViewModel) {

    HomeScreen(userViewModel)
}

@Composable
fun HomeScreen(userViewModel: UserViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Composable function to manage location permissions -> MenuList()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        userViewModel.updatePermissionStatus(isGranted)
        if (isGranted) {
            // On update -> calculate position
            // show MenuList() screen
            userViewModel.getCurrentLocation(context)
            userViewModel.setCurrentScreen("MenuList")
        } else {
            userViewModel.setCurrentScreen("AlertPermission")
        }
    }

    // Composable function to manage location permissions -> MyOrder()
    val permissionLauncherOrder = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        userViewModel.updatePermissionStatus(isGranted)
        if (isGranted) {
            // On update -> calculate position
            // show MyOrder() screen
            userViewModel.getCurrentLocation(context)
            userViewModel.setCurrentScreen("MyOrder")
        } else {
            userViewModel.setCurrentScreen("AlertPermission")
        }
    }

    // Check permissions to update the situation (the user has provided them)
    LaunchedEffect(Unit) {
        userViewModel.checkPermission(context)
    }

    // Launcher that listens if the position changes and calculates it
    LaunchedEffect(Unit) {
        if(userViewModel.hasPermission.value){
            userViewModel.getCurrentLocation(context)
        }
    }

    fun handlePositionEventMenuList () {
        // If the user has not provided permissions, ask for them
        if (!userViewModel.hasPermission.value) {
            Log.d("MainActivity", "Request permits")
            userViewModel.requestPermission(permissionLauncher)

        } else {
            // If the user provided them, calculate his position
            // set the current screen to "MenuList"
            Log.d("Home" , "MenuList")
            userViewModel.getCurrentLocation(context)
            userViewModel.setCurrentScreen("MenuList")
        }
    }

    fun handlePositionEventMyOrder () {
        // If the user has not provided permissions, ask for them
        if (!userViewModel.hasPermission.value) {
            Log.d("MainActivity", "Request permits")
            userViewModel.requestPermission(permissionLauncherOrder)

        } else {
            // If the user provided them, calculate his position
            // set the current screen to "MyOrder"
            Log.d("Home" , "MyOrder")
            userViewModel.getCurrentLocation(context)
            userViewModel.setCurrentScreen("MyOrder")
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFEEEFF1)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = Color(0xFFF96167),
                        shape = RoundedCornerShape(200.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eatandstop),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // -> Profile screen
                    CustomButtonWithImage(
                        text = "Profile",
                        imageResId = R.drawable.profile,
                        onClick = {
                            coroutineScope.launch {
                                userViewModel.fetchUserInfo()
                            }
                        }
                    )
                    // -> MenuList screen
                    CustomButtonWithImage(
                        text = "Menu list",
                        imageResId = R.drawable.tacos,
                        onClick = {
                            coroutineScope.launch {
                                handlePositionEventMenuList()
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // -> Your orders screen
                    CustomButtonWithImage(
                        text = "MyOrders",
                        imageResId = R.drawable.pony_location,
                        onClick = {
                            coroutineScope.launch {
                                handlePositionEventMyOrder()
                            }
                        }
                    )
                    /*CustomButtonWithImage(
                        text = "...soon...",
                        imageResId = R.drawable.jalapenos,
                        onClick = {
                            userViewModel.setCurrentScreen("Error")
                        }
                    )*/
                }
            }
        }
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF96167),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun CustomButtonWithImage(text: String, imageResId: Int, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .height(120.dp)
                .width(185.dp)
                .background(
                    color = Color(0xFFF96167),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .height(85.dp)
                        .padding(1.dp)
                )
                Text(text = text, color = Color.White)
            }
        }
    }

@Composable
fun AlertPermission(userViewModel: UserViewModel) {
    Log.d("Home" , "Alert mounted")
    AlertDialog(
        onDismissRequest = { userViewModel.setCurrentScreen("Home") },
        title = {
            Text(text = "No location permissions")
        },
        text = {
            Text("Please , accept location permissions to access the app's features")
        },
        confirmButton = {
            CustomButton(
                onClick = {
                    userViewModel.setCurrentScreen("Home")
                }
            ) {
                Text("Ok" , color = Color.White)
            }
        }
    )
}