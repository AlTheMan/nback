package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository


/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int> //current score
    val highscore: StateFlow<Int> //highscore
    val nBack: Int //n number
    val eventInterval: StateFlow<Long> //how long interval between each tile. in miliseconds
    val nrOfEventsPerRound: StateFlow<Int>
    val eventCounter:StateFlow<Int>
    fun setGameType(gameType: GameType) //Audio, visual, audio-visual
    fun startGame()
    fun checkMatchVisual() //check if you scored a point (visual mode).
    fun checkMatchAudio() //check if you scored a point (Audio mode).

}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    override val nBack: Int = 2 //TODO: viewable object?

    private var job: Job? = null  // coroutine job for the game event
    //private val eventInterval: Long = 2000L
    // Implement eventInterval

    private val _eventInterval = MutableStateFlow(2000L) // 2000 ms (2s)
    override val eventInterval: StateFlow<Long>
        get() = _eventInterval

    private val _nrOfEventsPerRound = MutableStateFlow(10) //"size"
    override val nrOfEventsPerRound: StateFlow<Int>
        get() = _nrOfEventsPerRound

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var eventsVisual = emptyArray<Int>()  // Array with all events
    private var eventsAudio = emptyArray<Int>()  // Array with all events

    private val _eventCounter= MutableStateFlow(0)
    override val eventCounter: StateFlow<Int>
        get() = _eventCounter

    //private var eventCounter: Int=0; //counts how many times an event has occured. how many times a switch between numbers.
    private var eventCounterCheckerVisual: Int=0 //  checks if user has already checked match before for this event. Is used in conjunction with eventChecker
    private var eventCounterCheckerAudio: Int=0 //  checks if user has already checked match before for this event. Is used in conjunction with eventChecker

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        Log.d("GameVM", "inne i startGame")
        job?.cancel()  // Cancel any existing game loop
        _eventCounter.value=0
        eventCounterCheckerVisual=0
        eventCounterCheckerAudio=0
        resetScore()

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        eventsVisual = nBackHelper.generateNBackString(_nrOfEventsPerRound.value, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        eventsAudio = nBackHelper.generateNBackString(_nrOfEventsPerRound.value, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input

        Log.d("GameVM", "The following (Visual) sequence was generated: ${eventsVisual.contentToString()}")
        Log.d("GameVM", "The following (Audio) sequence was generated: ${eventsAudio.contentToString()}")


        val myArray = arrayOf(1, 2, 6, 2, 6, 2, 1, 2, 1, 9)
        val myArray2 = arrayOf(1, 2, 1, 2, 1, 2, 1, 2, 1, 9)
        eventsVisual = myArray2
        //Log.d("GameVM, startGame, eventSize: ", events.size.toString())
        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(eventsAudio)
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(eventsVisual)
            }

            // updates the highscore
            if(_score.value>userPreferencesRepository.highscore.first()){
                userPreferencesRepository.saveHighScore(_score.value)
                _highscore.value=_score.value
            }
        }
    }

    private fun resetScore(){
        _score.value=0
    }

    private fun increaseScore(){
        _score.value++
    }

    private fun decreaseScore(){
        if(_score.value>0){
            _score.value--
        }
    }


    override fun checkMatchAudio() {

        if (eventCounter.value >= nBack && eventCounterCheckerAudio != eventCounter.value) {
            eventCounterCheckerAudio = eventCounter.value; //  checks if user has already checked match before for this event. and reset it between each delay
            var currentEvent = _gameState.value.eventValueAudio
            var nStepBack = eventsAudio.get(eventCounter.value - nBack)
            if (currentEvent == nStepBack) {
                increaseScore()
            } else {
                decreaseScore()
            }
        }
    }

    override fun checkMatchVisual() {

        if(eventCounter.value>=nBack && eventCounterCheckerVisual!=eventCounter.value){
            eventCounterCheckerVisual=eventCounter.value; //  checks if user has already checked match before for this event. and reset it between each delay
            var currentEvent = _gameState.value.eventValueVisual
            var nStepBack= eventsVisual.get(eventCounter.value-nBack)
            if(currentEvent==nStepBack){
                increaseScore()
            }else{
                decreaseScore()
            }
        }


        /**
         * Todo: This function should check if there is a match when the user presses a match button
         * Make sure the user can only register a match once for each event.
         */
    }
    private suspend fun runAudioGame(events: Array<Int>) {
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValueAudio = value)
            delay(_eventInterval.value)
            _eventCounter.value++

            // Calculate the remaining tiles
            val tilesLeft = _nrOfEventsPerRound.value - _eventCounter.value
            _gameState.value = _gameState.value.copy(nrOfTilesLeft = tilesLeft)
        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValueVisual = value)
            delay(_eventInterval.value)
            _eventCounter.value++

            // Calculate the remaining tiles
            val tilesLeft = _nrOfEventsPerRound.value - _eventCounter.value
            _gameState.value = _gameState.value.copy(nrOfTilesLeft = tilesLeft)
        }
    }

    private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType =  GameType.Visual ,  // Type of the game
    val eventValueAudio: Int = -1,  // The value of the array string
    val eventValueVisual: Int = -1,  // The value of the array string
    val nrOfTilesLeft: Int=0
)




class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val nrOfEventsPerRound: StateFlow<Int>
        get() =  MutableStateFlow(10).asStateFlow()
    override val eventCounter: StateFlow<Int>
        get() = TODO("Not yet implemented")
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()

    override val eventInterval: StateFlow<Long>
        get() = MutableStateFlow(42L).asStateFlow()
    override val nBack: Int
        get() = 2

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatchVisual() {
    }

    override fun checkMatchAudio() {
        TODO("Not yet implemented")
    }
}