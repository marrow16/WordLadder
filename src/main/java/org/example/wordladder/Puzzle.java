package org.example.wordladder;

import org.example.wordladder.exceptions.PuzzleErrorException;
import org.example.wordladder.words.Dictionary;
import org.example.wordladder.words.Word;

public class Puzzle {
    private Dictionary dictionary;
    private Word startWord;
    private Word finalWord;

    public Puzzle() {
    }

    public Puzzle(String startWord) {
        setStartWord(startWord);
    }

    public Puzzle(String startWord, String finalWord) {
        this(startWord);
        setFinalWord(finalWord);
    }

    public void clear() {
        dictionary = null;
        startWord = null;
        finalWord = null;
    }

    public void setStartWord(String startWord) {
        this.dictionary = Dictionary.Factory.fromWord(startWord);
        this.startWord = this.dictionary.getWord(startWord);
        if (this.startWord == null) {
            throw new PuzzleErrorException("Start word '" + startWord.toUpperCase() + "' not found in dictionary");
        } else if (this.startWord.isIslandWord()) {
            throw new PuzzleErrorException("Start word '" + startWord.toUpperCase() + "' is an island word (varying any character does not create another valid word)");
        }
    }

    public void setFinalWord(String finalWord) {
        if (startWord == null) {
            throw new PuzzleErrorException("Start word has not been set yet!");
        }
        if (finalWord.length() != dictionary.getWordLength()) {
            throw new PuzzleErrorException("End word '" + finalWord.toUpperCase() + "' (length " + finalWord.length() + ") must match start word length (" + dictionary.getWordLength() + ")");
        }
        this.finalWord = dictionary.getWord(finalWord);
        if (this.finalWord == null) {
            throw new PuzzleErrorException("End word '" + finalWord.toUpperCase() + "' not found in dictionary");
        } else if (this.finalWord.isIslandWord()) {
            throw new PuzzleErrorException("End word '" + finalWord.toUpperCase() + "' is an island word (varying any character does not create another valid word)");
        }
    }

    public Word getStartWord() {
        return startWord;
    }

    public Word getFinalWord() {
        return finalWord;
    }
}
