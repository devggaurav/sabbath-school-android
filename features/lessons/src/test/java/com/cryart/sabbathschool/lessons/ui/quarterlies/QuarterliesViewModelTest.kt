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

package com.cryart.sabbathschool.lessons.ui.quarterlies

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.ss.models.QuarterlyGroup
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class QuarterliesViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val dispatcherProvider = TestDispatcherProvider()

    private val mockRepository: QuarterliesRepository = mockk(relaxed = true)
    private val mockFirebaseAuth: FirebaseAuth = mockk()
    private val mockSSPrefs: SSPrefs = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()

    private lateinit var viewModel: QuarterliesViewModel

    @Before
    fun setup() {
        every { mockSSPrefs.getLanguageCodeFlow() }.returns(flowOf("en"))
        every { mockSavedStateHandle.get<QuarterlyGroup>(SSConstants.SS_QUARTERLY_GROUP) }.returns(null)

        viewModel = QuarterliesViewModel(
            mockRepository,
            mockSSPrefs,
            mockFirebaseAuth,
            mockSavedStateHandle,
        )
    }

    @Test
    @Ignore("Flaky test")
    fun `should update selected language and quarterlies list`() = runTest {
        // given
        val language = "de"

        coEvery { mockRepository.getQuarterlies(any(), null) }.returns(
            flowOf(
                Resource.success(emptyList())
            )
        )
        every { mockSSPrefs.setLanguageCode(language) }.returns(Unit)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(true)

        // when
        viewModel.quarterliesFlow.test {
            viewModel.languageSelected(language)

            verify { mockSSPrefs.setLanguageCode(language) }

            awaitItem().status shouldBeEqualTo Status.LOADING
            // awaitItem().status shouldBeEqualTo Status.SUCCESS
        }
    }
}
