package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary

// 1. PRIMARY BUTTON (Filled)
@Composable
fun OutsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandPrimary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .shadow(
                elevation = if (enabled) 4.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = backgroundColor.copy(alpha = 0.4f),
                spotColor = backgroundColor.copy(alpha = 0.8f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) backgroundColor else Color(0xFFE2E0EC),
        contentColor = if (enabled) contentColor else Color(0xFF908D9C)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

// 2. SECONDARY BUTTON (Outlined)
@Composable
fun OutsSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = BrandPrimary,
    contentColor: Color = BrandPrimary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, if (enabled) borderColor else Color(0xFFE2E0EC)),
        contentColor = if (enabled) contentColor else Color(0xFF908D9C)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

// 3. ICON BUTTON
@Composable
fun OutsIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Action Button",
    containerColor: Color = Color.White.copy(alpha = 0.15f),
    contentColor: Color = Color.White,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (4).dp)
                    .size(18.dp)
                    .background(BrandAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 4. INPUT FIELD (Default, Focused, Error statuses)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = TextStyle(
                    color = if (isError) BrandAccent else if (isFocused) BrandPrimary else Color(0xFF6B6681),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9EA3B0), fontSize = 15.sp) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = if (isFocused) BrandPrimary else Color(0xFF6B6681)) }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            readOnly = readOnly,
            enabled = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(14.dp),
            textStyle = TextStyle(
                color = Color(0xFF121118),
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF121118),
                unfocusedTextColor = Color(0xFF121118),
                errorTextColor = Color(0xFF121118),
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = Color(0xFFE2E6EF),
                errorBorderColor = BrandAccent,
                focusedContainerColor = Color(0xFFFBFBFE),
                unfocusedContainerColor = Color(0xFFF5F6FA),
                errorContainerColor = Color(0xFFFFF5F7),
                disabledBorderColor = Color(0xFFE2E6EF),
                disabledContainerColor = Color(0xFFF5F6FA),
                disabledTextColor = Color(0xFF121118)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isFocused) {
                        // Subtle focused glow
                    }
                }
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = BrandAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// 5. DROPDOWN (Reusable component)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutsDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = TextStyle(
                    color = BrandPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select category", color = Color(0xFF9EA3B0), fontSize = 15.sp) },
                leadingIcon = { Icon(Icons.Default.Category, null, tint = BrandPrimary) },
                trailingIcon = { 
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                textStyle = TextStyle(color = Color(0xFF121118), fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = Color(0xFFE2E6EF),
                    focusedContainerColor = Color(0xFFFBFBFE),
                    unfocusedContainerColor = Color(0xFFF5F6FA)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Full-size clickable overlay to trigger dropdown
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.White)
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No categories available", color = Color.Gray) },
                        onClick = { expanded = false }
                    )
                } else {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = option, 
                                    color = Color(0xFF121118),
                                    fontWeight = FontWeight.Medium 
                                ) 
                            },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

// 6. SEARCH BAR
@Composable
fun OutsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search events, artists, or venues...",
    onFilterClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF2F3F8))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = Color(0xFF969AAC)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = Color(0xFF1E1F24),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF9095A6),
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (onFilterClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandPrimary)
                    .clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter events",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// 7. BADGE (VIP, Early Bird, Sold Out)
@Composable
fun OutsBadge(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandAccent,
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

// 8. GRAPHICAL GENERATOR CARD (To satisfy high-quality illustrations)
@Composable
fun DecorativeEventHeader(
    category: String,
    title: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!imageUrl.isNullOrEmpty()) {
            var isImageError by remember(imageUrl) { mutableStateOf(false) }
            val painter = coil.compose.rememberAsyncImagePainter(
                model = imageUrl,
                onError = { isImageError = true }
            )
            
            if (!isImageError) {
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Translucent premium dark gradient overlay for visual legibility of overlays and details
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )
            } else {
                DecorativeEventVectorCanvas(category = category)
            }
        } else {
            DecorativeEventVectorCanvas(category = category)
        }
    }
}

