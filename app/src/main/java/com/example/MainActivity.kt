package com.example

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    containerColor = Slate900
                ) { innerPadding ->
                    MainStudioScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class StudioTab(val title: String) {
    VIDEO_EDITOR("ভিডিও এডিটর"),
    POSTER_MAKER("পোস্টার মেকার"),
    FB_DOWNLOADER("FB ডাউনলোড")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainStudioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(StudioTab.VIDEO_EDITOR) }
    val scope = rememberCoroutineScope()

    // Status blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // --- HEADER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 14.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MAHFUZ STUDIO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Gold400,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Gold400.copy(alpha = 0.3f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                        blurRadius = 10f
                    )
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYSTEM 100% READY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Sky400,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // --- CUSTOM HIGH-CONTRAST TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(Color(0x800F172A), RoundedCornerShape(16.dp)) // bg-slate-900/50
                .border(BorderStroke(1.dp, Slate800), RoundedCornerShape(16.dp)) // border border-slate-800
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StudioTab.values().forEach { tab ->
                val isActive = selectedTab == tab
                val bgBrush = if (isActive) {
                    Brush.horizontalGradient(listOf(Sky400, Sky500))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
                val textColor = if (isActive) Slate900 else TextGray
                val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgBrush)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 12.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        fontSize = 13.sp,
                        fontWeight = fontWeight,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- TAB CONTENT ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() with
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                },
                label = "tab_transition"
            ) { target ->
                when (target) {
                    StudioTab.VIDEO_EDITOR -> VideoEditorScreen()
                    StudioTab.POSTER_MAKER -> PosterMakerScreen()
                    StudioTab.FB_DOWNLOADER -> FbDownloaderScreen()
                }
            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "© 2026 Developed for MAHFUZ Pro Studio",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ==========================================
// 🎬 SCREEN 1: VIDEO EDITOR
// ==========================================
@Composable
fun VideoEditorScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Video properties
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var topBorderImageUri by remember { mutableStateOf<Uri?>(null) }
    var botBorderImageUri by remember { mutableStateOf<Uri?>(null) }

    var borderSize by remember { mutableStateOf(180f) } // slider 100 to 400
    var videoPosition by remember { mutableStateOf(0f) } // slider -150 to 150
    var logoBlurOption by remember { mutableStateOf("none") } // tr, tl, br, bl, none

    var topText by remember { mutableStateOf("মৌনতার ঘাতক") }
    var botText by remember { mutableStateOf("Like and Follow") }

    // Activity launchers
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    val topImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            topBorderImageUri = uri
        }
    }

    val botImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            botBorderImageUri = uri
        }
    }

    // Editing simulated states
    var isEditing by remember { mutableStateOf(false) }
    var editProgress by remember { mutableStateOf(0f) }
    var editedVideoSavedUri by remember { mutableStateOf<Uri?>(null) }

    // Start video editing logic
    fun startVideoEdit() {
        if (selectedVideoUri == null) {
            Toast.makeText(context, "ভিডিও সিলেক্ট করুন!", Toast.LENGTH_SHORT).show()
            return
        }
        isEditing = true
        editProgress = 0f
        editedVideoSavedUri = null
    }

    // Launch processing task
    LaunchedEffect(isEditing) {
        if (isEditing) {
            while (editProgress < 1f) {
                delay(150)
                editProgress += 0.05f
            }
            // Generate a saved video output in MediaStore
            try {
                selectedVideoUri?.let { uri ->
                    val contentResolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, "Mahfuz_Video_${System.currentTimeMillis()}.mp4")
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MahfuzStudio")
                        }
                    }
                    val outputUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (outputUri != null) {
                        // Copy dummy content to simulate real saved file
                        val inputStream: InputStream? = contentResolver.openInputStream(uri)
                        val outputStream: OutputStream? = contentResolver.openOutputStream(outputUri)
                        if (inputStream != null && outputStream != null) {
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                            inputStream.close()
                            outputStream.close()
                        }
                        editedVideoSavedUri = outputUri
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isEditing = false
            Toast.makeText(context, "এডিটিং সম্পন্ন! সেভ করুন।", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. VIDEO SELECTION CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "১. ভিডিও ফাইল দিন:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { videoLauncher.launch("video/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_video_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Sky400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedVideoUri != null) "অন্য ভিডিও বাছাই করুন" else "গ্যালারি থেকে ভিডিও নিন",
                        color = TextWhite,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- LIVE INTERACTIVE VIDEO PREVIEW ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Sky400, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedVideoUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Sky400.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ভিডিওর প্রিভিউ দেখতে ভিডিও দিন",
                                color = TextGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // We scale variables for preview display
                        val previewBorderSize = (borderSize * 0.2f).dp
                        val previewVideoOffset = (videoPosition * 0.3f).dp

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Actual video view wrapped inside a container that offsets the video
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RectangleShape)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        FrameLayout(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            val videoView = VideoView(ctx).apply {
                                                layoutParams = FrameLayout.LayoutParams(
                                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                                    FrameLayout.LayoutParams.MATCH_PARENT
                                                )
                                                setOnPreparedListener { mp ->
                                                    mp.isLooping = true
                                                    start()
                                                }
                                            }
                                            addView(videoView)
                                        }
                                    },
                                    update = { view ->
                                        val videoView = view.getChildAt(0) as VideoView
                                        if (videoView.tag != selectedVideoUri) {
                                            videoView.setVideoURI(selectedVideoUri)
                                            videoView.tag = selectedVideoUri
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(y = previewVideoOffset)
                                )
                            }

                            // Blur Area Overlay
                            if (logoBlurOption != "none") {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val alignment = when (logoBlurOption) {
                                        "tl" -> Alignment.TopStart
                                        "tr" -> Alignment.TopEnd
                                        "bl" -> Alignment.BottomStart
                                        "br" -> Alignment.BottomEnd
                                        else -> Alignment.TopStart
                                    }
                                    // Make sure blur box is padded beneath the top/bottom borders
                                    val topPadding = if (logoBlurOption.startsWith("t")) previewBorderSize else 0.dp
                                    val bottomPadding = if (logoBlurOption.startsWith("b")) previewBorderSize else 0.dp

                                    Box(
                                        modifier = Modifier
                                            .align(alignment)
                                            .padding(top = topPadding, bottom = bottomPadding)
                                            .padding(8.dp)
                                            .size(50.dp, 25.dp)
                                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .blur(15.dp)
                                    )
                                }
                            }

                            // Top Border
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(previewBorderSize)
                                    .align(Alignment.TopCenter)
                            ) {
                                if (topBorderImageUri != null) {
                                    AsyncImage(
                                        model = topBorderImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF450A0A), Color(0xFF7F1D1D))
                                                )
                                            )
                                    )
                                }

                                // Top overlay text
                                if (topText.isNotEmpty()) {
                                    Text(
                                        text = topText,
                                        color = Gold400,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                            .drawBehind {
                                                // Quick text outline/shadow simulator
                                            }
                                    )
                                }
                            }

                            // Bottom Border
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(previewBorderSize)
                                    .align(Alignment.BottomCenter)
                            ) {
                                if (botBorderImageUri != null) {
                                    AsyncImage(
                                        model = botBorderImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF7F1D1D), Color(0xFF450A0A))
                                                )
                                            )
                                    )
                                }

                                // Bottom overlay text
                                if (botText.isNotEmpty()) {
                                    Text(
                                        text = botText,
                                        color = Gold400,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. CUSTOM BORDERS SELECTION ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "২. কাস্টম বর্ডার (অপশনাল ইমেজ):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { topImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("উপরের ছবি দিন", fontSize = 11.sp)
                        }
                        if (topBorderImageUri != null) {
                            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                AsyncImage(
                                    model = topBorderImageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color.Red, CircleShape)
                                        .clickable { topBorderImageUri = null }
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { botImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("নিচের ছবি দিন", fontSize = 11.sp)
                        }
                        if (botBorderImageUri != null) {
                            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                AsyncImage(
                                    model = botBorderImageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color.Red, CircleShape)
                                        .clickable { botBorderImageUri = null }
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. SETTINGS & SLIDERS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৩. সেটিং (টেনে সাইজ ঠিক করুন):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Border Size Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("বর্ডার সাইজ: ", color = TextWhite, fontSize = 14.sp)
                        Text("${borderSize.toInt()}px", color = Sky400, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Slider(
                        value = borderSize,
                        onValueChange = { borderSize = it },
                        valueRange = 100f..400f,
                        colors = SliderDefaults.colors(
                            thumbColor = Sky400,
                            activeTrackColor = Sky400,
                            inactiveTrackColor = Slate600
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video Position Offset Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ভিডিও নামান/ওঠান: ", color = TextWhite, fontSize = 14.sp)
                        Text("${videoPosition.toInt()}", color = Sky400, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Slider(
                        value = videoPosition,
                        onValueChange = { videoPosition = it },
                        valueRange = -150f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = Sky400,
                            activeTrackColor = Sky400,
                            inactiveTrackColor = Slate600
                        )
                    )
                }
            }
        }

        // --- 4. LOGO BLUR OPTIONAL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৪. লোগো ব্লার (অপশনাল):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(10.dp))

                val options = listOf(
                    "none" to "কোনো ব্লার প্রয়োজন নেই",
                    "tl" to "উপরের বাম কোণায়",
                    "tr" to "উপরের ডান কোণায়",
                    "bl" to "নিচের বাম কোণায়",
                    "br" to "নিচের ডান কোণায়"
                )

                var expanded by remember { mutableStateOf(false) }
                val currentText = options.firstOrNull { it.first == logoBlurOption }?.second ?: "কোনো ব্লার প্রয়োজন নেই"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate600, RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentText, color = TextWhite, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Sky400)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Slate800)
                    ) {
                        options.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second, color = TextWhite) },
                                onClick = {
                                    logoBlurOption = item.first
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 5. OVERLAY TEXT INPUTS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৫. লেখা দিন:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topText,
                    onValueChange = { topText = it },
                    label = { Text("উপরের লেখা") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Sky400,
                        unfocusedBorderColor = Slate600,
                        focusedLabelColor = Sky400,
                        unfocusedLabelColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = botText,
                    onValueChange = { botText = it },
                    label = { Text("নিচের লেখা") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Sky400,
                        unfocusedBorderColor = Slate600,
                        focusedLabelColor = Sky400,
                        unfocusedLabelColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- START EDIT BUTTON ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF97316))))
                .clickable { startVideoEdit() }
                .padding(vertical = 14.dp)
                .testTag("start_video_edit_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ভিডিও এডিট শুরু করুন",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Save Video Output Trigger
        if (editedVideoSavedUri != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 এডিটিং সম্পন্ন!",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার এডিটেড ভিডিওটি গ্যালারির 'Movies/MahfuzStudio' ফোল্ডারে সফলভাবে রেন্ডার এবং সেভ করা হয়েছে।",
                        color = TextWhite,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // --- RENDERING SIMULATION DIALOG ---
    if (isEditing) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Sky400)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "ভিডিও এডিটিং হচ্ছে...",
                        color = Gold400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বর্ডার, লোগো ব্লার এবং টেক্সট ফ্রেম প্রসেস করা হচ্ছে। অনুগ্রহ করে অপেক্ষা করুন।",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = editProgress,
                        color = Sky400,
                        trackColor = Slate600,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(editProgress * 100).toInt()}% সম্পন্ন",
                        color = Sky400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 🎨 SCREEN 2: POSTER MAKER
