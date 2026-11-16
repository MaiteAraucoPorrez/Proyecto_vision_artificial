package org.ucb.appp1

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ucb.appp1.core.util.OcrTextCleaner
import org.ucb.appp1.feature.camera.presentation.viewmodel.FrameResult
import org.ucb.appp1.feature.history.data.datasource.InMemoryHistoryDataSource
import org.ucb.appp1.feature.history.data.repository.HistoryRepositoryImpl
import org.ucb.appp1.feature.history.domain.usecase.SaveAnalysisUseCase
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScanKind
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannedCode
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerEvent
import org.ucb.appp1.feature.scanner.presentation.viewmodel.ScannerViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class OcrAndScannerTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun cleaner_dropsNumbersUnitsAndSymbols() {
        val raw = listOf("12", "320 px", "---", "a", "ok", "1920x1080", "Hola mundo", "  Salida   de   emergencia ")
        assertEquals(listOf("Hola mundo", "Salida de emergencia"), OcrTextCleaner.clean(raw))
    }

    @Test
    fun cleaner_removesDuplicatesIgnoringCase() {
        assertEquals(listOf("STOP"), OcrTextCleaner.clean(listOf("STOP", "stop", " Stop ")))
    }

    @Test
    fun cleaner_keepsMixedLinesWithEnoughLetters() {
        assertTrue(OcrTextCleaner.isMeaningful("Wi-Fi 5G"))
        assertFalse(OcrTextCleaner.isMeaningful("100%"))
        assertFalse(OcrTextCleaner.isMeaningful(""))
    }

    @Test
    fun scanner_savesNewCodeOnce_andKeepsLastCode() = runTest {
        val repo = HistoryRepositoryImpl(InMemoryHistoryDataSource())
        val vm = ScannerViewModel(SaveAnalysisUseCase(repo))
        val code = ScannedCode("https://ucb.edu.bo", "QR", ScanKind.Url)

        vm.emitEvent(ScannerEvent.OnCodesDetected(listOf(code), FrameResult()))
        vm.emitEvent(ScannerEvent.OnCodesDetected(listOf(code), FrameResult()))
        vm.emitEvent(ScannerEvent.OnCodesDetected(emptyList(), FrameResult()))

        assertEquals(code, vm.state.value.code)
        val saved = repo.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("qr", saved.first().source)
        assertEquals(listOf("https://ucb.edu.bo"), saved.first().texts)
    }

    @Test
    fun scanner_onlyHttpUrlsAreOpenable() {
        assertTrue(ScannedCode("https://a.com", "QR", ScanKind.Url).isOpenableUrl)
        assertFalse(ScannedCode("intent://x", "QR", ScanKind.Url).isOpenableUrl)
        assertFalse(ScannedCode("https://a.com", "QR", ScanKind.Text).isOpenableUrl)
    }

    @Test
    fun scanner_clearResetsCode() = runTest {
        val repo = HistoryRepositoryImpl(InMemoryHistoryDataSource())
        val vm = ScannerViewModel(SaveAnalysisUseCase(repo))
        vm.emitEvent(ScannerEvent.OnCodesDetected(listOf(ScannedCode("x1", "QR", ScanKind.Text)), FrameResult()))
        vm.emitEvent(ScannerEvent.OnClear)
        assertEquals(null, vm.state.value.code)
    }
}
