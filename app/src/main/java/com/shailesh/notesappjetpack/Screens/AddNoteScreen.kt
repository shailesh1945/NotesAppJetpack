package com.shailesh.notesappjetpack.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.shailesh.notesappjetpack.Models.Notes
import com.shailesh.notesappjetpack.ui.theme.colorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(navHostController: NavHostController, id: String?) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val notesDBRef = db.collection("notes")

    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }

    // Load existing note for edit
    LaunchedEffect(Unit) {
        if (id != "defaultId") {
            notesDBRef.document(id.toString()).get().addOnSuccessListener {
                val singleData = it.toObject(Notes::class.java)
                title.value = singleData?.title ?: ""
                description.value = singleData?.description ?: ""
            }
        }
    }

    // Auto-save when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            if (title.value.isNotEmpty() || description.value.isNotEmpty()) {
                val myNotesID = if (id != "defaultId") id.toString() else notesDBRef.document().id
                val notes = Notes(
                    id = myNotesID,
                    title = title.value,
                    description = description.value,
                )
                notesDBRef.document(myNotesID).set(notes)
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF7F2FA))
                .systemBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (id == "defaultId") "Add Note" else "Edit Note",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF1D1B20),
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title input
                TextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    textStyle = TextStyle(color = Color(0xFF1D1B20), fontSize = 18.sp),
                    placeholder = { Text("Enter title", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEADDFF),
                        unfocusedContainerColor = Color(0xFFEADDFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Description input
                TextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    textStyle = TextStyle(color = Color(0xFF1D1B20), fontSize = 16.sp),
                    placeholder = { Text("Enter description", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEADDFF),
                        unfocusedContainerColor = Color(0xFFEADDFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
