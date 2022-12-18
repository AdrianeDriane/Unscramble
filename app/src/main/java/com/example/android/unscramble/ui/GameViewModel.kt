package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    //Set ViewModel Flow state
    //_uiState is accessible only inside of GameViewModel class and will not be affected by
    //changes outside of the class.
    val _uiState = MutableStateFlow(GameUiState())

    //uiState is an immutable state flow version of the _uiState which can be used to pass states
    //in GameScreen. Read only and can only be changed by _uiState which it is assigned to.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    //Current word in the game
    private lateinit var currentWord: String

    //A mutable set of string of already used words so the words won't repeat unless
    //activity is broken
    private var usedWords: MutableSet<String> = mutableSetOf()

    //User's input inside the OutlinedTextField inside GameScreen.kt
    var userGuess by mutableStateOf("")

    //Calls the resetGame() function to initiate the process.
    init {
        resetGame()
    }


    private fun pickRandomWordAndShuffle(): String {
        // Pick random word
        currentWord = allWords.random()

        // If usedWords contains currentWord then recurse, else add currentWord to set of
        // usedWords and then shuffle the currentWord
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        // Set the word to an array of characters and assign to a variable
        val tempWord = word.toCharArray()
        // Shuffle the array of characters
        tempWord.shuffle()

        //If the shuffled word is equal to the actual world then shuffle again
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }

        //Return the String version of the shuffled array of characters
        return String(tempWord)
    }

    //Reset or start game
    fun resetGame() {
        //Clear set of usedWords
        usedWords.clear()
        //Update current scrambled word.
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    //Update user input inside the OutlinedTextBox in GameScreen.kt
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        //If userGuess is correct then increment the GameUiState's score value by 20 then update
        //game state.
        //Else, update UI State's isGuessedWordWrong variable to true.
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        updateUserGuess("")
    }

    fun updateGameState(updatedScore: Int){
        //If usedWords are at max which is 10, set the GameUiState's isGameOver value to true
        //else, copy the current state and increase currentWordCount, updateScore and continue
        //the game
        if(usedWords.size == MAX_NO_OF_WORDS){
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentWordCount = currentState.currentWordCount.inc(),
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore
                )
            }
        }
    }

    fun skipWord(){
        //Find another random word but still increase the usedWords by 1.
        updateGameState(_uiState.value.score)

        //Reset the user's input
        updateUserGuess("")
    }
}