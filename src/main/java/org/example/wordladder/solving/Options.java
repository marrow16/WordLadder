package org.example.wordladder.solving;

public class Options {
    public static final int DEFAULT_MAXIMUM_LADDER_LENGTH = 6;

    private int maximumLadderLength = DEFAULT_MAXIMUM_LADDER_LENGTH;

    public Options() {
    }

    public int getMaximumLadderLength() {
        return maximumLadderLength;
    }

    public void setMaximumLadderLength(int maximumLadderLength) {
        this.maximumLadderLength = maximumLadderLength;
    }
}
