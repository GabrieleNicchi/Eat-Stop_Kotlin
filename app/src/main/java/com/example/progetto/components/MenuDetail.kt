package com.example.progetto.components

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.classes.MenuDetail
import com.example.progetto.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Composable
fun MenuDetail(userViewModel: UserViewModel) {

    // State-collected menu details
    val menuDetail by userViewModel.menuDetail.collectAsState()


    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFEEEFF1))
    ){
        menuDetail?.let {
            HeaderMenuDetail(userViewModel)
            Spacer(modifier = Modifier.height(20.dp)) // spacer between them
            MenuDetailScreen(it, userViewModel)
        } ?: run {
            LoadingScreen()
        }
    }
}

/* ----------------------- Screen Logic ----------------------- */

@Composable
fun MenuDetailScreen(menuDetail: MenuDetail, userViewModel: UserViewModel) {

    val coroutineScope = rememberCoroutineScope()
    // Image collected by the state (taken from DB)
    val img64 by userViewModel.imgBase64DB.collectAsState()
    val scrollState = rememberScrollState()

    val boxModifier = Modifier
        .fillMaxWidth()
        .height(600.dp)
        .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
        .background(color = Color.White)
        .padding(8.dp)

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
                // If the resulting image is not empty
                if (img64.isNotEmpty()) {
                    Base64LargeViewer(base64 = img64)
                }
                Text(text = menuDetail.name, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Price: ${menuDetail.price} €", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = menuDetail.shortDescription, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Delivery Time: ${menuDetail.deliveryTime} mins", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = menuDetail.longDescription, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CustomButton(
                    onClick = {
                        coroutineScope.launch {
                            userViewModel.makeOrder(menuDetail.mid)
                        }
                    },
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Order Now!!", color = Color.White)
                }
            }
        }
    }
}

// Override , larger image
@OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)
@Composable
fun Base64LargeViewer(base64: String) {
    val byteArray = Base64.decode(base64)
    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "base64 encoded image",
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(16.dp)
    )
}

