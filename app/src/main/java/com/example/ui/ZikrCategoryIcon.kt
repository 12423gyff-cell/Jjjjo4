package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ZikrCategoryIcon(
    title: String,
    modifier: Modifier = Modifier
) {
    val goldColor = MaterialTheme.colorScheme.secondary
    val innerDarkBg = Color(0xFF10202B)

    val icon: ImageVector = remember(title) {
        when {
            title.contains("الاستيقاظ") || title.contains("استيقاظ") -> Icons.Default.WbSunny
            title.contains("فضل الذكر") || title.contains("تلاوة") || title.contains("قرآن") -> Icons.Default.MenuBook
            title.contains("الخلاء") || title.contains("الوضوء") -> Icons.Default.WaterDrop
            title.contains("المسجد") || title.contains("الآذان") || title.contains("الصلاة") || title.contains("الركوع") || title.contains("السجود") -> Icons.Default.Mosque
            title.contains("الصباح") -> Icons.Default.WbTwilight
            title.contains("المساء") -> Icons.Default.NightsStay
            title.contains("النوم") || title.contains("الرؤيا") -> Icons.Default.Bedtime
            title.contains("طعام") || title.contains("شراب") || title.contains("الصائم") -> Icons.Default.Restaurant
            title.contains("سفر") || title.contains("ركوب") || title.contains("دخول") || title.contains("خروج") -> Icons.Default.DirectionsCar
            title.contains("تسبيح") || title.contains("استغفار") -> Icons.Default.Loop
            else -> Icons.Default.StarBorder
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = innerDarkBg,
        border = BorderStroke(1.dp, goldColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = goldColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun getZikrIconGridIndex(title: String): Int {
    return when {
        // Row 0
        title.contains("الاستيقاظ") || title.contains("استيقاظ") -> 0
        title.contains("فضل الذكر") || title.contains("فضل الصلاة") || title.contains("فضل تلاوة") || title.contains("تلاوة") -> 1
        title.contains("لبس الثوب الجديد") || title.contains("الثوب الجديد") -> 3
        title.contains("لبس الثوب") || title.contains("ثوب") && !title.contains("وضع") -> 2
        title.contains("من لبس ثوباً") -> 4
        title.contains("وضع الثوب") -> 5
        title.contains("دخول الخلاء") -> 6
        title.contains("الخروج من الخلاء") -> 7
        title.contains("قبل الوضوء") -> 8
        title.contains("بعد الفراغ من الوضوء") || title.contains("بعد الوضوء") -> 9

        // Row 1
        title.contains("الخروج من المنزل") -> 10
        title.contains("دخول المنزل") -> 11
        title.contains("الذهاب إلى المسجد") -> 12
        title.contains("دخول المسجد") -> 13
        title.contains("الخروج من المسجد") -> 14
        title.contains("أذكار الآذان") || title.contains("الآذان") || title.contains("الأذان") -> 15
        title.contains("الاستفتاح") -> 16
        title.contains("الركوع") -> 17
        title.contains("الرفع من الركوع") -> 18
        title.contains("السجود") -> 19

        // Row 2
        title.contains("الجلسة بين السجدتين") -> 20
        title.contains("سجود التلاوة") -> 21
        title.contains("التشهد") && !title.contains("بعد") -> 22
        title.contains("الصلاة على النبي") -> 23
        title.contains("الدعاء بعد التشهد") -> 24
        title.contains("الأذكار بعد السلام") || title.contains("بعد السلام") -> 25
        title.contains("الاستخارة") || title.contains("صلاة الاستخارة") -> 26
        title.contains("الصباح") || title.contains("المساء") -> 27
        title.contains("النوم") && !title.contains("الاستيقاظ") && !title.contains("تقلب") && !title.contains("الفزع") -> 28
        title.contains("تقلب في الليل") || title.contains("تقلب") -> 29

        // Row 3
        title.contains("الوحشة") || title.contains("الفزع في النوم") -> 30
        title.contains("الرؤيا") || title.contains("الحلم") -> 31
        title.contains("قنوت الوتر") || title.contains("قنوت") -> 32
        title.contains("عقب السلام من الوتر") || title.contains("عقب الوتر") || title.contains("عقب السلام") -> 33
        title.contains("الهم والحزن") -> 34
        title.contains("الكرب") -> 35
        title.contains("لقاء العدو") -> 36
        title.contains("خاف قوما") || title.contains("الخوف من") -> 37
        title.contains("وسوسة في الإيمان") || title.contains("وسوسة") -> 38
        title.contains("قضاء الدين") || title.contains("الدين") -> 39

        // Row 4
        title.contains("استصعب") || title.contains("تعسر") -> 40
        title.contains("طرد الشيطان") || title.contains("الشيطان") -> 41
        title.contains("أصيب بمصيبة") || title.contains("مصيبة") -> 42
        title.contains("تلقين المحتضر") || title.contains("المحتضر") -> 43
        title.contains("إغماض الميت") -> 44
        title.contains("الصلاة عليه") || title.contains("صلاة الجنازة") || title.contains("للفرط") -> 45
        title.contains("التعزية") -> 46
        title.contains("إدخل الميت") || title.contains("إدخال الميت") -> 47
        title.contains("بعد دفن الميت") || title.contains("دفن") -> 48
        title.contains("زيارة القبور") || title.contains("القبور") -> 49

        // Row 5
        title.contains("الريح") -> 50
        title.contains("الرعد") -> 51
        title.contains("الاستسقاء") -> 52
        title.contains("الدعاء إذا نزل المطر") || title.contains("المطر") && !title.contains("نزول") -> 53
        title.contains("الذكر بعد نزول المطر") || title.contains("نزول المطر") -> 54
        title.contains("رؤية الهلال") || title.contains("الهلال") -> 55
        title.contains("عند إفطار الصائم") || title.contains("إفطار الصائم") || title.contains("إفطار") -> 56
        title.contains("الضيف لصاحب الطعام") || title.contains("الضيف") -> 57
        title.contains("طلب الطعام") || title.contains("حضر الطعام") -> 58
        title.contains("سابه أحد") -> 59

        // Row 6
        title.contains("باكورة الثمر") || title.contains("أول الثمر") -> 60
        title.contains("العطاس") || title.contains("عطس") && !title.contains("للكافر") -> 61
        title.contains("للكافر إذا عطس") || title.contains("للكافر") -> 62
        title.contains("المتزوج") && !title.contains("الجماع") -> 63
        title.contains("الجماع") || title.contains("إتيان الزوجة") -> 64
        title.contains("الغضب") -> 65
        title.contains("رأى مبتلى") || title.contains("مبتلى") -> 66
        title.contains("غفر الله لك") -> 67
        title.contains("أحبك في الله") || title.contains("أحبك") -> 68
        title.contains("عرض عليك ماله") -> 69

        // Row 7
        title.contains("أقرض عند القضاء") || title.contains("أقرض") -> 70
        title.contains("الخوف من الشرك") || title.contains("الشرك") -> 71
        title.contains("بارك الله فيك") -> 72
        title.contains("كراهية الطيرة") || title.contains("الطيرة") -> 73
        title.contains("الركوب") -> 74
        title.contains("السفر") -> 75
        title.contains("دخول القرية") -> 76
        title.contains("دخول السوق") || title.contains("السوق") -> 77
        title.contains("تعس المركوب") -> 78
        title.contains("المسافر للمقيم") -> 79

        // Row 8
        title.contains("المقيم للمسافر") -> 80
        title.contains("إذا أسحر") || title.contains("المسافر إذا أسحر") -> 81
        title.contains("الرجوع من السفر") -> 82
        title.contains("يسره أو يكرهه") || title.contains("أمر يسره") -> 83
        title.contains("إفشاء السلام") || title.contains("السلام") && !title.contains("بعد") && !title.contains("الكافر") -> 84
        title.contains("صياح الديك") || title.contains("الديك") || title.contains("الحمار") -> 85
        title.contains("نباح الكلب") || title.contains("الكلب") -> 86
        title.contains("سببته") -> 87
        title.contains("مدح المسلم") || title.contains("مدح") -> 88
        title.contains("زكي") -> 89

        // Row 9
        title.contains("الركن الأسود") || title.contains("الحجر الأسود") || title.contains("اليماني") -> 90
        title.contains("الصفا والمروة") -> 91
        title.contains("يوم عرفة") || title.contains("عرفة") -> 92
        title.contains("المشعر الحرام") -> 93
        title.contains("رمي الجمار") -> 94
        title.contains("عند الذبح") || title.contains("الذبح") -> 95
        title.contains("لرد كيد") || title.contains("مردة الشياطين") -> 96
        title.contains("كيف كان النبي يسبح") -> 97
        title.contains("الخير والآداب") -> 98
        title.contains("التعجب") -> 99

        // Row 10
        title.contains("وجعا في جسده") || title.contains("وجع") -> 100
        title.contains("يصيب شيئا بعينه") || title.contains("أصيب بعينه") -> 101
        title.contains("الاستغفار و التوبة") || title.contains("استغفار") -> 102
        title.contains("التسبيح، التحميد") -> 103
        title.contains("فضل الصلاة على النبي") -> 104
        title.contains("يرد السلام على الكافر") -> 105
        title.contains("يلبي المحرم") -> 106
        title.contains("أذنب ذنبا") -> 107
        title.contains("يعوذ به الأولاد") || title.contains("رقية") -> 108
        title.contains("تهنئة المولود") -> 109

        // Row 11
        title.contains("ما يقال في المجلس") || title.contains("مجلس") && !title.contains("كفارة") -> 110
        title.contains("كفارة المجلس") || title.contains("كفارة") -> 111
        title.contains("صنع إليك معروفا") -> 112
        title.contains("يعصم الله به من الدجال") -> 113
        title.contains("التكبير و التسبيح في سير السفر") -> 114
        title.contains("نزل منزلا في سفر") -> 115
        title.contains("أفطر عند أهل بيت") -> 116
        title.contains("الاستصحاء") -> 117
        title.contains("فضل عيادة المريض") || title.contains("عيادة") -> 118
        title.contains("يئس من حياته") || title.contains("المريض") -> 119

        // Row 12
        title.contains("ظلم السلطان") || title.contains("سلطان") -> 120
        title.contains("على العدو") || title.contains("العدو") -> 121
        title.contains("يقع ما لا يرضاه") -> 122
        title.contains("الرُّقية") || title.contains("الرقية") -> 123

        else -> 1 // default to general virtue of zikr
    }
}

fun getScenicBgForCategory(title: String): Int {
    return when {
        title.contains("الصباح") -> com.example.R.drawable.morning_bg
        title.contains("المساء") -> com.example.R.drawable.evening_bg
        title.contains("النوم") || title.contains("الرؤيا") || title.contains("وحشة") || title.contains("الفزع") -> com.example.R.drawable.sleep_bg
        title.contains("المسجد") || title.contains("قنوت") || title.contains("وتر") -> com.example.R.drawable.mosque_bg
        title.contains("الصلاة") || title.contains("الركوع") || title.contains("السجود") || title.contains("التشهد") || title.contains("الرفع") || title.contains("سجود التلاوة") || title.contains("الجلسة بين") -> com.example.R.drawable.zikr_prayer
        title.contains("قرآن") || title.contains("تلاوة") -> com.example.R.drawable.quran_bg
        title.contains("استغفار") || title.contains("توبة") || title.contains("تسبيح") || title.contains("التحميد") || title.contains("التهليل") || title.contains("التكبير") -> com.example.R.drawable.cat_tasbeeh_1783655934557
        title.contains("سفر") || title.contains("ركوب") || title.contains("المركوب") || title.contains("المسافر") -> com.example.R.drawable.travel_bg
        title.contains("طعام") || title.contains("شراب") || title.contains("الضيف") || title.contains("الصائم") || title.contains("إفطار") -> com.example.R.drawable.food_bg
        title.contains("الوضوء") || title.contains("الخلاء") || title.contains("حمام") -> com.example.R.drawable.water_bg
        else -> com.example.R.drawable.cat_general_1783655981915
    }
}

fun getCategorySubtext(title: String): Pair<String, String> {
    return when {
        title.contains("الصباح") -> Pair("Morning Blessings", "دعاء الصباح والرزق")
        title.contains("المساء") -> Pair("Evening Supplications", "دعاء المساء وحفظ النفس")
        title.contains("النوم") || title.contains("الرؤيا") -> Pair("Before Sleeping", "دعاء قبل النوم والراحة")
        title.contains("المسجد") -> Pair("Etiquette and Prayers", "دعاء ودخول المسجد")
        title.contains("الصلاة") -> Pair("Prayer & Remembrance", "أذكار الصلاة المفروضة")
        title.contains("قرآن") || title.contains("تلاوة") -> Pair("Holy Quran Reading", "فضل تلاوة وحفظ القرآن")
        title.contains("استغفار") || title.contains("تسبيح") -> Pair("Praise & Repentance", "الاستغفار والتسبيح اليومي")
        title.contains("سفر") || title.contains("ركوب") -> Pair("Travel & Riding", "دعاء الركوب والسفر")
        title.contains("طعام") || title.contains("شراب") -> Pair("Food and Drinks", "أدعية الطعام والشراب والضيف")
        title.contains("الوضوء") || title.contains("الخلاء") -> Pair("Ablution & Purity", "أذكار الوضوء والخلاء")
        else -> Pair("Islamic Supplications", "أدعية وأذكار نافعة")
    }
}
