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

package app.ss.lessons.data.repository

import app.ss.storage.db.dao.PdfAnnotationsDao
import app.ss.storage.db.dao.ReadCommentsDao
import app.ss.storage.db.dao.ReadHighlightsDao
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responsible for user stored lesson data (Highlights, Comments, Annotations).
 */
@Singleton
class UserDataRepository @Inject constructor(
    private val readHighlightsDao: ReadHighlightsDao,
    private val readCommentsDao: ReadCommentsDao,
    private val pdfAnnotationsDao: PdfAnnotationsDao,
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider,
) {

    /**
     * Clears all cached user data.
     */
    suspend fun clear() {
        withContext(dispatcherProvider.io) {
            readHighlightsDao.clear()
            readCommentsDao.clear()
            pdfAnnotationsDao.clear()
            ssPrefs.clear()
        }
    }
}