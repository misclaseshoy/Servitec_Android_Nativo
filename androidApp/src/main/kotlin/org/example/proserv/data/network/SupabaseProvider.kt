package org.example.proserv.data.network


import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://ucupwwtsfkgerdnkgpjx.supabase.co",
        // Usamos de forma segura tu Anon Public Key
        supabaseKey = "sb_publishable_QEAC5IjbNN9-IYaWt_5QdQ_x7ZZGMbI"
    ) {
        install(Auth)
        install(Postgrest)
    }
}

