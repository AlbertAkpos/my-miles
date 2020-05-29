package me.alberto.mymiles.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "distance_table")
@Parcelize
data class Miles(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val distanceCovered: Float,
    val coordinates: List<LatLng>,
    val date: String
): Parcelable


class Converter {
    @TypeConverter
    fun fromLocationList(coordinates: List<LatLng>): String {
        val gson = Gson()
        val type = object : TypeToken<List<LatLng>>(){}.type
        return gson.toJson(coordinates, type)
    }

    @TypeConverter
    fun toLocationList(string: String): List<LatLng> {
        val gson = Gson()
        val type = object : TypeToken<List<LatLng>>(){}.type
        return gson.fromJson(string, type)
    }

}