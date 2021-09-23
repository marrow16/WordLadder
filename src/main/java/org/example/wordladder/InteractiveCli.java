package org.example.wordladder;

import org.example.wordladder.exceptions.ApplicationErrorException;
import org.example.wordladder.solving.Generator;
import org.example.wordladder.solving.Options;
import org.example.wordladder.solving.Solution;
import org.example.wordladder.solving.Solver;
import org.example.wordladder.words.Word;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.wordladder.Application.MAX_LADDER_ARG_NAME;

public class InteractiveCli {
    private static final String APP_NAME = "WordLadder";
    private static final String PROMPT = APP_NAME + "> ";
    private static final boolean USE_TERMINAL_COLOURS = true;
    private static final String TERMINAL_COLOUR_RED = "\u001b[31m";
    private static final String TERMINAL_COLOUR_GREEN = "\u001b[32m";
    private static final String TERMINAL_COLOUR_BLACK = "\u001b[0m";

    private static final int DEFAULT_LIMIT = 10;
    private static final double NANOS_IN_MILLI = 1000000d;
    private static final double MAX_MILLIS = 1000d;

    private static final int MINIMUM_WORD_LENGTH = 2;
    private static final int MAXIMUM_WORD_LENGTH = 15;
    private static final int MINIMUM_LADDER_LENGTH = 4;
    private static final int MAXIMUM_LADDER_LENGTH = 20;

