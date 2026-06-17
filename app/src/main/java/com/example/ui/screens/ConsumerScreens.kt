package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.state.EventItem
import com.example.ui.state.EventStatus
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.state.OutsViewModel
import com.example.ui.state.TicketTier
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.BrandBgLight
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.provider.MediaStore
import com.example.ui.utils.TicketUtils
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

// ==========================================
// 1. CONSUMER SIGN-IN / REGISTER SCREEN
// ==========================================
@Composable
fun ConsumerLoginScreen(
    viewModel: OutsViewModel,
    onBack: () -> Unit
) {
    var isRegistering by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val webClientId = context.getString(com.example.R.string.default_web_client_id)
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken, "consumer",
                    onSuccess = { viewModel.navigateConsumerTo("home") },
                    onFailure = { errorMsg = it }
                )
            } else {
                errorMsg = "Google Sign-In failed: No ID Token"
            }
        } catch (e: ApiException) {
            errorMsg = "Google Sign-In failed: ${e.localizedMessage}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0xFFF3F4F6), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to role chooser",
                    tint = Color(0xFF13111C)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "LIVE NOW",
                color = BrandPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isRegistering) "Create\nAccount" else "Join the\nScene",
                color = Color(0xFF13111C),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 46.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isRegistering) "Set up your profile to start exploring." else "Get your tickets and hit the floor.",
                color = Color(0xFF6E7489),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message Banner
            if (errorMsg != null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Name Field (Register mode only)
            if (isRegistering) {
                OutsTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Email Field
            OutsTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "yourname@domain.com",
                leadingIcon = Icons.Default.Mail
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Field
            OutsTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Color(0xFF9EA3B0)
                        )
                    }
                }
            )

            // Forgot password label
            if (!isRegistering) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "FORGOT?",
                        color = BrandAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.clickable { 
                            viewModel.resetPassword(email, 
                                onSuccess = { errorMsg = null },
                                onFailure = { errorMsg = it }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            OutsPrimaryButton(
                text = if (viewModel.isAuthenticating) "Processing..." else (if (isRegistering) "Register" else "Sign In"),
                icon = Icons.Default.ArrowForward,
                onClick = {
                    errorMsg = null
                    if (isRegistering) {
                        viewModel.registerUser(email, password, "consumer", name,
                            onSuccess = { viewModel.navigateConsumerTo("home") },
                            onFailure = { errorMsg = it }
                        )
                    } else {
                        viewModel.loginUser(email, password, "consumer",
                            onSuccess = { viewModel.navigateConsumerTo("home") },
                            onFailure = { errorMsg = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider Match Mockup
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
                Text(
                    text = "OR CONNECT VIA",
                    color = Color(0xFF9095A6),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    letterSpacing = 1.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFECEFF1))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google sign in
            OutsSecondaryButton(
                text = if (viewModel.isAuthenticating) "Processing..." else "Continue with Google",
                onClick = {
                    if (viewModel.isAuthenticating) return@OutsSecondaryButton
                    errorMsg = null
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                borderColor = Color(0xFFE2E6EF),
                contentColor = Color(0xFF263238),
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.AlternateEmail
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Apple Sign in
            OutsPrimaryButton(
                text = "Continue with Apple",
                onClick = {
                    errorMsg = null
                    viewModel.loginUser("hello@outs.app", "password", "consumer",
                        onSuccess = { viewModel.navigateConsumerTo("home") },
                        onFailure = { errorMsg = it }
                    )
                },
                backgroundColor = Color.Black,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PhoneIphone
            )

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegistering) "Already have an account? " else "Don't have an account? ",
                    color = Color(0xFF868A9A),
                    fontSize = 14.sp
                )
                Text(
                    text = if (isRegistering) "Sign In" else "Sign Up",
                    color = BrandAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { 
                        isRegistering = !isRegistering
                        errorMsg = null
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 2. CONSUMER HOME SCREEN
// ==========================================
@Composable
fun ConsumerHomeScreen(
    viewModel: OutsViewModel,
    onTabSelected: (Int) -> Unit
) {
    val consumerTabIndex by viewModel.consumerTabIndex.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Nightlife", "Fitness", "Culture", "Fashion", "Entertainment")

    // Get active events belonging to correct category (with custom newly approved events on top)
    val activeEvents = viewModel.events.filter {
        it.status == EventStatus.APPROVED &&
        (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
        (query.isEmpty() || it.title.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true))
    }.sortedByDescending { it.createdAt.toString() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBgLight)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Header with Location and profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { /* change location */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Place indicator",
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Locations",
                        color = Color(0xFF13111C),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle location menu",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Alert Icon
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { /* alerts */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alert notifications",
                            tint = Color(0xFF13111C),
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-3).dp, y = (3).dp)
                                .size(8.dp)
                                .background(BrandAccent, CircleShape)
                        )
                    }

                    // Avatar
                    OutsAvatar(
                        name = viewModel.currentUserProfileName,
                        size = 42.dp,
                        modifier = Modifier.clickable { viewModel.setConsumerTabIndex(3); viewModel.navigateConsumerTo("profile") }
                    )
                }
            }

            // Central search & filter
            OutsSearchBar(
                query = query,
                onQueryChange = { query = it },
                onFilterClick = { /* filter settings overlay */ },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Custom horizontally scrollable category slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScrollable(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) BrandPrimary else Color.White)
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E6EF), RoundedCornerShape(24.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (category == "Nightlife") Icon(Icons.Default.MusicNote, contentDescription = null, tint = if (isSelected) Color.White else BrandPrimary, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            if (category == "Fitness") Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = if (isSelected) Color.White else BrandPrimary, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            if (category == "Culture") Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (isSelected) Color.White else BrandPrimary, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                            
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else Color(0xFF555B6F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // LazyColumn scrolling elements
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Trending in London
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Featured Events",
                            color = Color(0xFF13111C),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "See all",
                            color = BrandPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* see all */ }
                        )
                    }
                }

                // Trending Grid/Cards - Dynamically feature events with > 10 sold or < 15 remaining
                val trending = activeEvents.filter { 
                    it.isHotSeller || it.ticketsSold > 10 || (it.remainingTickets > 0 && it.remainingTickets < 15)
                }
                if (trending.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(msg = "No trending events in this category yet!")
                    }
                } else {
                    items(trending) { item ->
                        OutsEventCard(
                            title = item.title,
                            category = item.category,
                            date = item.date,
                            location = item.location,
                            priceInfo = if (item.isFree) "Free" else "£" + String.format("%.2f", item.ticketTiers.minOfOrNull { it.price } ?: 25.0),
                            isHotSeller = item.isHotSeller,
                            imageUrl = item.imageUrl,
                            onClick = { viewModel.navigateConsumerTo("event_details", item.id) }
                        )
                    }
                }

                // Section 2: Upcoming Near You
                item {
                    Text(
                        text = "Upcoming Near You",
                        color = Color(0xFF13111C),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                val upcoming = activeEvents.filter { !it.isHotSeller }
                if (upcoming.isEmpty() && trending.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                } else if (upcoming.isEmpty() && trending.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(msg = "Click 'Host your own ->' to add events in Host mode")
                    }
                } else {
                    // Render custom double vertical grid items
                    items(upcoming.chunked(2)) { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(3.dp, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.navigateConsumerTo("event_details", item.id) },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column {
                                        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                            DecorativeEventHeader(category = item.category, title = item.title, imageUrl = item.imageUrl)
                                        }
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = item.date.uppercase(),
                                                color = BrandAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.title,
                                                color = Color(0xFF13111C),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (item.isFree) "Free" else "£" + String.format("%.0f", item.ticketTiers.firstOrNull()?.price ?: 15.0),
                                                color = Color(0xFF555B6F),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                // Add Create Event quick entry card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(BorderStroke(2.dp, Brush.linearGradient(listOf(BrandPrimary, BrandAccent))), RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.navigateTo("host")
                                            viewModel.navigateHostTo("login")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = "Host your own",
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Host your own",
                                            color = BrandPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "Create Event",
                                            color = BrandAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        // Floating Back to Platform Chooser option so the app never locks
        FloatingRoleBackButton(
            onClick = { viewModel.navigateTo("role_chooser") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )

        // Bottom Navigation
        OutsBottomNavigation(
            tabs = listOf("Home", "Explore", "My Tickets", "Profile"),
            icons = listOf(Icons.Default.Home, Icons.Default.Explore, Icons.Default.ConfirmationNumber, Icons.Default.Person),
            selectedTabIndex = consumerTabIndex,
            onTabSelected = { idx ->
                viewModel.setConsumerTabIndex(idx)
                if (idx == 0 || idx == 1) {
                    viewModel.navigateConsumerTo("home")
                } else if (idx == 2) {
                    viewModel.navigateConsumerTo("my_tickets")
                } else if (idx == 3) {
                    viewModel.navigateConsumerTo("profile")
                }
                onTabSelected(idx)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==========================================
// 3. CONSUMER EVENT DETAILS SCREEN
// ==========================================
@Composable
fun ConsumerEventDetailsScreen(
    viewModel: OutsViewModel,
    eventId: String
) {
    val context = LocalContext.current
    val event = viewModel.events.firstOrNull { it.id == eventId } ?: return
    var readMoreExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Elegant Cover Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                DecorativeEventHeader(category = event.category, title = event.title, imageUrl = event.imageUrl)
                
                // Overlay Top Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row {
                        val isFavorited = viewModel.favoritedEventIds.contains(event.id)
                        IconButton(
                            onClick = { viewModel.toggleFavorite(event.id) },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorited) Color.Red else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.showToast("Event flyers and info saved! Invitation link copied to clipboard.", "info") },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (event.isSellingFast) {
                    OutsBadge(
                        label = "SELLING FAST",
                        backgroundColor = BrandAccent,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            // Central card with details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "${event.category.uppercase()} / TECHNO",
                    color = BrandPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.title,
                    color = Color(0xFF13111C),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Rows matching custom styling
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    title = event.date,
                    subtitle = "Mark your calendar"
                )

                DetailRow(
                    icon = Icons.Default.Schedule,
                    title = event.time,
                    subtitle = "Doors open at 21:30"
                )

                DetailRow(
                    icon = Icons.Default.Place,
                    title = event.location,
                    subtitle = "Event Venue",
                    onArrowClick = { /* map redirection trigger */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Host row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F6FA)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutsAvatar(name = event.hostName, size = 44.dp, badgeColor = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ORGANIZED BY",
                                fontSize = 9.sp,
                                color = Color(0xFF8C90A0),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                event.hostName,
                                fontSize = 15.sp,
                                color = Color(0xFF13111C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutsSecondaryButton(
                            text = "Follow",
                            onClick = { /* follow host */ },
                            borderColor = BrandPrimary,
                            modifier = Modifier.height(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About details
                Text(
                    text = "About Event",
                    color = Color(0xFF13111C),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (readMoreExpanded) event.description else event.description.take(120) + "...",
                    color = Color(0xFF555B6F),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
                Text(
                    text = if (readMoreExpanded) "Read Less" else "Read More ▾",
                    color = BrandPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { readMoreExpanded = !readMoreExpanded }
                    )

                Spacer(modifier = Modifier.height(24.dp))

                // Custom elegant map layout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFFF1F3E9),
                            size = size,
                            cornerRadius = CornerRadius(24f, 24f)
                        )
                        // Vector line arrays representing simple street designs
                        drawLine(Color.White, Offset(0f, 40f), Offset(size.width, 140f), 10f)
                        drawLine(Color.White, Offset(0f, 180f), Offset(size.width, 100f), 8f)
                        drawLine(Color.White, Offset(120f, 0f), Offset(180f, size.height), 12f)
                        drawLine(Color.White, Offset(420f, 0f), Offset(380f, size.height), 8f)
                        // Pinpoint glow ripple center
                        drawCircle(BrandPrimary.copy(alpha = 0.2f), 30f, Offset(size.width * 0.45f, size.height * 0.5f))
                        drawCircle(BrandPrimary, 8f, Offset(size.width * 0.45f, size.height * 0.5f))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .clickable { 
                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(event.location)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(event.location)}"))
                                    context.startActivity(browserIntent)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View on Map", color = Color(0xFF13111C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Sticky Bottom Banner bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFECEFF1))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "FROM",
                        fontSize = 11.sp,
                        color = Color(0xFF8E909D),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (event.isFree) "Free" else "£" + String.format("%.2f", event.ticketTiers.minOfOrNull { it.price } ?: 25.0),
                        fontSize = 24.sp,
                        color = Color(0xFF13111C),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutsPrimaryButton(
                    text = "Get Tickets",
                    icon = Icons.Default.ConfirmationNumber,
                    onClick = { viewModel.navigateConsumerTo("tickets") },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

// Helper Row
@Composable
fun DetailRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onArrowClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable(enabled = onArrowClick != null) { onArrowClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                color = Color(0xFF13111C),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6E7489),
                fontWeight = FontWeight.Medium
            )
        }

        if (onArrowClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate detail map",
                tint = Color(0xFF9EA3B0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// 4. CONSUMER TICKET SELECTION SCREEN
// ==========================================
@Composable
fun ConsumerTicketSelectionScreen(
    viewModel: OutsViewModel
) {
    val event = viewModel.events.firstOrNull { it.id == viewModel.selectedEventId.value } ?: return
    
    // Manage quantity states locally mapped to ticket names
    val quantities = remember { mutableStateMapOf<String, Int>() }
    
    // Initialize standard quantities
    event.ticketTiers.forEach { tier ->
        if (quantities[tier.name] == null) {
            quantities[tier.name] = if (tier.availableCount > 0 && tier.price > 0 && tier.name.contains("VIP")) 1 else 0
        }
    }

    val totalSelected = quantities.values.sum()
    val totalPrice = event.ticketTiers.sumOf { (quantities[it.name] ?: 0) * it.price }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0912)) // Dark Cosmic Background Theme for tickets
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Select Tickets",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        event.title.take(18) + (if (event.title.length > 18) "..." else ""),
                        fontSize = 11.sp,
                        color = Color(0xFF9489A6),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { /* help tooltip */ },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "More information",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Event Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF161224),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))) {
                        DecorativeEventHeader(category = event.category, title = event.title, imageUrl = event.imageUrl)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "OUTS EXCLUSIVE",
                            fontSize = 9.sp,
                            color = BrandAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            event.title,
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${event.date} • ${event.time.substringBefore(" -")}",
                            fontSize = 12.sp,
                            color = Color(0xFF9093A2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "Available Tiers",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Render ticket selection rows
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(event.ticketTiers) { tier ->
                    val isSoldOut = tier.availableCount <= 0
                    val count = quantities[tier.name] ?: 0
                    val isSelected = count > 0

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF201735) else Color(0xFF13111C),
                        border = if (isSelected) BorderStroke(1.5.dp, BrandAccent) else BorderStroke(1.dp, Color(0xFF22202E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (tier.badge != null) {
                                            OutsBadge(
                                                label = tier.badge,
                                                backgroundColor = if (isSoldOut) Color(0xFF333333) else BrandAccent,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        }
                                        Text(
                                            tier.name,
                                            fontSize = 18.sp,
                                            color = if (isSoldOut) Color(0x80FFFFFF) else Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        tier.description,
                                        fontSize = 13.sp,
                                        color = if (isSoldOut) Color(0x50FFFFFF) else Color(0xFF8B8FA3),
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 18.sp
                                    )
                                }

                                // Interactive Counter mapping
                                if (!isSoldOut) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (count > 0) quantities[tier.name] = count - 1 },
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                                .size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = count.toString(),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        IconButton(
                                            onClick = { if (count < tier.availableCount) quantities[tier.name] = count + 1 },
                                            modifier = Modifier
                                                .background(BrandPrimary, CircleShape)
                                                .size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("UNAVAILABLE", color = Color(0xFF6F7483), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (isSelected && !isSoldOut && tier.availableCount <= 15) {
                                Text(
                                    "Only ${tier.availableCount} left!",
                                    color = BrandAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "£" + String.format("%.2f", tier.price),
                                fontSize = 20.sp,
                                color = if (isSoldOut) Color(0x60FFFFFF) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Checkout calculations
            Surface(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color.Transparent
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "ORDER TOTAL",
                                fontSize = 10.sp,
                                color = Color(0xFF6F727F),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Total: £" + String.format("%.2f", totalPrice),
                                fontSize = 22.sp,
                                color = BrandAccent,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "$totalSelected Ticket Selected\nExcl. booking fees",
                            fontSize = 12.sp,
                            color = BrandPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }

                    OutsPrimaryButton(
                        text = if (viewModel.isPurchasing) "Purchasing..." else "Proceed to Checkout",
                        icon = if (viewModel.isPurchasing) null else Icons.Default.ArrowForward,
                        enabled = totalSelected > 0 && !viewModel.isPurchasing,
                        onClick = {
                            // Buy action
                            val firstSelectedTier = quantities.filter { it.value > 0 }.keys.firstOrNull()
                            if (firstSelectedTier != null) {
                                val selectedQty = quantities[firstSelectedTier] ?: 1
                                viewModel.buyTicket(event.id, firstSelectedTier, selectedQty)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. CONSUMER PURCHASE SUCCESS SCREEN
// ==========================================
@Composable
fun ConsumerSuccessScreen(
    viewModel: OutsViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Elegant pulsing circle confirmation rings
            Box(contentAlignment = Alignment.Center) {
                // outer visual glowing ring
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(Color(0xFF00E676).copy(alpha = 0.08f), radius = size.minDimension / 2f)
                    drawCircle(Color(0xFF00E676).copy(alpha = 0.15f), radius = size.minDimension * 0.4f)
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success check",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "Success! Your ticket\nis ready",
                color = Color(0xFF13111C),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Get ready for an unforgettable\nexperience. Your order is confirmed.",
                color = Color(0xFF6E7489),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Confirmation order ticket Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))) {
                            DecorativeEventHeader(category = "Nightlife", title = "")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                viewModel.lastOrderEventTitle,
                                fontSize = 16.sp,
                                color = Color(0xFF13111C),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                viewModel.lastOrderLocation,
                                fontSize = 12.sp,
                                color = Color(0xFF8E909D),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFECEFF1))
                    Spacer(modifier = Modifier.height(16.dp))

                    ConfirmationRow(label = "Date & Time", value = viewModel.lastOrderDate)
                    ConfirmationRow(label = "Ticket Type", value = viewModel.lastOrderType)
                    ConfirmationRow(label = "Order ID", value = viewModel.lastOrderID)
                    ConfirmationRow(label = "Total Price", value = "£" + String.format("%.2f", viewModel.lastOrderPrice))

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFECEFF1))
                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code generator graphic using vectors
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // outer boundary
                            drawRoundRect(Color(0xFFE2E6EF), cornerRadius = CornerRadius(8f, 8f), style = Stroke(2f))
                            // Abstract QR blocks representation
                            val step = size.width / 10f
                            for (i in 0 until 10) {
                                for (j in 0 until 10) {
                                    if ((i + j) % 3 == 0 || (i * j) % 5 == 2 || (i == 0 && j < 3) || (j == 0 && i < 3) || (i == 9 && j > 6) || (j == 9 && i > 6)) {
                                        drawRect(
                                            color = Color(0xFF1A237E),
                                            topLeft = Offset(i * step + 4f, j * step + 4f),
                                            size = Size(maxOf(0f, step - 8f), maxOf(0f, step - 8f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invite your crew", color = Color(0xFF6E7489), fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = {}, modifier = Modifier.background(Color(0xFFEDE7F6), CircleShape).size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {}, modifier = Modifier.background(Color(0xFFFCE4EC), CircleShape).size(32.dp)) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandAccent)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // "View Ticket" Button
            OutsPrimaryButton(
                text = "View Ticket",
                icon = Icons.Default.QrCode,
                onClick = {
                    if (viewModel.lastBookingId.isNotEmpty()) {
                        viewModel.navigateConsumerTo("ticket_detail", bookingId = viewModel.lastBookingId)
                    } else {
                        viewModel.setConsumerTabIndex(2)
                        viewModel.navigateConsumerTo("my_tickets")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // "Back to Home" Button
            OutsSecondaryButton(
                text = "Back to Home",
                onClick = { viewModel.navigateConsumerTo("home") },
                borderColor = Color(0xFFE2E6EF),
                contentColor = Color(0xFF555B6F),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Confirmation helper
@Composable
fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF8F93A3), fontSize = 13.sp, fontWeight = FontWeight.Normal)
        Text(value, color = Color(0xFF13111C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ==========================================
// 6. CONSUMER PROFILE SCREEN
// ==========================================
@Composable
fun ConsumerProfileScreen(
    viewModel: OutsViewModel,
    onBackToHome: () -> Unit
) {
    val consumerTabIndex by viewModel.consumerTabIndex.collectAsStateWithLifecycle()
    var selectedTabBarIndex by remember(consumerTabIndex) {
        mutableStateOf(if (consumerTabIndex == 2) 0 else 1)
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(viewModel.currentUserProfileName) }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutsTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = "Full Name",
                        placeholder = "Enter your name"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUserProfile(editedName, (0..5).random())
                    showEditProfileDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val currentUid = viewModel.currentUserId ?: viewModel.auth?.currentUser?.uid
    
    // Explicitly reactive filtering - tracks any change to viewModel.bookings list
    val myBookings by remember(currentUid) {
        derivedStateOf {
            viewModel.bookings.filter { it.buyerId == currentUid }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBgLight)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Profile Top Header Icon Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        color = Color(0xFF13111C),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    IconButton(
                        onClick = { /* profile settings */ },
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Profile Settings",
                            tint = Color(0xFF13111C)
                        )
                    }
                }
            }

            // Profile info card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        OutsAvatar(name = viewModel.currentUserProfileName, size = 110.dp, badgeColor = null)
                        Box(
                            modifier = Modifier
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { /* edit profile avatar photo */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = viewModel.currentUserProfileName,
                        color = Color(0xFF13111C),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "@${viewModel.currentUserProfileName.lowercase().replace(" ", "_")}",
                        color = Color(0xFF8E909D),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutsPrimaryButton(
                            text = "Edit Profile",
                            onClick = { 
                                editedName = viewModel.currentUserProfileName
                                showEditProfileDialog = true 
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { /* share my profile qr code */ },
                            modifier = Modifier
                                .background(Color(0xFFE2E6EF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Profile QR",
                                tint = Color(0xFF13111C)
                            )
                        }
                    }
                }
            }

            // Scrollable tabs
            item {
                Spacer(modifier = Modifier.height(28.dp))
                OutsTabs(
                    tabs = listOf("My Tickets", "Payment History"),
                    selectedTabIndex = selectedTabBarIndex,
                    onTabSelected = { idx ->
                        selectedTabBarIndex = idx
                        viewModel.setConsumerTabIndex(if (idx == 0) 2 else 3)
                    },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedTabBarIndex == 0) {
                // Active Tickets Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Events",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF13111C)
                        )

                        val activeCount = myBookings.size
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE7F6))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "$activeCount Active",
                                color = BrandPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (myBookings.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(24.dp)) {
                            EmptyStatePlaceholder(msg = "No tickets purchased yet.")
                        }
                    }
                } else {
                    items(myBookings.take(2)) { tx ->
                        val event = viewModel.events.find { it.id == tx.eventId }
                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                            TicketPurchasedListItem(
                                title = tx.eventTitle,
                                date = event?.date ?: "Confirmed",
                                location = event?.location ?: "Outs Venue",
                                orderID = tx.id.replace("bk_", "#OUTS-").uppercase(),
                                imageUrl = event?.imageUrl,
                                category = event?.category ?: "Nightlife",
                                onViewTicket = { viewModel.navigateConsumerTo("ticket_detail", bookingId = tx.id) }
                            )
                        }
                    }
                    if (myBookings.size > 2) {
                        item {
                            Text(
                                text = "View All Tickets",
                                color = BrandPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().clickable { 
                                    viewModel.setConsumerTabIndex(2)
                                    viewModel.navigateConsumerTo("my_tickets")
                                }.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            } else {
                // Payment History Section
                item {
                    if (myBookings.isEmpty()) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            EmptyStatePlaceholder(msg = "No transaction history found.")
                        }
                    } else {
                        // Fixed height scrollable section for payments
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .heightIn(max = 320.dp), // Limit height to prevent stretching the whole page
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                myBookings.forEach { tx ->
                                    val event = viewModel.events.find { it.id == tx.eventId }
                                    TransactionHistoryRow(
                                        title = tx.eventTitle,
                                        date = event?.date ?: "Recently",
                                        amount = "£" + String.format("%.2f", tx.totalPrice),
                                        status = "SUCCESS"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Account settings Section
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Account Settings",
                        color = Color(0xFF13111C),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsOptionRow(icon = Icons.Default.Notifications, color = Color(0xFFE3F2FD), tint = Color(0xFF1E88E5), label = "Notifications")
                            SettingsOptionRow(icon = Icons.Default.Security, color = Color(0xFFEDE7F6), tint = Color(0xFF5E35B1), label = "Privacy & Security")
                            SettingsOptionRow(icon = Icons.Default.SupportAgent, color = Color(0xFFE0F2F1), tint = Color(0xFF00897B), label = "Help & Support")
                            SettingsOptionRow(icon = Icons.Default.Logout, color = Color(0xFFFFEBEE), tint = Color(0xFFE53935), label = "Logout", isLast = true, onClick = { 
                                viewModel.logoutUser()
                                viewModel.navigateTo("role_chooser") 
                            })
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "OUTS App Version 2.4.1 (Build 82)",
                    color = Color(0xFF9E9E9E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text(
                    "Back to Home",
                    color = BrandPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { onBackToHome() }.padding(vertical = 12.dp)
                )
            }
        }

        // Floating Back to Platform Chooser option so the app never locks
        FloatingRoleBackButton(
            onClick = { viewModel.navigateTo("role_chooser") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )

        // Bottom Navigation
        OutsBottomNavigation(
            tabs = listOf("Home", "Explore", "My Tickets", "Profile"),
            icons = listOf(Icons.Default.Home, Icons.Default.Explore, Icons.Default.ConfirmationNumber, Icons.Default.Person),
            selectedTabIndex = consumerTabIndex,
            onTabSelected = { idx ->
                viewModel.setConsumerTabIndex(idx)
                if (idx == 0 || idx == 1) {
                    viewModel.navigateConsumerTo("home")
                } else if (idx == 2) {
                    viewModel.navigateConsumerTo("my_tickets")
                } else if (idx == 3) {
                    viewModel.navigateConsumerTo("profile")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==========================================
// 8. CONSUMER TICKET DETAIL SCREEN
// ==========================================
@Composable
fun ConsumerTicketDetailScreen(
    viewModel: OutsViewModel,
    bookingId: String
) {
    val context = LocalContext.current
    
    // Simplest reactive lookup - directly find in the list
    val booking = viewModel.bookings.find { it.id == bookingId }
    
    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0C0912)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    val event = viewModel.events.find { it.id == booking.eventId }
    
    val qrContent = remember(booking) {
        if (booking.ticketQRCode.isNotEmpty()) {
            booking.ticketQRCode
        } else {
            "bookingId: ${booking.id}\neventId: ${booking.eventId}\nuserId: ${booking.buyerId}\ntickets: ${booking.ticketCount}"
        }
    }

    val qrBitmap = remember(qrContent) {
        TicketUtils.generateQRCode(qrContent, 512)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0912))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    "Electronic Ticket",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                IconButton(
                    onClick = { 
                        viewModel.showToast("Link copied to clipboard!", "success")
                    },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium Ticket Card with ticket shape styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Column {
                    // Top Event Banner
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        DecorativeEventHeader(
                            category = event?.category ?: "Nightlife",
                            title = booking.eventTitle,
                            imageUrl = event?.imageUrl
                        )
                        
                        // Status Badge overlay
                        Surface(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd),
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = booking.bookingStatus.uppercase(),
                                color = if (booking.bookingStatus == "confirmed") Color(0xFF00E676) else Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = booking.eventTitle,
                            fontSize = 26.sp,
                            color = Color(0xFF13111C),
                            fontWeight = FontWeight.Black,
                            lineHeight = 32.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TicketInfoItem(
                                label = "DATE",
                                value = booking.eventDate,
                                modifier = Modifier.weight(1.2f)
                            )
                            TicketInfoItem(
                                label = "ADMIT",
                                value = "${booking.ticketCount} Person${if(booking.ticketCount>1) "s" else ""}",
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketInfoItem(
                            label = "VENUE",
                            value = booking.eventVenue
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TicketInfoItem(
                                label = "REFERENCE",
                                value = booking.bookingReference.ifEmpty { booking.id.take(8).uppercase() },
                                modifier = Modifier.weight(1.2f)
                            )
                            TicketInfoItem(
                                label = "HOLDER",
                                value = booking.buyerName,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Perforation line with semi-circle cutouts simulation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                drawLine(
                                    color = Color(0xFFE2E6EF),
                                    start = Offset(0f, 0.5f),
                                    end = Offset(size.width, 0.5f),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = pathEffect
                                )
                            }
                            
                            // Left cutout
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(x = (-16).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0C0912))
                            )
                            
                            // Right cutout
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 16.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0C0912))
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // QR Code Section
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(horizontal = 20.dp),
                            color = Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFECEFF1))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize().padding(24.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    CircularProgressIndicator(color = BrandPrimary, strokeWidth = 3.dp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "SCAN THIS CODE AT ENTRANCE",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF9095A6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            OutsPrimaryButton(
                text = "Download PNG Ticket",
                icon = Icons.Default.Download,
                onClick = {
                    if (qrBitmap != null) {
                        viewModel.showToast("Saving ticket to gallery...", "info")
                        try {
                            val savedUri = MediaStore.Images.Media.insertImage(
                                context.contentResolver,
                                qrBitmap,
                                "OUTS-Ticket-${booking.bookingReference}",
                                "Ticket for ${booking.eventTitle}"
                            )
                            if (savedUri != null) {
                                viewModel.showToast("Saved! Check your photos. ✅", "success")
                            } else {
                                viewModel.showToast("Permission or storage error.", "error")
                            }
                        } catch (e: Exception) {
                            viewModel.showToast("Failed: ${e.message}", "error")
                        }
                    } else {
                        viewModel.showToast("Ticket not ready yet.", "error")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutsSecondaryButton(
                text = "Add to Apple/Google Wallet",
                icon = Icons.Default.Wallet,
                onClick = { viewModel.showToast("Pass integration coming soon!", "info") },
                borderColor = Color.White.copy(alpha = 0.15f),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TicketInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 10.sp,
            color = Color(0xFF8E909D),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF13111C),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Purchased Item Row
@Composable
fun TicketPurchasedListItem(
    title: String,
    date: String,
    location: String,
    orderID: String,
    imageUrl: String?,
    category: String = "Nightlife",
    onViewTicket: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().clickable { onViewTicket() },
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))) {
                DecorativeEventHeader(category = category, title = title, imageUrl = imageUrl)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(date, color = BrandAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(title, color = Color(0xFF13111C), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF8E909D), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(location, color = Color(0xFF6E7489), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            // QR active stub icon on right side matching screenshot
            IconButton(
                onClick = onViewTicket,
                modifier = Modifier
                    .background(Color(0xFFEDE7F6), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Show QR",
                    tint = BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Transaction item helper
@Composable
fun TransactionHistoryRow(title: String, date: String, amount: String, status: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color(0xFF13111C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(date, color = Color(0xFF8F93A3), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, color = Color(0xFF13111C), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(status, color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Option item helper
@Composable
fun SettingsOptionRow(
    icon: ImageVector,
    color: Color,
    tint: Color,
    label: String,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color(0xFF13111C),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9EA3B0),
            modifier = Modifier.size(16.dp)
        )
    }
    if (!isLast) {
        Divider(color = Color(0xFFECEFF1), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

// Custom HorizontalScroll container modifier helper
fun Modifier.horizontalScrollable(): Modifier = composed {
    this.horizontalScroll(rememberScrollState())
}

// Empty State helper
@Composable
fun EmptyStatePlaceholder(msg: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                msg,
                color = Color(0xFF6E7489),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Universal role back trigger
@Composable
fun FloatingRoleBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF0C0912))
            .border(1.dp, BrandPrimary.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SwapCalls, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Switch Roles (Demo Switcher)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 7. CONSUMER MY TICKETS DEDICATED SCREEN
// ==========================================
@Composable
fun ConsumerMyTicketsScreen(
    viewModel: OutsViewModel
) {
    val consumerTabIndex by viewModel.consumerTabIndex.collectAsStateWithLifecycle()
    val currentUid = viewModel.currentUserId ?: viewModel.auth?.currentUser?.uid
    
    // Explicitly reactive filtering - tracks any change to viewModel.bookings list
    val myBookings by remember(currentUid) {
        derivedStateOf {
            viewModel.bookings.filter { it.buyerId == currentUid }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBgLight)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tickets",
                    color = Color(0xFF13111C),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                
                IconButton(
                    onClick = { /* help */ },
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF13111C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (myBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    EmptyStatePlaceholder(msg = "No tickets purchased yet. Your upcoming events will appear here.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myBookings) { tx ->
                        val event = viewModel.events.find { it.id == tx.eventId }
                        TicketPurchasedListItem(
                            title = tx.eventTitle,
                            date = event?.date ?: "Confirmed",
                            location = event?.location ?: "Outs Venue",
                            orderID = tx.id.replace("bk_", "#OUTS-").uppercase(),
                            imageUrl = event?.imageUrl,
                            category = event?.category ?: "Nightlife",
                            onViewTicket = { viewModel.navigateConsumerTo("ticket_detail", bookingId = tx.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(110.dp)) }
                }
            }
        }

        // Floating Switch Role
        FloatingRoleBackButton(
            onClick = { viewModel.navigateTo("role_chooser") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )

        // Bottom Navigation
        OutsBottomNavigation(
            tabs = listOf("Home", "Explore", "My Tickets", "Profile"),
            icons = listOf(Icons.Default.Home, Icons.Default.Explore, Icons.Default.ConfirmationNumber, Icons.Default.Person),
            selectedTabIndex = consumerTabIndex,
            onTabSelected = { idx ->
                viewModel.setConsumerTabIndex(idx)
                if (idx == 0 || idx == 1) {
                    viewModel.navigateConsumerTo("home")
                } else if (idx == 2) {
                    viewModel.navigateConsumerTo("my_tickets")
                } else if (idx == 3) {
                    viewModel.navigateConsumerTo("profile")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
