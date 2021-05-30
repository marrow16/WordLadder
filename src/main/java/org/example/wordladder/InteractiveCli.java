package org.example.wordladder;

import org.example.wordladder.exceptions.ApplicationErrorException;
import org.example.wordladder.solving.Generator;
import org.example.wordladder.solving.Options;
import org.example.wordladder.solving.Solution;
import org.example.wordladder.solving.Solver;
import org.example.wordladder.words.Word;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.wordladder.Application.MAX_LADDER_ARG_NAME;

public class InteractiveCli {
    private static final String APP_NAME = "WordLadder";
    private static final String PROMPT = APP_NAME + "> ";
    private static final String TERMINAL_COLOUR_RED = "\u001b[31m";
    private static final String TERMINAL_COLOUR_GREEN = "\u001b[32m";
    private static final String TERMINAL_COLOUR_BLACK = "\u001b[0m";

    private final LineReader lineReader;
    private Step onStep;
    private Puzzle puzzle;
    private Options options;
    private long loadOverhead;

    public InteractiveCli() {
        lineReader = LineReaderBuilder.builder().appName(APP_NAME).build();
    }

    public void run(String[] initialArgs) {
        puzzle = new Puzzle();
        options = new Options();
        onStep = Step.START_STEP;
        processStepsFromArgs(initialArgs);
        run();
    }

    private void processStepsFromArgs(String[] args) {
        try {
            if (args.length > 0) {
                puzzle.setStartWord(args[0]);
                System.out.println(Step.GET_START_WORD.getPrompt() + puzzle.getStartWord());
                onStep = Step.GET_FINAL_WORD;
            }
            if (args.length > 1) {
                puzzle.setFinalWord(args[1]);
                System.out.println(Step.GET_FINAL_WORD.getPrompt() + puzzle.getFinalWord());
                onStep = Step.GET_MAXIMUM_LADDER_LENGTH;
            }
            if (args.length > 2 && args[2].startsWith(MAX_LADDER_ARG_NAME + "=")) {
                try {
                    Integer maxLadder = Integer.parseInt(args[2].substring(MAX_LADDER_ARG_NAME.length() + 1));
                    if (maxLadder < 0) {
                        throw new NumberFormatException("Max ladder cannot be less than 0 (zero)");
                    }
                    options.setMaximumLadderLength(maxLadder);
                    onStep = Step.DONE_STEP;
                } catch (NumberFormatException e) {
                    System.out.println(TERMINAL_COLOUR_RED + "Command line arg '" + MAX_LADDER_ARG_NAME + "' error - " + e.getMessage() + TERMINAL_COLOUR_BLACK);
                }
            }
        } catch (ApplicationErrorException e) {
            System.err.println("Error - " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by:");
                System.err.println(e.getCause().toString());
            }
        }
    }

    private void run() {
        boolean again = true;
        long startTime, endTime;
        while (again) {
            while (onStep != Step.DONE_STEP) {
                processStepInput(lineReader.readLine(onStep.getPrompt()));
            }


            Solver solver = new Solver(puzzle, options);
            boolean solvable = true;
            if (options.getMaximumLadderLength() == 0) {
                System.out.println("Determining minimum ladder length required...");
                // get solver to determine minimum ladder length...
                startTime = System.currentTimeMillis();
                Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
                endTime = System.currentTimeMillis();
                if (minimumLadderLength.isPresent()) {
                    System.out.println("Took " + TERMINAL_COLOUR_GREEN + (endTime - startTime) + "ms" + TERMINAL_COLOUR_BLACK +
                            " to determine minimum ladder length of " + TERMINAL_COLOUR_GREEN + minimumLadderLength.get() + TERMINAL_COLOUR_BLACK);
                    options.setMaximumLadderLength(minimumLadderLength.get().intValue());
                } else {
                    System.out.println(TERMINAL_COLOUR_RED + "Puzzle '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'" +
                            " is not solvable!" +TERMINAL_COLOUR_BLACK +
                            " (Took " + (endTime - startTime) + "ms to determine that)");
                    solvable = false;
                }
            }
            if (solvable) {
                startTime = System.currentTimeMillis();
                solver.solve();
                endTime = System.currentTimeMillis();

                displaySolutions(solver, startTime, endTime);
            }

            System.out.println();
            String input = lineReader.readLine("Run again? [y/n/g]: ");
            if ("g".equals(input)) {
                runGenerator();
            } else {
                again = !"n".equals(input);
                if (again) {
                    onStep = Step.START_STEP;
                    options = new Options();
                    puzzle = new Puzzle();
                    System.out.println();
                }
            }
        }
    }

