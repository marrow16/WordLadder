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

    /**
     * Compare solutions for sorting
     * @param other the other solution to be compared with
     * @return 0, 1 or -1 (comparison)
     */
    public int compareTo(Solution other) {
        int sizeCompare = Integer.compare(ladder.size(), other.ladder.size());
        if (sizeCompare == 0) {
            int wordCompare = 0;
            for (int w = 0; w < (ladder.size() - 1) && wordCompare == 0; w++) {
                wordCompare = ladder.get(w).toString().compareTo(other.ladder.get(w).toString());
            }
            return wordCompare;
        }
        return sizeCompare;
    }
}
