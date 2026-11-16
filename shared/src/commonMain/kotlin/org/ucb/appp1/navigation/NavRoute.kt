package org.ucb.appp1.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute {

    @Serializable
    data object Home : NavRoute()

    @Serializable
    data object Vision : NavRoute()

    @Serializable
    data object Camera : NavRoute()

    @Serializable
    data object Scanner : NavRoute()

    @Serializable
    data object History : NavRoute()

    @Serializable
    data object Settings : NavRoute()
}
