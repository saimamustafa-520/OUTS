package com.example.ui.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import java.util.UUID

data class TicketTier(
    val name: String,
    val price: Double,
    val availableCount: Int,
    val soldCount: Int = 0,
    val description: String = "",
    val badge: String? = null // "VIP", "Early Bird", "Sold Out"
)

enum class EventStatus {
    PENDING, APPROVED, REJECTED, SOLD_OUT
}

data class EventItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val imageUrl: String? = null,
    val imageCategory: String = "party",
    val date: String,
    val time: String,
    val location: String,
    val hostName: String,
    val hostId: String? = null,
    val ticketTiers: List<TicketTier>,
    val status: EventStatus,
    val totalTickets: Int = 0,
    val ticketsSold: Int = 0,
    val remainingTickets: Int = 0,
    val revenue: Double = 0.0,
    val createdAt: Any? = null, // Using Any to support FieldValue.serverTimestamp()
    val isHotSeller: Boolean = false,
    val isSellingFast: Boolean = false,
    val isFree: Boolean = false
)

data class UserItem(
    val id: String,
    val name: String,
    val email: String,
    val role: String = "consumer", // "consumer", "host", "admin"
    val profileImage: String? = null,
    val status: String = "active", // "active", "blocked"
    val createdAt: Any? = null,
    val favoritedEventIds: List<String> = emptyList()
)

data class BookingItem(
    val id: String,
    val bookingReference: String = "",
    val eventId: String,
    val eventTitle: String,
    val eventDate: String = "",
    val eventVenue: String = "",
    val buyerId: String? = null,
    val buyerName: String,
    val buyerEmail: String,
    val hostId: String? = null,
    val ticketCount: Int = 1,
    val ticketPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val bookingStatus: String = "confirmed", // "confirmed", "cancelled"
    val ticketQRCode: String = "",
    val createdAt: Any? = null
)

data class NavState(
    val currentScreen: String,
    val consumerScreen: String,
    val hostScreen: String,
    val adminScreen: String,
    val selectedEventId: String? = null,
    val selectedBookingId: String? = null,
    val consumerTabIndex: Int = 0
)

class OutsViewModel : ViewModel() {
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null

    private var eventsListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var bookingsListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null

    // Screen navigation back stack history
    private val navHistory = mutableListOf<NavState>()
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private fun updateCanGoBack() {
        _canGoBack.value = navHistory.isNotEmpty()
    }

    private fun pushCurrentStateToHistory() {
        val state = NavState(
            currentScreen = _currentScreen.value,
            consumerScreen = _consumerScreen.value,
            hostScreen = _hostScreen.value,
            adminScreen = _adminScreen.value,
            selectedEventId = _selectedEventId.value,
            selectedBookingId = _selectedBookingId.value,
            consumerTabIndex = _consumerTabIndex.value
        )
        if (navHistory.isEmpty() || navHistory.last() != state) {
            navHistory.add(state)
            updateCanGoBack()
        }
    }

    fun goBack(): Boolean {
        if (navHistory.isNotEmpty()) {
            val prevState = navHistory.removeAt(navHistory.size - 1)
            _currentScreen.value = prevState.currentScreen
            _consumerScreen.value = prevState.consumerScreen
            _hostScreen.value = prevState.hostScreen
            _adminScreen.value = prevState.adminScreen
            _selectedEventId.value = prevState.selectedEventId
            _selectedBookingId.value = prevState.selectedBookingId
            _consumerTabIndex.value = prevState.consumerTabIndex
            updateCanGoBack()
            return true
        }
        updateCanGoBack()
        return false
    }

    // Current Active Flows / Navigation state
    // "splash" -> "role_chooser" -> depending on role selected
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _isScreenLoading = MutableStateFlow(false)
    val isScreenLoading: StateFlow<Boolean> = _isScreenLoading.asStateFlow()

    fun triggerScreenLoad() {
        viewModelScope.launch {
            _isScreenLoading.value = true
            delay(450)
            _isScreenLoading.value = false
        }
    }

    // Core Toast system for premium visual dynamic alerts
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _toastType = MutableStateFlow("success") // "success", "error", "info"
    val toastType: StateFlow<String> = _toastType.asStateFlow()

    private var toastJob: Job? = null

    fun showToast(message: String, type: String = "success") {
        toastJob?.cancel()
        _toastMessage.value = message
        _toastType.value = type
        toastJob = viewModelScope.launch {
            delay(2800)
            _toastMessage.value = null
        }
    }

    // B2C Customer Flow state: "login" -> "home" -> "event_details" -> "tickets" -> "success" -> "profile"
    private val _consumerScreen = MutableStateFlow("login")
    val consumerScreen: StateFlow<String> = _consumerScreen.asStateFlow()

