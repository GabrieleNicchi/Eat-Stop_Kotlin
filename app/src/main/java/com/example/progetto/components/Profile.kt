package com.example.progetto.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progetto.classes.UserInfo
import com.example.progetto.classes.UserInfoPut
import com.example.progetto.viewmodel.UserViewModel
import kotlinx.coroutines.launch

// Component to show the profile screen
@Composable
fun Profile(userViewModel: UserViewModel) {

    val userInfo by userViewModel.userInfo.collectAsState()
    var isRegistered by remember { mutableStateOf(false) }

    // Check whether the user is registered or not when the component launches
    LaunchedEffect(Unit) {
        isRegistered = userViewModel.isRegistered()
    }

    // Background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFEEEFF1))
    ) {

        // Profile
        if (isRegistered) {
            if(userInfo != null){
                // Header
                Header(userViewModel)
                Spacer(modifier = Modifier.height(40.dp)) // spacer between them
                RegisteredProfile(userInfo, userViewModel) {
                    isRegistered = false // Update state to re-render the screen
                }
            } else {
                LoadingScreen()
            }
        } else {
            Header(userViewModel)
            Spacer(modifier = Modifier.height(40.dp)) // spacer between them
            NotRegisteredProfile(userViewModel) {
                isRegistered = true // Update state to re-render the screen
            }
        }
    }
}

@Composable
fun RegisteredProfile(userInfo: UserInfo, userViewModel: UserViewModel, onChangeInfo: () -> Unit) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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

    // If the user accesses the order from the profile,
    // permission for the position must still be requested
    fun handlePositionEventMyOrder () {
        // If the user has not provided permissions, ask for them
        if (!userViewModel.hasPermission.value) {
            Log.d("MainActivity", "Request permits")
            userViewModel.requestPermission(permissionLauncherOrder)

        } else {
            // If the user provided them, calculate his position
            // set the current screen to "MyOrder"
            userViewModel.getCurrentLocation(context)
            userViewModel.setCurrentScreen("MyOrder")
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Your information:",
            color = Color(0xFFF96167),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        val boxModifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .padding(8.dp)

        // Function to create rows with label and value
        @Composable
        fun InfoRow(label: String, value: String?) {
            Box(
                modifier = boxModifier
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label: ",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = value ?: "N/A",
                        color = Color.Gray,
                        fontSize = 16.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Show user data
        InfoRow("Name", userInfo.firstName)
        InfoRow("Last Name", userInfo.lastName)
        InfoRow("Card Full Name", userInfo.cardFullName)
        InfoRow("Card Number", userInfo.cardNumber)
        InfoRow("Card Expiry", "${userInfo.cardExpireMonth}/${userInfo.cardExpireYear}")
        InfoRow("CVV", userInfo.cardCVV)
        InfoRow("Order Status", userInfo.orderStatus)
        // Change user credentials
        CustomButton(
            onClick = {
                coroutineScope.launch {
                    userViewModel.deRegistered()
                    onChangeInfo() // Call the callback to update the state
                }
            },
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Change your information", color = Color.White)
        }
        // Shows the user's order
        CustomButton(
            onClick = {
                coroutineScope.launch {
                    handlePositionEventMyOrder()
                }
            },
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Check your order", color = Color.White)
        }
    }
}

@Composable
fun NotRegisteredProfile(userViewModel: UserViewModel, onRegister: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val sid by userViewModel.sid.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var cardFullName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpireMonth by remember { mutableStateOf("") }
    var cardExpireYear by remember { mutableStateOf("") }
    var cardCVV by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Enter your information:",
            color = Color(0xFFF96167),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Function to create input fields
        @Composable
        fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = label,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .border(1.dp, color = Color(0xFFF96167), shape = RoundedCornerShape(8.dp))
                        .background(color = Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Set the user's entered values
        InputField("First Name", firstName) { firstName = it }
        InputField("Last Name", lastName) { lastName = it }
        InputField("Card Full Name", cardFullName) { cardFullName = it }
        InputField("Card Number", cardNumber) { cardNumber = it }
        InputField("Card Expiry Month", cardExpireMonth) { cardExpireMonth = it }
        InputField("Card Expiry Year", cardExpireYear) { cardExpireYear = it }
        InputField("Card CVV", cardCVV) { cardCVV = it }

        // By clicking "Submit" create the 'UserInfoPut' object with the values entered by the user
        // Call the suspend function: AppViewModel -> putUserInfo()
        CustomButton(
            onClick = {
                val userInfoPut = UserInfoPut(
                    firstName = firstName,
                    lastName = lastName,
                    cardFullName = cardFullName,
                    cardNumber = cardNumber,
                    cardExpireMonth = cardExpireMonth.toIntOrNull(),
                    cardExpireYear = cardExpireYear.toIntOrNull(),
                    cardCVV = cardCVV,
                    sid = sid
                )
                coroutineScope.launch {
                    userViewModel.putUserInfo(userInfoPut)
                    onRegister() // Call the callback to update the state
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF96167),
                    RoundedCornerShape(8.dp)
                )
        ) {
            Text(text = "Submit", color = Color.White)
        }
    }
}

// Component to show an alert of successful registration
@Composable
fun AlertProfile(userViewModel: UserViewModel) {
    AlertDialog(
        onDismissRequest = { userViewModel.setCurrentScreen("Home")},
        title = {
            Text(text = "Registration successful")
        },
        text = {
            Text("Your registration was completed successfully.")
        },
        confirmButton = {
            // Pressing "Ok" sets the screen to "Home"
            CustomButton(
                onClick = {
                    userViewModel.setCurrentScreen("Home")
                }
            ) {
                Text("Go to home", color = Color.White)
            }
        }
    )
}