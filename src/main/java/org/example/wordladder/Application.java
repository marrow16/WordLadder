package org.example.wordladder;

import org.example.wordladder.words.Dictionary;

public class Application {
    public static final String MAX_LADDER_ARG_NAME = "-maxLadder";

    public static void main(String[] args) {
        if (args.length == 1 && "?".equals(args[0])) {
            System.out.println("WordLadder Puzzle Solver - Command line help:");
            System.out.println("  [startWord] [endWord] [" + MAX_LADDER_ARG_NAME + "=nnn]");
            return;
        }
        new InteractiveCli().run(args);
    }
}
