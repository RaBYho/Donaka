package com.example.donaka100.data.local.converters

import androidx.room.TypeConverter
import com.example.donaka100.data.ModeReglement
import com.example.donaka100.data.TypeClient
import com.example.donaka100.data.TypeMouvement
import com.example.donaka100.data.TypeMouvementStock
import com.example.donaka100.data.UniteStock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class DonakaTypeConverters {

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromEpochMillis(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun toEpochMillis(dateTime: LocalDateTime?): Long? {
        return dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }

    @TypeConverter
    fun fromSecondOfDay(value: Int?): LocalTime? {
        return value?.let { LocalTime.ofSecondOfDay(it.toLong()) }
    }

    @TypeConverter
    fun toSecondOfDay(time: LocalTime?): Int? {
        return time?.toSecondOfDay()
    }

    @TypeConverter
    fun fromUniteStock(unite: UniteStock?): String? {
        return unite?.name
    }

    @TypeConverter
    fun toUniteStock(value: String?): UniteStock? {
        return value?.let {
            try {
                UniteStock.valueOf(it)
            } catch (e: IllegalArgumentException) {
                UniteStock.KG
            }
        }
    }

    @TypeConverter
    fun fromTypeMouvementStock(type: TypeMouvementStock?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTypeMouvementStock(value: String?): TypeMouvementStock? {
        return value?.let {
            try {
                TypeMouvementStock.valueOf(it)
            } catch (e: IllegalArgumentException) {
                TypeMouvementStock.ENTREE
            }
        }
    }

    @TypeConverter
    fun fromModeReglement(mode: ModeReglement?): String? {
        return mode?.name
    }

    @TypeConverter
    fun toModeReglement(value: String?): ModeReglement? {
        return value?.let {
            try {
                ModeReglement.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun fromTypeClient(type: TypeClient?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTypeClient(value: String?): TypeClient? {
        return value?.let {
            try {
                TypeClient.valueOf(it)
            } catch (e: IllegalArgumentException) {
                TypeClient.AUTRE
            }
        }
    }

    @TypeConverter
    fun fromTypeMouvement(type: TypeMouvement?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTypeMouvement(value: String?): TypeMouvement? {
        return value?.let {
            try {
                TypeMouvement.valueOf(it)
            } catch (e: IllegalArgumentException) {
                TypeMouvement.ENCAISSE
            }
        }
    }
}