    private val _consumerTabIndex = MutableStateFlow(0)
    val consumerTabIndex: StateFlow<Int> = _consumerTabIndex.asStateFlow()

    fun setConsumerTabIndex(index: Int) {
        _consumerTabIndex.value = index
        triggerScreenLoad()
    }

    // Host Flow State: "login" -> "dashboard" / tabs: Dashboard, Events, Analytics, Profile
    private val _hostScreen = MutableStateFlow("login")
    val hostScreen: StateFlow<String> = _hostScreen.asStateFlow()
    
    // Admin Flow State: "login" -> "dashboard" / tabs: Users, Moderation, Revenue, Settings
    private val _adminScreen = MutableStateFlow("login")
    val adminScreen: StateFlow<String> = _adminScreen.asStateFlow()

    // Selected items for detail/purchasing screens
    private val _selectedEventId = MutableStateFlow<String?>(null)
    val selectedEventId: StateFlow<String?> = _selectedEventId.asStateFlow()

    private val _selectedBookingId = MutableStateFlow<String?>(null)
    val selectedBookingId: StateFlow<String?> = _selectedBookingId.asStateFlow()

    // Last purchased ticket details
    var lastOrderEventTitle by mutableStateOf("")
    var lastOrderType by mutableStateOf("")
    var lastOrderID by mutableStateOf("")
    var lastOrderPrice by mutableStateOf(0.0)
    var lastOrderDate by mutableStateOf("")
    var lastOrderLocation by mutableStateOf("")
    var lastBookingId by mutableStateOf("")

    // Dynamic Lists & DB Models
    val events = mutableStateListOf<EventItem>()
    val users = mutableStateListOf<UserItem>()
    val bookings = mutableStateListOf<BookingItem>()
    val favoritedEventIds = mutableStateListOf<String>()
    val categories = mutableStateListOf<String>()

    private fun loadDefaultCategories() {
        if (categories.isEmpty()) {
            categories.addAll(listOf("Nightlife", "Fitness", "Culture", "Fashion", "Entertainment"))
        }
    }

    fun toggleFavorite(eventId: String) {
        if (favoritedEventIds.contains(eventId)) {
            favoritedEventIds.remove(eventId)
            showToast("Removed from wishlist! 💔", "info")
        } else {
            favoritedEventIds.add(eventId)
            showToast("Added to wishlist! ❤️", "success")
        }
        syncFavorites()
    }

    private fun syncFavorites() {
        val uid = currentUserId ?: return
        firestore?.collection("users")?.document(uid)?.update("favoritedEventIds", favoritedEventIds.toList())
    }

    fun updateUserProfile(name: String, avatarColorIndex: Int) {
        val uid = currentUserId ?: return
        val updates = mapOf(
            "name" to name,
            "avatarColorIndex" to avatarColorIndex
        )
        firestore?.collection("users")?.document(uid)?.update(updates)?.addOnSuccessListener {
            currentUserProfileName = name
            showToast("Profile updated successfully!", "success")
        }
    }

    // Local Platform Financials Dynamic Aggregation
    var platformEarnings by mutableStateOf(0.0)
    var totalVolumeProcessed by mutableStateOf(0.0)
    var hostTicketsSold by mutableStateOf(0)
    var hostRevenueTotal by mutableStateOf(0.0)

    // Dynamic auth states
    var currentUserId by mutableStateOf<String?>(null)
    var currentUserEmail by mutableStateOf<String?>(null)
    var currentRole by mutableStateOf("guest") // "consumer", "host", "admin"
    var currentUserProfileName by mutableStateOf("Guest")
    
