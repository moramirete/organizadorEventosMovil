package com.example.organizadoreventosmovil

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    const val SUPABASE_URL = "https://kdgjkiptelkrzahmfafi.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_w1PGlybSUlCeCzLWEkozww_gfZLZtMF"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}