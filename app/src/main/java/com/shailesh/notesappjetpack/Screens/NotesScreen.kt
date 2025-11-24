package com.shailesh.notesappjetpack.Screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shailesh.notesappjetpack.Models.Notes
import com.shailesh.notesappjetpack.Navigation.NotesNavigationItem
import com.shailesh.notesappjetpack.ui.theme.colorRed
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navHostController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val notesDBRef = db.collection("notes")
    val notesList = remember { mutableStateListOf<Notes>() }
    val dataValue = remember { mutableStateOf(false) }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 🔍 Search states
    var searchQuery by remember { mutableStateOf("") }

    // Contextual Action Mode
    var selectedNotes by remember { mutableStateOf(setOf<String>()) }
    var isActionMode by remember { mutableStateOf(false) }

    // Firestore listener
    LaunchedEffect(Unit) {
        notesDBRef.addSnapshotListener { value, error ->
            if (error == null) {
                val data = value?.toObjects(Notes::class.java) ?: emptyList()
                notesList.clear()
                notesList.addAll(data)
                dataValue.value = true
            } else {
                dataValue.value = false
            }
        }
    }

    // 🔎 Filter notes in real-time
    val filteredNotes = remember(searchQuery, notesList) {
        if (searchQuery.isBlank()) {
            notesList
        } else {
            notesList.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) ||
                        note.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("All Notes") },
                    selected = false,
                    onClick = { /*TODO*/ }
                )
                NavigationDrawerItem(
                    label = { Text("Favorites") },
                    selected = false,
                    onClick = { /*TODO*/ }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { /*TODO*/ }
                )
                NavigationDrawerItem(
                    label = { Text("GitHub")},
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isActionMode) {
                    TopAppBar(
                        title = { Text("${selectedNotes.size} selected") },
                        navigationIcon = {
                            IconButton(onClick = {
                                isActionMode = false
                                selectedNotes = emptySet()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                // Delete selected notes
                                selectedNotes.forEach { id ->
                                    notesDBRef.document(id).delete()
                                }
                                selectedNotes = emptySet()
                                isActionMode = false
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }

                            IconButton(onClick = {
                                // Toggle pin/unpin
                                selectedNotes.forEach { id ->
                                    val note = notesList.find { it.id == id } ?: return@forEach
                                    notesDBRef.document(id).update("pinned", !note.pinned)
                                }
                                selectedNotes = emptySet()
                                isActionMode = false
                            }) {
                                val allPinned = selectedNotes.all { id ->
                                    notesList.find { it.id == id }?.pinned == true
                                }
                                Icon(
                                    imageVector = if (allPinned) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = if (allPinned) "Unpin" else "Pin"
                                )                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("My Notes") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isActionMode) {
                    FloatingActionButton(
                        contentColor = Color.White,
                        containerColor = colorRed,
                        onClick = {
                            navHostController.navigate(NotesNavigationItem.AddNotesScreen.route + "/defaultId")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF7F2FA))
                    .padding(16.dp)
            ) {
                // 🔍 Search bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {}
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dataValue.value) {
                    // Show pinned notes at top
                    val pinnedNotes = filteredNotes.filter { it.pinned }
                    val normalNotes = filteredNotes.filter { !it.pinned }
                    val displayedNotes = pinnedNotes + normalNotes

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(displayedNotes) { note ->
                            NoteCard(
                                note = note,
                                notesDBRef = notesDBRef,
                                navHostController = navHostController,
                                selectedNotes = selectedNotes,
                                onNoteLongPress = { selected ->
                                    isActionMode = true
                                    selectedNotes = if (selectedNotes.contains(selected.id)) {
                                        selectedNotes - selected.id
                                    } else {
                                        selectedNotes + selected.id
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEADDFF),
            unfocusedContainerColor = Color(0xFFEADDFF),
            disabledContainerColor = Color(0xFFEADDFF),
            errorContainerColor = Color(0xFFEADDFF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Notes,
    notesDBRef: CollectionReference,
    navHostController: NavHostController,
    selectedNotes: Set<String>,
    onNoteLongPress: (Notes) -> Unit
) {
    val isSelected = selectedNotes.contains(note.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    if (selectedNotes.isNotEmpty()) {
                        onNoteLongPress(note)
                    } else {
                        navHostController.navigate(NotesNavigationItem.AddNotesScreen.route + "/${note.id}")
                    }
                },
                onLongClick = { onNoteLongPress(note) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFD1C4E9) else Color(0xFFE6E0E9)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = Color(0xFF1D1B20)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                maxLines = 4
            )
        }
    }
}
