package com.akshaglobal.smartcallshield.data.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.akshaglobal.smartcallshield.data.model.DeviceContact
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class DeviceContactsProvider @Inject constructor(@ApplicationContext private val context: Context) {
    private var cachedContacts: List<DeviceContact>? = null
    private var lastCacheTime: Long = 0
    private val CACHE_EXPIRY_MS = 5 * 60 * 1000 // 5 minutes cache

    fun fetchDeviceContacts(): List<DeviceContact> {
        val currentTime = System.currentTimeMillis()
        if (cachedContacts != null && (currentTime - lastCacheTime < CACHE_EXPIRY_MS)) {
            return cachedContacts!!
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.w("DeviceContactsProvider", "READ_CONTACTS permission not granted. Returning empty list.")
            return emptyList()
        }

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
                    
                    // Simple normalization for storage
                    val normalized = numberRaw.replace(Regex("[^+0-9]"), "")

                    if (name.isNotEmpty() && normalized.isNotEmpty()) {
                        // Using normalized number as key to truly deduplicate entries for the same person
                        contactsMap[normalized] = DeviceContact(id = id, displayName = name, phoneNumber = normalized)
                    }
                }
            }
            
            cachedContacts = contactsMap.values.toList()
            lastCacheTime = currentTime
            
        } catch (e: Exception) {
            Log.e("DeviceContactsProvider", "Error fetching contacts", e)
            return cachedContacts ?: emptyList()
        }

        return cachedContacts!!
    }

    fun invalidateCache() {
        cachedContacts = null
        lastCacheTime = 0
    }
}
