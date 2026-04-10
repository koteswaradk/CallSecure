package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ExportDataSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf<String?>(null) }

    Text(
        "Export Data",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(start = 8.dp)
    )

    Text("Export your call history and analytics report.", fontSize = 14.sp, color = Color.Gray)
    Spacer(modifier = Modifier.height(8.dp))

    Row(Modifier.fillMaxWidth()) {
        Button( enabled = false,
            onClick = {
                exportType = "call_history"
                showExportDialog = true
            },
            modifier = Modifier.weight(1f).padding(end = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Export Call History")
        }
        Button(enabled = false,
            onClick = {
                exportType = "analytics"
                showExportDialog = true
            },
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Export Analytics")
        }
    }

    if (showExportDialog && exportType != null) {
        ExportDialog(
            exportType = exportType!!,
            onDismiss = { showExportDialog = false },
            onExport = { method: String ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val fileName = "SmartCallShield.pdf"
                        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        val smartCallShieldDir = File(documentsDir, "SmartCallShield")
                        if (!smartCallShieldDir.exists()) {
                            smartCallShieldDir.mkdirs()
                        }
                        val file = File(smartCallShieldDir, fileName)
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas
                        val paint = android.graphics.Paint()
                        paint.textSize = 24f
                        paint.isFakeBoldText = true
                        canvas.drawText(
                            if (exportType == "call_history") "Call History" else "Analytics Report",
                            40f, 60f, paint
                        )
                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        canvas.drawText("Exported on: ${java.time.LocalDateTime.now()}", 40f, 90f, paint)
                        var y = 130f
                        paint.textSize = 14f
                        paint.isFakeBoldText = true
                        if (exportType == "call_history") {
                            canvas.drawText("Number", 40f, y, paint)
                            canvas.drawText("Type", 180f, y, paint)
                            canvas.drawText("Timestamp", 300f, y, paint)
                            canvas.drawText("Duration", 480f, y, paint)
                            y += 20f
                            paint.isFakeBoldText = false
                        } else {
                            canvas.drawText("Metric", 40f, y, paint)
                            canvas.drawText("Value", 300f, y, paint)
                            y += 20f
                            paint.isFakeBoldText = false
                        }
                        pdfDocument.finishPage(page)
                        val outputStream = FileOutputStream(file)
                        pdfDocument.writeTo(outputStream)
                        pdfDocument.close()
                        outputStream.close()
                        if (method == "download") {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                            }
                        } else if (method == "email") {
                            val uri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".provider",
                                file
                            )
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, if (exportType == "call_history") "Call History" else "Analytics Report")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Please find attached the exported ${if (exportType == "call_history") "call history" else "analytics report"} as PDF.")
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                context.startActivity(android.content.Intent.createChooser(intent, "Send Email"))
                            }
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ExportDialog(exportType: String, onDismiss: () -> Unit, onExport: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Export ${if (exportType == "call_history") "Call History" else "Analytics Report"}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("How would you like to export?", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onExport("download"); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Download PDF File")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onExport("email"); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send via Email")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        },
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

