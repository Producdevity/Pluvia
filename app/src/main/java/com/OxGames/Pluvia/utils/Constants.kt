package com.OxGames.Pluvia.utils

/**
 * Constants values that may be used around the app more than once.
 * Constants that are used in composables and or viewmodels should be here too.
 */
object Constants {

    object XServer {
        const val DEFAULT_WINE_DEBUG_CHANNELS = "warn,err,fixme,loaddll"
        const val CONTAINER_PATTERN_COMPRESSION_LEVEL = 9
    }

    object Persona {
        const val AVATAR_BASE_URL = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/"
        const val MISSING_AVATAR_URL = "${AVATAR_BASE_URL}fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg"
    }

    object Misc {
        const val DONATION_LINK = "https://buy.stripe.com/5kAaFU1bx2RFeLmbII"
        const val GITHUB_LINK = "https://github.com/oxters168/Pluvia"
        const val PRIVACY_LINK = "https://github.com/oxters168/Pluvia/tree/master/PrivacyPolicy"
    }
}