    var isAuthenticating by mutableStateOf(false)
    var isPurchasing by mutableStateOf(false)
    var authErrorMessage by mutableStateOf<String?>(null)

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            storage = FirebaseStorage.getInstance()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        loadDefaultCategories() // Ensure defaults are present immediately
        recalculatePlatformStats()
        initFirebaseListeners()
    }

    fun navigateTo(screen: String) {
        if (screen == "role_chooser" || screen == "splash") {
            navHistory.clear()
            updateCanGoBack()
            _currentScreen.value = screen
            return
        }
        pushCurrentStateToHistory()
        _currentScreen.value = screen
        triggerScreenLoad()
    }

    fun navigateConsumerTo(subScreen: String, eventId: String? = null, bookingId: String? = null) {
        if (subScreen == "home") {
            navHistory.clear()
            updateCanGoBack()
            _currentScreen.value = "consumer"
            _consumerScreen.value = subScreen
            if (_consumerTabIndex.value != 0 && _consumerTabIndex.value != 1) {
                _consumerTabIndex.value = 0
            }
            triggerScreenLoad()
            return
        }
        pushCurrentStateToHistory()
        if (eventId != null) {
            _selectedEventId.value = eventId
        }
        if (bookingId != null) {
            _selectedBookingId.value = bookingId
        }
        if (subScreen == "profile") {
            if (_consumerTabIndex.value != 2 && _consumerTabIndex.value != 3) {
                _consumerTabIndex.value = 3
            }
        }
        _consumerScreen.value = subScreen
        triggerScreenLoad()
    }

    fun navigateHostTo(subScreen: String) {
        if (subScreen == "dashboard") {
            navHistory.clear()
            updateCanGoBack()
            _currentScreen.value = "host"
            _hostScreen.value = subScreen
            triggerScreenLoad()
            return
        }
        pushCurrentStateToHistory()
        _hostScreen.value = subScreen
        triggerScreenLoad()
    }

    fun navigateAdminTo(subScreen: String) {
        if (subScreen == "dashboard") {
            navHistory.clear()
            updateCanGoBack()
            _currentScreen.value = "admin"
            _adminScreen.value = subScreen
            triggerScreenLoad()
            return
        }
        pushCurrentStateToHistory()
        _adminScreen.value = subScreen
        triggerScreenLoad()
    }

    private fun initFirebaseListeners() {
        try {
            // Auth Change state listener
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    currentUserId = user.uid
                    currentUserEmail = user.email
                    firestore?.collection("users")?.document(user.uid)?.get()?.addOnSuccessListener { snapshot ->
                        val userDataVal = snapshot.data
                        if (userDataVal != null && userDataVal["status"] == "blocked") {
                            auth?.signOut()
                            showToast("This account is BLOCKED.", "error")
                            return@addOnSuccessListener
                        }
                        val role = if (userDataVal != null) {
                            userDataVal["role"] as? String ?: "consumer"
                        } else {
                            when {
                                user.email?.equals("admin@outs.app", ignoreCase = true) == true -> "admin"
                                user.email?.contains("host", ignoreCase = true) == true -> "host"
                                else -> "consumer"
                            }
                        }
                        
                        // Strict singleton admin enforcement
                        val finalRole = if (role == "admin" && user.email?.lowercase() != "admin@outs.app") {
                            "consumer"
                        } else {
                            role
                        }

                        viewModelScope.launch {
                            try {
                                currentRole = finalRole
                                currentUserProfileName = if (userDataVal != null) {
                                    userDataVal["name"] as? String ?: user.email?.substringBefore("@") ?: "User"
                                } else {
                                    user.email?.substringBefore("@") ?: "User"
                                }
                                
                                // Auto-correct role in DB if it's an illegal admin
                                if (role != finalRole && userDataVal != null) {
                                    firestore?.collection("users")?.document(user.uid)?.update("role", "consumer")
                                }
                                
                                if (userDataVal != null) {
                                    val favs = (userDataVal["favoritedEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                    favoritedEventIds.clear()
                                    favoritedEventIds.addAll(favs)
                                }
                                
                                // Auto-navigate to respective dashboard if on login / startup screens
                                val currentScr = _currentScreen.value
                                val consScr = _consumerScreen.value
                                val hstScr = _hostScreen.value
                                val admScr = _adminScreen.value
                                
                                if (currentScr == "splash" || currentScr == "role_chooser" || 
                                    (currentScr == "consumer" && consScr == "login") || 
                                    (currentScr == "host" && hstScr == "login") || 
                                    (currentScr == "admin" && admScr == "login")) {
                                    
                                    when (role) {
                                        "admin" -> navigateAdminTo("dashboard")
                                        "host" -> navigateHostTo("dashboard")
                                        else -> navigateConsumerTo("home")
                                    }
                                }
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        currentUserId = null
                        currentUserEmail = null
                        currentRole = "guest"
                        currentUserProfileName = "Guest"
                        favoritedEventIds.clear()
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        try {
            // Realtime Event Sync (Firestore)
            eventsListener = firestore?.collection("events")?.addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val dbEvents = mutableListOf<EventItem>()
                snapshots?.forEach { doc ->
                    try {
                        dbEvents.add(doc.data.toEventItem())
                    } catch (err: Exception) { err.printStackTrace() }
                }
                viewModelScope.launch {
                    events.clear()
                    events.addAll(dbEvents)
                    recalculatePlatformStats()
                }
            }
        } catch (e: Throwable) { e.printStackTrace() }

        try {
            // Realtime Users list sync (Firestore)
            usersListener = firestore?.collection("users")?.addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val dbUsers = mutableListOf<UserItem>()
                snapshots?.forEach { doc ->
                    try {
                        dbUsers.add(doc.data.toUserItem())
                    } catch (err: Exception) { err.printStackTrace() }
                }
                viewModelScope.launch {
                    users.clear()
                    users.addAll(dbUsers)
                }
            }
        } catch (e: Throwable) { e.printStackTrace() }

        try {
            // Realtime Transaction synchronization (Firestore)
            bookingsListener?.remove()
            bookingsListener = firestore?.collection("bookings")
                ?.addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        android.util.Log.e("OutsAuth", "Bookings listener failed: ${e.message}")
                        return@addSnapshotListener
                    }
                    val dbBookings = mutableListOf<BookingItem>()
                    snapshots?.forEach { doc ->
                        try {
                            dbBookings.add(doc.data.toBookingItem())
                        } catch (err: Exception) { err.printStackTrace() }
                    }
                    viewModelScope.launch {
                        // Incremental update to avoid flickering/clearing
                        val sortedBookings = dbBookings.sortedWith(
                            compareByDescending<BookingItem> { 
                                when (val time = it.createdAt) {
                                    is com.google.firebase.Timestamp -> time.toDate().time
                                    is Long -> time
                                    else -> Long.MAX_VALUE // Local pending write
                                }
                            }.thenByDescending { it.id }
                        )
                        
                        // Clear and add all at once to trigger a single state change
                        bookings.clear()
                        bookings.addAll(sortedBookings)
                        recalculatePlatformStats()
                    }
                }
        } catch (e: Throwable) { e.printStackTrace() }

        try {
            // Realtime Categories sync
            categoriesListener = firestore?.collection("categories")?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    loadDefaultCategories()
                    return@addSnapshotListener
                }
                val dbCats = snapshots?.mapNotNull { it.getString("name") } ?: emptyList()
                viewModelScope.launch {
                    categories.clear()
                    if (dbCats.isEmpty()) {
                        loadDefaultCategories()
                        // Auto-populate firestore if empty so the user sees data next time
                        categories.forEach { name ->
                            firestore?.collection("categories")?.document(name.lowercase())?.set(mapOf("name" to name))
                        }
                    } else {
                        categories.addAll(dbCats)
                    }
                }
            }
        } catch (e: Throwable) { 
            e.printStackTrace() 
            loadDefaultCategories()
        }
    }

    private fun recalculatePlatformStats() {
        val total = bookings.sumOf { it.totalPrice }
        totalVolumeProcessed = total
        platformEarnings = total * 0.05 // Platform fee logic
        
        // Host-specific stats for the current user
        val uid = currentUserId
        if (uid != null) {
            val myBookings = bookings.filter { it.hostId == uid }
            
            hostTicketsSold = myBookings.sumOf { it.ticketCount }
            hostRevenueTotal = myBookings.sumOf { it.totalPrice }
        }
    }

    // Dynamic Firebase Authentication Hooks
    fun registerUser(email: String, password: String, role: String, name: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val cleanedEmail = email.trim()
        val cleanedPassword = password.trim()
        if (cleanedEmail.isEmpty() || cleanedPassword.isEmpty()) {
            onFailure("Email and password cannot be empty.")
            return
        }
        
        isAuthenticating = true
        authErrorMessage = null
        
        val currentAuth = auth ?: run {
            isAuthenticating = false
            onFailure("Firebase Auth not initialized.")
            return
        }
        
        currentAuth.createUserWithEmailAndPassword(cleanedEmail, cleanedPassword)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    
                    // Proceed immediately
                    currentUserId = uid
                    currentUserEmail = cleanedEmail
                    currentRole = role
                    currentUserProfileName = name.ifBlank { cleanedEmail.substringBefore("@") }
                    
                    isAuthenticating = false
                    showToast("Welcome to OUTS, $currentUserProfileName!", "success")
                    onSuccess()

                    // Strict validation for admin role on registration
                    val validatedRole = if (role == "admin" && cleanedEmail.lowercase() != "admin@outs.app") "consumer" else role

                    // Create user profile in Firestore in background
                    val userItem = UserItem(
                        id = uid,
                        name = currentUserProfileName,
                        email = cleanedEmail,
                        role = validatedRole,
                        status = "active"
                    )
                    
                    firestore?.collection("users")?.document(uid)?.set(userItem.toMap())
                } else {
                    isAuthenticating = false
                    onFailure("User initialization failed.")
                }
            }
            .addOnFailureListener { e ->
                isAuthenticating = false
                onFailure(e.localizedMessage ?: "Registration failed.")
            }
    }

    fun loginUser(email: String, password: String, expectedRole: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val cleanedEmail = email.trim()
        val cleanedPassword = password.trim()
        if (cleanedEmail.isEmpty() || cleanedPassword.isEmpty()) {
            onFailure("Email and password cannot be empty.")
            return
        }
        
        isAuthenticating = true
        authErrorMessage = null
        
        val currentAuth = auth ?: run {
            isAuthenticating = false
            onFailure("Firebase Auth not initialized.")
            return
        }
        
        currentAuth.signInWithEmailAndPassword(cleanedEmail, cleanedPassword)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    
                    // Pre-fill with basic info and clear processing state to avoid hanging
                    currentUserId = uid
                    currentUserEmail = cleanedEmail
                    currentRole = expectedRole
                    currentUserProfileName = cleanedEmail.substringBefore("@")
                    
                    isAuthenticating = false
                    showToast("Welcome back!", "success")
                    onSuccess()

                    // Sync full profile details in background from Firestore
                    firestore?.collection("users")?.document(uid)?.get()?.addOnSuccessListener { snapshot ->
                        val userDataVal = snapshot.data
                        if (userDataVal != null) {
                            try {
                                val userItem = userDataVal.toUserItem()
                                
                                if (userItem.status == "blocked") {
                                    auth?.signOut()
                                    currentUserId = null
                                    currentUserEmail = null
                                    currentRole = "guest"
                                    showToast("Your account has been blocked by Admin.", "error")
                                    // No navigate forward
                                    return@addOnSuccessListener
                                }

                                currentUserId = uid
                                currentUserEmail = cleanedEmail
                                currentRole = userItem.role
                                currentUserProfileName = userItem.name
                                
                                val favs = (userDataVal["favoritedEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                favoritedEventIds.clear()
                                favoritedEventIds.addAll(favs)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    isAuthenticating = false
                    showToast("Login authentication failed.", "error")
                    onFailure("Login failed.")
                }
            }
            .addOnFailureListener { e ->
                // AUTO-CREATE TEST ACCOUNTS BYPASS
                val testEmails = listOf("admin@outs.app", "host@outs.app", "user@outs.app")
                if (cleanedEmail.lowercase() in testEmails) {
                    val resolvedRole = when (cleanedEmail.lowercase()) {
                        "admin@outs.app" -> "admin"
                        "host@outs.app" -> "host"
                        else -> "consumer"
                    }
                    val testName = when (resolvedRole) {
                        "admin" -> "Global Admin"
                        "host" -> "Vibe Events Host"
                        else -> "Sarah Consumer"
                    }
                    
                    android.util.Log.d("OutsAuth", "Auto-provisioning test account: $cleanedEmail")
                    registerUser(cleanedEmail, cleanedPassword, resolvedRole, testName, onSuccess, onFailure)
                } else {
                    isAuthenticating = false
                    onFailure(e.localizedMessage ?: "Login failed.")
                }
            }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("Please enter your email address.")
            return
        }
        auth?.sendPasswordResetEmail(email)
            ?.addOnSuccessListener {
                showToast("Password reset email sent!", "success")
                onSuccess()
            }
            ?.addOnFailureListener {
                onFailure(it.localizedMessage ?: "Failed to send reset email.")
            }
    }

    fun logoutUser(onSuccess: () -> Unit = {}) {
        val prevName = currentUserProfileName
        auth?.signOut()
        currentUserId = null
        currentUserEmail = null
        currentRole = "guest"
        currentUserProfileName = "Guest"
        favoritedEventIds.clear()
        navHistory.clear()
        updateCanGoBack()
        _currentScreen.value = "role_chooser"
        _consumerScreen.value = "login"
        _hostScreen.value = "login"
        _adminScreen.value = "login"
        showToast("Logged out successfully. See you soon, $prevName!", "info")
        onSuccess()
    }

    fun signInWithGoogle(idToken: String, expectedRole: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        isAuthenticating = true
        android.util.Log.d("OutsAuth", "Starting Google Sign-In with token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnSuccessListener { authResult ->
                android.util.Log.d("OutsAuth", "Sign-In with credential successful")
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val email = firebaseUser.email ?: ""
                    
                    // Proced immediately with available Google profile data to avoid hanging on DB
                    currentUserId = uid
                    currentUserEmail = email
                    currentRole = expectedRole
                    currentUserProfileName = firebaseUser.displayName ?: email.substringBefore("@")
                    
                    isAuthenticating = false
                    showToast("Welcome, $currentUserProfileName!", "success")
                    onSuccess()
                    
                    // Synchronize with database in background
                    firestore?.collection("users")?.document(uid)?.get()?.addOnSuccessListener { snapshot ->
                        val userDataVal = snapshot.data
                        if (userDataVal != null && userDataVal["status"] == "blocked") {
                            auth?.signOut()
                            showToast("Account blocked.", "error")
                            return@addOnSuccessListener
                        }
                        if (userDataVal != null) {
                            try {
                                val userItem = userDataVal.toUserItem()
                                currentRole = userItem.role
                                currentUserProfileName = userItem.name
                                
                                val favs = (userDataVal["favoritedEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                                favoritedEventIds.clear()
                                favoritedEventIds.addAll(favs)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            // Create record for new Google user
                            val newUser = UserItem(
                                id = uid,
                                name = currentUserProfileName,
                                email = email,
                                role = expectedRole,
                                status = "active"
                            )
                            firestore?.collection("users")?.document(uid)?.set(newUser.toMap())
                        }
                    }
                } else {
                    isAuthenticating = false
                    onFailure("Google user data is null.")
                }
            }
            ?.addOnFailureListener { e ->
                android.util.Log.e("OutsAuth", "Google Sign-In failed: ${e.message}")
                isAuthenticating = false
                onFailure(e.localizedMessage ?: "Google Sign-In failed.")
            } ?: run {
                isAuthenticating = false
                onFailure("Firebase Auth not initialized.")
            }
    }

    // Interactive Purchase Process
    fun buyTicket(eventId: String, tierName: String, quantity: Int) {
        if (quantity <= 0) return
        val db = firestore ?: return
        val eventRef = db.collection("events").document(eventId)
        val uid = currentUserId ?: auth?.currentUser?.uid

        if (uid == null) {
            showToast("Session expired. Please log in again.", "error")
            return
        }
        
        isPurchasing = true
        db.runTransaction { transaction ->
            val snapshot = transaction.get(eventRef)
            val event = snapshot.data?.toEventItem() ?: throw Exception("Event not found")
            
            if (event.remainingTickets < quantity) {
                throw Exception("Not enough tickets available")
            }

            val targetTier = event.ticketTiers.find { it.name == tierName } ?: throw Exception("Tier not found")
            if (targetTier.availableCount < quantity) {
                 throw Exception("Not enough tickets in this tier")
            }

            // Update Tiers
            val updatedTiers = event.ticketTiers.map { tier ->
                if (tier.name == tierName) {
                    tier.copy(
                        availableCount = tier.availableCount - quantity,
                        soldCount = tier.soldCount + quantity
                    )
                } else {
                    tier
                }
            }

            val newTicketsSold = event.ticketsSold + quantity
            val newRemaining = event.remainingTickets - quantity
            val pricePerTicket = targetTier.price
            val purchaseTotal = pricePerTicket * quantity
            val newRevenue = event.revenue + purchaseTotal
            
            val newStatus = if (newRemaining <= 0) EventStatus.SOLD_OUT else event.status

            val updatedEvent = event.copy(
                ticketTiers = updatedTiers,
                ticketsSold = newTicketsSold,
                remainingTickets = newRemaining,
                revenue = newRevenue,
                status = newStatus
            )

            transaction.set(eventRef, updatedEvent.toMap())
            
            // Create Booking Record
            val bookingId = "bk_${UUID.randomUUID().toString().take(8)}"
            val bookingRef = com.example.ui.utils.TicketUtils.generateBookingReference()
            val qrContent = "bookingId: $bookingId\neventId: $eventId\nuserId: $uid\ntickets: $quantity"
            
            val newBooking = BookingItem(
                id = bookingId,
                bookingReference = bookingRef,
                eventId = eventId,
                eventTitle = event.title,
                eventDate = event.date,
                eventVenue = event.location,
                buyerId = uid,
                buyerName = currentUserProfileName,
                buyerEmail = currentUserEmail ?: "",
                hostId = event.hostId,
                ticketCount = quantity,
                ticketPrice = pricePerTicket,
                totalPrice = purchaseTotal,
                bookingStatus = "confirmed",
                ticketQRCode = qrContent,
                createdAt = com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            transaction.set(db.collection("bookings").document(bookingId), newBooking.toMap())
            
            newBooking // Return the new booking item from the transaction
        }.addOnSuccessListener { result ->
            val booking = result as BookingItem
            // Update local state for success screen only AFTER server success
            lastBookingId = booking.id
            lastOrderEventTitle = booking.eventTitle
            lastOrderType = tierName
            lastOrderID = "#OUTS-${booking.id.uppercase()}"
            lastOrderPrice = booking.totalPrice
            lastOrderDate = booking.eventDate
            lastOrderLocation = booking.eventVenue

            // Crucially: Add to local list immediately so "My Tickets" and "View Ticket" work INSTANTLY
            // even before the SnapshotListener fires.
            if (bookings.none { it.id == booking.id }) {
                bookings.add(0, booking)
            }

            showToast("Purchase successful!", "success")
            isPurchasing = false
            navigateConsumerTo("success")
        }.addOnFailureListener { e ->
            isPurchasing = false
            showToast("Booking failed: ${e.message}", "error")
        }
    }

    // Create custom event from Host flow
    fun deleteEvent(eventId: String) {
        firestore?.collection("events")?.document(eventId)?.delete()
        showToast("Event deleted successfully.", "info")
    }

    fun uploadEventImage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val storageRef = storage?.reference ?: return onFailure("Storage not initialized")
        val fileName = "events/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }.addOnFailureListener {
                    onFailure("Failed to get download URL")
                }
            }
            .addOnFailureListener {
                onFailure("Upload failed: ${it.message}")
            }
    }

    fun addNewEvent(
        title: String,
        category: String,
        description: String,
        date: String,
        time: String,
        location: String,
        totalTickets: Int,
        ticketTiers: List<TicketTier>,
        imageUrl: String? = null
    ) {
        val uid = currentUserId ?: auth?.currentUser?.uid
        if (uid == null) {
            showToast("Error: User session not found. Please log in again.", "error")
            return
        }

        val newId = "e_${(10000..99999).random()}"
        val newEvent = EventItem(
            id = newId,
            title = title.ifBlank { "Untitled Event" },
            category = category.ifBlank { "Nightlife" },
            description = description.ifBlank { "No description provided." },
            imageUrl = imageUrl,
            imageCategory = "custom",
            date = date.ifBlank { "TBD" },
            time = time.ifBlank { "TBD" },
            location = location.ifBlank { "Online" },
            hostName = currentUserProfileName,
            hostId = uid,
            ticketTiers = ticketTiers,
            status = EventStatus.PENDING,
            totalTickets = totalTickets,
            ticketsSold = 0,
            remainingTickets = totalTickets,
            revenue = 0.0
        )
        firestore?.collection("events")?.document(newId)?.set(newEvent.toMap())
            ?.addOnSuccessListener {
                showToast("Event published! Pending admin moderation.", "success")
            }
            ?.addOnFailureListener { e ->
                showToast("Failed to save event: ${e.localizedMessage}", "error")
            }
    }

    fun updateEvent(event: EventItem) {
        firestore?.collection("events")?.document(event.id)?.set(event.toMap())
            ?.addOnSuccessListener {
                showToast("Event updated successfully.", "success")
            }
            ?.addOnFailureListener {
                showToast("Failed to update event.", "error")
            }
    }

    // Moderation Action (Approve / Reject)
    fun moderateEvent(eventId: String, approve: Boolean) {
        val newStatus = if (approve) EventStatus.APPROVED else EventStatus.REJECTED
        firestore?.collection("events")?.document(eventId)?.update("status", newStatus.name)
            ?.addOnSuccessListener {
                if (approve) {
                    showToast("Event approved! It is now LIVE on OUTS.", "success")
                } else {
                    showToast("Event rejected. Marked as Rejected.", "error")
                }
            }
            ?.addOnFailureListener { e ->
                showToast("Moderation failed: ${e.message}", "error")
            }
    }

    // User Management (Approve / Block / Unblock)
    fun updateUserStatus(userId: String, newStatus: String) {
        firestore?.collection("users")?.document(userId)?.update("status", newStatus)
        if (newStatus == "blocked") {
            showToast("User account has been BLOCKED.", "error")
        } else {
            showToast("User account is now ACTIVE.", "success")
        }
    }
}

// Converter Extensions for clean programmatic serialization to/from Firebase Map node representation
fun Map<String, Any?>.toTicketTier(): TicketTier {
    return TicketTier(
        name = this["name"] as? String ?: "",
        price = (this["price"] as? Number)?.toDouble() ?: 0.0,
        availableCount = (this["availableCount"] as? Number)?.toInt() ?: 0,
        soldCount = (this["soldCount"] as? Number)?.toInt() ?: 0,
        description = this["description"] as? String ?: "",
        badge = this["badge"] as? String
    )
}

fun TicketTier.toMap(): Map<String, Any?> {
    return mapOf(
        "name" to name,
        "price" to price,
        "availableCount" to availableCount,
        "soldCount" to soldCount,
        "description" to description,
        "badge" to badge
    )
}

fun Map<String, Any?>.toEventItem(): EventItem {
    val rawTiers = this["ticketTiers"]
    val tiers = when (rawTiers) {
        is List<*> -> {
            rawTiers.filterIsInstance<Map<String, Any?>>().map { it.toTicketTier() }
        }
        is Map<*, *> -> {
            rawTiers.values.filterIsInstance<Map<String, Any?>>().map { it.toTicketTier() }
        }
        else -> emptyList()
    }
    val statusStr = this["status"] as? String ?: "PENDING"
    val status = try { EventStatus.valueOf(statusStr) } catch(e: Exception) { EventStatus.PENDING }
    
    return EventItem(
        id = this["id"] as? String ?: "",
        title = this["title"] as? String ?: "",
        category = this["category"] as? String ?: "Nightlife",
        description = this["description"] as? String ?: "",
        imageUrl = this["imageUrl"] as? String,
        imageCategory = this["imageCategory"] as? String ?: "party",
        date = this["date"] as? String ?: "",
        time = this["time"] as? String ?: "",
        location = this["location"] as? String ?: "",
        hostName = this["hostName"] as? String ?: "",
        hostId = this["hostId"] as? String,
        ticketTiers = tiers,
        status = status,
        totalTickets = (this["totalTickets"] as? Number)?.toInt() ?: 0,
        ticketsSold = (this["ticketsSold"] as? Number)?.toInt() ?: 0,
        remainingTickets = (this["remainingTickets"] as? Number)?.toInt() ?: 0,
        revenue = (this["revenue"] as? Number)?.toDouble() ?: 0.0,
        createdAt = this["createdAt"],
        isHotSeller = this["isHotSeller"] as? Boolean ?: false,
        isSellingFast = this["isSellingFast"] as? Boolean ?: false,
        isFree = this["isFree"] as? Boolean ?: false
    )
}

fun EventItem.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "title" to title,
        "category" to category,
        "description" to description,
        "imageUrl" to imageUrl,
        "imageCategory" to imageCategory,
        "date" to date,
        "time" to time,
        "location" to location,
        "hostName" to hostName,
        "hostId" to hostId,
        "ticketTiers" to ticketTiers.map { it.toMap() },
        "status" to status.name,
        "totalTickets" to totalTickets,
        "ticketsSold" to ticketsSold,
        "remainingTickets" to remainingTickets,
        "revenue" to revenue,
        "createdAt" to (createdAt ?: com.google.firebase.firestore.FieldValue.serverTimestamp()),
        "isHotSeller" to isHotSeller,
        "isSellingFast" to isSellingFast,
        "isFree" to isFree
    )
}

