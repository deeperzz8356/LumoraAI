package com.deep.lumoraai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deep.lumoraai.core.navigation.NavGraph
import com.deep.lumoraai.core.theme.LumoraTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LumoraTheme {
                NavGraph()
            }
        }
    }
}