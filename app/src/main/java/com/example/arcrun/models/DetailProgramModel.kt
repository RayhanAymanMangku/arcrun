package com.example.arcrun.models

import android.os.Parcel
import android.os.Parcelable

data class DetailProgramModel(
    val hari: String? = "",
    val jumlah_hari: Int = 0,
    var tanggal_waktu: String? = "",
    var gambar_checklist: String? = ""
) : Parcelable {

    // Constructor untuk membaca data dari Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Membaca string (nullable dengan fallback empty string)
        parcel.readInt(),          // Membaca integer
        parcel.readString() ?: "", // Membaca string (nullable dengan fallback empty string)
        parcel.readString() ?: ""  // Membaca string (nullable dengan fallback empty string)
    )

    // Menulis data ke Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hari)
        parcel.writeInt(jumlah_hari)
        parcel.writeString(tanggal_waktu)
        parcel.writeString(gambar_checklist)
    }

    // Deskripsi konten Parcel
    override fun describeContents(): Int {
        return 0
    }

    // Companion object untuk menginisialisasi Creator
    companion object CREATOR : Parcelable.Creator<DetailProgramModel> {
        override fun createFromParcel(parcel: Parcel): DetailProgramModel {
            return DetailProgramModel(parcel)
        }

        override fun newArray(size: Int): Array<DetailProgramModel?> {
            return arrayOfNulls(size)
        }
    }
}