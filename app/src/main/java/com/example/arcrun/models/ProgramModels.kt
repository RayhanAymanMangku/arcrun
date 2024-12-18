package com.example.arcrun.models

import android.os.Parcel
import android.os.Parcelable

data class ProgramModels(
    var batas_akhir: String? = null,
    var deskripsi_program: String? = null,
    var nama_program: String? = null,
    var status_program: String? = null,
    var start_date: String? = null,
    //var program_id: String? = null, // pastikan program_id juga memiliki nilai default
) : Parcelable {

    // Konstruktor untuk Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        //parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(batas_akhir)
        parcel.writeString(deskripsi_program)
        parcel.writeString(nama_program)
        parcel.writeString(status_program)
        parcel.writeString(start_date)
        //parcel.writeString(program_id)
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