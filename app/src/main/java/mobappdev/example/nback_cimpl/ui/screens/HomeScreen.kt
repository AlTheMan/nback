package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.R.id.planets_spinner
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

fun speakHome(  textToSpeech: TextToSpeech, text: String) {
    // Use the TextToSpeech instance to speak the provided text
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold {
        if(isLandscape){
            LandScapeHomeScreen(vm, navController,textToSpeech, Modifier.padding(it))
        }
        else{
            PortraitHomeScreen(vm, navController,textToSpeech, Modifier.padding(it))
        }
    }
}


@Composable
fun LandScapeHomeScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech,
    modifier: Modifier
)
{
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val eventInterval by vm.eventInterval.collectAsState()
    val nBack by vm.nBack.collectAsState()
    val gridSize by vm.gridSize.collectAsState()
    val nrOfSpokenLetters by vm.nrOfSpokenLetters.collectAsState()
    val nrOfEventsPerRound by vm.nrOfEventsPerRound.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row{
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "High-Score = $highscore\n",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Current Settings:\n"+
                            "N = ${nBack}\n"+
                            "delay = $eventInterval ms\n"+
                            "gameType = ${gameState.gameType}\n"+
                            "nrOfEventsPerRound = $nrOfEventsPerRound\n"+
                            "nrOfSpokenLetters= $nrOfSpokenLetters\n"+
                            "grid size = ${gridSize}x${gridSize}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(horizontal = 16.dp)
                            ,
                            onClick = {
                                //vm.startGame()
                                navController.navigate("GameScreen")
                            }) {
                            Text(text = "Start game")
                        }

                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.navigate("SettingScreen")
                }){
                    Text(text = "Settings")
                }
                Button(onClick = {
                    speakHome(textToSpeech,"Audio selected")
                    vm.setGameType(GameType.Audio);
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(onClick = {
                    vm.setGameType(GameType.AudioVisual);
                }) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f),
                        contentAlignment = Alignment.Center
                    ){
                        Text("Both")
                    }
                }

                Button(
                    onClick = {
                        vm.setGameType(GameType.Visual);
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}


@Composable
fun PortraitHomeScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech,
    modifier: Modifier
)
{
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val eventInterval by vm.eventInterval.collectAsState()
    val nBack by vm.nBack.collectAsState()
    val gridSize by vm.gridSize.collectAsState()
    val nrOfSpokenLetters by vm.nrOfSpokenLetters.collectAsState()
    val nrOfEventsPerRound by vm.nrOfEventsPerRound.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "High-Score = $highscore\n",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Current Settings:\n"+
                        "N = ${nBack}\n"+
                        "delay = $eventInterval ms\n"+
                        "gameType = ${gameState.gameType}\n"+
                        "nrOfEventsPerRound = $nrOfEventsPerRound\n"+
                        "nrOfSpokenLetters= $nrOfSpokenLetters\n"+
                        "grid size = ${gridSize}x${gridSize}",
                style = MaterialTheme.typography.bodyLarge
            )
            // Todo: You'll probably want to change this "BOX" part of the composable
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { navController.navigate("SettingScreen")
                    }){
                        Text(text = "Settings")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 16.dp)
                        ,
                        onClick = {
                        //vm.startGame()
                        navController.navigate("GameScreen")
                    }) {
                        Text(text = "Start game")
                    }

                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    speakHome(textToSpeech,"Audio selected")
                    vm.setGameType(GameType.Audio);
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(onClick = {
                    vm.setGameType(GameType.AudioVisual);
                }) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f),
                        contentAlignment = Alignment.Center
                    ){
                        Text("Both")
                    }
                }

                Button(
                    onClick = {
                        vm.setGameType(GameType.Visual);
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomDropdownMenu(
    title: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    Column {
        Button(onClick = { expanded = true }) {
            Text(text=title)
            //Text(text = title+": " + items[selectedIndex])
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false
                    onItemSelected(item)
                }, text={
                    Text(text = item)
                })
            }
        }
    }
}



@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    /*
    Surface(){
        HomeScreen(FakeVM(), navController)
    }
     */
}