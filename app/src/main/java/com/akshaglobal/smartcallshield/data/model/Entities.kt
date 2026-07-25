package com.akshaglobal.smartcallshield.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Junction
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

import androidx.room.ColumnInfo

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val displayName: String = "",
    val category: String, // WHITELIST, BLACKLIST, FAMILY, EMERGENCY
    val isEmergency: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0, // in seconds
    val callType: Int, // INCOMING, OUTGOING, MISSED
    val isSpam: Boolean = false,
    val spamScore: Float = 0f,
    val wasBlocked: Boolean = false
)

@Entity(tableName = "spam_reports")
data class SpamReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val reportCount: Int = 1,
    val lastReportedAt: Long = System.currentTimeMillis(),
    val spamCategory: String = "", // ROBOCALL, SCAM, HARASSMENT, etc
    val confidence: Float = 0f // AI confidence score
)

@Entity(tableName = "driving_mode_logs")
data class DrivingModeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String = "",
    val smsMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT" // SENT, FAILED, PENDING
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

data class SpamDetectionResult(
    val phoneNumber: String,
    val isSpam: Boolean,
    val confidence: Float,
    val category: String? = null,
    val aiModel: String = "TensorFlow Lite"
)

data class CallStatistics(
    val totalCalls: Long = 0,
    val blockedCalls: Long = 0,
    val spamCallsPrevented: Long = 0,
    val drivingModeRepliesSent: Long = 0,
    val familyCallsReceived: Long = 0,
    val emergencyCallsReceived: Long = 0
)

enum class CallMode {
    NORMAL,
    FAMILY,
    DRIVING,
    EMERGENCY,
    SLEEP
}

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

// New entities for call modes
@Entity(tableName = "call_modes")
data class ModeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    primaryKeys = ["modeId", "contactId"],
    tableName = "mode_contact_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = ModeEntity::class,
            parentColumns = ["id"],
            childColumns = ["modeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["modeId"]),
        Index(value = ["contactId"])
    ]
)
data class ModeContactCrossRef(
    val modeId: Long,
    val contactId: Long
)

// Convenience relation object
data class ModeWithContacts(
    @Embedded val mode: ModeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ModeContactCrossRef::class,
            parentColumn = "modeId",
            entityColumn = "contactId"
        )
    )
    val contacts: List<ContactEntity>
)

// DTO for device contacts fetched from ContentResolver
data class DeviceContact(
    val id: String,
    val displayName: String,
    val phoneNumber: String
)