    private void displaySolutions(Solver solver, long startTime, long endTime) {
        List<Solution> solutions = solver.getSolutions();
        if (!solutions.isEmpty()) {
            System.out.println("Found " + TERMINAL_COLOUR_GREEN + solutions.size() + TERMINAL_COLOUR_BLACK + " solutions" +
                    " in " + TERMINAL_COLOUR_GREEN + ((endTime - startTime)) + "ms" + TERMINAL_COLOUR_BLACK +
                    " (Dictionary load overhead time of " + TERMINAL_COLOUR_GREEN + loadOverhead + "ms" + TERMINAL_COLOUR_BLACK + ")" +
                    " - Explored " + TERMINAL_COLOUR_GREEN + solver.getExploredCount() + TERMINAL_COLOUR_BLACK + " solutions");

            AtomicLong pageStart = new AtomicLong();
            pageStart.set(0);
            solutions.sort(Comparator.comparingInt(Solution::size));
            while (pageStart.get() < solutions.size()) {
                String more = lineReader.readLine("List " + (pageStart.get() == 0 ? "" : "more") + " solutions? (Enter 'n' for no, 'y' or return for next 10, 'all' for all or how many): ");
                if ("n".equals(more)) {
                    break;
                }
                long limit = 10;
                if ("all".equals(more)) {
                    limit = solutions.size();
                } else if (!more.isEmpty() && !"y".equals(more)) {
                    try {
                        limit = Long.parseLong(more);
                    } catch (NumberFormatException e) {
                        limit = 10;
                    }
                }
                solutions.stream()
                        .skip(pageStart.get())
                        .limit(limit)
                        .forEach(solution -> System.out.println(" " + pageStart.incrementAndGet() + "/" + solutions.size() + "  " +
                                TERMINAL_COLOUR_GREEN + solution + TERMINAL_COLOUR_BLACK));
            }
        } else {
            System.out.println(TERMINAL_COLOUR_RED + "Did not find any solutions!" + TERMINAL_COLOUR_BLACK +
                    " in " + TERMINAL_COLOUR_GREEN + ((endTime - startTime)) + "ms" + TERMINAL_COLOUR_BLACK +
                    " (Dictionary load overhead time of " + TERMINAL_COLOUR_GREEN + loadOverhead + "ms" + TERMINAL_COLOUR_BLACK + ")" +
                    " - explored " + TERMINAL_COLOUR_GREEN + solver.getExploredCount() + TERMINAL_COLOUR_BLACK + " solutions");
            Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
            if (minimumLadderLength.isPresent()) {
                System.out.println("Requires a minimum ladder length of " + TERMINAL_COLOUR_GREEN + minimumLadderLength.get() + TERMINAL_COLOUR_BLACK +
                        " to solve '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'");
            } else {
                System.out.println(TERMINAL_COLOUR_RED + "Puzzle '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'" +
                        " is not solvable!" +TERMINAL_COLOUR_BLACK);
            }
        }
    }

