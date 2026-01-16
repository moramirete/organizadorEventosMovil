package com.example.organizadoreventosmovil

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    const val SUPABASE_URL = "https://kdgjkiptelkrzahmfafi.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtkZ2praXB0ZWxrcnphaG1mYWZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgwMTg3OTgsImV4cCI6MjA4MzU5NDc5OH0.QUz5Z3OGQ4a44zhQ4Xokbpc5AKDP4sSmFsJwftTsSUI"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}