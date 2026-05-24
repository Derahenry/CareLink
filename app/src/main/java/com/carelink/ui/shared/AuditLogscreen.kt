package com.carelink.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.AuditEntry
import com.carelink.util.DeadlineUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Load all audit entries directly from database
    val auditEntries = remember {
        val db = DatabaseHelper(context).readableDatabase
        val cursor = db.rawQuery(
            """SELECT a.*, u.full_name, u.role, r.title as req_title
               FROM audit_log a
               LEFT JOIN users u ON a.actor_id = u.id
               LEFT JOIN requests r ON a.request_id = r.id
               ORDER BY a.timestamp DESC""",
            null
        )
        val list = mutableListOf<AuditDisplayEntry>()
        while (cursor.moveToNext()) {
            list.add(AuditDisplayEntry(
                id        = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                requestId = cursor.getInt(cursor.getColumnIndexOrThrow("request_id")),
                actorName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")) ?: "Unknown",
                actorRole = cursor.getString(cursor.getColumnIndexOrThrow("role")) ?: "",
                action    = cursor.getString(cursor.getColumnIndexOrThrow("action")),
                detail    = cursor.getString(cursor.getColumnIndexOrThrow("detail")) ?: "",
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                reqTitle  = cursor.getString(cursor.getColumnIndexOrThrow("req_title")) ?: "Unknown request"
            ))
        }
        cursor.close()
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Audit Log", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("${auditEntries.size} entries", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        if (auditEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No audit entries yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(auditEntries) { entry ->
                AuditEntryCard(entry = entry)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Data class for display
// ─────────────────────────────────────────────────────────────────────────────
data class AuditDisplayEntry(
    val id: Int,
    val requestId: Int,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val detail: String,
    val timestamp: Long,
    val reqTitle: String
)

// ─────────────────────────────────────────────────────────────────────────────
// Audit Entry Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuditEntryCard(entry: AuditDisplayEntry) {
    val (actionColor, actionBg) = when (entry.action) {
        "SUBMITTED"  -> Color(0xFF2F5468) to Color(0x1A2F5468)
        "ASSIGNED"   -> Color(0xFF6B5B8C) to Color(0x1A6B5B8C)
        "COMPLETED"  -> Color(0xFF3F6E54) to Color(0x1A3F6E54)
        "VERIFIED"   -> Color(0xFF3F6E54) to Color(0x1A3F6E54)
        "ESCALATED"  -> Color(0xFFA1413A) to Color(0x1AA1413A)
        "CREATED"    -> Color(0xFF94661E) to Color(0x1A94661E)
        else         -> Color(0xFF5A6B68) to Color(0x1A5A6B68)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surface,
        border   = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action dot
            Surface(
                shape = CircleShape,
                color = actionBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = entry.action.first().toString(),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = actionColor
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Action + request
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = actionBg,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, actionColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text     = entry.action,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color    = actionColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text     = DeadlineUtils.formatTimestamp(entry.timestamp),
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Request title
                Text(
                    text       = entry.reqTitle,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                // Actor
                Text(
                    text     = "${entry.actorName} (${entry.actorRole.replace("_", " ")})",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Detail
                if (entry.detail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = entry.detail,
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}