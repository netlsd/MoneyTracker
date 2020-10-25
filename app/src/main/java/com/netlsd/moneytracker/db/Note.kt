package com.netlsd.moneytracker.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "money") var money: Double,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "comment") var comment: String?,
    @ColumnInfo(name = "repay") var repay: Double?
) : Parcelable {
    constructor(
        name: String,
        money: Double,
        type: String,
        date: String,
        comment: String?,
        repay: Double?
    ) : this(0, name, money, type, date, comment, repay)
}
