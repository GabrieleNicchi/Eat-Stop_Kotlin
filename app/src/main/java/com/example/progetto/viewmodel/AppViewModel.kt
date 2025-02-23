package com.example.progetto.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progetto.classes.ImageVersion
import com.example.progetto.classes.ImageVersionInfo
import com.example.progetto.classes.MenuDetail
import com.example.progetto.classes.MenuList
import com.example.progetto.classes.OrderInfo
import com.example.progetto.classes.OrderMenu
import com.example.progetto.classes.UserInfo
import com.example.progetto.classes.UserInfoPut
import com.example.progetto.model.CommunicationController
import com.example.progetto.model.DBController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(private val dbController: DBController){

    /* ------ Registered or unregistered user logic ------ */

    // Get the bound value from the asyncStorage
    suspend fun getProfile(): Boolean {
        return dbController.getProfile()
    }

    // Set the bound value from the asyncStorage
    suspend fun setProfile(profile: Boolean) {
        dbController.setProfile(profile)
    }

    /* ------ Background logic ------ */

    // Get the bound value from the asyncStorage (currentScreen)
    suspend fun getScreen() : String {
        return dbController.getScreen()
    }

    // Set the bound value from the asyncStorage (currentScreen)
    suspend fun setScreen(screen : String) {
        dbController.setScreen(screen)
    }

    // Get the bound value from the asyncStorage (latitude)
    suspend fun getLat() : Double {
        return dbController.getLat()
    }

    // Set the bound value from the asyncStorage (latitude)
    suspend fun setLat(lat : Double) {
        dbController.setLat(lat)
    }

    // Get the bound value from the asyncStorage (longitude)
    suspend fun getLng() : Double {
        return dbController.getLng()
    }

    // Set the bound value from the asyncStorage (longitude)
    suspend fun setLng(lng : Double) {
        dbController.setLng(lng)
    }

    /* ------ Find sid, uid and oid ------ */

    suspend fun getSid(): String {
        return dbController.getSid()
    }

    suspend fun setSid(sid : String) {
        dbController.setSid(sid)
    }

    suspend fun getUid() : Int {
        return dbController.getUid()
    }

    suspend fun setUid(uid : Int) {
        dbController.setUid(uid)
    }

    suspend fun getOid() : Int {
        return dbController.getOid()
    }

    suspend fun setOid(oid : Int) {
        dbController.setOid(oid)
    }

    suspend fun getMid() : Int {
        return dbController.getMid()
    }

    suspend fun setMid(mid : Int) {
        dbController.setMid(mid)
    }

    // Check whether it is the first boot for the user or not
    // if it's the first boot (sid in AsyncStorage set to ""):
    // - make the network call postUser() [CommunicationController]
    // - it returns userResponse object
    // - save sid and uid in the AsyncStorage
    // else:
    // - isn't first boot, do nothing
    suspend fun firstLaunch() {

        val sid = getSid()

        if(sid == "") {
            Log.d("AppViewModel" , "firstLaunch -> first launch")
            val userResponse = CommunicationController.postUser()
            userResponse?.let { setSid(it.sid) }
            userResponse?.let { setUid(it.uid) }
        } else {
            Log.d("AppViewModel" , "firstLaunch -> NOT first launch")
        }
    }


}

class DBRepository(private val dbController: DBController) {

    val dao = dbController.database.imageVersionDao()

    // As soon as open the app or when change location, have to update the DB immediately
    // with the new menu data
    // Insert mid, version and the base64 image string into the DB
    suspend fun insertImageVersionDB(imageVersion: ImageVersion) {
        try {
            Log.d("AppViewModel", "insertImageVersionDB")
            dao.insertImageVersion(imageVersion)
        } catch (e: Exception) {
            Log.d("DBRepository", "Error insertImageVersionDB: ${e.message}")
        }
    }

    // Testing
    suspend fun getAllImageVersionInfo() : List<ImageVersionInfo> {
        return try {
            Log.d("DBRepository", "getAllImageVersionDB")
            dao.getAllImageVersions()
        } catch (e: Exception) {
            Log.d("DBRepository", "Error getAllImageVersionDB: ${e.message}")
            emptyList()
        }
    }

