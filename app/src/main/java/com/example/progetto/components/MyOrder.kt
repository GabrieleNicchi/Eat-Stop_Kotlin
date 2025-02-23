package com.example.progetto.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.classes.MenuDetail
import com.example.progetto.classes.OrderInfo
import com.example.progetto.model.CommunicationController
import com.example.progetto.viewmodel.UserViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MyOrder(userViewModel: UserViewModel) {

    // State-collected order details
    val myOrder by userViewModel.myOrder.collectAsState()
    val oid by userViewModel.oid.collectAsState()
    val hasPermission by userViewModel.hasPermission.collectAsState()
    val context = LocalContext.current

    // Status to track whether permission verification is completed
    var isPermissionChecked by remember { mutableStateOf(false) }

    // Recall the order placed on component mounted
    LaunchedEffect(Unit) {
        Log.d("MyOrder", "Mounted component")
        userViewModel.getMyOrder()
    }

    // Update the component every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // 5 seconds
            Log.d("MyOrder", "5 seconds have passed")
            userViewModel.getMyOrder()
        }
    }

    // Check if the user has already provided permissions
    LaunchedEffect(Unit) {
        userViewModel.checkPermission(context)
        isPermissionChecked = true
    }

    // Shows an initial loading screen
    // until the permission verification is completed
    if (!isPermissionChecked) {
        LoadingScreen()
    } else {
        // If the user has not provided permissions, show AlertPermission screen
        if (!hasPermission) {
            AlertPermission(userViewModel)
        } else {
            // If the user has never placed any order, the asyncStorage of key 'oid' will be equal to -1
            if (myOrder != null && oid != -1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0xFFEEEFF1))
                ) {
                    Header(userViewModel)
                    myOrder?.let { MyMap(userViewModel, it) }
                    myOrder?.let { MyOrderScreen(userViewModel, it) }
                }
            } else if (oid == -1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Header(userViewModel)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "No Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            } else {
                // Shows a loading status or waiting screen
                LoadingScreen()
            }
        }
    }
}

@Composable
fun MyOrderScreen(userViewModel: UserViewModel, myOrderInfo: OrderInfo) {

    var menu by remember { mutableStateOf<MenuDetail?>(null) }

    // To display name of menu
    LaunchedEffect(Unit) {
        try {
            menu = userViewModel.menuInfoOrder(myOrderInfo.mid)
        } catch (e: Exception) {
            Log.d("MyOrder", "Error recovering menu: ${e.message}")
        }
    }

    val boxModifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
        .background(color = Color.White)
        .padding(8.dp)
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.padding(16.dp) // external padding
    ) {
        Box(
            modifier = boxModifier
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState) // Enable vertical scrolling
            ) {
                menu?.let { Text(text = it.name, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF96167)) } ?: run {
                    // Show loading text if menu is null
                    Text(text = "Loading...", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
                Text(text = "Time the order was placed: ${formatTimestamp(myOrderInfo.creationTimeStamp)}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // If the order has not yet been completed
                if (myOrderInfo.deliveryTimeStamp == null) {
                    Text(text = "Estimated delivery time: ${myOrderInfo.expectedDeliveryTimeStamp?.let {
                        formatTimestamp(
                            it
                        )
                    }}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Text(text = "Delivered at: ${formatTimestamp(myOrderInfo.deliveryTimeStamp)}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text = myOrderInfo.status, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MyMap(userViewModel: UserViewModel , myOrderInfo: OrderInfo) {

    val location by userViewModel.location.collectAsState()
    val context = LocalContext.current

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(location?.let { Point.fromLngLat(it.latitude, it.longitude) })
            zoom(15.5)
        }
    }
    Box(
        Modifier.padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
                .height(400.dp)
        ) {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = mapViewportState
            ) {
                // Need to acquire location permissions first
                MapEffect(Unit) { mapView ->
                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.HEADING
                        enabled = true
                    }
                    mapViewportState.transitionToFollowPuckState()
                }
                val marker1 = rememberIconImage(key = R.drawable.position_marker1, painter = painterResource(R.drawable.position_marker1))
                PointAnnotation(
                    point = Point.fromLngLat(myOrderInfo.deliveryLocation.lng, myOrderInfo.deliveryLocation.lat),
                    onClick = {
                        Toast.makeText(context, "Delivery location", Toast.LENGTH_SHORT).show()
                        true
                    }
                ) {
                    iconImage = marker1
                }

                val marker2 = rememberIconImage(key = R.drawable.position_marker2, painter = painterResource(R.drawable.position_marker2))
                PointAnnotation(
                    point = Point.fromLngLat(myOrderInfo.currentPosition.lng, myOrderInfo.currentPosition.lat),
                    onClick = {
                        Toast.makeText(context, "Current order position", Toast.LENGTH_SHORT).show()
                        true
                    }
                ) {
                    iconImage = marker2
                    iconSize = 0.4
                }

                // Aggiungi una linea tra i due marker
                PolylineAnnotation(
                    points = listOf(
                        Point.fromLngLat(myOrderInfo.deliveryLocation.lng, myOrderInfo.deliveryLocation.lat),
                        Point.fromLngLat(myOrderInfo.currentPosition.lng, myOrderInfo.currentPosition.lat)
                    )
                ) {
                    lineColor = Color.Blue
                    lineWidth = 3.0
                }
            }
        }
        // Add zoom buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    mapViewportState.easeTo(
                        cameraOptions {
                            zoom(mapViewportState.cameraState!!.zoom + 1)
                        }
                    )
                },
                containerColor = Color(0xFFF96167)
            ) {
                Text("+" , color = Color.White)
            }

            FloatingActionButton(
                onClick = {
                    mapViewportState.easeTo(
                        cameraOptions {
                            zoom(mapViewportState.cameraState!!.zoom - 1)
                        }
                    )
                },
                containerColor = Color(0xFFF96167)
            ) {
                Text("-" , color = Color.White)
            }
        }
    }
}