@Composable
fun DecorativeEventVectorCanvas(category: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val brush = when (category.lowercase()) {
            "nightlife" -> Brush.linearGradient(
                colors = listOf(Color(0xFF1C0D43), Color(0xFF6C3FE4), Color(0xFFFF3366)),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
            "fitness" -> Brush.radialGradient(
                colors = listOf(Color(0xFFFF85A2), Color(0xFFFF3366), Color(0xFF6C3FE4)),
                center = Offset(width * 0.8f, height * 0.2f),
                radius = width * 0.7f
            )
            "culture" -> Brush.sweepGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF6C3FE4), Color(0xFF0D9488)),
                center = Offset(width * 0.5f, height * 0.5f)
            )
            else -> Brush.linearGradient(
                colors = listOf(Color(0xFF6C3FE4), Color(0xFFFF3366)),
                start = Offset(0f, height),
                end = Offset(width, 0f)
            )
        }

        drawRoundRect(
            brush = brush,
            size = Size(width, height),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Draw abstract aesthetic structures
        val path = Path()
        if (category.lowercase() == "nightlife" || category.lowercase() == "underground") {
            // Neon triangles and strobe representations
            path.moveTo(width * 0.2f, height)
            path.lineTo(width * 0.5f, height * 0.4f)
            path.lineTo(width * 0.8f, height)
            path.close()
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.08f)
            )
            drawCircle(
                color = Color(0xFFFF3366).copy(alpha = 0.6f),
                radius = 60f,
                center = Offset(width * 0.5f, height * 0.45f)
            )
            // Glowing lines
            drawLine(
                color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                start = Offset(0f, height * 0.1f),
                end = Offset(width, height * 0.9f),
                strokeWidth = 6f
            )
        } else if (category.lowercase() == "fitness" || category.lowercase() == "yoga") {
            // Sun radiating soft curves
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = 120f,
                center = Offset(width * 0.5f, height * 0.3f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.11f),
                radius = 200f,
                center = Offset(width * 0.5f, height * 0.3f)
            )
        } else {
            // Grid flow representational lines
            for (i in 0..10) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, height * (i / 10f)),
                    end = Offset(width, height * (i / 10f)),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(width * (i / 10f), 0f),
                    end = Offset(width * (i / 10f), height),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}

// 9. EVENT CARD
@Composable
fun OutsEventCard(
    title: String,
    category: String,
    date: String,
    location: String,
    priceInfo: String,
    modifier: Modifier = Modifier,
    isHotSeller: Boolean = false,
    imageUrl: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                DecorativeEventHeader(category = category, title = title, imageUrl = imageUrl)
                if (isHotSeller) {
                    OutsBadge(
                        label = "HOT SELLER",
                        backgroundColor = BrandAccent,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }
                IconButton(
                    onClick = { /* favorite toggle */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "${category.uppercase()} • $date",
                    color = BrandPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    color = Color(0xFF13111C),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = Color(0xFF868A9A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        color = Color(0xFF6F727E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFECEFF1), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "From $priceInfo",
                        color = Color(0xFF13111C),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small avatar cluster indicators
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9C27B0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = (-6).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = (-12).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+12", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 10. AVATAR
@Composable
fun OutsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    badgeColor: Color? = BrandPrimary
) {
    val firstChar = if (name.isNotEmpty()) name[0].toString() else "O"
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE2D9FF), Color(0xFFC0B2F2))
                    )
                )
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstChar.uppercase(),
                color = BrandPrimary,
                fontSize = (size.value * 0.45).sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (badgeColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (2).dp, y = (2).dp)
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

// 11. TABS
@Composable
fun OutsTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFF2F4F7)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(containerColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val animatedWeight by animateFloatAsState(
                targetValue = if (isSelected) 1f else 1f,
                animationSpec = tween(150),
                label = "tab_weight"
            )

            Box(
                modifier = Modifier
                    .weight(animatedWeight)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) BrandPrimary else Color(0xFF7A7F8E),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// 12. STATUS INDICATORS
@Composable
fun OutsStatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, text, label) = when (status.uppercase()) {
        "APPROVED" -> Triple(Color(0xFFE2F9EE), Color(0xFF0F9D58), "Approved")
        "PENDING" -> Triple(Color(0xFFFFFAEB), Color(0xFFD97706), "Pending")
        "REJECTED" -> Triple(Color(0xFFFFF5F5), Color(0xFFE53935), "Rejected")
        "SOLD_OUT" -> Triple(Color(0xFFE1F5FE), Color(0xFF0288D1), "Sold Out")
        "BLOCKED" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Blocked")
        else -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), status)
    }

    Box(
        modifier = modifier
            .widthIn(min = 80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}

// 13. BOTTOM NAVIGATION (Mobile)
@Composable
fun OutsBottomNavigation(
    tabs: List<String>,
    icons: List<ImageVector>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badgeIndices: Map<Int, Int> = emptyMap()
) {
    Surface(
        color = Color.White,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(
                color = Color(0xFFECEFF1),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                val color = if (isSelected) BrandPrimary else Color(0xFF8D92A3)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icons.getOrElse(index) { Icons.Default.Home },
                            contentDescription = tab,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                        val badgeCount = badgeIndices[index]
                        if (badgeCount != null && badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-4).dp)
                                    .size(16.dp)
                                    .background(BrandAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badgeCount.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
}

// 14. MODAL POPUP
@Composable
fun OutsModalPopup(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF13111C)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF555B6F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

// 15. CUSTOM TICKET STUB LOGO (Real Cutout Vector Graphics)
@Composable
fun OutsLogo(
    modifier: Modifier = Modifier,
    tint: Color = BrandPrimary,
    cutoutColor: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val notchRadius = h * 0.18f
        val r = h * 0.22f // Corner radius

        val path = Path().apply {
            // Start at top-left corner
            moveTo(r, 0f)
            // Top edge
            lineTo(w - r, 0f)
            // Top-right corner
            quadraticTo(w, 0f, w, r)
            
            // Right edge down to top of right notch
            lineTo(w, h / 2f - notchRadius)
            // Right scoop inward
            quadraticTo(w - notchRadius * 1.5f, h / 2f, w, h / 2f + notchRadius)
            
            // Right edge down to bottom-right corner
            lineTo(w, h - r)
            // Bottom-right corner
            quadraticTo(w, h, w - r, h)
            
            // Bottom edge
            lineTo(r, h)
            // Bottom-left corner
            quadraticTo(0f, h, 0f, h - r)
            
            // Left edge up to bottom of left notch
            lineTo(0f, h / 2f + notchRadius)
            // Left scoop inward
            quadraticTo(notchRadius * 1.5f, h / 2f, 0f, h / 2f - notchRadius)
            
            // Left edge up to top-left corner
            lineTo(0f, r)
            // Top-left corner
            quadraticTo(0f, 0f, r, 0f)
            
            close()
        }

        drawPath(path = path, color = tint)

        // Draw the 3 vertical perforation dots in the designated cutoutColor to create cutout illusion safely
        val dotRadius = h * 0.08f
        val centerCol = w / 2f
        val spacing = h * 0.24f
        
        drawCircle(color = cutoutColor, radius = dotRadius, center = Offset(centerCol, h / 2f - spacing))
        drawCircle(color = cutoutColor, radius = dotRadius, center = Offset(centerCol, h / 2f))
        drawCircle(color = cutoutColor, radius = dotRadius, center = Offset(centerCol, h / 2f + spacing))
    }
}

// 16. CORE SYSTEM GRAPHICAL TOAST OVERLAY (Dynamic & Animated)
@Composable
fun OutsToastOverlay(
    message: String?,
    type: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = !message.isNullOrEmpty(),
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it / 2 }) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it / 2 }) + androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            val bgColor = when (type) {
                "success" -> Color(0xFFE8F5E9)
                "error" -> Color(0xFFFFEBEE)
                else -> Color(0xFFE0F7FA)
            }
            val contentColor = when (type) {
                "success" -> Color(0xFF2E7D32)
                "error" -> Color(0xFFC62828)
                else -> Color(0xFF006064)
            }
            val icon = when (type) {
                "success" -> Icons.Default.CheckCircle
                "error" -> Icons.Default.Error
                else -> Icons.Default.Info
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Notification Icon",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// 17. REUSABLE GLOWING SHIMMER CARDS & SKELETON SCREENS
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .background(color = Color(0xFFECEFF1).copy(alpha = alphaAnim), shape = shape)
    )
}

