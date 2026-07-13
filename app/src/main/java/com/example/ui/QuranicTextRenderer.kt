package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Converts Western digits in a string to Arabic-Indic digits (٠, ١, ٢, ...).
 */
fun convertToArabicNumerals(text: String): String {
    val arabicNumerals = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder()
    for (char in text) {
        if (char in '0'..'9') {
            builder.append(arabicNumerals[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

/**
 * Renders Arabic text beautifully, converting any verse numbers inside ﴿ ﴾ or parentheses
 * into premium, circular gold-rimmed stickers just like an authentic Mushaf.
 */
@Composable
fun QuranicTextRenderer(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF1A3140), // Premium Dark Slate/Charcoal for Mushaf page
    fontSize: Float = 26f,
    lineHeightScale: Float = 1.8f,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip
) {
    // Regex matches ﴿number﴾, [number], or (number) inside Quranic texts supporting both Western and Arabic-Indic digits
    val regex = Regex("[﴿\\(\\[]\\s*([0-9٠-٩]+)\\s*[﴾\\)\\]]")
    val matches = remember(text) { regex.findAll(text).toList() }

    if (matches.isEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineHeightScale).sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            color = baseColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
        return
    }

    val inlineContent = remember(text) { mutableMapOf<String, InlineTextContent>() }
    val annotatedString = remember(text) {
        buildAnnotatedString {
            var lastIndex = 0
            matches.forEachIndexed { index, match ->
                val start = match.range.first
                val end = match.range.last + 1
                
                // Append text before the match
                if (start > lastIndex) {
                    append(text.substring(lastIndex, start))
                }
                
                val rawNumber = match.groupValues[1].trim()
                val arabicNumber = convertToArabicNumerals(rawNumber)
                val inlineKey = "verse_${index}_${rawNumber}"
                
                // Append inline content placeholder
                appendInlineContent(inlineKey, " $arabicNumber ")
                
                // Design the custom premium inline verse sticker
                inlineContent[inlineKey] = InlineTextContent(
                    Placeholder(
                        width = (fontSize * 1.15f).sp,
                        height = (fontSize * 1.15f).sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size((fontSize * 0.95f).dp)
                            .border(1.5.dp, Color(0xFFC5A85C), CircleShape) // Warm golden border
                            .background(Color(0xFFFFF9E6), CircleShape), // Gold tint background
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = arabicNumber,
                            fontSize = (fontSize * 0.45f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8C6D23), // Deep golden brown
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
                
                lastIndex = end
            }
            
            // Append remaining text
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            lineHeight = (fontSize * lineHeightScale).sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        ),
        color = baseColor,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}
