package com.example.progetto.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.R
import com.example.progetto.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ErrorScreen(userViewModel: UserViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFEEEFF1)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.error),
                contentDescription = "Error",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something goes wrong",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF96167)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                onClick = {
                    userViewModel.setCurrentScreen("Home")
                },
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Go back to homepage", color = Color.White)
            }
        }
    }
}