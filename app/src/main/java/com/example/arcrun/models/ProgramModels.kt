package com.example.arcrun.models

import android.os.Parcel
import android.os.Parcelable

data class ProgramModels(

    var batas_akhir: String? = null,
    var deskripsi_program: String? = null,
    var gambar: String? = null,
    var nama_program: String? = null,
    var status_program: String? = null,
    var waktu_mulai: String? = null,
    val programId: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(batas_akhir)
        parcel.writeString(deskripsi_program)
        parcel.writeString(gambar)
        parcel.writeString(nama_program)
        parcel.writeString(status_program)
        parcel.writeString(waktu_mulai)
        parcel.writeString(programId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProgramModels> {
        override fun createFromParcel(parcel: Parcel): ProgramModels {
            return ProgramModels(parcel)
        }

        override fun newArray(size: Int): Array<ProgramModels?> {
            return arrayOfNulls(size)
        }
    }
}