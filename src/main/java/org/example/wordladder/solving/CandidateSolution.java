package org.example.wordladder.solving;

import org.example.wordladder.words.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CandidateSolution {
    final Solver solver;
    final Set<Word> seenWords = new HashSet<>();
    final List<Word> ladder = new ArrayList<>();

    CandidateSolution(Solver solver, Word startWord, Word nextWord) {
        this.solver = solver;
        addWord(startWord);
        addWord(nextWord);
        solver.incrementExplored();
    }

    CandidateSolution(CandidateSolution ancestor, Word nextWord) {
        solver = ancestor.solver;
        this.seenWords.addAll(ancestor.seenWords);
        this.ladder.addAll(ancestor.ladder);
        addWord(nextWord);
        solver.incrementExplored();
    }

    private void addWord(Word word) {
        seenWords.add(word);
        ladder.add(word);
    }

    List<Word> getLadder() {
        return ladder;
    }
}