fun Map<String, Any?>.toUserItem(): UserItem {
    return UserItem(
        id = this["id"] as? String ?: "",
        name = this["name"] as? String ?: "",
        email = this["email"] as? String ?: "",
        role = this["role"] as? String ?: "user",
        profileImage = this["profileImage"] as? String,
        status = this["status"] as? String ?: "active",
        createdAt = this["createdAt"],
        favoritedEventIds = (this["favoritedEventIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}

fun UserItem.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "role" to role,
        "profileImage" to profileImage,
        "status" to status,
        "createdAt" to (createdAt ?: com.google.firebase.firestore.FieldValue.serverTimestamp()),
        "favoritedEventIds" to favoritedEventIds
    )
}

fun Map<String, Any?>.toBookingItem(): BookingItem {
    return BookingItem(
        id = this["id"] as? String ?: "",
        bookingReference = this["bookingReference"] as? String ?: "",
        eventId = this["eventId"] as? String ?: "",
        eventTitle = this["eventTitle"] as? String ?: "",
        eventDate = this["eventDate"] as? String ?: "",
        eventVenue = this["eventVenue"] as? String ?: "",
        buyerId = this["buyerId"] as? String,
        buyerName = this["buyerName"] as? String ?: "",
        buyerEmail = this["buyerEmail"] as? String ?: "",
        hostId = this["hostId"] as? String,
        ticketCount = (this["ticketCount"] as? Number)?.toInt() ?: 1,
        ticketPrice = (this["ticketPrice"] as? Number)?.toDouble() ?: 0.0,
        totalPrice = (this["totalPrice"] as? Number)?.toDouble() ?: 0.0,
        bookingStatus = this["bookingStatus"] as? String ?: "confirmed",
        ticketQRCode = this["ticketQRCode"] as? String ?: "",
        createdAt = this["createdAt"]
    )
}

fun BookingItem.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "bookingReference" to bookingReference,
        "eventId" to eventId,
        "eventTitle" to eventTitle,
        "eventDate" to eventDate,
        "eventVenue" to eventVenue,
        "buyerId" to buyerId,
        "buyerName" to buyerName,
        "buyerEmail" to buyerEmail,
        "hostId" to hostId,
        "ticketCount" to ticketCount,
        "ticketPrice" to ticketPrice,
        "totalPrice" to totalPrice,
        "bookingStatus" to bookingStatus,
        "ticketQRCode" to ticketQRCode,
        "createdAt" to (createdAt ?: com.google.firebase.firestore.FieldValue.serverTimestamp())
    )
}
