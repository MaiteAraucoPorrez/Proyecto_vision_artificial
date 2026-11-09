package org.ucb.appp1

import androidx.compose.ui.window.ComposeUIViewController

// iosApp lo llama como MainViewControllerKt.MainViewController(), por eso el nombre empieza con mayúscula.
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { App() }
