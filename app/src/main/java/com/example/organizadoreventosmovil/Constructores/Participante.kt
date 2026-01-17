package com.example.organizadoreventosmovil.Constructores

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Participante(
    val nombre: String,
    val prefiere: String,
    val noPrefiere: String
) : Parcelable