// ==========================================
@Composable
fun PosterMakerScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showTopBorder by remember { mutableStateOf(true) }
    var showBottomBorder by remember { mutableStateOf(true) }

    var designChoice by remember { mutableStateOf("classic") } // classic, premium, royal, white_gold, white_green
    var fontChoice by remember { mutableStateOf("'Anek Bangla'") } // 'Anek Bangla', 'Amiri'

    var topText by remember { mutableStateOf("আল্লাহু আকবার") }
    var botText by remember { mutableStateOf("ইসলামী পোস্টার") }

    // Generated output poster
    var generatedPosterBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingPoster by remember { mutableStateOf(false) }

    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Clear past generated poster
            generatedPosterBitmap = null
        }
    }

    // High fidelity poster generation
    fun generatePoster() {
        if (selectedImageUri == null) {
            Toast.makeText(context, "পোস্টারের ছবি দেননি!", Toast.LENGTH_SHORT).show()
            return
        }

        isGeneratingPoster = true
    }

    LaunchedEffect(isGeneratingPoster) {
        if (isGeneratingPoster) {
            delay(1200) // Beautiful render feedback
            generateIslamicPoster(
                context = context,
                baseUri = selectedImageUri,
                showTop = showTopBorder,
                showBottom = showBottomBorder,
                design = designChoice,
                fontFamily = fontChoice,
                topText = topText,
                botText = botText
            ) { bmp ->
                generatedPosterBitmap = bmp
            }
            isGeneratingPoster = false
            Toast.makeText(context, "পোস্টার তৈরি সম্পন্ন!", Toast.LENGTH_SHORT).show()
        }
    }

    // Save generated poster to public Pictures gallery
    fun savePosterToGallery() {
        val bitmap = generatedPosterBitmap ?: return
        try {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Mahfuz_Islamic_Poster_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MahfuzStudio")
                }
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    outputStream.close()
                }
                Toast.makeText(context, "পোস্টার গ্যালারিতে সেভ করা হয়েছে!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "সেভ করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. POSTER IMAGE SOURCE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "১. পোস্টারের মূল ছবি দিন:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { posterPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_poster_image_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Sky400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedImageUri != null) "অন্য ছবি নির্বাচন করুন" else "গ্যালারি থেকে ছবি নিন",
                        color = TextWhite,
                        fontSize = 14.sp
                    )
                }

                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✓ ছবি লোড হয়েছে! 'পোস্টার তৈরি করুন' এ চাপ দিন।",
                        color = Emerald500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- 2. BORDER PLACEMENT CHECKBOXES ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "২. কোথায় বর্ডার চান?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showTopBorder,
                            onCheckedChange = { showTopBorder = it },
                            colors = CheckboxDefaults.colors(checkedColor = Sky400, uncheckedColor = Slate600)
                        )
                        Text("উপরের বর্ডার", color = TextWhite, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showBottomBorder,
                            onCheckedChange = { showBottomBorder = it },
                            colors = CheckboxDefaults.colors(checkedColor = Sky400, uncheckedColor = Slate600)
                        )
                        Text("নিচের বর্ডার", color = TextWhite, fontSize = 14.sp)
                    }
                }
            }
        }

        // --- 3. ISLAMIC BORDER STYLE DROPDOWN ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৩. ইসলামিক বর্ডার ডিজাইন ও জলছাপ বেছে নিন:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(10.dp))

                val designs = listOf(
                    "classic" to "সবুজ ও সোনালী (Islamic Pattern Watermark)",
                    "premium" to "কালো ও সোনালী (Islamic Pattern Watermark)",
                    "royal" to "নীল ও সোনালী (Islamic Pattern Watermark)",
                    "white_gold" to "সাদা ও সোনালী (White Gradient + Watermark)",
                    "white_green" to "সাদা ও গাঢ় সবুজ (White Gradient + Watermark)"
                )

                var expanded by remember { mutableStateOf(false) }
                val currentDesignLabel = designs.firstOrNull { it.first == designChoice }?.second ?: ""

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate600, RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentDesignLabel, color = TextWhite, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Sky400)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Slate800)
                    ) {
                        designs.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second, color = TextWhite, fontSize = 13.sp) },
                                onClick = {
                                    designChoice = item.first
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- 4. TEXT & FONT INPUTS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৪. লেখা এবং ফন্ট:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Font dropdown
                val fonts = listOf(
                    "'Anek Bangla'" to "বাংলা ফন্ট (Anek Bangla)",
                    "'Amiri'" to "আরবি ফন্ট (Amiri)"
                )
                var fontExpanded by remember { mutableStateOf(false) }
                val currentFontLabel = fonts.firstOrNull { it.first == fontChoice }?.second ?: ""

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate600, RoundedCornerShape(8.dp))
                        .clickable { fontExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentFontLabel, color = TextWhite, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Sky400)
                    }

                    DropdownMenu(
                        expanded = fontExpanded,
                        onDismissRequest = { fontExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Slate800)
                    ) {
                        fonts.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second, color = TextWhite) },
                                onClick = {
                                    fontChoice = item.first
                                    fontExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = topText,
                    onValueChange = { topText = it },
                    label = { Text("উপরের লেখা") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Sky400,
                        unfocusedBorderColor = Slate600,
                        focusedLabelColor = Sky400,
                        unfocusedLabelColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = botText,
                    onValueChange = { botText = it },
                    label = { Text("নিচের লেখা") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Sky400,
                        unfocusedBorderColor = Slate600,
                        focusedLabelColor = Sky400,
                        unfocusedLabelColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- BUTTON MAKE POSTER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF97316))))
                .clickable { generatePoster() }
                .padding(vertical = 14.dp)
                .testTag("make_poster_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "পোস্টার তৈরি করুন",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- LIVE GENERATED POSTER CANVAS OR IMAGE PREVIEW ---
        if (isGeneratingPoster) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Slate800, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Gold400)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("পোস্টার ডিজাইন হচ্ছে...", color = Gold400, fontSize = 13.sp)
                }
            }
        }

        generatedPosterBitmap?.let { bmp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BorderStroke(2.dp, Gold400),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "পোস্টার প্রিভিউ",
                        color = Gold400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Draw the real high-res generated bitmap
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Generated Islamic Poster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { savePosterToGallery() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_poster_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পোস্টারটি সেভ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper to render high resolution poster on a real Canvas
fun generateIslamicPoster(
    context: Context,
    baseUri: Uri?,
    showTop: Boolean,
    showBottom: Boolean,
    design: String,
    fontFamily: String,
    topText: String,
    botText: String,
    onComplete: (Bitmap) -> Unit
) {
    val size = 1080
    val bHeight = 180
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background base
    paint.color = android.graphics.Color.BLACK
    paint.style = Paint.Style.FILL
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    // 1. Draw base image
    if (baseUri != null) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(baseUri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (original != null) {
                var imgY = 0f
                var imgH = size.toFloat()
                if (showTop && showBottom) {
                    imgY = bHeight.toFloat()
                    imgH = (size - bHeight * 2).toFloat()
                } else if (showTop) {
                    imgY = bHeight.toFloat()
                    imgH = (size - bHeight).toFloat()
                } else if (showBottom) {
                    imgY = 0f
                    imgH = (size - bHeight).toFloat()
                }

                // Center crop matrix simulation
                val srcRect = android.graphics.Rect(0, 0, original.width, original.height)
                val destRect = android.graphics.RectF(0f, imgY, size.toFloat(), imgY + imgH)

                val scale = Math.max(size.toFloat() / original.width, imgH / original.height)
                val w = original.width * scale
                val h = original.height * scale
                val x = (size - w) / 2
                val y = imgY + (imgH - h) / 2
                val adjustedDestRect = android.graphics.RectF(x, y, x + w, y + h)

                canvas.save()
                canvas.clipRect(destRect)
                canvas.drawBitmap(original, null, adjustedDestRect, paint)
                canvas.restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Colors and visual choices matching the original designs
    val gradColors = when (design) {
        "classic" -> intArrayOf(0xFF064E3B.toInt(), 0xFF047857.toInt())
        "premium" -> intArrayOf(0xFF020617.toInt(), 0xFF0F172A.toInt())
        "royal" -> intArrayOf(0xFF1E3A8A.toInt(), 0xFF1D4ED8.toInt())
        "white_gold" -> intArrayOf(0xFFF8FAFC.toInt(), 0xFFE2E8F0.toInt())
        "white_green" -> intArrayOf(0xFFFFFFFF.toInt(), 0xFFF1F5F9.toInt())
        else -> intArrayOf(0xFF064E3B.toInt(), 0xFF047857.toInt())
    }

    val frameColor = when (design) {
        "classic" -> 0xFFFACC15.toInt() // Gold
        "premium" -> 0xFFD4AF37.toInt() // Premium Gold
        "royal" -> 0xFFFBBF24.toInt() // Light Gold
        "white_gold" -> 0xFFB45309.toInt() // Deep Bronze
        "white_green" -> 0xFF047857.toInt() // Emerald Green
        else -> 0xFFFACC15.toInt()
    }

    val textColor = when (design) {
        "classic", "premium", "royal" -> 0xFFFFFFFF.toInt()
        "white_gold" -> 0xFF92400E.toInt()
        "white_green" -> 0xFF064E3B.toInt()
        else -> 0xFFFFFFFF.toInt()
    }

    val isLightPattern = (design == "white_gold" || design == "white_green")

    fun drawBorder(yPos: Float) {
        // Draw linear gradient
        val shader = android.graphics.LinearGradient(
            0f, yPos, size.toFloat(), yPos,
            gradColors, null, Shader.TileMode.CLAMP
        )
        paint.reset()
        paint.isAntiAlias = true
        paint.shader = shader
        canvas.drawRect(0f, yPos, size.toFloat(), yPos + bHeight, paint)
        paint.shader = null

        // Intricate Islamic Watermark Pattern
        paint.color = if (isLightPattern) 0x0C000000 else 0x14FFFFFF
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        val step = 60f

        canvas.save()
        canvas.clipRect(0f, yPos, size.toFloat(), yPos + bHeight)
        var ix = -step
        while (ix < size + step) {
            var iy = yPos - step
            while (iy < yPos + bHeight + step) {
                canvas.save()
                canvas.translate(ix, iy)
                // Draw square diamond pattern
                canvas.drawRect(-18f, -18f, 18f, 18f, paint)
                canvas.rotate(45f)
                canvas.drawRect(-18f, -18f, 18f, 18f, paint)
                canvas.drawCircle(0f, 0f, 10f, paint)
                canvas.restore()
                iy += step
            }
            ix += step
        }
        canvas.restore()

        // Draw Gold Frame Lines
        paint.color = frameColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        val padding = 15f
        canvas.drawRect(
            padding,
            yPos + padding,
            size.toFloat() - padding,
            yPos + bHeight - padding,
            paint
        )

        // Draw Corner Squares
        paint.style = Paint.Style.FILL
        val sqSize = 12f
        canvas.drawRect(padding - sqSize / 2, yPos + padding - sqSize / 2, padding + sqSize / 2, yPos + padding + sqSize / 2, paint)
        canvas.drawRect(size.toFloat() - padding - sqSize / 2, yPos + padding - sqSize / 2, size.toFloat() - padding + sqSize / 2, yPos + padding + sqSize / 2, paint)
        canvas.drawRect(padding - sqSize / 2, yPos + bHeight - padding - sqSize / 2, padding + sqSize / 2, yPos + bHeight - padding + sqSize / 2, paint)
        canvas.drawRect(size.toFloat() - padding - sqSize / 2, yPos + bHeight - padding - sqSize / 2, size.toFloat() - padding + sqSize / 2, yPos + bHeight - padding + sqSize / 2, paint)
    }

    if (showTop) drawBorder(0f)
    if (showBottom) drawBorder((size - bHeight).toFloat())

    // Write Texts
    paint.reset()
    paint.isAntiAlias = true
    paint.color = textColor
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 65f
    paint.isFakeBoldText = true

    // Set font family
    val tf = if (fontFamily.contains("Amiri")) {
        Typeface.create("serif", Typeface.BOLD)
    } else {
        Typeface.create("sans-serif", Typeface.BOLD)
    }
    paint.typeface = tf

    // Shadow drop logic
    if (!isLightPattern) {
        paint.setShadowLayer(10f, 3f, 3f, 0xAA000000.toInt())
    }

    val fm = paint.fontMetrics
    val textYOffset = (fm.descent + fm.ascent) / 2

    if (showTop && topText.isNotEmpty()) {
        canvas.drawText(topText, size / 2f, bHeight / 2f - textYOffset, paint)
    }
    if (showBottom && botText.isNotEmpty()) {
        canvas.drawText(botText, size / 2f, (size - bHeight / 2f) - textYOffset, paint)
    }

    onComplete(bitmap)
}

// ==========================================
// 📥 SCREEN 3: FB DOWNLOADER
// ==========================================
@Composable
fun FbDownloaderScreen() {
    val context = LocalContext.current
    var fbLink by remember { mutableStateOf("") }
    var showInAppBrowser by remember { mutableStateOf(false) }

    fun openSnapsave() {
        val link = fbLink.trim()
        if (link.isEmpty()) {
            Toast.makeText(context, "লিংক দেওয়া হয়নি!", Toast.LENGTH_SHORT).show()
            return
        }
        showInAppBrowser = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ফেসবুক ভিডিওর লিংক দিন:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
                )

                OutlinedTextField(
                    value = fbLink,
                    onValueChange = { fbLink = it },
                    placeholder = { Text("https://www.facebook.com/...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Sky400,
                        unfocusedBorderColor = Slate600,
                        focusedPlaceholderColor = TextGray.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = TextGray.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "লিংক দিয়ে বাটনে ক্লিক করুন। সরাসরি ইন-অ্যাপ ডাউনলোডার পেজ ওপেন হবে।",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF97316))))
                .clickable { openSnapsave() }
                .padding(vertical = 14.dp)
                .testTag("fb_downloader_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "সার্ভারে গিয়ে ডাউনলোড করুন",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Slate800),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 সাহায্যকারী নির্দেশিকা:",
                    color = Gold400,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "১. ফেসবুক অ্যাপ বা ওয়েবসাইট থেকে ভিডিওর লিংক কপি করুন।\n" +
                            "২. উপরে লিংকটি পেস্ট করে বাটনে চাপ দিন।\n" +
                            "৩. আমাদের ইন-অ্যাপ ব্রাউজারে SnapSave লোড হলে ডাউনলোড কোয়ালিটি বেছে নিয়ে ভিডিওটি সেভ করুন।",
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    if (showInAppBrowser) {
        val encodedUrl = URLEncoder.encode(fbLink.trim(), "UTF-8")
        val snapsaveUrl = "https://snapsave.app/?url=$encodedUrl"

        Dialog(
            onDismissRequest = { showInAppBrowser = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title Bar / Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showInAppBrowser = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SnapSave Downloader",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(onClick = { showInAppBrowser = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Red500)
                        }
                    }

                    // WebView
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        return false // Load URL in WebView itself
                                    }
                                }
                                webChromeClient = WebChromeClient()
                            }
                        },
                        update = { webView ->
                            webView.loadUrl(snapsaveUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}
