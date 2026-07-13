package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReadingScreen(
    title: String,
    customText: String? = null,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val settingsManager = remember { com.example.model.SettingsManager.getInstance(context) }
    val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
    var fontSize by remember { mutableFloatStateOf(28f * textSizeMultiplier) }
    val scrollState = rememberScrollState()

    // Get the full Quranic text with diacritics and verse numbers
    val quranText = remember(title, customText) {
        if (!customText.isNullOrBlank()) {
            customText
        } else {
            getFullQuranicText(title)
        }
    }

    // Colors according to the specified design system
    val primaryColor = Color(0xFF1A3140) // Blue Whale
    val secondaryColor = MaterialTheme.colorScheme.secondary // Dynamic secondary accent color
    val surfaceColor = Color(0xFFFFF3E5) // Milk (Perfect for Mushaf paper!)
    val textColor = Color(0xFF1E2A38) // Deep readable charcoal
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(quranText))
                        android.widget.Toast.makeText(context, "تم نسخ النص القرآني كاملًا", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "$title:\n\n$quranText")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة النص"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = primaryColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryColor, Color(0xFF0F1B24))
                    )
                )
        ) {
            // Text size controls bar
            Surface(
                color = Color.Black.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حجم الخط",
                        color = secondaryColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (fontSize > 20f) fontSize -= 2f },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White)
                        }
                        Text(
                            text = "${fontSize.toInt()}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (fontSize < 44f) fontSize += 2f },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White)
                        }
                    }
                }
            }

            // Mushaf Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceColor)
                    .border(
                        BorderStroke(4.dp, secondaryColor.copy(alpha = 0.7f)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(4.dp)
                    .border(
                        BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .border(
                                BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Quran Text with Serifs, centered, comfortable layout with premium verse stickers
                    QuranicTextRenderer(
                        text = quranText,
                        fontSize = fontSize,
                        baseColor = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Footer Decorative Symbol
                    Text(
                        text = "۩ ۩ ۩",
                        style = MaterialTheme.typography.headlineSmall,
                        color = primaryColor.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Function returning beautifully formatted Quranic text
fun getFullQuranicText(title: String): String {
    return when {
        title.contains("الملك") -> """
            تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ ﴿١﴾ الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا وَهُوَ الْعَزِيزُ الْغَفُورُ ﴿٢﴾ الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا مَّا تَرَى فِي خَلْقِ الرَّحْمَنِ مِن تَفَاوُتٍ فَارْجِعِ الْبَصَرَ هَلْ تَرَى مِن فُطُورٍ ﴿٣﴾ ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ ﴿٤﴾ وَلَقَدْ زَيَّنَّا السَّمَاءَ الدُّنْيَا بِمَصَابِيحَ وَجَعَلْنَاهَا رُجُومًا لِّلشَّيَاطِينِ وَأَعْتَدْنَا لَهُمْ عَذَابَ السَّعِيرِ ﴿٥﴾ وَلِلَّذِينَ كَفَرُوا بِرَبِّهِمْ عَذَابُ جَهَنَّمَ وَبِئْسَ الْمَصِيرُ ﴿٦﴾ إِذَا أُلْقُوا فِيهَا سَمِعُوا لَهَا شَهِيقًا وَهِيَ تَفُورُ ﴿٧﴾ تَكَادُ تَمَيَّزُ مِنَ الْغَيْظِ كُلَّمَا أُلْقِيَ فِيهَا فَوْجٌ سَأَلَهُمْ خَزَنَتُهَا أَلَمْ يَأْتِكُمْ نَذِيرٌ ﴿٨﴾ قَالُوا بَلَى قَدْ جَاءَنَا نَذِيرٌ فَكَذَّبْنَا وَقُلْنَا مَا نَزَّلَ اللَّهُ مِن شَيْءٍ إِنْ أَنتُمْ إِلَّا فِي ضَلَالٍ كَبِيرٍ ﴿٩﴾ وَقَالُوا لَوْ كُنَّا نَسْمَعُ أَوْ نَعْقِلُ مَا كُنَّا فِي أَصْحَابِ السَّعِيرِ ﴿١٠﴾ فَاعْتَرَفُوا بِذَنبِهِمْ فَسُحْقًا لِّأَصْحَابِ السَّعِيرِ ﴿١١﴾ إِنَّ الَّذِينَ يَخْشَوْنَ رَبَّهُم بِالْغَيْبِ لَهُم مَّغْفِرَةٌ وَأَجْرٌ كَبِيرٌ ﴿١٢﴾ وَأَسِرُّوا قَوْلَكُمْ أَوِ اجْهَرُوا بِهِ إِنَّهُ عَلِيمٌ بِذَاتِ الصُّدُورِ ﴿١٣﴾ أَلَا يَعْلَمُ مَنْ خَلَقَ وَهُوَ اللَّطِيفُ الْخَبِيرُ ﴿١٤﴾ هُوَ الَّذِي جَعَلَ لَكُمُ الْأَرْضَ ذَلُولًا فَامْشُوا فِي مَنَاكِبِهَا وَكُلُوا مِن رِّزْقِهِ وَإِلَيْهِ النُّشُورُ ﴿١٥﴾ أَأَمِنتُم مَّن فِي السَّمَاءِ أَن يَخْسِفَ بِكُمُ الْأَرْضَ فَإِذَا هِيَ تَمُورُ ﴿١٦﴾ أَمْ أَمِنتُم مَّن فِي السَّمَاءِ أَن يُرْسِلَ عَلَيْكُمْ حَاصِبًا فَسَتَعْلَمُونَ كَيْفَ نَذِيرٌ ﴿١٧﴾ وَلَقَدْ كَذَّبَ الَّذِينَ مِن قَبْلِهِمْ فَكَيْفَ كَانَ نَكِيرِ ﴿١٨﴾ أَوَلَمْ يَرَوْا إِلَى الطَّيْرِ فَوْقَهُمْ صَافَّاتٍ وَيَقْبِضْنَ مَا يُمْسِكُهُنَّ إِلَّا الرَّحْمَنُ إِنَّهُ بِكُلِّ شَيْءٍ بَصِيرٌ ﴿١٩﴾ أَمَّنْ هَذَا الَّذِي هُوَ جُندٌ لَّكُمْ يَنصُرُكُم مِّن دُونِ الرَّحْمَنِ إِنِ الْكَافِرُونَ إِلَّا فِي غُرُورٍ ﴿٢٠﴾ أَمَّنْ هَذَا الَّذِي يَرْزُقُكُمْ إِنْ أَمْسَكَ رِزْقَهُ بَل لَّجُّوا فِي عُتُوٍّ وَنُفُورٍ ﴿٢١﴾ أَفَمَن يَمْشِي مُكِبًّا عَلَى وَجْهِهِ أَهْدَى أَمَّن يَمْشِي سَوِيًّا عَلَى صِرَاطٍ مُّسْتَقِيمٍ ﴿٢٢﴾ قُلْ هُوَ الَّذِي أَنشَأَكُمْ وَجَعَلَ لَكُمُ السَّمْعَ وَالْأَبْصَارَ وَالْأَفْئِدَةَ قَلِيلًا مَّا تَشْكُرُونَ ﴿٢٣﴾ قُلْ هُوَ الَّذِي ذَرَأَكُمْ فِي الْأَرْضِ وَإِلَيْهِ تُحْشَرُونَ ﴿٢٤﴾ وَيَقُولُونَ مَتَى هَذَا الْوَعْدُ إِن كُنتُمْ صَادِقِينَ ﴿٢٥﴾ قُلْ إِنَّمَا الْعِلْمُ عِندَ اللَّهِ وَإِنَّمَا أَنَا نَذِيرٌ مُّبِينٌ ﴿٢٦﴾ فَلَمَّا رَأَوْهُ زُلْفَةً سِيئَتْ وُجُوهُ الَّذِينَ كَفَرُوا وَقِيلَ هَذَا الَّذِي كُنتُم بِهِ تَدَّعُونَ ﴿٢٧﴾ قُلْ أَرَأَيْتُمْ إِنْ أَهْلَكَنِيَ اللَّهُ وَمَن مَّعِيَ أَوْ رَحِمَنَا فَمَن يُجِيرُ الْكَافِرِينَ مِنْ عَذَابٍ أَلِيمٍ ﴿٢٨﴾ قُلْ هُوَ الرَّحْمَنُ آمَنَّا بِهِ وَعَلَيْهِ تَوَكَّلْنَا فَسَتَعْلَمُونَ مَنْ هُوَ فِي ضَلَالٍ مُّبِينٍ ﴿٢٩﴾ قُلْ أَرَأَيْتُمْ إِنْ أَصْبَحَ مَاؤُكُمْ غَوْرًا فَمَن يَأْتِيكُم بِمَاءٍ مَّعِينٍ ﴿٣٠﴾
        """.trimIndent()

        title.contains("السجدة") -> """
            الم ﴿١﴾ تَنزِيلُ الْكِتَابِ لَا رَيْبَ فِيهِ مِن رَّبِّ الْعَالَمِينَ ﴿٢﴾ أَمْ يَقُولُونَ افْتَرَاهُ بَلْ هُوَ الْحَقُّ مِن رَّبِّكَ لِتُنذِرَ قَوْمًا مَّا أَتَاهُم مِّن نَّذِيرٍ مِّن قَبْلِكَ لَعَلَّهُمْ يَهْتَدُونَ ﴿٣﴾ اللَّهُ الَّذِي خَلَقَ السَّمَاوَاتِ وَالْأَرْضَ وَمَا بَيْنَهُمَا فِي سِتَّةِ أَيَّامٍ ثُمَّ اسْتَوَى عَلَى الْعَرْشِ مَا لَكُم مِّن دُونِهِ مِن وَلِيٍّ وَلَا شَفِيعٍ أَفَلَا تَتَذَكَّرُونَ ﴿٤﴾ يُدَبِّرُ الْأَمْرَ مِنَ السَّمَاءِ إِلَى الْأَرْضِ ثُمَّ يَعْرُجُ إِلَيْهِ فِي يَوْمٍ كَانَ مِقْدَارُهُ أَلْفَ سَنَةٍ مِّمَّا تَعُدُّونَ ﴿٥﴾ ذَلِكَ عَالِمُ الْغَيْبِ وَالشَّهَادَةِ الْعَزِيزُ الرَّحِيمُ ﴿٦﴾ الَّذِي أَحْسَنَ كُلَّ شَيْءٍ خَلَقَهُ وَبَدَأَ خَلْقَ الْإِنسَانِ مِن طِينٍ ﴿٧﴾ ثُمَّ جَعَلَ نَسْلَهُ مِن سُلَالَةٍ مِّن مَّاءٍ مَّهِينٍ ﴿٨﴾ ثُمَّ سَوَّاهُ وَنَفَخَ فِيهِ مِن رُّوحِهِ وَجَعَلَ لَكُمُ السَّمْعَ وَالْأَبْصَارَ وَالْأَفْئِدَةَ قَلِيلًا مَّا تَشْكُرُونَ ﴿٩﴾ وَقَالُوا أَإِذَا ضَلَلْنَا فِي الْأَرْضِ أَإِنَّا لَفِي خَلْقٍ جَدِيدٍ بَلْ هُم بِلِقَاءِ رَبِّهِمْ كَافِرُونَ ﴿١٠﴾ قُلْ يَتَوَفَّاكُم مَّلَكُ الْمَوْتِ الَّذِي وُكِّلَ بِكُمْ ثُمَّ إِلَى رَبِّكُمْ تُرْجَعُونَ ﴿١١﴾ وَلَوْ تَرَى إِذِ الْمُجْرِمُونَ نَاكِسُو رُؤُوسِهِمْ عِندَ رَبِّهِمْ رَبَّنَا أَبْصَرْنَا وَسَمِعْنَا فَارْجِعْنَا نَعْمَلْ صَالِحًا إِنَّا مُوقِنُونَ ﴿١٢﴾ وَلَوْ شِئْنَا لَآتَيْنَا كُلَّ نَفْسٍ هُدَاهَا وَلَكِنْ حَقَّ الْقَوْلُ مِنِّي لَأَمْلَأَنَّ جَهَنَّمَ مِنَ الْجِنَّةِ وَالنَّاسِ أَجْمَعِينَ ﴿١٣﴾ فَذُوقُوا بِمَا نَسِيتُمْ لِقَاءَ يَوْمِكُمْ هَذَا إِنَّا نَسِينَاكُمْ وَذُوقُوا عَذَابَ الْخُلْدِ بِمَا كُنتُمْ تَعْمَلُونَ ﴿١٤﴾ إِنَّمَا يُؤْمِنُ بِآيَاتِنَا الَّذِينَ إِذَا ذُكِّرُوا بِهَا خَرُّوا سُجَّدًا وَسَبَّحُوا بِحَمْدِ رَبِّهِمْ وَهُمْ لَا يَسْتَكْبِرُونَ ﴿١٥﴾ تَتَجَافَى جُنُوبُهُمْ عَنِ الْمَضَاجِعِ يَدْعُونَ رَبَّهُمْ خَوْفًا وَطَمَعًا وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ ﴿١٦﴾ فَلَا تَعْلَمُ نَفْسٌ مَّا أُخْفِيَ لَهُم مِّن قُرَّةِ أَعْيُنٍ جَزَاءً بِمَا كَانُوا يَعْمَلُونَ ﴿١٧﴾ أَفَمَن كَانَ مُؤْمِنًا كَمَن كَانَ فَاسِقًا لَّا يَسْتَوُونَ ﴿١٨﴾ أَمَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ فَلَهُمْ جَنَّاتُ الْمَأْوَى نُزُلًا بِمَا كَانُوا يَعْمَلُونَ ﴿١٩﴾ وَأَمَّا الَّذِينَ فَسَقُوا فَمَأْوَاهُمُ النَّارُ كُلَّمَا أَرَادُوا أَن يَخْرُجُوا مِنْهَا أُعِيدُوا فِيهَا وَقِيلَ لَهُمْ ذُوقُوا عَذَابَ النَّارِ الَّذِي كُنتُم بِهِ تُكَذِّبُونَ ﴿٢٠﴾ وَلَنُذِيقَنَّهُم مِّنَ الْعَذَابِ الْأَدْنَى دُونَ الْعَذَابِ الْأَكْبَرِ لَعَلَّهُمْ يَرْجَعُونَ ﴿٢١﴾ وَمَنْ أَظْلَمُ مِمَّن ذُكِّرَ بِآيَاتِ رَبِّهِ ثُمَّ أَعْرَضَ عَنْهَا إِنَّا مِنَ الْمُجْرِمُونَ مُنتَقِمُونَ ﴿٢٢﴾ وَلَقَدْ آتَيْنَا مُوسَى الْكِتَابَ فَلَا تَكُن فِي مِرْيَةٍ مِّن لِّقَائِهِ وَجَعَلْنَاهُ هُدًى لِّبَنِي إِسْرَائِيلَ ﴿٢٣﴾ وَجَعَلْنَا مِنْهُمْ أَئِمَّةً يَهْدُونَ بِأَمْرِنَا لَمَّا صَبَرُوا وَكَانُوا بِآيَاتِنَا يُوقِنُونَ ﴿٢٤﴾ إِنَّ رَبَّكَ هُوَ يَفْصِلُ بَيْنَهُمْ يَوْمَ الْقِيَامَةِ فِيمَا كَانُوا فِيهِ يَخْتَلِفُونَ ﴿٢٥﴾ أَوَلَمْ يَهْدِ لَهُمْ كَمْ أَهْلَكْنَا مِن قَبْلِهِم مِّنَ الْقُرُونِ يَمْشُونَ فِي مَسَاكِنِهِمْ إِنَّ فِي ذَلِكَ لَآيَاتٍ أَفَلَا يَسْمَعُونَ ﴿٢٦﴾ أَوَلَمْ يَرَوْا أَنَّا نَسُوقُ الْمَاءَ إِلَى الْأَرْضِ الْجُرُزِ فَنُخْرِجُ بِهِ زَرْعًا تَأْكُلُ مِنْهُ أَنْعَامُهُمْ وَأَنفُسُهُمْ أَفَلَا يُبْصِرُونَ ﴿٢٧﴾ وَيَقُولُونَ مَتَى هَذَا الْفَتْحُ إِن كُنتُمْ صَادِقِينَ ﴿٢٨﴾ قُلْ يَوْمَ الْفَتْحِ لَا يَنفَعُ الَّذِينَ كَفَرُوا إِيمَانُهُمْ وَلَا هُمْ يُنظَرُونَ ﴿٢٩﴾ فَأَعْرِضْ عَنْهُمْ وَانتَظِرْ إِنَّهُم مُّنتَظِرُونَ ﴿٣٠﴾
        """.trimIndent()

        title.contains("الكرسي") -> """
            اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ ﴿٢٥٥﴾
        """.trimIndent()

        title.contains("البقرة") -> """
            آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللَّهِ وَمَلآئِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لاَ نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ وَقَالُواْ سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ ﴿٢٨٥﴾ لاَ يُكَلِّفُ اللَّهُ نَفْساً إِلاَّ وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لاَ تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا رَبَّنَا وَلاَ تَحْمِلْ عَلَيْنَا إِصْراً كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا رَبَّنَا وَلاَ تُحَمِّلْنَا مَا لاَ طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَآ أَنتَ مَوْلاَنَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ ﴿٢٨٦﴾
        """.trimIndent()

        title.contains("الإخلاص") -> """
            قُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾
        """.trimIndent()

        title.contains("الفلق") -> """
            قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾
        """.trimIndent()

        title.contains("الناس") -> """
            قُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَهِ النَّاسِ ﴿٣﴾ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾
        """.trimIndent()

        else -> title
    }
}
