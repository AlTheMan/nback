package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.R
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



fun speak(  textToSpeech: TextToSpeech, text: String) {
    // Use the TextToSpeech instance to speak the provided text
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}
@Composable
fun GameScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold {
        if(isLandscape){
            LandScapeGameScreen(vm, navController,textToSpeech, Modifier.padding(it))
        }
        else{
            PortraitGameScreen(vm, navController,textToSpeech, Modifier.padding(it))
        }
    }

}

@Composable
fun PortraitGameScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech,
    modifier: Modifier
) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val eventCounter by vm.eventCounter.collectAsState()
    val firstLetterForAudio by vm.firstLetterForAudio.collectAsState()
    val gridSize by vm.gridSize.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var isShakingVisual by remember { mutableStateOf(false) }
    var isShakingAudio by remember { mutableStateOf(false) }

    LaunchedEffect(gameState.correctVisualPress){
        if(!gameState.correctVisualPress){
            isShakingVisual = true
            delay(400) // Adjust the duration as needed (in milliseconds)
            isShakingVisual = false
        }else{
            isShakingVisual = false
        }
    }
    LaunchedEffect(gameState.correctAudioPress){
        if(!gameState.correctAudioPress){
            isShakingAudio = true
            delay(400) // Adjust the duration as needed (in milliseconds)
            isShakingAudio = false
        }else{
            isShakingAudio = false
        }
    }

    // Observe the eventCounter using LaunchedEffect
    LaunchedEffect(eventCounter, firstLetterForAudio) {
        if(gameState.gameType.equals(GameType.Audio) || gameState.gameType.equals(GameType.AudioVisual)){
            if(gameState.eventValueAudio!=-1){
                // This block will be executed whenever vm.eventCounter changes
                val eventValueAudio = (gameState.eventValueAudio + 'a'.code).toChar()
                // Call the speak function with the updated eventCounter
                speak(textToSpeech, eventValueAudio.toString())
            }
        }
    }

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
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.1f),  // Text occupies 15% of the vertical space
                text="score: $score\n"
                        //+ "nrOfTilesLeft: ${gameState.nrOfTilesLeft}\n" +
                        //"Current eventValue: ${gameState.eventValueVisual}, ${(gameState.eventValueAudio + 'a'.code).toChar()}"
                        ,
                style = MaterialTheme.typography.headlineSmall
            )
            Box(
                modifier = Modifier.weight(0.7f), // Box occupies 70% of the vertical space
                contentAlignment = Alignment.Center
            ) {
                Column(

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .aspectRatio(1f)
                            .fillMaxWidth()

                            //.padding(42.dp)
                    ){
                        GenerateTiles(Modifier, gameState.eventValueVisual, gridSize)
                    }
                    //Spacer(modifier = Modifier.padding(42.dp))
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 42.dp)
                            .weight(0.1f)
                        ,
                        onClick = {
                        vm.startGame()
                    }) {
                        Text(
                            text = "Start",
                            //modifier = Modifier.weight(1f)
                            )

                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f) // Row occupies 15% of the vertical space
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { //Audio-button
                    vm.checkMatchAudio()
                },
                    modifier = Modifier
                        .graphicsLayer(
                            rotationZ = if (isShakingAudio) 30f else 0f, //roterar 30 grader, ananrs 0 grader. f står för float tror jag.
                        )
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button( //Visual-buton
                    onClick = {
                        vm.checkMatchVisual();
                    },
                    modifier = Modifier
                        .graphicsLayer(
                            rotationZ = if (isShakingVisual) 30f else 0f, //roterar 30 grader, ananrs 0 grader. f står för float tror jag.
                        )
                    ) {
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
fun LandScapeGameScreen(
    vm: GameViewModel,
    navController: NavHostController,
    textToSpeech: TextToSpeech,
    modifier: Modifier
) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val eventCounter by vm.eventCounter.collectAsState()
    val gridSize by vm.gridSize.collectAsState()
    val firstLetterForAudio by vm.firstLetterForAudio.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var isShakingVisual by remember { mutableStateOf(false) }
    var isShakingAudio by remember { mutableStateOf(false) }

    //detta borde wgentligen vara i Vy-modellen istället.
    LaunchedEffect(gameState.correctVisualPress){
        if(!gameState.correctVisualPress){
            isShakingVisual = true
            delay(400) // Adjust the duration as needed (in milliseconds)
            isShakingVisual = false
        }else{
            isShakingVisual = false
        }
    }
    LaunchedEffect(gameState.correctAudioPress){
        if(!gameState.correctAudioPress){
            isShakingAudio = true
            delay(400) // Adjust the duration as needed (in milliseconds)
            isShakingAudio = false
        }else{
            isShakingAudio = false
        }
    }

    // Observe the eventCounter using LaunchedEffect
    LaunchedEffect(eventCounter, firstLetterForAudio) {
        if(gameState.gameType.equals(GameType.Audio) || gameState.gameType.equals(GameType.AudioVisual)){
            if(gameState.eventValueAudio!=-1){
                // This block will be executed whenever vm.eventCounter changes
                val eventValueAudio = (gameState.eventValueAudio + 'a'.code).toChar()
                // Call the speak function with the updated eventCounter
                speak(textToSpeech, eventValueAudio.toString())
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                ,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier= Modifier
                        .aspectRatio(1f)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GenerateTiles(Modifier, gameState.eventValueVisual, gridSize)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .padding(end = 16.dp) // Add padding to the right
                    .weight(0.4f)
                ,
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    //modifier = Modifier.padding(32.dp),
                    text="score: $score\n",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(onClick = {
                    vm.startGame()
                }) {
                    Text(
                        text = "Start",
                    )

                }
                Button(onClick = { //Audio-button
                    vm.checkMatchAudio()
                },
                    modifier = Modifier
                        .padding(top = 16.dp) // Add padding to the top
                        .graphicsLayer(
                            rotationZ = if (isShakingAudio) 30f else 0f, //roterar 30 grader, ananrs 0 grader. f står för float tror jag.
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(64.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button( //Visual-buton
                    onClick = {
                        vm.checkMatchVisual();
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .graphicsLayer(
                            rotationZ = if (isShakingVisual) 30f else 0f, //roterar 30 grader, ananrs 0 grader. f står för float tror jag.
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(64.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun GenerateTiles(modifier: Modifier = Modifier, currentTile: Int, gridSize: Int) {
    Column (
        modifier= modifier
    ){
        var counter=0;
        for (i in 1..gridSize) {
            Row(
                modifier= modifier
                //.weight(1f)
            ) {
                for (j in 1..gridSize) {
                    counter++;
                    val boxColor = when {
                        currentTile == counter  -> Color.Gray // Change this to the desired color
                        else -> Color.LightGray
                    }
                    Box(
                        modifier= modifier
                            .weight(.2f) // Each box will take up equal space
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .background(boxColor)
                            //.padding(horizontal = 32.dp, vertical = 16.dp)
                            //.padding(horizontal = 48.dp, vertical = 32.dp)
                            .aspectRatio(1F)
                    ) {
                        Text("");
                    }
                    //Spacer(
                    //    modifier = Modifier.width(16.dp)
                    //)
                }
            }
            //Spacer(
            //    modifier = Modifier.height(16.dp)
            //)
        }
    }
}



@Preview
@Composable
fun GameScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    /*
    Surface(){
        HomeScreen(FakeVM(), navController)
    } */
}