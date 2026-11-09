package org.ucb.appp1.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute {

    @Serializable
    data object Vision : NavRoute()

    @Serializable
    data object Localization : NavRoute()

    @Serializable
    data object DesignSystem : NavRoute()
}
