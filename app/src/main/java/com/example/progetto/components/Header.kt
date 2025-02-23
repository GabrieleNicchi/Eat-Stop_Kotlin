package com.example.progetto.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.example.progetto.R
import com.example.progetto.viewmodel.UserViewModel


@Composable
fun Header(userViewModel: UserViewModel) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // back button
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color(0xFFF96167),
                    shape = CircleShape
                )
                .clickable { userViewModel.setCurrentScreen("Home") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = Color(0xFFEEEFF1),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // "home" button
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color(0xFFF96167),
                    shape = CircleShape
                )
                .clickable { userViewModel.setCurrentScreen("Home") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.house),
                contentDescription = "Home",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HeaderMenuDetail(userViewModel: UserViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // back button
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color(0xFFF96167),
                    shape = CircleShape
                )
                .clickable { userViewModel.setCurrentScreen("MenuList") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = Color(0xFFEEEFF1),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // "home" button
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color(0xFFF96167),
                    shape = CircleShape
                )
                .clickable { userViewModel.setCurrentScreen("Home") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.house),
                contentDescription = "Home",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
