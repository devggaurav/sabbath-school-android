/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.lessons.data.repository.lessons

import app.ss.lessons.data.model.PdfAnnotations
import app.ss.lessons.data.model.QuarterlyLessonInfo
import app.ss.lessons.data.model.TodayData
import app.ss.lessons.data.model.WeekData
import app.ss.lessons.data.model.WeekDay
import app.ss.lessons.data.repository.mediator.QuarterliesDataSource
import app.ss.lessons.data.repository.mediator.QuarterlyInfoDataSource
import app.ss.models.SSLessonInfo
import app.ss.models.SSQuarterlyInfo
import app.ss.models.SSRead
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.DateHelper.formatDate
import com.cryart.sabbathschool.core.misc.DateHelper.parseDate
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LessonsRepositoryImpl @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val quarterliesDataSource: QuarterliesDataSource,
    private val quarterlyInfoDataSource: QuarterlyInfoDataSource,
    private val lessonInfoDataSource: LessonInfoDataSource,
    private val readsDataSource: ReadsDataSource,
    private val pdfAnnotationsDataSource: PdfAnnotationsDataSource,
) : LessonsRepository {

    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> =
        lessonInfoDataSource.getItem(LessonInfoDataSource.Request(lessonIndex))

    override suspend fun getTodayRead(): Resource<TodayData> {
        val dataResponse = getQuarterlyAndLessonInfo()
        val lessonInfo = dataResponse.data?.lessonInfo ?: return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))

        val today = DateTime.now().withTimeAtStartOfDay()
        val todayModel = lessonInfo.days.find { day ->
            today.isEqual(parseDate(day.date))
        }?.let { day ->
            TodayData(
                day.index,
                lessonInfo.lesson.index,
                day.title,
                formatDate(day.date),
                lessonInfo.lesson.cover
            )
        } ?: return Resource.error(Throwable("Error Finding Today Read"))

        return Resource.success(todayModel)
    }

    private suspend fun getQuarterlyAndLessonInfo(): Resource<QuarterlyLessonInfo> {
        val quarterlyResponse = getQuarterlyInfo()
        val quarterlyInfo = quarterlyResponse.data ?: return Resource.error(quarterlyResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        val lessonInfo = getWeekLessonInfo(quarterlyInfo) ?: return Resource.error(Throwable("Invalid LessonInfo"))

        return Resource.success(QuarterlyLessonInfo(quarterlyInfo, lessonInfo))
    }

    private suspend fun getQuarterlyInfo(): Resource<SSQuarterlyInfo> {
        val index = getLastQuarterlyInfoIfCurrent()?.let {
            return Resource.success(it)
        } ?: getDefaultQuarterlyIndex() ?: return Resource.error(Throwable("Invalid Quarterly Index"))

        return quarterlyInfoDataSource.cache.getItem(QuarterlyInfoDataSource.Request(index))
    }

    private suspend fun getLastQuarterlyInfoIfCurrent(): SSQuarterlyInfo? {
        val index = ssPrefs.getLastQuarterlyIndex() ?: return null
        val info = quarterlyInfoDataSource.cache.getItem(QuarterlyInfoDataSource.Request(index)).data ?: return null

        val today = DateTime.now().withTimeAtStartOfDay()
        return if (today.isBefore(parseDate(info.quarterly.end_date))) {
            info
        } else {
            null
        }
    }

    private suspend fun getDefaultQuarterlyIndex(): String? {
        val resource = quarterliesDataSource.cache.get(QuarterliesDataSource.Request(ssPrefs.getLanguageCode()))
        val quarterly = resource.data?.firstOrNull()
        return quarterly?.index
    }

    private suspend fun getWeekLessonInfo(quarterlyInfo: SSQuarterlyInfo): SSLessonInfo? {
        val lesson = quarterlyInfo.lessons.find { lesson ->
            val startDate = parseDate(lesson.start_date)
            val endDate = parseDate(lesson.end_date)

            val today = DateTime.now().withTimeAtStartOfDay()
            startDate?.isBeforeNow == true && (endDate?.isAfterNow == true || today.isEqual(endDate))
        }

        return lesson?.let { getLessonInfo(it.index).data }
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> =
        readsDataSource.getItem(ReadsDataSource.Request(dayIndex))

    override suspend fun getWeekData(): Resource<WeekData> {
        val dataResponse = getQuarterlyAndLessonInfo()
        val (quarterlyInfo, lessonInfo) = dataResponse.data ?: return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        val today = DateTime.now().withTimeAtStartOfDay()

        val days = lessonInfo.days.map { ssDay ->
            WeekDay(
                ssDay.index,
                ssDay.title,
                formatDate(ssDay.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT),
                today.isEqual(parseDate(ssDay.date))
            )
        }.take(7)

        return Resource.success(
            WeekData(
                quarterlyInfo.quarterly.index,
                quarterlyInfo.quarterly.title,
                lessonInfo.lesson.index,
                lessonInfo.lesson.title,
                quarterlyInfo.quarterly.cover,
                days
            )
        )
    }

    override suspend fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>) {
        pdfAnnotationsDataSource.sync(
            PdfAnnotationsDataSource.Request(lessonIndex, pdfId),
            annotations
        )
    }

    override suspend fun getAnnotations(
        lessonIndex: String,
        pdfId: String
    ): Flow<Resource<List<PdfAnnotations>>> = pdfAnnotationsDataSource.getAsFlow(
        PdfAnnotationsDataSource.Request(lessonIndex, pdfId)
    )
}
