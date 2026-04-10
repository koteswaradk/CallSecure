package com.akshaglobal.smartcallshield.data.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.akshaglobal.smartcallshield.data.model.DeviceContact
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class DeviceContactsProvider @Inject constructor(@ApplicationContext private val context: Context) {
    fun fetchDeviceContacts(): List<DeviceContact> {
        val contacts = mutableListOf<DeviceContact>()
        val resolver: ContentResolver = context.contentResolver
        val contactsMap = mutableMapOf<String, DeviceContact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            val cursor: Cursor? = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use { c ->
                val idIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val id = c.getString(idIndex)
                    val name = c.getString(nameIndex) ?: ""
                    val numberRaw = c.getString(numberIndex) ?: ""
                    val normalized = numberRaw.replace(Regex("[^+0-9]"), "")

                    // Only add if not already present (prevents duplicates for contacts with multiple phone numbers)
                    if (!contactsMap.containsKey(id) && name.isNotEmpty() && normalized.isNotEmpty()) {
                        contactsMap[id] = DeviceContact(id = id, displayName = name, phoneNumber = normalized)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("DeviceContactsProvider", "Missing READ_CONTACTS permission", e)
            // Optionally notify the user or return an empty list
        }

        return contactsMap.values.toList()
    }
}
