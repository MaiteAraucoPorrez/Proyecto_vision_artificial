package org.ucb.appp1

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ucb.appp1.feature.history.data.datasource.InMemoryHistoryDataSource
import org.ucb.appp1.feature.history.data.repository.HistoryRepositoryImpl
import org.ucb.appp1.feature.history.domain.usecase.ClearHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.DeleteAnalysisUseCase
import org.ucb.appp1.feature.history.domain.usecase.GetHistoryUseCase
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryEvent
import org.ucb.appp1.feature.history.presentation.viewmodel.HistoryViewModel
import org.ucb.appp1.feature.vision.data.repository.VisionRepositoryImpl
import org.ucb.appp1.feature.vision.domain.model.VisionResult
import org.ucb.appp1.feature.vision.domain.repository.VisionRepository
import org.ucb.appp1.feature.vision.domain.quality.ImageQualityEvaluator
import org.ucb.appp1.feature.vision.domain.quality.QualityIssue
import org.ucb.appp1.feature.vision.domain.usecase.AnalyzeImageUseCase
import org.ucb.appp1.feature.vision.domain.usecase.DescribeImageUseCase
import org.ucb.appp1.feature.vision.domain.repository.ImageDescriptionRepository
import org.ucb.appp1.feature.vision.data.datasource.ImageAnalyzerDataSource
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionEvent
import org.ucb.appp1.feature.vision.presentation.viewmodel.VisionViewModel

private class FakeVisionRepository(private val fail: Boolean = false) : VisionRepository {
    override suspend fun analyzeImage(imageUri: String): VisionResult {
        if (fail) error("boom")
        return VisionResult(imageUri, listOf("Dog (90%)"), listOf("STOP"), 1L)
    }
}

private class FakeAnalyzer : ImageAnalyzerDataSource {
    override suspend fun analyze(imageUri: String) = VisionResult(imageUri, listOf("Cat"), emptyList(), 5L)
}

@OptIn(ExperimentalCoroutinesApi::class)
class VisionAndHistoryTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun historyUseCases(): Triple<InMemoryHistoryDataSource, SaveAnalysisUseCase, HistoryRepositoryImpl> {
        val ds = InMemoryHistoryDataSource()
        val repo = HistoryRepositoryImpl(ds)
        return Triple(ds, SaveAnalysisUseCase(repo), repo)
    }

    @Test
    fun analyzeUseCase_success() = runTest {
        val result = AnalyzeImageUseCase(FakeVisionRepository())("uri")
        assertTrue(result.isSuccess)
        assertEquals(listOf("Dog (90%)"), result.getOrNull()?.labels)
    }

    @Test
    fun analyzeUseCase_failure() = runTest {
        assertTrue(AnalyzeImageUseCase(FakeVisionRepository(fail = true))("uri").isFailure)
    }

    @Test
    fun visionRepository_delegatesToDataSource() = runTest {
        val result = VisionRepositoryImpl(FakeAnalyzer()).analyzeImage("u")
        assertEquals(listOf("Cat"), result.labels)
    }

    @Test
    fun visionViewModel_analyzeSuccess_updatesStateAndSavesHistory() = runTest {
        val (_, save, repo) = historyUseCases()
        val vm = VisionViewModel(AnalyzeImageUseCase(FakeVisionRepository()), save, describer())
        vm.emitEvent(VisionEvent.OnImageSelected("content://img"))
        vm.emitEvent(VisionEvent.OnAnalyzeClick)

        assertEquals(false, vm.state.value.isLoading)
        assertNotNull(vm.state.value.result)
        assertNull(vm.state.value.error)
        val saved = repo.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("gallery", saved.first().source)
    }

    @Test
    fun visionViewModel_analyzeFailure_setsError() = runTest {
        val (_, save, _) = historyUseCases()
        val vm = VisionViewModel(AnalyzeImageUseCase(FakeVisionRepository(fail = true)), save, describer())
        vm.emitEvent(VisionEvent.OnImageSelected("content://img"))
        vm.emitEvent(VisionEvent.OnAnalyzeClick)

        assertEquals("boom", vm.state.value.error)
        assertNull(vm.state.value.result)
    }

    @Test
    fun visionViewModel_analyzeWithoutImage_doesNothing() = runTest {
        val (_, save, _) = historyUseCases()
        val vm = VisionViewModel(AnalyzeImageUseCase(FakeVisionRepository()), save, describer())
        vm.emitEvent(VisionEvent.OnAnalyzeClick)
        assertEquals(false, vm.state.value.isLoading)
        assertNull(vm.state.value.result)
    }

    @Test
    fun visionViewModel_clearResetsState() = runTest {
        val (_, save, _) = historyUseCases()
        val vm = VisionViewModel(AnalyzeImageUseCase(FakeVisionRepository()), save, describer())
        vm.emitEvent(VisionEvent.OnImageSelected("content://img"))
        vm.emitEvent(VisionEvent.OnClearResult)
        assertNull(vm.state.value.selectedImageUri)
    }

    @Test
    fun historyViewModel_saveDeleteClear() = runTest {
        val (_, save, repo) = historyUseCases()
        save("gallery", listOf("A"), emptyList())
        save("camera", emptyList(), listOf("t"))
        val vm = HistoryViewModel(GetHistoryUseCase(repo), DeleteAnalysisUseCase(repo), ClearHistoryUseCase(repo))

        assertEquals(2, vm.state.value.items.size)
        assertEquals(false, vm.state.value.isLoading)

        vm.emitEvent(HistoryEvent.OnDelete(vm.state.value.items.first().id))
        assertEquals(1, vm.state.value.items.size)

        vm.emitEvent(HistoryEvent.OnClearAll)
        assertTrue(vm.state.value.items.isEmpty())
    }
}

