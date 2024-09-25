package gaur.himanshu.booksummary


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun MainScreen() {
    var uiState by remember { mutableStateOf(emptyList<Summary>()) }

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val summaryEdit = remember { mutableStateOf(Summary("", "")) }
    val isEdit = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf(Type.INTERNAL) }

    LaunchedEffect(key1 = sheetState) {
        if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
            summaryEdit.value = Summary("", "", Type.INTERNAL)
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier.safeContentPadding(),
        sheetContent = {
            Form(
                summary = summaryEdit.value,
                type = type,
                onTypeChanged = { type = it }) { title, desc ->
                if (isEdit.value) {
                    // TODO: Update
                } else {
                    // TODO: Insert
                }
                scope.launch { sheetState.hide() }
            }

        }, sheetState = sheetState
    ) {

        Scaffold(topBar = {
            TopAppBar(title = { Text(text = "Notes App") }, actions = {
                IconButton(onClick = {
                    scope.launch { sheetState.show() }
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            })
        }) {
            if (uiState.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing found")
                }
            } else {

                LazyColumn(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {

                    items(uiState) {
                        Card(modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                isEdit.value = true
                                summaryEdit.value = it
                                type = it.type
                                scope.launch { sheetState.show() }
                            }) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = it.fileName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it.summary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = it.type.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                IconButton(onClick = {
                                    // TODO: Delete
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }

                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun Form(
    summary: Summary,
    type: Type,
    onTypeChanged: (Type) -> Unit,
    onClick: (String, String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val desc = remember { mutableStateOf("") }

    LaunchedEffect(summary) {
        if (summary.fileName.isNotBlank()) {
            title.value = summary.fileName
            desc.value = summary.summary
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = title.value, onValueChange = {
            title.value = it
        }, singleLine = true, placeholder = { Text(text = "Title") })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = desc.value, onValueChange = {
            desc.value = it
        }, singleLine = true, placeholder = { Text(text = "Description") })
        Spacer(modifier = Modifier.height(8.dp))


        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = type == Type.INTERNAL, onCheckedChange = {
                onTypeChanged.invoke(Type.INTERNAL)
            })
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Internal")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = type == Type.PRIVATE_EXTERNAL, onCheckedChange = {
                onTypeChanged.invoke(Type.PRIVATE_EXTERNAL)
            })
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Private External")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = type == Type.SHARED, onCheckedChange = {
                onTypeChanged.invoke(Type.SHARED)
            })
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Shared")
        }
        Spacer(modifier = Modifier.height(8.dp))


        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onClick.invoke(title.value, desc.value) }) {
            Text(text = "Save")
        }
    }

}