// Order status
@Composable
fun AlertOrder(userViewModel: UserViewModel) {
    // State-collected order details
    val orderError by userViewModel.orderError.collectAsState()

    when (orderError) {
        // The order was placed successfully -> MyOrder()
        null -> {
            AlertDialog(
                onDismissRequest = { userViewModel.setCurrentScreen("MyOrder") },
                title = {
                    Text(text = "Order placed!")
                },
                text = {
                    Text("You can find all the information in the \"MyOrder\" section")
                },
                confirmButton = {
                    CustomButton(
                        onClick = {
                            userViewModel.setCurrentScreen("MyOrder")
                        }
                    ) {
                        Text("Ok" , color = Color.White)
                    }
                }
            )
        }
        // The user is not registered -> Profile()
        "notRegistered" -> {
            AlertDialog(
                onDismissRequest = { userViewModel.setCurrentScreen("Profile") },
                title = {
                    Text(text = "User not registered")
                },
                text = {
                    Text("Please, register before placing a new order")
                },
                confirmButton = {
                    CustomButton(
                        onClick = {
                            userViewModel.setCurrentScreen("Profile")
                        }
                    ) {
                        Text("Ok" , color = Color.White)
                    }
                }
            )
        }
        // The credit card is invalid -> Profile()
        "403" -> {
            AlertDialog(
                onDismissRequest = { userViewModel.setCurrentScreen("Profile") },
                title = {
                    Text(text = "Invalid card format")
                },
                text = {
                    Text("Please, enter a valid credit card (starts with 1)")
                },
                confirmButton = {
                    CustomButton(
                        onClick = {
                            userViewModel.setCurrentScreen("Profile")
                        }
                    ) {
                        Text("Ok" , color = Color.White)
                    }
                }
            )
        }
        // User has already an active order -> MenuList()
        "409" -> {
            AlertDialog(
                onDismissRequest = { userViewModel.setCurrentScreen("MenuList") },
                title = {
                    Text(text = "You already have an active order")
                },
                text = {
                    Text("Please, wait for your order to arrive before placing another")
                },
                confirmButton = {
                    CustomButton(
                        onClick = {
                            userViewModel.setCurrentScreen("MenuList")
                        }
                    ) {
                        Text("Ok" , color = Color.White)
                    }
                }
            )
        }
        // Unexpected error -> Home()
        else -> {
            AlertDialog(
                onDismissRequest = { userViewModel.setCurrentScreen("Home") },
                title = {
                    Text(text = "An unexpected error occurred")
                },
                text = {
                    Text("Please try again later.")
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
    }
}

fun formatTimestamp(timestamp: String): String {
    val zonedDateTime = ZonedDateTime.parse(timestamp)
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")
    return zonedDateTime.format(formatter)
}