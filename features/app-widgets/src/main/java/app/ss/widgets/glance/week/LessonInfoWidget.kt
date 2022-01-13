/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.widgets.glance.week

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import app.ss.widgets.R
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.extensions.divider
import app.ss.widgets.glance.extensions.modifyAppWidgetBackground
import app.ss.widgets.glance.theme.SsAppWidgetTheme
import app.ss.widgets.glance.theme.copy
import app.ss.widgets.glance.theme.todayBody
import app.ss.widgets.glance.theme.todayTitle
import app.ss.widgets.model.WeekDayWidgetModel
import app.ss.widgets.model.WeekLessonWidgetModel
import com.cryart.design.theme.Spacing12
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing4
import com.cryart.design.theme.Spacing8
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class LessonInfoWidget @AssistedInject constructor(
    private val dataProvider: WidgetDataProvider,
    @Assisted private val context: Context,
) : BaseGlanceAppWidget<LessonInfoWidget.Data>(context = context) {

    data class Data(
        val model: WeekLessonWidgetModel? = null,
        val cover: Bitmap? = null
    )

    override suspend fun loadData(): Data {
        val model = dataProvider.getWeekLessonModel()
        val cover = context.fetchBitmap(model?.cover)
        return Data(model = model, cover = null) // struggling to show Bitmap in this widget 😢
    }

    @Composable
    override fun Content(data: Data?) {
        val default = context.getString(R.string.ss_widget_error_label)
        val model = data?.model
        val cover = data?.cover

        SsAppWidgetTheme {
            LazyColumn(
                modifier = GlanceModifier
                    .modifyAppWidgetBackground()
                    .padding(vertical = Spacing8)
            ) {
                item {
                    LessonInfoRow(
                        quarterlyTitle = model?.quarterlyTitle ?: default,
                        lessonTitle = model?.lessonTitle ?: default,
                        cover = cover
                    )
                }

                item {
                    Spacer(modifier = GlanceModifier.size(Spacing16))
                }

                item {
                    Spacer(modifier = GlanceModifier.divider())
                }

                val items = model?.days ?: emptyList()
                itemsIndexed(items) { index, item ->
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing12)
                    ) {
                        DayInfo(
                            model = item,
                            modifier = GlanceModifier.fillMaxWidth()
                        )

                        if (index != items.lastIndex) {
                            Spacer(modifier = GlanceModifier.divider())
                        }
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context): LessonInfoWidget
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun LessonInfoRow(
    quarterlyTitle: String,
    lessonTitle: String,
    cover: Bitmap?,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier.padding(Spacing12),
    ) {
        Spacer(modifier = GlanceModifier.width(Spacing12))

        cover?.let { bitmap ->
            Image(
                provider = BitmapImageProvider(bitmap),
                contentDescription = quarterlyTitle,
                modifier = GlanceModifier
                    .size(width = CoverWidth, height = CoverHeight)
                    .cornerRadius(Spacing4)
            )
        }

        Column(
            modifier = GlanceModifier.defaultWeight(),
        ) {
            Text(
                text = quarterlyTitle,
                style = todayTitle(),
                maxLines = 2,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(
                modifier = GlanceModifier
                    .height(Spacing8)
                    .fillMaxWidth()
            )

            Text(
                text = lessonTitle,
                style = todayBody().copy(
                    fontSize = 13.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )
        }

        Image(
            provider = ImageProvider(R.drawable.ic_widget_logo),
            contentDescription = LocalContext.current.getString(R.string.ss_app_name),
            modifier = GlanceModifier.size(AppLogoSize)
        )

        Spacer(modifier = GlanceModifier.width(Spacing12))
    }
}

private val CoverWidth = 64.dp
private val CoverHeight = 100.dp
private val AppLogoSize = 64.dp

@Composable
private fun DayInfo(
    model: WeekDayWidgetModel,
    modifier: GlanceModifier = GlanceModifier
) {
    val textStyle = todayBody().copy(
        fontSize = 13.sp
    )
    val titleStyle = if (model.today) {
        todayBody(MaterialTheme.colorScheme.onSurface)
            .copy(fontWeight = FontWeight.Bold)
    } else textStyle

    Row(modifier = modifier.padding(vertical = Spacing12)) {
        Text(
            text = model.title,
            style = titleStyle,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(modifier = GlanceModifier.width(Spacing8))

        Text(
            text = model.date,
            style = textStyle,
            maxLines = 1,
            modifier = GlanceModifier
        )
    }
}
