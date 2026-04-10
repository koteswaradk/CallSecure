package com.akshaglobal.smartcallshield.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.common_ui.components.ContactCard
import com.akshaglobal.smartcallshield.presentation.viewmodel.ContactsViewModel



@Composable
fun ContactsScreen(viewModel: ContactsViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddContactDialog by remember { mutableStateOf(false) }

    val allContacts by viewModel.allContacts.collectAsState()
    // Filter contacts by category for display
    val familyContacts = allContacts.filter { it.category.equals("FAMILY", ignoreCase = true) }
    val drivingContacts = allContacts.filter { it.category.equals("DRIVING", ignoreCase = true) }
    val emergencyContacts = allContacts.filter { it.category.equals("EMERGENCY", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Text(
            "Contacts Management",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All (${allContacts.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Family (${familyContacts.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Driving (${drivingContacts.size})") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Emergency (${emergencyContacts.size})") }
            )
        }

        // Content
        when (selectedTab) {
            0 -> ContactsList(allContacts) { contact ->
                viewModel.deleteContact(contact)
            }
            1 -> ContactsList(familyContacts) { contact ->
                viewModel.deleteContact(contact)
            }
            2 -> ContactsList(drivingContacts) { contact ->
                viewModel.deleteContact(contact)
            }
            3 -> ContactsList(emergencyContacts) { contact ->
                viewModel.deleteContact(contact)
            }
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onAdd = { phoneNumber, name, category ->
                val contact = ContactEntity(
                    phoneNumber = phoneNumber,
                    displayName = name,
                    category = category,
                    isEmergency = category == "EMERGENCY"
                )
                viewModel.addContact(contact)
                showAddContactDialog = false
            },
            onDismiss = { showAddContactDialog = false }
        )
    }
}

@Composable
private fun ContactsList(
    contacts: List<ContactEntity>,
    onDelete: (ContactEntity) -> Unit
) {
    if (contacts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No contacts found", fontSize = 16.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            items(contacts) { contact ->
                ContactCard(contact) {
                    onDelete(contact)
                }
            }
        }
    }
}

@Composable
private fun AddContactDialog(
    onAdd: (phoneNumber: String, name: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FAMILY") } // Default to FAMILY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Contact", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("FAMILY", "DRIVING", "EMERGENCY").forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat)
                                    MaterialTheme.colorScheme.primary
                                else Color.LightGray
                            )
                        ) {
                            Text(cat, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (phoneNumber.isNotEmpty()) {
                                onAdd(phoneNumber, displayName, category)
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
