package com.example.booksguru.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(onSearch: (Int, Boolean) -> Unit) {
    var bookId by remember { mutableStateOf("") }
    var visualize by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Books Guru",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = bookId,
            onValueChange = { 
                bookId = it 
                isError = false
            },
            label = { Text("Enter Book ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            supportingText = { if (isError) Text("Please enter a valid book ID") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Checkbox(
                checked = visualize,
                onCheckedChange = { visualize = it }
            )
            
            Text("Show visualization")
        }
        
        Button(
            onClick = {
                try {
                    val id = bookId.toInt()
                    onSearch(id, visualize)
                } catch (e: NumberFormatException) {
                    isError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analyze Book")
        }
    }
}