@Composable
fun SkeletonFeedLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                ShimmerPlaceholder(modifier = Modifier.size(width = 110.dp, height = 16.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerPlaceholder(modifier = Modifier.size(width = 180.dp, height = 24.dp))
            }
            ShimmerPlaceholder(modifier = Modifier.size(44.dp), shape = CircleShape)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search options shimmer
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp))

        // Categories list horizontal scroll shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(4) {
                ShimmerPlaceholder(modifier = Modifier.size(width = 75.dp, height = 34.dp), shape = RoundedCornerShape(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feature cards
        Text("Curating matches...", fontSize = 13.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.SemiBold)
        repeat(2) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F7)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(150.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShimmerPlaceholder(modifier = Modifier.size(118.dp), shape = RoundedCornerShape(16.dp))
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            ShimmerPlaceholder(modifier = Modifier.size(width = 70.dp, height = 12.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerPlaceholder(modifier = Modifier.width(100.dp).height(14.dp))
                        }
                        ShimmerPlaceholder(modifier = Modifier.size(width = 120.dp, height = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonDashboardLoader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                ShimmerPlaceholder(modifier = Modifier.size(width = 90.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerPlaceholder(modifier = Modifier.size(width = 160.dp, height = 28.dp))
            }
            ShimmerPlaceholder(modifier = Modifier.size(48.dp), shape = CircleShape)
        }

        // Payout Financial Metric Rows
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(92.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerPlaceholder(modifier = Modifier.size(width = 80.dp, height = 10.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerPlaceholder(modifier = Modifier.size(width = 110.dp, height = 22.dp))
                }
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFEEEEEE)))
                Column(
                    modifier = Modifier.weight(1f).padding(start = 24.dp)
                ) {
                    ShimmerPlaceholder(modifier = Modifier.size(width = 80.dp, height = 10.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerPlaceholder(modifier = Modifier.size(width = 100.dp, height = 22.dp))
                }
            }
        }

        // Sales Overview curve vector design matching requirement
        Column {
            ShimmerPlaceholder(modifier = Modifier.size(width = 130.dp, height = 18.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(24.dp))
        }

        // Listings List layout
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerPlaceholder(modifier = Modifier.size(width = 110.dp, height = 16.dp))
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerPlaceholder(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(14.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerPlaceholder(modifier = Modifier.size(width = 150.dp, height = 10.dp))
                    }
                    ShimmerPlaceholder(modifier = Modifier.size(width = 50.dp, height = 24.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}

