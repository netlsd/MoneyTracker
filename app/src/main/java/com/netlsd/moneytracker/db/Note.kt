package com.netlsd.moneytracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "money") val money: Double?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "comment") val comment: String?

) {
    constructor(
        name: String?,
        money: Double?,
        type: String?,
        date: String?,
        comment: String?
    ) : this(0, name, money, type, date, comment)
}
