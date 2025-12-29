package com.example.organizadoreventosmovil.Constructores

import android.os.Parcel
import android.os.Parcelable

data class Participante(
    val nombre: String,
    val prefiereA: String,
    val noPrefiereA: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(prefiereA)
        parcel.writeString(noPrefiereA)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Participante> {
        override fun createFromParcel(parcel: Parcel): Participante {
            return Participante(parcel)
        }

        override fun newArray(size: Int): Array<Participante?> {
            return arrayOfNulls(size)
        }
    }
}
