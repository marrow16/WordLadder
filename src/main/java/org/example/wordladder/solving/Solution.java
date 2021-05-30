package org.example.wordladder.solving;

import org.example.wordladder.words.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Solution {
    private final List<Word> ladder;

    Solution(CandidateSolution candidateSolution, boolean reversed) {
        if (reversed) {
            List<Word> copy = new ArrayList<>(candidateSolution.getLadder());
            Collections.reverse(copy);
            this.ladder = Collections.unmodifiableList(copy);
        } else {
            this.ladder = Collections.unmodifiableList(candidateSolution.getLadder());
        }
    }

    Solution(Word... words) {
        ladder = new ArrayList<>(Arrays.asList(words));
    }

    public List<Word> getLadder() {
        return ladder;
    }

    public int size() {
        return ladder.size();
    }

    @Override
    public String toString() {
        return ladder.toString();
    }
}
