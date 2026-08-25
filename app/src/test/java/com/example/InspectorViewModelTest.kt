package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.InspectorViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspectorViewModelTest {

    @Test
    fun testUrlUpdating() {
        val viewModel = InspectorViewModel(ApplicationProvider.getApplicationContext())
        
        // Initial state
        println("Initial urlInput: ${viewModel.uiState.value.urlInput}")
        println("Initial activeUrl: ${viewModel.uiState.value.activeUrl}")
        
        // Simulate user typing
        viewModel.onUrlInputChanged("google.com")
        assertEquals("google.com", viewModel.uiState.value.urlInput)
        
        // Simulate pressing Go
        viewModel.applyUrlAndLoad()
        
        println("After apply urlInput: ${viewModel.uiState.value.urlInput}")
        println("After apply activeUrl: ${viewModel.uiState.value.activeUrl}")
        assertEquals("https://google.com", viewModel.uiState.value.activeUrl)
    }
}