    // Logic to choose the image,
    // if the DB version is the same as that of the network call (from which got the menu list):
    // - take the image from the DB and return it
    // else:
    // - get the image through a network call
    // - create a new 'imageVersion' object that reflects the updated version and the new image
    // - save in the DB and return the image
    suspend fun whichImage(mid : Int, sid : String, version : Int) : String {

        var img = ""
        val local = dao.getMidVersionImage(mid)

        try{
            if(local === version) {

                Log.d("AppViewModel" , "Version and DBVersion match")
                img = dao.getImage64(mid)

            } else {
                Log.d("AppViewModel" , "Version and DBVersionDB doesn't match!")
                img = CommunicationController.getMenuImage(mid, sid).base64


                val newImageVersion = ImageVersion(mid = mid, version = version, image = img)
                dao.insertImageVersion(newImageVersion)
                Log.d("AppViewModel" , "DBVersion updated!")
            }

            return img
        } catch (e : Exception) {
            Log.d("AppViewModel" , "Error whichImage: ${e.message}")
            return ""
        }
    }
}

class LocationRepository {

    // Checks whether the user has provided permissions or not
    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}


class UserViewModel(
    private val userRepository: UserRepository ,
    private val DBRepository : DBRepository ,
    private val locationRepository: LocationRepository
) : ViewModel(){

    /* ----------------------- AsyncStorage variables ----------------------- */

    private val _sid = MutableStateFlow("")
    val sid : StateFlow<String> = _sid

    private val _uid = MutableStateFlow(-1)
    val uid : StateFlow<Int> = _uid

    private val _oid = MutableStateFlow(-1)
    val oid : StateFlow<Int> = _oid

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // Recall the first boot logic
    // save the AsyncStorage data in "local"
    // when finished, set a value 'isInitializated' == 'true' ,
    // this means that you can perform operations with sid , uid and oid correctly
    suspend fun checkFirstLaunch() {
        userRepository.firstLaunch()
        _sid.value = userRepository.getSid()
        _uid.value = userRepository.getUid()
        _oid.value = userRepository.getOid()
        Log.d("AppViewModel" , "Sid: ${sid.value} ,Uid: ${uid.value}, Oid: ${oid.value}")
        _isInitialized.value = true
    }

    suspend fun checkOnResumeLaunch() {

        val lastScreen = userRepository.getScreen()
        withContext(Dispatchers.IO) {
            Log.d("AppViewModel" , "onResume on screen: ${lastScreen}")
            if (lastScreen != null) {

                _currentScreen.value = lastScreen
                Log.d("AppViewModel", "Screen loaded: ${_currentScreen.value}")
                _sid.value = userRepository.getSid()
                _uid.value = userRepository.getUid()
                _oid.value = userRepository.getOid()
                val lat = userRepository.getLat()
                val lng = userRepository.getLng()
                val currentLocation = Location("provider")
                currentLocation.latitude = lat
                currentLocation.longitude = lng
                _location.value = currentLocation

                if (_currentScreen.value == "MenuDetail") {
                    _mid.value = userRepository.getMid()
                    if (_mid.value != -1) {
                        handleMenuDetail(_mid.value)
                    } else {
                        Log.d("UserViewModel", "Invalid menu ID")
                        setCurrentScreen("Error")
                    }
                } else if (_currentScreen.value == "Profile") {
                    fetchUserInfo()
                } else {

                }
            } else {
                setCurrentScreen("Home")
                Log.d("AppViewModel", "checkOnResumeLaunch -> no screen has been saved")
            }
        }
    }

    /* ----------------------- Screen Logic ----------------------- */

    private val _currentScreen = MutableStateFlow("Home")
    val currentScreen : StateFlow<String> = _currentScreen


    fun setCurrentScreen(screen: String) {
        _currentScreen.value = screen
    }

    /* ----------------------- Background logic ----------------------- */

    private val _mid = MutableStateFlow(-1)
    val mid: StateFlow<Int> = _mid

    // Logic when the app is about to go into the background
    // save the currentScreen to AsyncStorage
    // save the coordinates in the AsyncStorage
    // if currentScreen == "MenuDetail", save the mid in the AsyncStorage
    suspend fun onPauseScreenLogic() {
        runBlocking {
            Log.d("UserViewModel", "onPauseScreenLogic on screen: ${_currentScreen.value}")
            userRepository.setScreen(_currentScreen.value)
            Log.d("UserViewModel", "Screen saved: ${_currentScreen.value}")

            _location.value?.let {
                userRepository.setLat(it.latitude)
                userRepository.setLng(it.longitude)
            }

            if (_currentScreen.value == "MenuDetail") {
                _menuDetail.value?.let { menuDetail ->
                    userRepository.setMid(menuDetail.mid)
                    Log.d("UserViewModel", "Menu ID saved: ${menuDetail.mid}")
                }
            }
        }
    }

    // Logic when the app runs again
    // set the currentScreen to the one saved in the AsyncStorage
    // get sid , uid and oid from AsyncStorage
    // to avoid synchronization delays with their recovery in checkFirstLaunch()
    // get lat and lng again from AsyncStorage
    // create a new Location object (Android) and set _location
    // if the screen obtained is == "MenuDetail" ,
    // take the mid from the AsyncStorage and call the handleMenuDetail() function
    // if the screen obtained is == "Profile"
    // call the fetchUserInfo() function
    /*suspend fun onResumeScreenLogic() {

        Log.d("UserViewModel", "onResumeScreenLogic on screen: ${_currentScreen.value}")
        withContext(Dispatchers.IO) {
            if (!screenUpdatedDuringPermissionRequest) {
                val lastScreen = userRepository.getScreen()
                if (_currentScreen.value != lastScreen) {
                    _currentScreen.value = lastScreen
                }
            }
            screenUpdatedDuringPermissionRequest = false

            _sid.value = userRepository.getSid()
            _uid.value = userRepository.getUid()
            _oid.value = userRepository.getOid()
            val lat = userRepository.getLat()
            val lng = userRepository.getLng()
            val currentLocation = Location("provider")
            currentLocation.latitude = lat
            currentLocation.longitude = lng
            _location.value = currentLocation

            if (_currentScreen.value == "MenuDetail") {
                _mid.value = userRepository.getMid()
                if (_mid.value != -1) {
                    handleMenuDetail(_mid.value)
                } else {
                    Log.d("UserViewModel", "Invalid menu ID")
                    setCurrentScreen("Error")
                }
            } else if (_currentScreen.value == "Profile") {
                fetchUserInfo()
            }
        }
    }*/


    /* ----------------------------- Permits on location stuff ----------------------------- */

    // Variable to take into account user permissions
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission


    // Inserts the status of the permissions into _hasPermission: true if granted, false otherwise
    fun checkPermission(context: Context) {
        _hasPermission.value = locationRepository.checkLocationPermission(context)
    }

    // Requires permissions to access the location
    fun requestPermission(permissionLauncher: ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Update user permissions
    fun updatePermissionStatus(isGranted: Boolean) {
        _hasPermission.value = isGranted
        Log.d("LocationViewModel", "Permission granted: $isGranted")
    }

    /* ----------------------------- Position calculation stuff ----------------------------- */

    // Variable to save user position
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    // Gets the current location
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            try {
                val task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                val location = task.await()
                _location.value = location
                Log.d("LocationViewModel", "Lat: ${location.latitude}, Lon: ${location.longitude}")
            } catch (e: Exception) {
                Log.d("LocationViewModel", "Impossible to get the position: ${e.message}")
            }
        }
    }


    /* ----------------------- Profile Logic ----------------------- */

    private val _userInfo = MutableStateFlow(UserInfo(
        firstName = null,
        lastName = null,
        cardFullName = null,
        cardNumber = null,
        cardExpireMonth = null,
        cardExpireYear = null,
        cardCVV = null,
        uid = 0,
        lastOid = null,
        orderStatus = null
    ))
    val userInfo : StateFlow<UserInfo> = _userInfo



    // Make the network call -> getUserInfo()
    // if it's successful:
    // - record the values in the '_userInfo' variable
    // - set the screen to "Profile"
    // else:
    // - go to error screen
    suspend fun fetchUserInfo() {

        Log.d("AppViewModel" , "fetchUserInfo called")
        val userInfo = CommunicationController.getUserInfo(sid.value, uid.value)
        if (userInfo != null) {
            _userInfo.value = userInfo
            setCurrentScreen("Profile")
        } else {
            Log.d("AppViewModel" , "error -> fetchUserInfo")
            setCurrentScreen("Error")
        }
    }

    // Make the network call -> putUserInfo() [CommunicationController]
    // if it's successful:
    // - profile in AsyncStorage == 'true' (user is registered)
    // - shows the alert to inform the user of the correct registration
    // else:
    // - go to the error screen
    suspend fun putUserInfo(userInfoPut: UserInfoPut) {
        Log.d("AppViewModel" , "putUserInfo called")
        try{
            CommunicationController.putUserInfo(uid.value,userInfoPut)
            userRepository.setProfile(true)
            _currentScreen.value = "AlertProfile"
        } catch (e : Exception) {
            Log.d("AppViewModel" , "error -> putUserInfo: ${e.message}")
            setCurrentScreen("Error")
        }
    }

    // Check that the user is registered
    // if it is getProfile() returns 'true'
    // 'false' otherwise
    suspend fun isRegistered(): Boolean {

        return userRepository.getProfile()


    }

    // De-register the user so they can re-enter new information
    // set profile in the AsyncStorage == 'false'
    // once the data has been put correctly, it is reset to 'true'
    suspend fun deRegistered() {
        userRepository.setProfile(false)
    }

    /* ----------------------- MenuList Logic ----------------------- */

    // 'isUpdateDB' is the "cap" of the menuList logic,
    // until have downloaded the list into the DB I cannot do any operation therefore == 'false'
    // otherwise the image comparison between network and DB would not be able to be performed
    // while is 'false' --> don't show MenuList()
    // when downloaded the whole menu list into the DB == true
    private val _isUpdatedDB = MutableStateFlow(false)
    val isUpdateDB: StateFlow<Boolean> = _isUpdatedDB
    // Variable to save menu list
    private val _menuList = MutableStateFlow<List<MenuList>>(emptyList())
    val menuList: StateFlow<List<MenuList>> = _menuList.asStateFlow()


    // Download the menu list from the network and save in the DB
    // once it's done, save the menu list into the variable
    suspend fun getMenuList() {
        Log.d("AppViewModel" , "getMenuListDB")

        _isUpdatedDB.value = false

        try{
            var menuList = emptyList<MenuList>()
            location.value?.let{
                 menuList = CommunicationController.getMenuList(it.latitude , it.longitude, sid.value)
            }
            // Iterate through the entire list
            for(menu in menuList) {
                // Take the image from the net
                val image =  CommunicationController.getMenuImage(menu.mid, sid.value)
                // Save the information of the single menu in the DB
                val newImageVersion = ImageVersion(mid = menu.mid, version = menu.imageVersion, image = image.base64)
                DBRepository.insertImageVersionDB(newImageVersion)
            }

            // Testing
            val result = DBRepository.getAllImageVersionInfo()

            // Update the menu list StateFlow
            _menuList.value = menuList

            // Operation complete --> can display MenuList()
            _isUpdatedDB.value = true
            Log.d("saveAllMenuInDb", "$result")
        } catch (e : Exception) {
            Log.d("AppViewModel", "getMenuListDB -> error: ${e.message}")
        }
    }


    // Resume the image selection logic from the repository
    suspend fun handleImageMenu(mid : Int , version : Int) : String {
        try{
            return DBRepository.whichImage(mid, _sid.value, version)
        } catch (e : Exception) {
            Log.d("AppViewModel" , "The error occurred while managing the image ${e.message}")
        }
            return ""
    }

    /* ----------------------- MenuDetail Logic ----------------------- */

    // Variable to save menu detail
    private val _menuDetail = MutableStateFlow<MenuDetail?>(null)
    val menuDetail: StateFlow<MenuDetail?> = _menuDetail.asStateFlow()

    // Variable to save img of menu
    private val _imgBase64DB = MutableStateFlow<String>("")
    val imgBase64DB : StateFlow<String> = _imgBase64DB


    // Set the screen to "MenuDetail"
    // get the details through network call -> getMenuDetail()
    // take the image from the db (the logic of updating the version has already necessarily happened)
    suspend fun handleMenuDetail(mid : Int) {
        setCurrentScreen("MenuDetail")

        // To ensure that before the component is initialized, everything is set up
        viewModelScope.launch {
            try {
                location.value?.let {
                    _menuDetail.value = CommunicationController.getMenuDetail(mid, it.latitude, it.longitude, sid.value)
                    _imgBase64DB.value = DBRepository.dao.getImage64(mid)
                    _mid.value = mid
                }
            } catch (e: Exception) {
                Log.d("AppViewModel", "handleMenuDetail -> error: ${e.message}")
                setCurrentScreen("Error")
            }
        }
    }

    /* ----------------------- myOrder Logic ----------------------- */

    // Variable to save new order
    private val _myOrder = MutableStateFlow<OrderInfo?>(null)
    val myOrder: StateFlow<OrderInfo?> = _myOrder.asStateFlow()

    // Variable to catch the error
    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError.asStateFlow()

    // Place a new order
    // verify that the user is registered, getProfile() == 'true'
    // if it is:
    // - creates a new location instance
    // - creates a new instance of 'OrderMenu'
    // - the network call is made -> postOrder()
    // - save the oid in the asyncStorage and variable
    // - if there are no error -> 'orderError' == 'null'
    // - else catch the error and set it to 'orderError'
    // else:
    // - set 'orderError' to "notRegistered"
    suspend fun makeOrder(mid: Int) {

        if (userRepository.getProfile()) {

            val currentLocation = _location.value?.let {
                com.example.progetto.classes.Location(
                    lat = it.latitude,
                    lng = it.longitude
                )
            }

            val orderMenu = currentLocation?.let {
                OrderMenu(
                    sid = _sid.value,
                    deliveryLocation = it
                )
            }

            //Log.d("AppViewModel", "makeOrder -> orderMenu: ${orderMenu.toString()}")

            try {
                _myOrder.value = orderMenu?.let { CommunicationController.postOrder(mid, it) }
                _orderError.value = null
                _myOrder.value?.let { userRepository.setOid(it.oid) }
                _myOrder.value?.let { _oid.value = it.oid }
                setCurrentScreen("AlertOrder")
            } catch (e: Exception) {
                //Log.d("AppViewModel", "Caught exception: ${e.message}")
                when {
                    e.message?.contains("409") == true -> _orderError.value = "409"
                    e.message?.contains("403") == true -> _orderError.value = "403"
                    else -> setCurrentScreen("Error")
                }
                setCurrentScreen("AlertOrder")
            }
        } else {
            _orderError.value = "notRegistered"
            setCurrentScreen("AlertOrder")
            }
        }

    // Call the order data
    // if the user has never placed an order before the asyncStorage key 'oid' will equal to -1
    // otherwise it is retrieved and saved in the variable at checkFirstLaunch()
    // for background logic: check user's location permission
    suspend fun getMyOrder() {
            try{
                _myOrder.value = CommunicationController.getOrderInfo(_oid.value, _sid.value)
            } catch (e : Exception) {
                Log.d("AppViewModel", "getMyOrder -> error: ${e.message}")
            }
    }

    // Return a MenuDetail
    // need it to retrieve some information, in this context the name of the menu ordered
    suspend fun menuInfoOrder(mid : Int) : MenuDetail? {

        try{
            return _location.value?.let { CommunicationController.getMenuDetail(mid, it.latitude, it.longitude, _sid.value) }
        } catch (e : Exception) {
            Log.d("AppViewModel", "menuInfoOrder -> error: ${e.message}")
            return null
        }
    }
}

