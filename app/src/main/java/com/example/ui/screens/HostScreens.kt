package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.state.*
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandBgLight
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

// ==========================================
// 1. HOST ACCESS/LOGIN SCREEN
// ==========================================
@Composable
fun HostLoginScreen(
    viewModel: OutsViewModel,
    onBack: () -> Unit
) {
    var isRegistering by remember { mutableStateOf(false) }
    var brandName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                viewModel.signInWithGoogle(idToken, "host",
                    onSuccess = { viewModel.navigateHostTo("dashboard") },
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
            .background(Color(0xFF0C0912)) // Luxury dark mode for B2B login
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
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(BrandAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OUTS PORTAL",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isRegistering) "Request Host\nAccess" else "Host Dashboard",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 46.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isRegistering) "Register your brand to list your events." else "Create listings, monitor payouts, and manage real-time queues.",
                color = Color(0xFF9489A6),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Error Message Banner
            if (errorMsg != null) {
                Surface(
                    color = Color(0xFF3E1F2A),
                    border = BorderStroke(1.dp, Color(0xFF8C3E52)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFFF8A9B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Credentials Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF161224),
                border = BorderStroke(1.dp, Color(0xFF2B224C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (isRegistering) {
                        OutsTextField(
                            value = brandName,
                            onValueChange = { brandName = it },
                            label = "Brand / Host Name",
                            placeholder = "Neon Dreams",
                            leadingIcon = Icons.Default.Business
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    OutsTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Business Email Address",
                        placeholder = "owner@brand.com",
                        leadingIcon = Icons.Default.Mail
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutsTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Dashboard Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OutsPrimaryButton(
                        text = if (viewModel.isAuthenticating) "Processing..." else (if (isRegistering) "Register Brand" else "Sign In to Portal"),
                        icon = Icons.Default.Login,
                        onClick = {
                            errorMsg = null
                            if (isRegistering) {
                                viewModel.registerUser(email, password, "host", brandName,
                                    onSuccess = { viewModel.navigateHostTo("dashboard") },
                                    onFailure = { errorMsg = it }
                                )
                            } else {
                                viewModel.loginUser(email, password, "host",
                                    onSuccess = { viewModel.navigateHostTo("dashboard") },
                                    onFailure = { errorMsg = it }
                                )
                            }
                        },
                        backgroundColor = BrandAccent,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutsSecondaryButton(
                        text = if (viewModel.isAuthenticating) "Processing..." else "Continue with Google",
                        icon = Icons.Default.AlternateEmail,
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
                        borderColor = Color(0xFF2B224C),
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegistering) "Already registered? " else "New Host Brand? ",
                    color = Color(0xFF9489A6),
                    fontSize = 14.sp
                )
                Text(
                    text = if (isRegistering) "Sign In" else "Request Access",
                    color = BrandAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { 
                        isRegistering = !isRegistering
                        errorMsg = null
                    }
                )
            }
        }
    }
}

// ==========================================
// 2. HOST MAIN LAYOUT & NAVIGATION WRAPPER
// ==========================================
@Composable
fun HostMainDashboard(
    viewModel: OutsViewModel
) {
    var selectedTabIdx by remember { mutableStateOf(0) }
    var editingEvent by remember { mutableStateOf<EventItem?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIdx) {
                0 -> HostDashboardOverview(viewModel, onProfileClick = { selectedTabIdx = 4 }, onCreateEventTrigger = { selectedTabIdx = 1 })
                1 -> HostCreateEventScreen(
                    viewModel = viewModel, 
                    editEvent = editingEvent,
                    onPublished = { 
                        selectedTabIdx = 2
                        editingEvent = null
                    }
                )
                2 -> HostManageEventsScreen(
                    viewModel = viewModel,
                    onEditEvent = { 
                        editingEvent = it
                        selectedTabIdx = 1
                    }
                )
                3 -> HostAnalyticsScreen(viewModel)
                4 -> HostProfileScreen(viewModel)
            }
            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Back to Model Chooser
        FloatingRoleBackButton(
            onClick = { 
                viewModel.logoutUser()
                viewModel.navigateTo("role_chooser") 
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )

        // Custom Host Bottom Nav
        OutsBottomNavigation(
            tabs = listOf("Dashboard", "New Event", "My Listings", "Analytics", "Profile"),
            icons = listOf(Icons.Default.Dashboard, Icons.Default.AddCircle, Icons.Default.ListAlt, Icons.Default.TrendingUp, Icons.Default.Person),
            selectedTabIndex = selectedTabIdx,
            onTabSelected = { idx ->
                selectedTabIdx = idx
                val tabsList = listOf("Host Dashboard", "Create Event Studio", "Event Listings", "Business Analytics", "Partner Profile")
                viewModel.showToast("Loading ${tabsList[idx]}...", "info")
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==========================================
// 2A. HOST DASHBOARD OVERVIEW SUB-SCREEN
// ==========================================
@Composable
fun HostDashboardOverview(
    viewModel: OutsViewModel,
    onProfileClick: () -> Unit,
    onCreateEventTrigger: () -> Unit
) {
    val currentUid = viewModel.currentUserId ?: viewModel.auth?.currentUser?.uid
    val myEvents = viewModel.events.filter { it.hostId == currentUid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning,",
                        color = Color(0xFF8E909D),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = viewModel.currentUserProfileName,
                        color = Color(0xFF13111C),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                OutsAvatar(
                    name = viewModel.currentUserProfileName,
                    size = 48.dp,
                    badgeColor = Color(0xFF00E676),
                    modifier = Modifier.clickable { onProfileClick() }
                )
            }
        }

        // Payout Financial Metric Rows
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TOTAL REVENUE",
                            color = Color(0xFF8E909D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "£" + String.format("%.2f", viewModel.hostRevenueTotal),
                            color = Color(0xFF13111C),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    VerticalDivider(color = Color(0xFFECEFF1), modifier = Modifier.height(44.dp).padding(horizontal = 16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TICKETS SOLD",
                            color = Color(0xFF8E909D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${viewModel.hostTicketsSold} stubs",
                            color = BrandPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    VerticalDivider(color = Color(0xFFECEFF1), modifier = Modifier.height(44.dp).padding(horizontal = 16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TOTAL EVENTS",
                            color = Color(0xFF8E909D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${myEvents.size}",
                            color = BrandAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Sales Overview curve vector design matching requirement
        item {
            Text(
                "Sales Overview",
                fontSize = 18.sp,
                color = Color(0xFF13111C),
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                border = BorderStroke(1.dp, Color(0xFFECEFF1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weekly Performance", fontSize = 12.sp, color = Color(0xFF8E909D), fontWeight = FontWeight.Bold)
                        Text("+12.4% vs last week", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw abstract line graph on canvas
                    Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val w = size.width
                        val h = size.height

                        // Grid lanes
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), 1.5f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1.5f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), 1.5f)

                        // Path trace
                        val linePath = Path().apply {
                            moveTo(0f, h * 0.82f)
                            cubicTo(w * 0.2f, h * 0.75f, w * 0.35f, h * 0.25f, w * 0.5f, h * 0.35f)
                            cubicTo(w * 0.65f, h * 0.45f, w * 0.8f, h * 0.05f, w, h * 0.15f)
                        }

                        // Gradient shadow
                        val shadowPath = Path().apply {
                            addPath(linePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = shadowPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(BrandPrimary.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = linePath,
                            color = BrandPrimary,
                            style = Stroke(width = 6f)
                        )

                        // Marker dots
                        drawCircle(BrandAccent, 6f, Offset(w * 0.5f, h * 0.35f))
                        drawCircle(Color.White, 3f, Offset(w * 0.5f, h * 0.35f))
                        
                        drawCircle(BrandAccent, 6f, Offset(w, h * 0.15f))
                        drawCircle(Color.White, 3f, Offset(w, h * 0.15f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach {
                            Text(it, fontSize = 9.sp, color = Color(0xFFB0BEC5), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Listings List mapping
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Active Listings",
                    fontSize = 18.sp,
                    color = Color(0xFF13111C),
                    fontWeight = FontWeight.Black
                )
                Text(
                    "View all",
                    color = BrandPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* redirect */ }
                )
            }
        }

        if (myEvents.isEmpty()) {
            item {
                EmptyStatePlaceholder("You haven't submitted any listings under '${viewModel.currentUserProfileName}' yet! Click 'New Event' below.")
            }
        } else {
            items(myEvents) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().height(88.dp),
                    border = BorderStroke(1.dp, Color(0xFFECEFF1))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))) {
                            DecorativeEventHeader(category = item.category, title = "", imageUrl = item.imageUrl)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    color = Color(0xFF13111C),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutsStatusIndicator(status = item.status.name)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, tint = Color(0xFF8E909D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.location,
                                    fontSize = 11.sp,
                                    color = Color(0xFF6E7489),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            val soldPrcent = if (item.totalTickets > 0) (item.ticketsSold * 100 / item.totalTickets) else 0
                            Text(
                                "$soldPrcent%",
                                color = BrandAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (item.totalTickets > 0) item.ticketsSold.toFloat() / item.totalTickets.toFloat() else 0f },
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = BrandPrimary,
                                trackColor = Color(0xFFEEEEEE)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2B. HOST CREATE EVENT FORM SUB-SCREEN (Interactive model workflow)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostCreateEventScreen(
    viewModel: OutsViewModel,
    editEvent: EventItem? = null,
    onPublished: () -> Unit
) {
    var title by remember(editEvent) { mutableStateOf(editEvent?.title ?: "") }
    var category by remember(editEvent) { mutableStateOf(editEvent?.category ?: "Nightlife") }
    var description by remember(editEvent) { mutableStateOf(editEvent?.description ?: "") }
    var location by remember(editEvent) { mutableStateOf(editEvent?.location ?: "") }
    var dateString by remember(editEvent) { mutableStateOf(editEvent?.date ?: "Friday, 18 September 2026") }
    var timeString by remember(editEvent) { mutableStateOf(editEvent?.time ?: "11:30 PM - 05:00 AM") }

    var uploadedImageUrl by remember(editEvent) { mutableStateOf(editEvent?.imageUrl) }
    var isUploading by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                        val startStr = sdf.format(java.util.Date(start))
                        val endStr = sdf.format(java.util.Date(end))
                        dateString = "$startStr - $endStr ${java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date(end))}"
                    } else if (start != null) {
                        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
                        dateString = sdf.format(java.util.Date(start))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Select Event Duration", modifier = Modifier.padding(16.dp)) },
                headline = { 
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        Text("${java.text.SimpleDateFormat("MMM dd").format(java.util.Date(start))} - ${java.text.SimpleDateFormat("MMM dd").format(java.util.Date(end))}", modifier = Modifier.padding(16.dp))
                    } else {
                        Text("Select range", modifier = Modifier.padding(16.dp))
                    }
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isUploading = true
            viewModel.uploadEventImage(uri,
                onSuccess = { url ->
                    uploadedImageUrl = url
                    isUploading = false
                    viewModel.showToast("Image uploaded successfully!", "success")
                },
                onFailure = { err ->
                    isUploading = false
                    viewModel.showToast(err, "error")
                }
            )
        }
    }

    // Simplification for editing tiers
    var selectedTierName by remember(editEvent) { mutableStateOf(editEvent?.ticketTiers?.firstOrNull()?.name ?: "General Admission") }
    var selectedTierPrice by remember(editEvent) { mutableStateOf(editEvent?.ticketTiers?.firstOrNull()?.price?.toString() ?: "25.00") }
    var selectedTierQty by remember(editEvent) { mutableStateOf(editEvent?.totalTickets?.toString() ?: "150") }

    // Use categories directly from ViewModel for reactivity
    val options = if (viewModel.categories.isEmpty()) {
        listOf("Nightlife", "Fitness", "Culture", "Fashion", "Entertainment")
    } else {
        viewModel.categories.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Create Event",
            color = Color(0xFF13111C),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Draft your proposal. New submissions are pending admin authentication.",
            color = Color(0xFF6E7489),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Visual Cover Upload Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFECEFF1))
                .border(2.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (uploadedImageUrl != null) {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(uploadedImageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else if (isUploading) {
                CircularProgressIndicator(color = BrandPrimary)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF546E7A), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select Covers & Flyers", color = Color(0xFF37474F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Supports PNG / JPG high-res up to 5MB", color = Color(0xFF78909C), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutsTextField(
            value = title,
            onValueChange = { title = it },
            label = "Event Title",
            placeholder = "e.g. Neon Nights Summer Festival"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutsDropdown(
            label = "Category Type",
            selectedOption = category,
            options = options,
            onOptionSelected = { category = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutsTextField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            placeholder = "Tell your audience what to expect..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutsTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = "Date Info",
                    placeholder = "Select Date",
                    readOnly = true,
                    leadingIcon = Icons.Default.CalendarToday
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true })
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutsTextField(
                value = timeString,
                onValueChange = { timeString = it },
                label = "Timeslot",
                placeholder = "22:00 - 04:00",
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Default.Schedule
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Info
        OutsTextField(
            value = location,
            onValueChange = { location = it },
            label = "Venue Location",
            placeholder = "e.g. Warehouse Block 7, London"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ticket Constructor
        Text(
            "Ticket Tiers setup",
            fontSize = 18.sp,
            color = Color(0xFF13111C),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFECEFF1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutsTextField(
                    value = selectedTierName,
                    onValueChange = { selectedTierName = it },
                    label = "Tier Identifier",
                    placeholder = "e.g. VIP Pass"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    OutsTextField(
                        value = selectedTierPrice,
                        onValueChange = { selectedTierPrice = it },
                        label = "Price (£)",
                        placeholder = "25.00",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutsTextField(
                        value = selectedTierQty,
                        onValueChange = { selectedTierQty = it },
                        label = "Capacity",
                        placeholder = "150",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Publish action
        OutsPrimaryButton(
            text = if (editEvent != null) "Update Event" else "Publish Event Proposal",
            icon = Icons.Default.CloudUpload,
            enabled = !isUploading,
            onClick = {
                val tierPrice = selectedTierPrice.toDoubleOrNull()
                val tierQty = selectedTierQty.toIntOrNull()

                if (tierPrice == null || tierQty == null) {
                    viewModel.showToast("Please enter valid numbers for price and capacity.", "error")
                    return@OutsPrimaryButton
                }

                if (tierPrice < 0 || tierQty < 0) {
                    viewModel.showToast("Price and capacity cannot be negative.", "error")
                    return@OutsPrimaryButton
                }
                
                if (editEvent != null) {
                    val updated = editEvent.copy(
                        title = title,
                        category = category,
                        description = description,
                        date = dateString,
                        time = timeString,
                        location = location,
                        totalTickets = tierQty,
                        ticketTiers = listOf(TicketTier(selectedTierName, tierPrice, tierQty - editEvent.ticketsSold, editEvent.ticketsSold, "Capacity updated")),
                        imageUrl = uploadedImageUrl,
                        remainingTickets = tierQty - editEvent.ticketsSold
                    )
                    viewModel.updateEvent(updated)
                } else {
                    viewModel.addNewEvent(
                        title = title,
                        category = category,
                        description = description,
                        date = dateString,
                        time = timeString,
                        location = location,
                        totalTickets = tierQty,
                        ticketTiers = listOf(
                            TicketTier(
                                name = selectedTierName,
                                price = tierPrice,
                                availableCount = tierQty,
                                description = "Full entry to $category experience listing"
                            )
                        ),
                        imageUrl = uploadedImageUrl
                    )
                }
                onPublished()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

// ==========================================
// 2C. HOST MANAGE EVENTS SUB-SCREEN
// ==========================================
@Composable
fun HostManageEventsScreen(
    viewModel: OutsViewModel,
    onEditEvent: (EventItem) -> Unit
) {
    var filterTabIdx by remember { mutableStateOf(0) }
    val currentUid = viewModel.currentUserId ?: viewModel.auth?.currentUser?.uid
    val myEvents = viewModel.events.filter { it.hostId == currentUid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "My Listings",
            color = Color(0xFF13111C),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutsTabs(
            tabs = listOf("All", "Active", "Drafts/Pending/Rejected", "Past"),
            selectedTabIndex = filterTabIdx,
            onTabSelected = { filterTabIdx = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredList = when (filterTabIdx) {
            1 -> myEvents.filter { it.status == EventStatus.APPROVED }
            2 -> myEvents.filter { it.status == EventStatus.PENDING || it.status == EventStatus.REJECTED }
            3 -> myEvents.filter { it.status == EventStatus.SOLD_OUT }
            else -> myEvents
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder("Empty state. No events match this category.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList) { event ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFECEFF1)),
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    OutsStatusIndicator(status = event.status.name)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        event.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF13111C),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onEditEvent(event) }, modifier = Modifier.size(36.dp).background(Color(0xFFF5F6FA), CircleShape)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.deleteEvent(event.id) }, modifier = Modifier.size(36.dp).background(Color(0xFFFFF1F2), CircleShape)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color(0xFFF1F4F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AnalyticsSmallStat(label = "TICKETS", value = "${event.ticketsSold}/${event.totalTickets}")
                                AnalyticsSmallStat(label = "REMAINING", value = "${event.remainingTickets}")
                                AnalyticsSmallStat(label = "REVENUE", value = "£${String.format("%.0f", event.revenue)}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = event.location, 
                                    fontSize = 12.sp, 
                                    color = Color(0xFF7A7F8E), 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSmallStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, color = Color(0xFF8E909D), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, fontSize = 14.sp, color = Color(0xFF13111C), fontWeight = FontWeight.Black)
    }
}

// ==========================================
// 2D. HOST ANALYTICS CHART FLOW SUB-SCREEN
// ==========================================
@Composable
fun HostAnalyticsScreen(
    viewModel: OutsViewModel
) {
    var periodTabIdx by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Analytics Overview",
            color = Color(0xFF13111C),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutsTabs(
            tabs = listOf("Day", "Week", "Month", "Year"),
            selectedTabIndex = periodTabIdx,
            onTabSelected = { periodTabIdx = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large total highlight stats
        Text("ESTIMATED GROSS REVENUE", color = Color(0xFF8F93A3), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text("£" + String.format("%.2f", viewModel.hostRevenueTotal), color = Color(0xFF13111C), fontSize = 36.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(20.dp))

        // Breakdown distribution visual bars
        val currentUid = viewModel.currentUserId ?: viewModel.auth?.currentUser?.uid
    val myEvents = viewModel.events.filter { it.hostId == currentUid }
        val myEventTitles = myEvents.map { it.title.lowercase() }
        val myBookings = viewModel.bookings.filter { it.eventTitle.lowercase() in myEventTitles }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val total = viewModel.hostRevenueTotal
            val salesPercent = if (total > 0) "100%" else "0%"
            MetricProgressOverviewItem(label = "Ticket Sales", percent = salesPercent, fraction = if (total > 0) 1f else 0f, color = BrandPrimary, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top performing layouts
        Text(
            "Top Curation Listings",
            fontSize = 18.sp,
            color = Color(0xFF13111C),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        myEvents.sortedByDescending { ev -> 
            myBookings.filter { it.eventId == ev.id }.sumOf { it.totalPrice }
        }.take(3).forEachIndexed { index, event ->
            val revenue = myBookings.filter { it.eventId == event.id }.sumOf { it.totalPrice }
            val count = myBookings.filter { it.eventId == event.id }.sumOf { it.ticketCount }
            TopPerformanceListingItem(
                rank = index + 1,
                title = event.title,
                ticketsCount = count,
                revenue = "£" + String.format("%.0f", revenue)
            )
        }
        
        Spacer(modifier = Modifier.height(110.dp))
    }
}

// Helper analytics row
@Composable
fun MetricProgressOverviewItem(
    label: String,
    percent: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF1)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color(0xFF7A7F8E), fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(percent, fontSize = 18.sp, color = Color(0xFF13111C), fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = Color(0xFFEEEEEE)
            )
        }
    }
}

// rank item listing helper
@Composable
fun TopPerformanceListingItem(
    rank: Int,
    title: String,
    ticketsCount: Int,
    revenue: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (rank == 1) Color(0xFFFFD54F) else Color(0xFFECEFF1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = if (rank == 1) Color.Black else Color.Black.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 13.sp,
                    color = Color(0xFF13111C),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$ticketsCount tickets sold",
                    fontSize = 11.sp,
                    color = Color(0xFF7A7F8E)
                )
            }

            Text(
                revenue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary
            )
        }
    }
}

// ==========================================
// 2E. HOST PROFILE SCREEN
// ==========================================
@Composable
fun HostProfileScreen(
    viewModel: OutsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Partner Profile",
                color = Color(0xFF13111C),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Profile Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFECEFF1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutsAvatar(name = viewModel.currentUserProfileName, size = 96.dp, badgeColor = Color(0xFF00E676))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.currentUserProfileName,
                    color = Color(0xFF13111C),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Text(
                        text = "VERIFIED PARTNER",
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFECEFF1))
                Spacer(modifier = Modifier.height(20.dp))

                // Metadata list
                ProfileDetailRow(label = "Business Email", value = viewModel.currentUserEmail ?: "host@outs.app")
                ProfileDetailRow(label = "Account Tier", value = "Enterprise Host")
                ProfileDetailRow(label = "Platform Region", value = "United Kingdom")
                ProfileDetailRow(label = "Payout Cycle", value = "Instant (Auto-draw)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Platform Switch Settings
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFECEFF1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsNavigationRow(icon = Icons.Default.Business, title = "Brand Guidelines", subtitle = "Logo, typography & visual curation assets")
                SettingsNavigationRow(icon = Icons.Default.VerifiedUser, title = "Security Credentials", subtitle = "Manage API keys & webhooks")
                SettingsNavigationRow(icon = Icons.Default.Notifications, title = "Notification Rules", subtitle = "Configure real-time SMS & web ticket updates")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Premium RED Logout / Switch Button
        Button(
            onClick = {
                viewModel.logoutUser()
                viewModel.navigateTo("role_chooser")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Sign Out of Portal",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF8E909D),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color(0xFF13111C),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* mock setting action */ }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrandPrimary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = BrandPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF13111C))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF8E909D))
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Go", tint = Color(0xFFB0BEC5), modifier = Modifier.size(20.dp))
    }
}
