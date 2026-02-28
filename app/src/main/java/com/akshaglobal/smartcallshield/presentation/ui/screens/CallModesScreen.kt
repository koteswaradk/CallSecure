package com.akshaglobal.smartcallshield.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.akshaglobal.smartcallshield.presentation.viewmodel.CallModesViewModel

@Composable
fun CallModesScreen(viewModel: CallModesViewModel) {
    val contactsState = viewModel.deviceContacts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Call Modes", style = MaterialTheme.typography.titleLarge)

        Button(onClick = { viewModel.createMode("FAMILY") }) {
            Text("Create FAMILY Mode")
        }

        LazyColumn {
            items(contactsState.value) { contact ->
                Text("${contact.displayName} - ${contact.phoneNumber}")
            }
        }
    }
}
