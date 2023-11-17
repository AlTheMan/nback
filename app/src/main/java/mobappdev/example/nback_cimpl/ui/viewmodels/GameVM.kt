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
import kotlinx.coroutines.flow.combine
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
    val nBack: StateFlow<Int> //n number
    val eventInterval: StateFlow<Long> //how long interval between each tile. in miliseconds
    val nrOfEventsPerRound: StateFlow<Int>
    val eventCounter:StateFlow<Int>
    val gridSize:StateFlow<Int>
    val nrOfSpokenLetters:StateFlow<Int>

    fun setGameType(gameType: GameType) //Audio, visual, audio-visual
    fun startGame()
    fun checkMatchVisual() //check if you scored a point (visual mode).
    fun checkMatchAudio() //check if you scored a point (Audio mode).
    fun setNBack(newNback: Int) //set nBack value
    fun setEventInterval(eventInterval: Long)
    fun setNrOfEvents(nrOfevents: Int)
    fun setGridSize(gridSize: Int)
    fun setNrOfSpokenLetters(nrOfSpokenLetters: Int)
    fun saveSettings()
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

    private val _nBack = MutableStateFlow(2)
    override val nBack: StateFlow<Int>
        get() = _nBack

    // nBack is currently hardcoded
    //override val nBack: Int = 2 //TODO: viewable object?

    private var job: Job? = null  // coroutine job for the game event
    //private val eventInterval: Long = 2000L
    // Implement eventInterval

    private val _eventInterval = MutableStateFlow(2000L) // 2000 ms (2s)
    override val eventInterval: StateFlow<Long>
        get() = _eventInterval

    private val _nrOfEventsPerRound = MutableStateFlow(10) //"size"
    override val nrOfEventsPerRound: StateFlow<Int>
        get() = _nrOfEventsPerRound

    private val _gridSize = MutableStateFlow(3) //"size"
    override val gridSize: StateFlow<Int>
        get() = _gridSize

    private val _nrOfSpokenLetters = MutableStateFlow(9) //"size"
    override val nrOfSpokenLetters: StateFlow<Int>
        get() = _nrOfSpokenLetters

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
        resetGameState()

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        eventsVisual = nBackHelper.generateNBackString(_nrOfEventsPerRound.value, gridSize.value*gridSize.value, 30, nBack.value).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        eventsAudio = nBackHelper.generateNBackString(_nrOfEventsPerRound.value, nrOfSpokenLetters.value, 30, nBack.value).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input

        Log.d("GameVM", "The following (Visual) sequence was generated: ${eventsVisual.contentToString()}")
        Log.d("GameVM", "The following (Audio) sequence was generated: ${eventsAudio.contentToString()}")



        val myArray = arrayOf(1, 2, 6, 2, 6, 2, 1, 2, 1, 9)
        val myArray2 = arrayOf(1, 2, 1, 2, 1, 2, 1, 2, 1, 9)
        //eventsVisual = myArray2
        //eventsAudio=myArray2
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

    override fun saveSettings(){
        viewModelScope.launch {
            saveSettingsToDatabase()
        }
    }

     private suspend fun saveSettingsToDatabase(){

         viewModelScope.launch{
             launch {
                 userPreferencesRepository.saveGridSize(gridSize.value)
             }
             launch{
                 userPreferencesRepository.saveNBack(nBack.value)
             }
             launch{
                 userPreferencesRepository.saveNrOfEvents(nrOfEventsPerRound.value)
             }
             launch{
                 userPreferencesRepository.saveNrOfSpokenLetters(nrOfSpokenLetters.value)
             }
             launch{
                 userPreferencesRepository.saveEventInterval(eventInterval.value)

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
        if (eventCounter.value >= nBack.value && eventCounterCheckerAudio != eventCounter.value && !_gameState.value.gameType.equals(GameType.Visual)) {
            eventCounterCheckerAudio = eventCounter.value; //  checks if user has already checked match before for this event. and reset it between each delay
            var currentEvent = _gameState.value.eventValueAudio
            var nStepBack = eventsAudio.get(eventCounter.value - nBack.value)
            if (currentEvent == nStepBack) {
                increaseScore()
            } else {
                decreaseScore()
                _gameState.value = _gameState.value.copy(correctAudioPress = false)
            }
        }
        else{
            _gameState.value = _gameState.value.copy(correctAudioPress = false)
        }
    }

    override fun setNBack(newNback: Int) {
        if(newNback>0 && newNback<=50){
            _nBack.value=newNback
            if(newNback>nrOfEventsPerRound.value){
                _nrOfEventsPerRound.value=newNback
            }
        }
    }

    override fun setEventInterval(eventInterval: Long) {
        if(eventInterval>0){
            _eventInterval.value=eventInterval
        }
    }

    override fun setNrOfEvents(nrOfevents: Int) {
        if(nrOfevents>0){
            _nrOfEventsPerRound.value=nrOfevents
            if(nrOfevents<nBack.value){
                _nBack.value=nrOfevents
            }
        }
    }

    override fun setGridSize(gridSizeNew: Int) {
        if(gridSizeNew>0){
            _gridSize.value=gridSizeNew
        }
    }

    override fun setNrOfSpokenLetters(nrOfSpokenLettersNew: Int) {
        if(nrOfSpokenLettersNew>0){
            _nrOfSpokenLetters.value=nrOfSpokenLettersNew
        }
    }


    /**
     * This function should check if there is a match when the user presses a match button
     */
    override fun checkMatchVisual() {
        if(eventCounter.value>=nBack.value && eventCounterCheckerVisual!=eventCounter.value && !_gameState.value.gameType.equals(GameType.Audio)){
            eventCounterCheckerVisual=eventCounter.value; //  checks if user has already checked match before for this event. and reset it between each delay
            var currentEvent = _gameState.value.eventValueVisual
            var nStepBack= eventsVisual.get(eventCounter.value-nBack.value)
            if(currentEvent==nStepBack){
                increaseScore()
            }else{
                decreaseScore()
                _gameState.value = _gameState.value.copy(correctVisualPress = false)
                Log.d("GameVM", "correctVisualPress: " + _gameState.value.correctVisualPress.toString())
            }
        }else{
            _gameState.value = _gameState.value.copy(correctVisualPress = false)
        }
    }

    private fun resetGameState(){
        _gameState.value = _gameState.value.copy(eventValueAudio = -1)
        _gameState.value = _gameState.value.copy(eventValueVisual = -1)
        _gameState.value = _gameState.value.copy(correctAudioPress = true)
        _gameState.value = _gameState.value.copy(correctVisualPress = true)

    }

    private suspend fun runAudioGame(events: Array<Int>) {

        // Calculate the initial remaining tiles
        _gameState.value = _gameState.value.copy(nrOfTilesLeft = _nrOfEventsPerRound.value)


        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValueAudio = value)
            delay(_eventInterval.value)
            _eventCounter.value++

            // Calculate the remaining tiles
            val tilesLeft = _nrOfEventsPerRound.value - _eventCounter.value
            _gameState.value = _gameState.value.copy(nrOfTilesLeft = tilesLeft)

            //reset correctAudioPress
            _gameState.value = _gameState.value.copy(correctAudioPress = true)

        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        // Calculate the initial remaining tiles
        _gameState.value = _gameState.value.copy(nrOfTilesLeft = _nrOfEventsPerRound.value)

        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValueVisual = value)
            delay(_eventInterval.value)
            _eventCounter.value++

            // Calculate the remaining tiles
            val tilesLeft = _nrOfEventsPerRound.value - _eventCounter.value
            _gameState.value = _gameState.value.copy(nrOfTilesLeft = tilesLeft)

            //reset correctVisualPress
            _gameState.value = _gameState.value.copy(correctVisualPress = true)

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
            launch {
                userPreferencesRepository.highscore.collect { value ->
                    _highscore.value = value
                }
            }
            launch {
                userPreferencesRepository.nrofspokenletters.collect { value ->
                    _nrOfSpokenLetters.value = value
                }
            }
            launch {
                userPreferencesRepository.nback.collect { value ->
                    _nBack.value = value
                }
            }
            launch {
                userPreferencesRepository.gridsize.collect { value ->
                    _gridSize.value = value
                }
            }
            launch {
                userPreferencesRepository.nrofevents.collect { value ->
                    _nrOfEventsPerRound.value = value
                }
            }

            launch {
                userPreferencesRepository.eventinterval.collect { value ->
                    _eventInterval.value = value
                }
            }
            /*
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
            userPreferencesRepository.nrofspokenletters.collect{
                _nrOfSpokenLetters.value=it
            }
            //userPreferencesRepository.eventInterval.collect{
            //    _eventInterval.value=it
            //}
            userPreferencesRepository.nback.collect{
                _nBack.value=it
            }
            userPreferencesRepository.gridsize.collect{
                _gridSize.value=it
            }
            userPreferencesRepository.nrofevents.collect{
                _nrOfEventsPerRound.value=it
            }
             */
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
    val nrOfTilesLeft: Int=0,
    val correctAudioPress: Boolean=true, //if user has pressed the audio button correctly and gained a score. Is used to shake UI-buttons when pressing at the wrong time.
    val correctVisualPress: Boolean=true,
)



/*
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
    override val nBack: StateFlow<Int>
        get() = TODO("Not yet implemented")

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatchVisual() {
    }

    override fun checkMatchAudio() {
        TODO("Not yet implemented")
    }
} */