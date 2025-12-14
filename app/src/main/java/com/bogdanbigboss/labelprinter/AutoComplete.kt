package com.example.customautocomplete

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.collectLatest

@Composable
private fun OverlayAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Vertical-only expansion so it "unfolds" downward (line -> panel), not dot -> rectangle
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        content()
    }
}

@Composable
fun AutoComplete() {
    val categories = listOf(
        "Food",
        "Beverages",
        "Sports",
        "Learning",
        "Travel",
        "Rent",
        "Bills",
        "Fees",
        "Others",
    )

    var category by remember { mutableStateOf(TextFieldValue("")) }
    val heightTextFields = 55.dp

    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    var expanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Lets us open dropdown when clicking the field even if it's already focused
    val textFieldInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(textFieldInteractionSource) {
        textFieldInteractionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                expanded = true
            }
        }
    }

    val dismissInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .padding(30.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = dismissInteractionSource,
                indication = null
            ) {
                expanded = false
                focusManager.clearFocus()
            }
    ) {
        Text(
            modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
            text = "Category",
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Box(modifier = Modifier.fillMaxWidth()) {

            // Field + button (stays in place)
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightTextFields)
                        .border(
                            width = 1.8.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size
                        }
                        .onFocusChanged { focusState ->
                            expanded = focusState.isFocused
                        },
                    value = category,
                    onValueChange = { newValue ->
                        category = newValue
                        expanded = true
                    },
                    placeholder = { Text("Start entering the name") },
                    interactionSource = textFieldInteractionSource,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            expanded = false
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { /* no functionality */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text("Print")
                }
            }

            // Dropdown OVER the button (overlay)
            val dropdownOffsetY = heightTextFields + 6.dp

            OverlayAnimatedVisibility(
                visible = expanded,
                modifier = Modifier
                    .zIndex(1f)
                    .offset(y = dropdownOffsetY)
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth()
                        // Consume clicks so parent "dismiss" click doesn't fire
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 15.dp)
                ) {
                    val query = category.text.trim().lowercase()
                    val listToShow =
                        if (query.isNotEmpty()) {
                            categories
                                .filter { it.lowercase().startsWith(query) }
                                .sorted()
                        } else {
                            categories.sorted()
                        }

                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(listToShow) { item ->
                            CategoryItems(title = item) { title ->
                                category = TextFieldValue(
                                    text = title,
                                    selection = TextRange(title.length) // cursor at end
                                )
                                expanded = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItems(
    title: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(title) }
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}
