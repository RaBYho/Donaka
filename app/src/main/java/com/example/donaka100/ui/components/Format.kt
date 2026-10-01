package com.example.donaka100.ui.components
import com.example.donaka100.data.UniteStock

/** "1500000" -> "1 500 000" */
fun String.avecEspacesMilliers(): String =
    reversed().chunked(3).joinToString(" ").reversed()

fun Long.enMGA(): String {
    val signe = if (this < 0) "-" else ""
    return signe + kotlin.math.abs(this).toString().avecEspacesMilliers() + " Ar"
}
/** "0341234567" -> "034 12 345 67" */
fun String.enTelephone(): String {
    val d = filter { it.isDigit() }
    return if (d.length == 10)
        "${d.substring(0, 3)} ${d.substring(3, 5)} ${d.substring(5, 8)} ${d.substring(8)}"
    else this
}
/** 1.5 -> "1,5"   12.0 -> "12"   0.225 -> "0,23" */
fun Double.enQuantite(): String =
    String.format(java.util.Locale.FRENCH, "%.2f", this).trimEnd('0').trimEnd(',')

fun Double.avecUnite(unite: UniteStock): String = "${enQuantite()} ${unite.libelle}"

/** Accepte "1,5" comme "1.5" */
fun String.enDecimal(): Double? = replace(',', '.').toDoubleOrNull()

fun Int.enMGA(): String = toLong().enMGA()