private fun describer() = DescribeImageUseCase(FakeDescriptions())

private class FakeDescriptions(private val fail: Boolean = false) : ImageDescriptionRepository {
    override suspend fun describe(imageUri: String): String {
        if (fail) error("sin internet")
        return "Un perro en un parque."
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DescribeAndQualityTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(fail: Boolean): VisionViewModel {
        val repo = HistoryRepositoryImpl(InMemoryHistoryDataSource())
        return VisionViewModel(
            AnalyzeImageUseCase(FakeVisionRepository()),
            SaveAnalysisUseCase(repo),
            DescribeImageUseCase(FakeDescriptions(fail))
        )
    }

    @Test
    fun describe_success_setsDescription() = runTest {
        val vm = vm(fail = false)
        vm.emitEvent(VisionEvent.OnImageSelected("content://img"))
        vm.emitEvent(VisionEvent.OnDescribeClick)
        assertEquals("Un perro en un parque.", vm.state.value.aiDescription)
        assertEquals(false, vm.state.value.isDescribing)
    }

    @Test
    fun describe_failure_setsAiError() = runTest {
        val vm = vm(fail = true)
        vm.emitEvent(VisionEvent.OnImageSelected("content://img"))
        vm.emitEvent(VisionEvent.OnDescribeClick)
        assertEquals("sin internet", vm.state.value.aiError)
        assertNull(vm.state.value.aiDescription)
    }

    @Test
    fun quality_uniformImage_isBlurryAndLowContrast() {
        val gray = IntArray(64 * 64) { 128 }
        val q = ImageQualityEvaluator.evaluate(gray, 64, 64)
        assertTrue(QualityIssue.Blurry in q.issues)
        assertTrue(QualityIssue.LowContrast in q.issues)
    }

    @Test
    fun quality_checkerboard_isSharpAndGood() {
        val gray = IntArray(64 * 64) { i -> if ((i % 64 + i / 64) % 2 == 0) 40 else 220 }
        val q = ImageQualityEvaluator.evaluate(gray, 64, 64)
        assertTrue(q.sharpness > ImageQualityEvaluator.BLUR_THRESHOLD)
        assertTrue(q.isGood)
    }

    @Test
    fun quality_darkAndBrightImages_areFlagged() {
        val checker = { low: Int, high: Int ->
            IntArray(64 * 64) { i -> if ((i % 64 + i / 64) % 2 == 0) low else high }
        }
        assertTrue(QualityIssue.Dark in ImageQualityEvaluator.evaluate(checker(5, 40), 64, 64).issues)
        assertTrue(QualityIssue.Bright in ImageQualityEvaluator.evaluate(checker(215, 255), 64, 64).issues)
    }

    @Test
    fun quality_smoothGradient_isBlurry() {
        val gray = IntArray(64 * 64) { i -> (i % 64) * 3 }
        assertTrue(QualityIssue.Blurry in ImageQualityEvaluator.evaluate(gray, 64, 64).issues)
    }
}
