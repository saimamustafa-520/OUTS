package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.components.OutsLogo

@Composable
fun RoleChooserScreen(
    onSelectConsumer: () -> Unit,
    onSelectHost: () -> Unit,
    onSelectAdmin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .textDarkBg()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        Column {
            // Header Row with Back Button & small OUTS Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* restart to splash */ },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutsLogo(
                        modifier = Modifier.size(width = 24.dp, height = 18.dp),
                        tint = BrandPrimary,
                        cutoutColor = Color(0xFF09070F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OUTS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Choose your role",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select how you'd like to experience the platform today.",
                color = Color(0xFF9EA3B0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CARD 1: CONSUMER - I want to discover events
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161225)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(24.dp))
                    .clickable(onClick = onSelectConsumer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Purple Compass Navigation Icon Container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = "Discover icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Badge "POPULAR" matching screenshot
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A1C50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "POPULAR",
                                color = BrandPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "I want to discover events",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Explore local happenings, buy tickets, and join the best communities around you.",
                        color = Color(0xFF9093AC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // "Continue as Consumer ->" Button inside card
                    Button(
                        onClick = onSelectConsumer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Continue as Consumer", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CARD 2: HOST - I want to host events
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E101D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3B1E38), RoundedCornerShape(24.dp))
                    .clickable(onClick = onSelectHost)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "Host stub icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "I want to host events",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create, manage, and promote your own listings. Access powerful hosting tools and analytics.",
                        color = Color(0xFFAC9AAD),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // "Continue as Host ->" Button inside card
                    Button(
                        onClick = onSelectHost,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Continue as Host", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CARD 3: Platform Management (Lock)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12141A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1F222B), RoundedCornerShape(16.dp))
                    .clickable(onClick = onSelectAdmin)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin lock",
                            tint = Color(0xFF9095A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Platform Management",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "For administrators and internal staff only.",
                            color = Color(0xFF6F7483),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = Color(0xFF6F7483),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Reach support
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need help? ",
                    color = Color(0xFF6F7483),
                    fontSize = 14.sp
                )
                Text(
                    text = "Contact Support",
                    color = BrandPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* help */ }
                )
            }
        }
    }
}

// Custom Extension to style Background elegantly
fun Modifier.textDarkBg(): Modifier {
    return this.background(Color(0xFF09070F))
}
