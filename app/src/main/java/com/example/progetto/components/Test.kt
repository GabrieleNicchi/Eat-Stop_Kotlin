/*package com.example.progetto.components

//AppViewModel.kt
/* ----------------------- favorites Logic ----------------------- */

    // Variable to save favorites
    private val _favorites = MutableStateFlow<List<Favorites>>(emptyList())
    val favorites: StateFlow<List<Favorites>> = _favorites.asStateFlow()
    // Variable to save menu list
    private val _favMenuList = MutableStateFlow<List<MenuDetail>>(emptyList())
    val favMenuList: StateFlow<List<MenuDetail>> = _favMenuList.asStateFlow()

    suspend fun fetchFavorite(mid: Int): Boolean {
        return try {
            val favorite = Favorites(mid = mid)
            DBRepository.insertFavorite(favorite)
            _favorites.value = DBRepository.getAllFavorites() //update status
            true
        } catch (e: Exception) {
            Log.d("AppViewModel", "Error fetching favorite: ${e.message}")
            false
        }
    }

    suspend fun removeFromFavorite(mid: Int): Boolean {
        return try {
            DBRepository.removeFromFavorites(mid)
            _favorites.value = DBRepository.getAllFavorites() //update status
            true
        } catch (e: Exception) {
            Log.d("AppViewModel", "Error removing favorite: ${e.message}")
            false
        }
    }

    suspend fun checkFavorite(mid: Int): Boolean {
        Log.d("AppViewModel" , "favorite: ${_favorites.value}")
        val favoritesList = DBRepository.getAllFavorites()
        return favoritesList.any { it.mid == mid }
    }

    suspend fun handleFavorite() {
        val tempFavMenuList = mutableListOf<MenuDetail>()
        for(favorite in _favorites.value){
            try{
                val menu = _location.value?.let{CommunicationController.getMenuDetail(favorite.mid , it.latitude, it.longitude, _sid.value)}
                menu?.let { tempFavMenuList.add(it) }
            } catch (e : Error) {
                Log.d("AppViewModel" , "Error -> handle favorite $e")
            }
        }
        _favMenuList.value = tempFavMenuList
    }

 */
/*
//DBController.kt

// Create the DB
// It is defined against the "ImageVersion" class in DataClasses
// define an abstract class in which I define the query function (below)
@Database(entities = [ImageVersion::class, Favorites::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageVersionDao(): ImageVersionDao
    abstract fun favoritesDao(): FavoritesDao
}

@Dao
interface FavoritesDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorites: Favorites)

    @Query("DELETE FROM Favorites WHERE mid= :mid")
    suspend fun removeFromFavorites(mid : Int)

    @Query("SELECT * FROM Favorites")
    suspend fun getAllFavorites() : List<Favorites>

}

 */

/*
     //Favorites.kt
     @Composable
fun Favorites(userViewModel: UserViewModel) {

    val favorites by userViewModel.favorites.collectAsState()
    val favMenuList by userViewModel.favMenuList.collectAsState()
    Log.d("Favorites" , favorites.toString())

    LaunchedEffect(Unit) {
        Log.d("Favorites", "Mounted component")
        userViewModel.handleFavorite()
    }

    // Iterate over the individual menus
    if (favMenuList.isNotEmpty()) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFEEEFF1))
        ){
            Header(userViewModel)
            Spacer(modifier = Modifier.height(40.dp)) // spacer between them
            LazyColumn {
                items(favMenuList) { menuItem ->
                    FavMenuItemView(
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

@Composable
fun FavMenuItemView(menuItem : MenuDetail, userViewModel: UserViewModel) {

    val coroutineScope = rememberCoroutineScope()
    var img64 by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    // Check if the item is a favorite
    LaunchedEffect(menuItem.mid) {
        isFavorite = userViewModel.checkFavorite(menuItem.mid)
    }

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
        modifier = Modifier.padding(16.dp) // external padding
    ) {
        Box(
            modifier = boxModifier
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
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
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton(
                        onClick = {
                            // -> handleMenuDetail [ViewModel]
                            coroutineScope.launch {
                                // Scroll to the middle of the menu interacted with
                                userViewModel.handleMenuDetail(menuItem.mid)
                            }
                        },
                        modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .background(
                                    color = Color(0xFFF96167),
                                    RoundedCornerShape(8.dp)
                                )
                    ) {
                        Text(text = "View Info", color = Color.White)
                    }
                    if (isFavorite) {
                        CustomButton(
                            onClick = {
                                // -> removeFromFavorite [ViewModel]
                                coroutineScope.launch {
                                    val success = userViewModel.removeFromFavorite(menuItem.mid)
                                    if (success) {
                                        isFavorite = false // Update state immediately
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .background(
                                    color = Color(0xFF4CAF50),
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(text = "Remove from Favorites", color = Color.White)
                        }
                    } else {
                        CustomButton(
                            onClick = {
                                // -> fetchFavorite [ViewModel]
                                coroutineScope.launch {
                                    val success = userViewModel.fetchFavorite(menuItem.mid)
                                    if (success) {
                                        isFavorite = true // Update state immediately
                                    }
                                }
                            },
                            modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .background(
                                        color = Color(0xFF4CAF50),
                                        RoundedCornerShape(8.dp)
                                    )
                        ) {
                            Text(text = "Add to Favorites", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

*/


