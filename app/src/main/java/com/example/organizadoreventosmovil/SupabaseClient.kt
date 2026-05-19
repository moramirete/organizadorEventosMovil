package com.example.organizadoreventosmovil

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    const val SUPABASE_URL = "https://yuaxnqjqxrmahlmlbujj.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl1YXhucWpxeHJtYWhsbWxidWpqIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3OTE2NjE1NCwiZXhwIjoyMDk0NzQyMTU0fQ.Ogh2OkTGlrEFSRkSJAmQCl1f94_iiBYKpYoxy04_-Sc"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}