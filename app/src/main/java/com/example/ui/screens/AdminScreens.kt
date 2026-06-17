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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
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
// 1. ADMIN AUTHORIZATION SECURE ACCESS
// ==========================================
@Composable
fun AdminLoginScreen(
    viewModel: OutsViewModel,
    onBack: () -> Unit
) {
    var keyphrase by remember { mutableStateOf("") }
    var pinPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberDevice by remember { mutableStateOf(true) }
    
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
                viewModel.signInWithGoogle(idToken, "admin",
                    onSuccess = { viewModel.navigateAdminTo("dashboard") },
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
            .background(Color(0xFFF1F5F9)) // Clean light gray/slate slate 100 background
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Back button + centered spacer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OUTS Logo in a custom white rounded card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .size(64.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    OutsLogo(
                        modifier = Modifier.size(width = 30.dp, height = 22.dp),
                        tint = BrandPrimary,
                        cutoutColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Admin Portal",
                color = Color(0xFF0F172A),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Secure access for platform administrators",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error Message Banner
            if (errorMsg != null) {
                Surface(
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFE11D48),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Centralized Login Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Work email
                    OutsTextField(
                        value = keyphrase,
                        onValueChange = { keyphrase = it },
                        label = "WORK EMAIL",
                        placeholder = "admin@outs.com",
                        leadingIcon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password custom row with Forgot?
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PASSWORD",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Forgot?",
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { errorMsg = "Credentials hint: admin@outs.app / admin123" }
                                .padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutsTextField(
                        value = pinPassword,
                        onValueChange = { pinPassword = it },
                        label = "",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Remember device checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberDevice = !rememberDevice }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = if (rememberDevice) BrandPrimary else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(if (rememberDevice) BrandPrimary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rememberDevice) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Remember this device for 30 days",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary login button
                    Button(
                        onClick = {
                            errorMsg = null
                            viewModel.loginUser(keyphrase, pinPassword, "admin",
                                onSuccess = { viewModel.navigateAdminTo("dashboard") },
                                onFailure = { errorMsg = it }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (viewModel.isAuthenticating) "SIGNING IN..." else "Sign In to Admin",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

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
                        borderColor = Color(0xFFE2E8F0),
                        contentColor = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Encryption Protocol badge matching mockup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "END-TO-END ENCRYPTED SESSION",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Systems audit / online check row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "• Systems Online",
                    color = Color(0xFF0D9488),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "• Support Connected",
                    color = Color(0xFF0D9488),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Footers
            Text(
                text = "OUTS ADMINISTRATION FRAMEWORK V4.2",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LOGIN", color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Divider(color = Color(0xFFCBD5E1), modifier = Modifier.size(width = 1.dp, height = 12.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("HELP", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Divider(color = Color(0xFFCBD5E1), modifier = Modifier.size(width = 1.dp, height = 12.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("REGION", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. ADMIN MAIN LAYOUT NAVIGATION CONTAINER
// ==========================================
@Composable
fun AdminMainDashboard(
    viewModel: OutsViewModel
) {
    var selectedTabIdx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIdx) {
                0 -> AdminUserManagementScreen(viewModel)
                1 -> AdminEventModerationScreen(viewModel)
                2 -> AdminRevenueOverviewScreen(viewModel)
                3 -> AdminPlatformSettingsScreen(viewModel)
            }
            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Switch Role
        FloatingRoleBackButton(
            onClick = { 
                viewModel.logoutUser()
                viewModel.navigateTo("role_chooser") 
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )

        // Web Admin Bottom tabs mimic
        OutsBottomNavigation(
            tabs = listOf("Users", "Moderation", "Revenue", "Settings"),
            icons = listOf(Icons.Default.People, Icons.Default.Warning, Icons.Default.AttachMoney, Icons.Default.Settings),
            selectedTabIndex = selectedTabIdx,
            onTabSelected = { idx ->
                selectedTabIdx = idx
                val tabsList = listOf("User Accounts Console", "Event Pending Moderation Queue", "Taxes & Platform Revenue", "System Diagnostics Settings")
                viewModel.showToast("Loading ${tabsList[idx]}...", "info")
            },
            badgeIndices = mapOf(1 to viewModel.events.count { it.status == EventStatus.PENDING }),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CountStatItem(label: String, count: String, modifier: Modifier = Modifier, color: Color = BrandPrimary) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), color = Color(0xFF8E909D), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

// ==========================================
// 2A. REVENUE & COMMISSION OVERVIEW PAGE
// ==========================================
@Composable
fun AdminRevenueOverviewScreen(
    viewModel: OutsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Platform System Stats", color = Color(0xFF8E909D), fontSize = 14.sp)
                Text("Revenue Overview", color = Color(0xFF13111C), fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }

        // Platform-wide Counts Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CountStatItem("Users", viewModel.users.size.toString(), Modifier.weight(1f))
                        CountStatItem("Hosts", viewModel.users.count { it.role == "host" }.toString(), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CountStatItem("Total Events", viewModel.events.size.toString(), Modifier.weight(1f))
                        CountStatItem("Pending", viewModel.events.count { it.status == EventStatus.PENDING }.toString(), Modifier.weight(1f), BrandAccent)
                    }
                }
            }
        }

        // Processing Vol. Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TOTAL GROSS VOLUME PROCESSED", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("£" + String.format("%.2f", viewModel.totalVolumeProcessed), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("COMMISSION NET EARNINGS (5%)", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("£" + String.format("%.2f", viewModel.platformEarnings), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("+8.2% MONTHLY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Daily Revenue status vertical columns drawing
        item {
            Text(
                "Daily Operating Status",
                fontSize = 18.sp,
                color = Color(0xFF13111C),
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                border = BorderStroke(1.dp, Color(0xFFECEFF1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Commission Streams by Host payouts", fontSize = 11.sp, color = Color(0xFF8C90A0), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Draw vertical column bars on Canvas
                    Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val w = size.width
                        val h = size.height
                        val barsCount = 7
                        val barWidth = 32.dp.toPx()
                        val spacing = (w - (barsCount * barWidth)) / (barsCount - 1)
                        val dataFractions = if (viewModel.bookings.isEmpty()) {
                            listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                        } else {
                            listOf(0.4f, 0.65f, 0.3f, 0.85f, 0.72f, 0.95f, 0.58f) // visualization logic placeholder
                        }

                        for (i in 0 until barsCount) {
                            val barHeight = h * dataFractions[i]
                            val x = i * (barWidth + spacing)
                            val y = h - barHeight

                            // Gradient brush column bar representation
                            drawRoundRect(
                                color = if (i == 5) BrandAccent else BrandPrimary,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEachIndexed { idx, day ->
                            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                                Text(day, fontSize = 9.sp, color = Color(0xFFB0BEC5), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Transaction History list
        item {
            Text(
                "Recent Transactions Audit",
                fontSize = 18.sp,
                color = Color(0xFF13111C),
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(viewModel.bookings) { tx ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFECEFF1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE7F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(tx.eventTitle, color = Color(0xFF13111C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${tx.buyerName}", color = Color(0xFF6E7489), fontSize = 11.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("£" + String.format("%.2f", tx.totalPrice), color = Color(0xFF13111C), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("+£" + String.format("%.2f", tx.totalPrice * 0.05) + " CUT", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==========================================
// 2B. USER PROFILES MANAGEMENT PAGE
// ==========================================
@Composable
fun AdminUserManagementScreen(
    viewModel: OutsViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    val roles = listOf("All", "Consumers", "Host", "Admin")

    val filteredUsers = viewModel.users.filter {
        val matchesRole = when (selectedRoleFilter) {
            "All" -> true
            "Consumers" -> it.role.equals("consumer", ignoreCase = true) || it.role.equals("user", ignoreCase = true)
            "Host" -> it.role.equals("host", ignoreCase = true)
            "Admin" -> it.role.equals("admin", ignoreCase = true)
            else -> true
        }
        matchesRole &&
        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text("Safety & Oversight", color = Color(0xFF8E909D), fontSize = 14.sp)
        Text("User Directory", color = Color(0xFF13111C), fontSize = 28.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(16.dp))

        OutsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search users by name or email...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutsTabs(
            tabs = roles,
            selectedTabIndex = roles.indexOf(selectedRoleFilter),
            onTabSelected = { selectedRoleFilter = roles[it] }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Table / Directory list mapping
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredUsers) { user ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFECEFF1)),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.name.take(1).uppercase(),
                                        color = BrandPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = user.name, 
                                        color = Color(0xFF13111C), 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.email, 
                                            color = Color(0xFF8E909D), 
                                            fontSize = 12.sp, 
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Standardized Role Tag
                                        val (tagBg, tagText) = when (user.role.lowercase()) {
                                            "admin" -> Color(0xFFEDE7F6) to Color(0xFF5E35B1)
                                            "host" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                            else -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
                                        }
                                        val displayRole = when(user.role.lowercase()) {
                                            "consumer", "user" -> "CONSUMER"
                                            else -> user.role.uppercase()
                                        }
                                        
                                        Surface(
                                            color = tagBg,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(20.dp)
                                        ) {
                                            Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = displayRole,
                                                    color = tagText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Dynamic status badge
                            OutsStatusIndicator(status = user.status)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFFF1F4F9))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Block / Unblock interactive choices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (user.status != "blocked") {
                                Button(
                                    onClick = { viewModel.updateUserStatus(user.id, "blocked") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("BLOCK USER", color = Color(0xFFC62828), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.updateUserStatus(user.id, "active") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("UNBLOCK USER", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2C. EVENTS MODERATION & AUTHENTICATORS PAGE
// ==========================================
@Composable
fun AdminEventModerationScreen(
    viewModel: OutsViewModel
) {
    val pendingEvents = viewModel.events.filter { it.status == EventStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text("Verification Center", color = Color(0xFF8E909D), fontSize = 14.sp)
        Text("Event Moderation", color = Color(0xFF13111C), fontSize = 28.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BrandAccent)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pendingEvents.size} PENDING ACTIONS",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingEvents.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder("All caught up! No active events pending validation.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingEvents) { event ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFECEFF1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))) {
                                    DecorativeEventHeader(category = event.category, title = "", imageUrl = event.imageUrl)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF13111C), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 180.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFEDE7F6)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(event.category.uppercase(), color = BrandPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("Hosted by " + event.hostName + " • " + event.date, fontSize = 11.sp, color = Color(0xFF6E7489))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(event.description, fontSize = 12.sp, color = Color(0xFF555B6F), maxLines = 2, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color(0xFFECEFF1))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive decision indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFEBEE))
                                        .clickable { viewModel.moderateEvent(event.id, false) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("REJECT EVENT", color = Color(0xFFC62828), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .clickable { viewModel.moderateEvent(event.id, true) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("APPROVE & GO LIVE", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2D. PLATFORM SETTINGS & PARAMS PAGE
// ==========================================
@Composable
fun CustomSneakerProfileAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(92.dp)
            .border(2.5.dp, Color(0xFF6366F1), CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color(0xFFEEF2F6)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(62.dp)) {
            val w = size.width
            val h = size.height

            // 1. Sole of sneaker
            val solePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.12f, h * 0.74f)
                quadraticTo(w * 0.5f, h * 0.77f, w * 0.88f, h * 0.64f)
                lineTo(w * 0.90f, h * 0.71f)
                quadraticTo(w * 0.5f, h * 0.83f, w * 0.10f, h * 0.77f)
                close()
            }
            drawPath(solePath, color = Color(0xFF312E81)) // Deep indigo sole

            // 2. Leather upper panel
            val upperPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.15f, h * 0.71f)
                cubicTo(w * 0.10f, h * 0.44f, w * 0.28f, h * 0.26f, w * 0.44f, h * 0.26f) // collar
                lineTo(w * 0.49f, h * 0.36f) // tongue
                cubicTo(w * 0.59f, h * 0.46f, w * 0.74f, h * 0.51f, w * 0.86f, h * 0.61f) // vamp & toe
                lineTo(w * 0.87f, h * 0.63f)
                close()
            }
            drawPath(upperPath, color = Color(0xFF6366F1)) // Classic OUTS Violet

            // 3. Iconic retro sports stripe
            val stripePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.26f, h * 0.57f)
                quadraticTo(w * 0.48f, h * 0.54f, w * 0.76f, h * 0.44f)
                lineTo(w * 0.74f, h * 0.49f)
                quadraticTo(w * 0.48f, h * 0.61f, w * 0.24f, h * 0.63f)
                close()
            }
            drawPath(stripePath, color = BrandAccent) // Vibrant neon purple/cyan stripe

            // 4. White laces crossed
            drawLine(
                color = Color.White,
                start = Offset(w * 0.44f, h * 0.40f),
                end = Offset(w * 0.53f, h * 0.43f),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.49f, h * 0.46f),
                end = Offset(w * 0.58f, h * 0.49f),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}

@Composable
fun AdminPlatformSettingsScreen(
    viewModel: OutsViewModel
) {
    var showRulesDialog by remember { mutableStateOf(false) }
    var showCommissionDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }

    var localCommission by remember { mutableStateOf("5.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft, pleasant light-gray surface
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Row with title centered & notification bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(40.dp)) // Center alignment weight spacer

            Text(
                text = "Admin Settings",
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )

            // Bell icon with cute notification dot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { showNotificationsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                // Red badge
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Profile Card Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar space
            Box(contentAlignment = Alignment.BottomEnd) {
                CustomSneakerProfileAvatar()
                // Online live green badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(3.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Alex Rivera",
                color = Color(0xFF0F172A),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SYSTEM ADMINISTRATOR",
                color = Color(0xFF6366F1), // Violet
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Senior Controller • ID: OUTS-9921",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 1. Section: PLATFORM CONFIGURATION
        Text(
            text = "PLATFORM CONFIGURATION",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column {
                // Platform Rules Gavel Option
                SettingsRowWithIcon(
                    title = "Platform Rules",
                    subtitle = "Governance & guidelines",
                    icon = Icons.Default.Gavel,
                    iconBgColor = Color(0xFFEEF2F6),
                    iconTint = Color(0xFF6366F1),
                    onClick = { showRulesDialog = true }
                )

                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                // Commission Rates Rate Option
                SettingsRowWithIcon(
                    title = "Commission Rates",
                    subtitle = "Financial fee structure",
                    icon = Icons.Default.AttachMoney,
                    iconBgColor = Color(0xFFEEF2F6),
                    iconTint = Color(0xFF22C55E),
                    hasActiveBadge = true,
                    onClick = { showCommissionDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Section: SECURITY & LOGS
        Text(
            text = "SECURITY & LOGS",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column {
                SettingsRowWithIcon(
                    title = "Notification Logs",
                    subtitle = "System-wide alert history",
                    icon = Icons.Default.QueryBuilder, // History representation
                    iconBgColor = Color(0xFFEEF2F6),
                    iconTint = Color(0xFF0EA5E9),
                    onClick = { showNotificationsDialog = true }
                )

                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))

                SettingsRowWithIcon(
                    title = "Security Audit",
                    subtitle = "Threat detection & access logs",
                    icon = Icons.Default.VerifiedUser,
                    iconBgColor = Color(0xFFEEF2F6),
                    iconTint = Color(0xFFF43F5E),
                    onClick = { showAuditDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Logout Session Button (Outlined red button matching mockup)
        Button(
            onClick = {
                viewModel.logoutUser()
                viewModel.navigateTo("role_chooser")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout Session",
                    color = Color(0xFFEF4444),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Version Watermark
        Text(
            text = "OUTS ADMIN V2.4.0 (BUILD 882)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 120.dp)
        )
    }

    // --- Interactive Dialogue Sheets ---

    // 1. Platform Rules (Firestore Code display) Dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, null, tint = Color(0xFF6366F1), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Platform Security Rules", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Active security policies enforced by the governance protocol:", color = Color(0xFF64748B), fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                    
                    // Code Terminal lookalike
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A), // Dark terminal
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "rules_version = '2';\n" +
                                        "\n" +
                                        "service cloud.firestore {\n" +
                                        "  match /databases/{database}/documents {\n" +
                                        "\n" +
                                        "    // ── 1. User Profiles ──\n" +
                                        "    match /users/{userId} {\n" +
                                        "      allow read: if request.auth != null;\n" +
                                        "      allow write: if request.auth != null && \n" +
                                        "                    request.auth.uid == userId;\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    // ── 2. Skill Providers ──\n" +
                                        "    match /skill_providers/{providerId} {\n" +
                                        "      allow read: if true;\n" +
                                        "      allow write: if request.auth != null;\n" +
                                        "      \n" +
                                        "      match /reviews/{reviewId} {\n" +
                                        "        allow read: if true;\n" +
                                        "        allow write: if request.auth != null;\n" +
                                        "      }\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    // ── 3. Bookings ──\n" +
                                        "    match /bookings/{bookingId} {\n" +
                                        "      allow read: if request.auth != null && \n" +
                                        "                   (resource.data.clientId == request.auth.uid || \n" +
                                        "                    resource.data.providerId == request.auth.uid);\n" +
                                        "      allow create: if request.auth != null && \n" +
                                        "                    request.resource.data.clientId == request.auth.uid;\n" +
                                        "      allow update: if request.auth != null && \n" +
                                        "                    (resource.data.clientId == request.auth.uid || \n" +
                                        "                     resource.data.providerId == request.auth.uid);\n" +
                                        "    }\n" +
                                        "\n" +
                                        "    // ── 4. Outsapp Lockdown Fallback ──\n" +
                                        "    match /{document=**} {\n" +
                                        "      allow read, write: if false;\n" +
                                        "    }\n" +
                                        "  }\n" +
                                        "}",
                                color = Color(0xFFA5B4FC), // Nice light indigo coding color
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("Close", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 2. Commission Dialog adjustment
    if (showCommissionDialog) {
        AlertDialog(
            onDismissRequest = { showCommissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF22C55E), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Platform Commission", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Configure the financial transaction fee split processed from guest ticket purchases:", color = Color(0xFF64748B), fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    OutsTextField(
                        value = localCommission,
                        onValueChange = { localCommission = it },
                        label = "Platform Service Rate (%)",
                        placeholder = "5.00"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("1.5%", "2.5%", "5.0%", "7.5%").forEach { valPercent ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { localCommission = valPercent.replace("%", "") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(valPercent, color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.platformEarnings = viewModel.totalVolumeProcessed * ((localCommission.toDoubleOrNull() ?: 5.0) / 100.0)
                        showCommissionDialog = false
                        viewModel.showToast("Commission rate updated to $localCommission%!", "success")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Apply & Update", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommissionDialog = false }) {
                    Text("Discard", color = Color(0xFF64748B))
                }
            }
        )
    }

    // 3. Notification Logs list Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notification Logs", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LogDetailItem("INFO", "Commission splits paid out to Host: Neon Dreams", "10 mins ago", Color(0xFF0EA5E9))
                    LogDetailItem("SUCCESS", "Platform rules successfully compiled and synchronized", "48 mins ago", Color(0xFF10B981))
                    LogDetailItem("WARN", "High-frequency ticket check attempts blocked for guest #90210", "2h ago", Color(0xFFF59E0B))
                    LogDetailItem("SUCCESS", "Administrative access granted to Sarah Chen", "5h ago", Color(0xFF10B981))
                    LogDetailItem("INFO", "SSL Certificate renewed with Auto-Renew", "1d ago", Color(0xFF0EA5E9))
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Dismiss", color = Color(0xFF64748B))
                }
            }
        )
    }

    // 4. Security Audit logs
    if (showAuditDialog) {
        AlertDialog(
            onDismissRequest = { showAuditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security Audit Logs", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LogDetailItem("AUTH", "Rivera session initiated on Google Pixel 8 Pro", "08:14 UTC", Color(0xFFF43F5E))
                    LogDetailItem("DB_WRITE", "Suspended profile list in RTDB synchronized", "07:22 UTC", Color(0xFF6366F1))
                    LogDetailItem("RULES_ENG", "Firestore Security Rules match verified: Default Deny Fallback Active", "06:05 UTC", Color(0xFFF43F5E))
                    LogDetailItem("IP_BLOCK", "Suspicious cross-origin API call from origin null rejected", "04:12 UTC", Color(0xFFEF4444))
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuditDialog = false }) {
                    Text("Close Terminal", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Help elements
@Composable
fun SettingsRowWithIcon(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    hasActiveBadge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }

        if (hasActiveBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7)) // soft green
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    color = Color(0xFF15803D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun LogDetailItem(
    type: String,
    message: String,
    time: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = type,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