    private static final DecimalFormat FORMAT_SECONDS = new DecimalFormat("#,##0.000");
    private static final DecimalFormat FORMAT_MILLIS = new DecimalFormat("#0.00");
    private static final DecimalFormat FORMAT_COUNT = new DecimalFormat("#,##0");

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
                    int maxLadder = Integer.parseInt(args[2].substring(MAX_LADDER_ARG_NAME.length() + 1));
                    if (maxLadder < 0) {
                        throw new NumberFormatException("Max ladder cannot be less than 0 (zero)");
                    }
                    options.setMaximumLadderLength(maxLadder);
                    onStep = Step.DONE_STEP;
                } catch (NumberFormatException e) {
                    System.out.println(red("Command line arg '" + MAX_LADDER_ARG_NAME
                            + "' error - " + e.getMessage()));
                }
            }
        } catch (ApplicationErrorException e) {
            System.out.println(red("Error - " + e.getMessage()));
            if (e.getCause() != null) {
                System.out.println(red("Caused by:"));
                System.out.println(red(e.getCause().toString()));
            }
        }
    }

    private void run() {
        boolean again = true;
        long startTime;
        long endTime;
        while (again) {
            while (onStep != Step.DONE_STEP) {
                processStepInput(lineReader.readLine(onStep.getPrompt()));
            }

            Solver solver = new Solver(puzzle, options);
            boolean solvable = true;
            if (options.getMaximumLadderLength() == 0) {
                System.out.println("Determining minimum ladder length required...");
                // get solver to determine minimum ladder length...
                startTime = System.nanoTime();
                Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
                endTime = System.nanoTime();
                if (minimumLadderLength.isPresent()) {
                    System.out.println("Took " + green(nanoTimeToMs(startTime, endTime))
                            + " to determine minimum ladder length of " + green(minimumLadderLength.get()));
                    options.setMaximumLadderLength(minimumLadderLength.get());
                } else {
                    System.out.println(red("Puzzle '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'"
                            + " is not solvable!")
                            + " (Took " + nanoTimeToMs(startTime, endTime) + " to determine that)");
                    solvable = false;
                }
            }
            if (solvable) {
                startTime = System.nanoTime();
                solver.solve();
                endTime = System.nanoTime();

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
            System.out.println("Found " + green(FORMAT_COUNT.format(solutions.size())) + " solutions"
                    + " in " + green(nanoTimeToMs(startTime, endTime))
                    + " (Dictionary load overhead time of " + green(nanoTimeToMs(loadOverhead)) + ")"
                    + " - Explored " + green(FORMAT_COUNT.format(solver.getExploredCount())) + " solutions");

            AtomicLong pageStart = new AtomicLong(0);
            solutions.sort(Solution::compareTo);
            while (pageStart.get() < solutions.size()) {
                String more = lineReader.readLine("List" + (pageStart.get() == 0 ? "" : " more")
                        + " solutions? (Enter 'n' for no, 'y' or return for next 10, 'all' for all or how many): ");
                if ("n".equals(more)) {
                    break;
                }
                long limit = DEFAULT_LIMIT;
                if ("all".equals(more)) {
                    limit = solutions.size();
                } else if (!more.isEmpty() && !"y".equals(more)) {
                    try {
                        limit = Long.parseLong(more);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                solutions.stream()
                        .skip(pageStart.get())
                        .limit(limit)
                        .forEach(solution -> System.out.println(" " + pageStart.incrementAndGet()
                                + "/" + solutions.size() + "  " + green(solution)));
            }
        } else {
            System.out.println(red("Did not find any solutions!")
                    + " in " + green(nanoTimeToMs(startTime, endTime))
                    + " (Dictionary load overhead time of " + green(nanoTimeToMs(loadOverhead)) + ")"
                    + " - explored " + green(solver.getExploredCount()) + " solutions");
            Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
            if (minimumLadderLength.isPresent()) {
                System.out.println("Requires a minimum ladder length of " + green(minimumLadderLength.get())
                        + " to solve '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'");
            } else {
                System.out.println(red("Puzzle '" + puzzle.getStartWord() + "' to '" + puzzle.getFinalWord() + "'"
                        + " is not solvable!"));
            }
        }
    }

    private static String red(Object message) {
        if (USE_TERMINAL_COLOURS) {
            return TERMINAL_COLOUR_RED + message + TERMINAL_COLOUR_BLACK;
        }
        return Objects.toString(message);
    }

    private static String green(Object message) {
        if (USE_TERMINAL_COLOURS) {
            return TERMINAL_COLOUR_GREEN + message + TERMINAL_COLOUR_BLACK;
        }
        return Objects.toString(message);
    }

    private static String nanoTimeToMs(long startTime, long endTime) {
        return nanoTimeToMs(endTime - startTime);
    }
    private static String nanoTimeToMs(long time) {
        double millis = time / NANOS_IN_MILLI;
        if (millis >= MAX_MILLIS) {
            return FORMAT_SECONDS.format(millis / MAX_MILLIS) + "sec";
        }
        return FORMAT_MILLIS.format(millis) + "ms";
    }

    private void runGenerator() {
        System.out.println(green("Generate Random Ladder Puzzle..."));
        String input;
        int wordLength = 0;
        int ladderLength = 0;
        boolean inputValid = false;
        while (!inputValid) {
            input = lineReader.readLine("Word length? ["
                    + MINIMUM_WORD_LENGTH + "-" + MAXIMUM_WORD_LENGTH + "]: ");
            try {
                wordLength = Integer.parseInt(input);
                if (wordLength < MINIMUM_WORD_LENGTH || wordLength > MAXIMUM_WORD_LENGTH) {
                    throw new IllegalArgumentException();
                }
                inputValid = true;
            } catch (Exception e) {
                System.out.println(red("              Please enter a number between "
                        + MINIMUM_WORD_LENGTH + " and " + MAXIMUM_WORD_LENGTH + "!"));
            }
        }
        inputValid = false;
        while (!inputValid) {
            input = lineReader.readLine("Ladder length? ["
                    + MINIMUM_LADDER_LENGTH + "-" + MAXIMUM_LADDER_LENGTH + "]: ");
            try {
                ladderLength = Integer.parseInt(input);
                if (ladderLength < MINIMUM_LADDER_LENGTH || ladderLength > MAXIMUM_LADDER_LENGTH) {
                    throw new IllegalArgumentException();
                }
                inputValid = true;
            } catch (Exception e) {
                System.out.println(red("                Please enter a number between "
                        + MINIMUM_LADDER_LENGTH + " and " + MAXIMUM_LADDER_LENGTH + "!"));
            }
        }
        Generator generator = new Generator(wordLength, ladderLength);
        List<Word> newPuzzle = null;
        while (newPuzzle == null) {
            try {
                newPuzzle = generator.generate();
            } catch (IllegalStateException e) {
                System.out.println(red(e.getMessage()));
            }
        }
        System.out.println("Generated puzzle: " + green(newPuzzle));
        puzzle = new Puzzle(newPuzzle.get(0).toString(), newPuzzle.get(newPuzzle.size() - 1).toString());
        System.out.println(Step.GET_START_WORD.getPrompt() + green(puzzle.getStartWord()));
        System.out.println(Step.GET_FINAL_WORD.getPrompt() + green(puzzle.getFinalWord()));
        options = new Options();
        options.setMaximumLadderLength(ladderLength);
        System.out.println(Step.GET_MAXIMUM_LADDER_LENGTH.getPrompt() + green(ladderLength));
        onStep = Step.DONE;
    }

    private void processStepInput(String input) {
        try {
            switch (onStep) {
                case GET_START_WORD:
                    puzzle.clear();
                    long startLoad = System.nanoTime();
                    puzzle.setStartWord(input);
                    long endLoad = System.nanoTime();
                    loadOverhead = endLoad - startLoad;
                    break;
                case GET_FINAL_WORD:
                    puzzle.setFinalWord(input);
                    break;
                case GET_MAXIMUM_LADDER_LENGTH:
                    if (input.isEmpty()) {
                        System.out.println(green(
                                "            No answer - assuming auto calc of minimum ladder length"));
                        options.setMaximumLadderLength(0);
                    } else {
                        try {
                            int intValue = Integer.parseInt(input);
                            if (intValue >= MINIMUM_LADDER_LENGTH && intValue < MAXIMUM_LADDER_LENGTH) {
                                options.setMaximumLadderLength(intValue);
                            } else {
                                throw new IllegalArgumentException("Please enter a number between "
                                        + MINIMUM_LADDER_LENGTH + " and " + MAXIMUM_LADDER_LENGTH);
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Please enter a number between "
                                    + MINIMUM_LADDER_LENGTH + " and " + MAXIMUM_LADDER_LENGTH);
                        }
                    }
                    break;
            }
            onStep = onStep.nextStep();
        } catch (ApplicationErrorException | IllegalArgumentException e) {
            System.out.println(red("            Error: " + e.getMessage()));
        }
    }


    private enum Step {
        GET_START_WORD(0, PROMPT + "Enter start word: "),
        GET_FINAL_WORD(1, PROMPT + "Enter final word: "),
        GET_MAXIMUM_LADDER_LENGTH(2, PROMPT
                + "Maximum ladder length? [" + MINIMUM_LADDER_LENGTH + "-" + MAXIMUM_LADDER_LENGTH + ", or return]: "),
        DONE(3, null);

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
