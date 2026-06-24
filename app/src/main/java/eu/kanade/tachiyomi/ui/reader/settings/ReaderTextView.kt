package eu.kanade.tachiyomi.ui.reader.settings

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import uy.kohesive.injekt.injectLazy
import yokai.i18n.MR
import yokai.presentation.theme.YokaiTheme

class ReaderTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {

    private val preferences: PreferencesHelper by injectLazy()

    init {
        setBackgroundResource(R.drawable.bottom_sheet_rounded_background)
    }

    @Composable
    override fun Content() {
        YokaiTheme {
            TextSettings(preferences)
        }
    }
}

@Composable
private fun TextSettings(preferences: PreferencesHelper) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        var size by remember { mutableIntStateOf(preferences.readerTextSize().get()) }
        SliderSetting(
            label = stringResource(MR.strings.text_size),
            value = "${size}sp",
            position = size.toFloat(),
            range = 12f..32f,
        ) {
            size = it.toInt()
            preferences.readerTextSize().set(size)
        }

        var spacing by remember { mutableIntStateOf(preferences.readerLineSpacing().get()) }
        SliderSetting(
            label = stringResource(MR.strings.line_spacing),
            value = String.format("%.1f", spacing / 10f),
            position = spacing.toFloat(),
            range = 10f..20f,
        ) {
            spacing = it.toInt()
            preferences.readerLineSpacing().set(spacing)
        }

        var argb by remember { mutableIntStateOf(preferences.readerTextColor().get()) }
        var custom by remember { mutableStateOf(argb != 0) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(MR.strings.text_color),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (custom) {
                    Spacer(Modifier.size(24.dp).background(Color(argb), RoundedCornerShape(6.dp)))
                    Spacer(Modifier.width(12.dp))
                }
                Switch(
                    checked = custom,
                    onCheckedChange = {
                        custom = it
                        if (it) {
                            if (argb == 0) argb = Color.White.toArgb()
                            preferences.readerTextColor().set(argb)
                        } else {
                            argb = 0
                            preferences.readerTextColor().set(0)
                        }
                    },
                )
            }
            if (custom) {
                val apply = { r: Int, g: Int, b: Int ->
                    argb = Color(r, g, b).toArgb()
                    preferences.readerTextColor().set(argb)
                }
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF
                ColorChannel("R", r) { apply(it, g, b) }
                ColorChannel("G", g) { apply(r, it, b) }
                ColorChannel("B", b) { apply(r, g, it) }
            }
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: String,
    position: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(value = position, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun ColorChannel(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
    }
}