    private void runGenerator() {
        System.out.println(TERMINAL_COLOUR_GREEN + "Generate Random Ladder Puzzle..." + TERMINAL_COLOUR_BLACK);
        String input;
        int wordLength = 0;
        int ladderLength = 0;
        boolean inputValid = false;
        while (!inputValid) {
            input = lineReader.readLine("Word length? [2-15]: ");
            try {
                wordLength = Integer.parseInt(input);
                if (wordLength < 2 || wordLength > 15) {
                    throw new IllegalArgumentException();
                }
                inputValid = true;
            } catch (Exception e) {
                System.out.println(TERMINAL_COLOUR_RED + "              Please enter a number between 2 and 8!" + TERMINAL_COLOUR_BLACK);
            }
        }
        inputValid = false;
        while (!inputValid) {
            input = lineReader.readLine("Ladder length? [4-20]: ");
            try {
                ladderLength = Integer.parseInt(input);
                if (ladderLength < 4 || ladderLength > 20) {
                    throw new IllegalArgumentException();
                }
                inputValid = true;
            } catch (Exception e) {
                System.out.println(TERMINAL_COLOUR_RED + "                Please enter a number between 4 and 20!" + TERMINAL_COLOUR_BLACK);
            }
        }
        Generator generator = new Generator(wordLength, ladderLength);
        List<Word> newPuzzle = null;
        while (newPuzzle == null) {
            try {
                newPuzzle = generator.generate();
            } catch (IllegalStateException e) {
                System.out.println(TERMINAL_COLOUR_RED + e.getMessage() + TERMINAL_COLOUR_BLACK);
            }
        }
        System.out.println("Generated puzzle: " + TERMINAL_COLOUR_GREEN + newPuzzle + TERMINAL_COLOUR_BLACK);
        puzzle = new Puzzle(newPuzzle.get(0).toString(), newPuzzle.get(newPuzzle.size() - 1).toString());
        System.out.println(Step.GET_START_WORD.getPrompt() + TERMINAL_COLOUR_GREEN + puzzle.getStartWord() + TERMINAL_COLOUR_BLACK);
        System.out.println(Step.GET_FINAL_WORD.getPrompt() + TERMINAL_COLOUR_GREEN + puzzle.getFinalWord() + TERMINAL_COLOUR_BLACK);
        options = new Options();
        options.setMaximumLadderLength(ladderLength);
        System.out.println(Step.GET_MAXIMUM_LADDER_LENGTH.getPrompt() + TERMINAL_COLOUR_GREEN + ladderLength + TERMINAL_COLOUR_BLACK);
        onStep = Step.DONE;
    }

    private void processStepInput(String input) {
        try {
            switch (onStep) {
                case GET_START_WORD:
                    puzzle.clear();
                    long startLoad = System.currentTimeMillis();
                    puzzle.setStartWord(input);
                    long endLoad = System.currentTimeMillis();
                    loadOverhead = endLoad - startLoad;
                    break;
                case GET_FINAL_WORD:
                    puzzle.setFinalWord(input);
                    break;
                case GET_MAXIMUM_LADDER_LENGTH:
                    if (input.isEmpty()) {
                        System.out.println(TERMINAL_COLOUR_GREEN + "            No answer - assuming 0 (auto calc minimum ladder length)" + TERMINAL_COLOUR_BLACK);
                        options.setMaximumLadderLength(0);
                    } else {
                        try {
                            int intValue = Integer.parseInt(input);
                            if (intValue >= 0 && intValue < 21) {
                                options.setMaximumLadderLength(intValue);
                            } else {
                                throw new IllegalArgumentException("Please enter a number between 0 and 20");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Please enter a number between 0 and 20");
                        }
                    }
                    break;
            }
            onStep = onStep.nextStep();
        } catch (ApplicationErrorException | IllegalArgumentException e) {
            System.out.println(TERMINAL_COLOUR_RED + "            Error: " + e.getMessage() + TERMINAL_COLOUR_BLACK);
        }
    }


    private enum Step {
        GET_START_WORD(0, PROMPT + "Enter start word: "),
        GET_FINAL_WORD(1, PROMPT + "Enter final word: "),
        GET_MAXIMUM_LADDER_LENGTH(2, PROMPT + "Maximum ladder length? [0-20]: "),
        DONE(3,null);

        private static final Step DONE_STEP = DONE;
        private static final Step START_STEP = GET_START_WORD;

        final int order;
        final String prompt;

        Step(int order, String prompt) {
            this.order = order;
            this.prompt = prompt;
        }

        String getPrompt() {
            return prompt;
        }

        Step nextStep() {
            if (this == DONE_STEP) {
                return this;
            }
            for (Step step: values()) {
                if (step.order > order) {
                    return step;
                }
            }
            return DONE_STEP;
        }
    }
}
