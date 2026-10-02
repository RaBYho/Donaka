package com.example.donaka100.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// =========================================================================
// ARTISAN WARMTH — palette brute
// Source de vérité : DESIGN.md. Ne jamais utiliser ces constantes
// directement dans un écran : passer par MaterialTheme.colorScheme.*
// ou par les extensions sémantiques en bas de fichier.
// =========================================================================

// --- Brand : Terracotta / Warm Clay --------------------------------------
internal val Terracotta            = Color(0xFFD96B27)
internal val TerracottaDeep        = Color(0xFFB55215)
internal val TerracottaWash        = Color(0xFFFDF3EB)
internal val TerracottaInverse     = Color(0xFFFFB690)

// --- Neutres structurants ------------------------------------------------
internal val Oatmeal               = Color(0xFFF9F8F6)
internal val Wheat                 = Color(0xFFF3F1EC)
internal val WheatDeep             = Color(0xFFEDEAE3)
internal val WheatDeeper           = Color(0xFFE7E3DA)
internal val PureWhite             = Color(0xFFFFFFFF)
internal val CharcoalSlate         = Color(0xFF1E293B)
internal val SlateMineral          = Color(0xFF64748B)
internal val StoneMist             = Color(0xFF94A3B8)
internal val FlourBorder           = Color(0xFFE5E7EB)
internal val CloudWhite            = Color(0xFFF1F5F9)

// --- Sémantique : Cash In / succès (Fresh Emerald) -----------------------
internal val Emerald               = Color(0xFF10B981)
internal val EmeraldDeep           = Color(0xFF065F46)
internal val MintWash              = Color(0xFFECFDF5)

// --- Sémantique : Dépense / dette / erreur (Deep Crimson) ----------------
internal val Crimson               = Color(0xFFEF4444)
internal val CrimsonDeep           = Color(0xFF991B1B)
internal val RoseTint              = Color(0xFFFEF2F2)

// --- Sémantique : Minuteries / avertissement (Warm Amber) ----------------
internal val Amber                 = Color(0xFFF59E0B)
internal val AmberDeep             = Color(0xFF92400E)
internal val HoneyTint             = Color(0xFFFFFBEB)

// =========================================================================
// Mapping Material 3
// =========================================================================

internal val DonakaLightColorScheme: ColorScheme = lightColorScheme(
    // --- Primaire (action focale, CTA) -----------------------------------
    primary              = Terracotta,
    onPrimary            = PureWhite,
    primaryContainer     = TerracottaWash,
    onPrimaryContainer   = TerracottaDeep,
    inversePrimary       = TerracottaInverse,

    // --- Secondaire (minuteries, avertissements chauds) ------------------
    secondary            = Amber,
    onSecondary          = Color(0xFF2A1700),
    secondaryContainer   = HoneyTint,
    onSecondaryContainer = AmberDeep,

    // --- Tertiaire (cash in, succès, delta positif) ----------------------
    tertiary             = Emerald,
    onTertiary           = PureWhite,
    tertiaryContainer    = MintWash,
    onTertiaryContainer  = EmeraldDeep,

    // --- Erreur (dépense, dette, rupture) --------------------------------
    error                = Crimson,
    onError              = PureWhite,
    errorContainer       = RoseTint,
    onErrorContainer     = CrimsonDeep,

    // --- Surfaces --------------------------------------------------------
    background                = Oatmeal,
    onBackground              = CharcoalSlate,

    surface                   = Oatmeal,
    onSurface                 = CharcoalSlate,
    surfaceVariant            = Wheat,
    onSurfaceVariant          = SlateMineral,

    surfaceContainerLowest    = PureWhite,
    surfaceContainerLow       = Oatmeal,
    surfaceContainer          = Wheat,
    surfaceContainerHigh      = WheatDeep,
    surfaceContainerHighest   = WheatDeeper,

    // --- Contours --------------------------------------------------------
    outline              = StoneMist,
    outlineVariant       = FlourBorder,

    // --- Inverses (Snackbars, toasts sur fond sombre) --------------------
    inverseSurface       = CharcoalSlate,
    inverseOnSurface     = CloudWhite,

    // --- Divers ----------------------------------------------------------
    surfaceTint          = Terracotta,
    scrim                = Color(0xFF000000),
)

// =========================================================================
// Alias sémantiques métier
// Confort de lecture : `MaterialTheme.colorScheme.cashIn` plutôt que
// `MaterialTheme.colorScheme.tertiary`. Strictement identique en valeur.
// =========================================================================

val ColorScheme.cashIn: Color             get() = tertiary
val ColorScheme.cashInContainer: Color    get() = tertiaryContainer
val ColorScheme.cashInOn: Color           get() = onTertiaryContainer

val ColorScheme.cashOut: Color            get() = error
val ColorScheme.cashOutContainer: Color   get() = errorContainer
val ColorScheme.cashOutOn: Color          get() = onErrorContainer

val ColorScheme.bakeTimer: Color          get() = secondary
val ColorScheme.bakeTimerContainer: Color get() = secondaryContainer
val ColorScheme.bakeTimerOn: Color        get() = onSecondaryContainer

// =========================================================================
// Compat : anciennes constantes utilisées par le code existant.
// À supprimer au fur et à mesure du portage.
// =========================================================================

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.primary"))
val Primary = Terracotta

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.primaryContainer"))
val PrimaireClair = TerracottaWash

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.background"))
val Fond = Oatmeal

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.onSurface"))
val TexteFonce = CharcoalSlate

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.onSurfaceVariant"))
val TexteGris = SlateMineral

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.surfaceContainerLowest"))
val SurfaceBlanche = PureWhite

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.surfaceContainer"))
val SurfaceMoyenne = Wheat

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.tertiary"))
val Vert = Emerald

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.tertiaryContainer"))
val VertClair = MintWash

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.error"))
val Rouge = Crimson

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.errorContainer"))
val RougeClair = RoseTint

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.secondary"))
val Ambre = Amber

@Deprecated("Utiliser MaterialTheme.colorScheme", ReplaceWith("MaterialTheme.colorScheme.secondaryContainer"))
val AmbreClair = HoneyTint