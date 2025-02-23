package com.example.progetto.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.classes.MenuList
import com.example.progetto.classes.UserInfoPut
import com.example.progetto.model.CommunicationController
//import com.example.progetto.viewmodel.LocationViewModel
import com.example.progetto.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun MenuList(userViewModel: UserViewModel) {

    Log.d("MenuList", "I am in the MenuList")
    val isUpdateDB by userViewModel.isUpdateDB.collectAsState()

    val location by userViewModel.location.collectAsState()
    val menuList by userViewModel.menuList.collectAsState()

    // As soon as I get the position, if it is nothing,
    // download the menus for the area into the database
    // and save the list in the stateflow variable
    LaunchedEffect(location) {
        Log.d("MenuList", "Mounted component")
            userViewModel.getMenuList()
    }

    // If the DB was updated and got all the menus in the variable
    // Iterate over the individual menus
    if (isUpdateDB && menuList.isNotEmpty()) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFEEEFF1))
        ){
            Header(userViewModel)
            Spacer(modifier = Modifier.height(40.dp)) // spacer between them
            LazyColumn {
                items(menuList) { menuItem ->
                    MenuItemView(
                        menuItem,
                        userViewModel
                    )
                }
            }
        }
    } else {
        // Shows a loading status or waiting screen
        LoadingScreen()
    }
}

/* ----------------------- Individual menu ----------------------- */
@Composable
fun MenuItemView(menuItem: MenuList, userViewModel: UserViewModel) {

    val coroutineScope = rememberCoroutineScope()
    var img64 by remember { mutableStateOf("") }

    // To display the menu image
    LaunchedEffect(Unit) {
        try {
            img64 = userViewModel.handleImageMenu(menuItem.mid, menuItem.imageVersion)
        } catch (e: Exception) {
            Log.d("MenuList", "Error recovering image: ${e.message}")
        }
    }

    val boxModifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
        .background(color = Color.White)
        .padding(8.dp)

    Box(
        modifier = Modifier.padding(16.dp) //external padding
    ) {
        Box(
            modifier = boxModifier
        ) {
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
                // If the resulting image is not empty
                if (img64.isNotEmpty()) {
                    Base64Viewer(base64 = img64)
                }
                menuItem.name?.let { Text(text = it, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Price: ${menuItem.price} €", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                menuItem.shortDescription?.let { Text(text = it, fontSize = 12.sp) }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Delivery Time: ${menuItem.deliveryTime} mins", fontSize = 14.sp)
                CustomButton(
                    onClick = {
                        // -> handleMenuDetail [ViewModel]
                        coroutineScope.launch {
                            // Scroll to the middle of the menu interacted with
                            userViewModel.handleMenuDetail(menuItem.mid)
                        }
                    },
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF96167),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(text = "View Info" , color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)
@Composable
fun Base64Viewer(base64: String) {
    val byteArray = Base64.decode(base64)
    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "base64 encoded image",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    )
}