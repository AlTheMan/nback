package mobappdev.example.nback_cimpl.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel


@Composable
fun SettingScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech
) {
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    NScreen(vm)
                    NrOfEventsScreen(vm)
                    TimeBetweenEventsScreen(vm)
                    SizeOfGridScreen(vm)
                    NrOfSpokenLettersscreen(vm)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { vm.saveSettings() },
                        modifier = Modifier
                            .width(220.dp)  // Sets the width
                            .height(80.dp)
                    ){
                        Text(text = "save settings")
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }

        }

    }
}


@Composable
fun saveSettings(vm: GameViewModel) {
    vm.saveSettings()
}

@Composable
fun NrOfSpokenLettersscreen(vm: GameViewModel) {
    val nrOfSpokenLetters by vm.nrOfSpokenLetters.collectAsState()
    val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20") // The list of items for the dropdown
    var selectedItem by remember { mutableStateOf(nrOfSpokenLetters.toString()) }
    CustomDropdownMenu(items = items, onItemSelected = { selectedItem = it }, title="Nr of Spoken Letters: " +selectedItem.toString())
    //Text("NrOfSpokenLetters is: ${selectedItem}")
    vm.setNrOfSpokenLetters(selectedItem.toInt())
}

@Composable
fun SizeOfGridScreen(vm: GameViewModel) {
    val gridSize by vm.gridSize.collectAsState()
    val items = listOf("1", "2", "3", "4", "5") // The list of items for the dropdown
    var selectedItem by remember { mutableStateOf(gridSize.toString()) }
    CustomDropdownMenu(items = items, onItemSelected = { selectedItem = it }, title="Size of Grid: " +selectedItem.toString())
    vm.setGridSize(selectedItem.toInt())
    //Text("Selected gridSize is: ${selectedItem}")
}


@Composable
fun TimeBetweenEventsScreen(vm: GameViewModel) {
    val eventInterval by vm.eventInterval.collectAsState()
    val items = listOf("500", "1000", "1500", "2000", "2500", "3000") // The list of items for the dropdown
    var selectedItem by remember { mutableStateOf(eventInterval.toString()) }
    CustomDropdownMenu(items = items, onItemSelected = { selectedItem = it }, title="Time between events: " +selectedItem.toString())
    vm.setEventInterval(selectedItem.toLong())
    //Text("Time Between Events is: ${selectedItem} ms")
}



@Composable
fun NrOfEventsScreen(vm: GameViewModel) {
    val nrOfEventsPerRound by vm.nrOfEventsPerRound.collectAsState()
    val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20") // The list of items for the dropdown
    //val items = (1..20).map { it.toString() }     // Define the list of items for the dropdown
    var selectedItem by remember { mutableStateOf(nrOfEventsPerRound.toString()) }
    CustomDropdownMenu(items = items, onItemSelected = { selectedItem = it }, title="Nr of events: "+selectedItem.toString())
    vm.setNrOfEvents(selectedItem.toInt())
    //Text("NrOfEvents is: ${selectedItem}")
}

@Composable
fun NScreen(vm: GameViewModel) {
    val nBack by vm.nBack.collectAsState()
    val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20") // The list of items for the dropdown
    var selectedItem by remember { mutableStateOf(nBack.toString()) }
    CustomDropdownMenu(items = items, onItemSelected = { selectedItem = it }, title="N: "+selectedItem.toString())
    vm.setNBack(selectedItem.toInt())
    //Text("N is: ${selectedItem}")
}

@Composable
fun SomeScreen() {
    val items = listOf("Item 1", "Item 2", "Item 3") // The list of items for the dropdown
    val selectedItem = remember { mutableStateOf(items.first()) }

    CustomDropdownMenu(items = items, onItemSelected = { selectedItem.value = it }, title="test")
    Text("Selected item is: ${selectedItem.value}")
}

