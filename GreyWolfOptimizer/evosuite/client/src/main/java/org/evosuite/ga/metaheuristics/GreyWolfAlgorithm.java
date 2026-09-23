package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;

import static org.evosuite.utils.Randomness.nextDouble;

/**
 * Discrete Grey Wolf Optimizer for EvoSuite test-suite generation.
 *
 * Each wolf represents one TestSuiteChromosome.
 *
 * Alpha = best wolf
 * Beta  = second best wolf
 * Delta = third best wolf
 *
 * Since EvoSuite test suites are discrete structures, the continuous
 * GWO equations cannot be applied literally. Therefore this class
 * implements a discrete, leader-guided approximation of GWO.
 *
 * The implementation keeps the three GWO leaders and uses their
 * influence to select a mutation base. Exactly one mutation is
 * applied to each generated candidate.
 */
public class GreyWolfAlgorithm
        extends GeneticAlgorithm<TestSuiteChromosome> {

    private static final long serialVersionUID = 1L;

    /**
     * Number of GWO leaders.
     *
     * Alpha = best
     * Beta  = second best
     * Delta = third best
     */
    private static final int NUMBER_OF_LEADERS = 3;

    /**
     * Default horizon used for the GWO exploration/exploitation
     * parameter when the search is not controlled by generations.
     *
     * This value does NOT control the actual stopping condition.
     */
    private static final double DEFAULT_GENERATION_HORIZON = 100.0;

    /**
     * Probability of performing pure random mutation instead of
     * leader-guided mutation.
     *
     * This is an additional diversity mechanism.
     */
    private static final double RANDOM_EXPLORATION_RATE = 0.20;

    /**
     * Prevent excessive debug output.
     */
    private int gwoDebugCount = 0;


    public GreyWolfAlgorithm(
            ChromosomeFactory<TestSuiteChromosome> factory) {

        super(factory);

        LoggingUtils.getEvoLogger().info(
                "========== CUSTOM GWO INITIALIZED =========="
        );
    }


    /**
     * Generate one new generation.
     *
     * Important:
     *
     * This method is responsible only for generating the next
     * population. Fitness evaluation is handled by generateSolution()
     * after evolve() returns.
     *
     * This avoids unnecessary fitness evaluations.
     */
    @Override
    protected void evolve() {

        if (gwoDebugCount < 5 || currentIteration % 20 == 0) {

            LoggingUtils.getEvoLogger().debug(
                    "========== GWO EVOLVE iteration="
                            + currentIteration
                            + " population="
                            + population.size()
                            + " =========="
            );

            gwoDebugCount++;
        }


        /*
         * Nothing to evolve.
         */
        if (population.isEmpty()) {
            return;
        }


        /*
         * ---------------------------------------------------------
         * SMALL POPULATION
         * ---------------------------------------------------------
         *
         * GWO requires Alpha, Beta and Delta.
         *
         * If fewer than three wolves are available, use a simpler
         * mutation-based strategy.
         */

        if (population.size() < NUMBER_OF_LEADERS) {

            evolveSmallPopulation();

            currentIteration++;

            return;
        }


        /*
         * ---------------------------------------------------------
         * LEADERS
         * ---------------------------------------------------------
         *
         * The population is expected to be sorted before evolve().
         *
         * population[0] = Alpha
         * population[1] = Beta
         * population[2] = Delta
         */

        TestSuiteChromosome alpha =
                population.get(0);

        TestSuiteChromosome beta =
                population.get(1);

        TestSuiteChromosome delta =
                population.get(2);


        /*
         * ---------------------------------------------------------
         * GWO PARAMETER
         * ---------------------------------------------------------
         *
         * a decreases from approximately 2 to 0 during the search.
         */

        double a = calculateA();


        List<TestSuiteChromosome> newGeneration =
                new ArrayList<>(population.size());


        /*
         * ---------------------------------------------------------
         * ELITISM
         * ---------------------------------------------------------
         *
         * Preserve Alpha, Beta and Delta.
         *
         * This guarantees that the best solutions are not destroyed
         * by mutation.
         */

        newGeneration.add(alpha.clone());

        if (!isNextPopulationFull(newGeneration)) {
            newGeneration.add(beta.clone());
        }

        if (!isNextPopulationFull(newGeneration)) {
            newGeneration.add(delta.clone());
        }


        /*
         * ---------------------------------------------------------
         * GENERATE REMAINING WOLVES
         * ---------------------------------------------------------
         */

        int populationIndex = NUMBER_OF_LEADERS;


        while (!isNextPopulationFull(newGeneration)) {

            /*
             * Cycle through the current population.
             *
             * Starting after the three leaders gives non-leader
             * wolves a chance to contribute as parents.
             */
            TestSuiteChromosome wolf =
                    population.get(
                            populationIndex % population.size()
                    );

            populationIndex++;


            TestSuiteChromosome candidate;


            /*
             * -----------------------------------------------------
             * RANDOM EXPLORATION
             * -----------------------------------------------------
             *
             * Occasionally ignore the leaders and mutate the
             * current wolf directly.
             *
             * This prevents excessive convergence toward Alpha.
             */

            if (nextDouble() < RANDOM_EXPLORATION_RATE) {

                candidate = wolf.clone();

                candidate.mutate();

                notifyMutation(candidate);

            } else {

                /*
                 * -------------------------------------------------
                 * LEADER-GUIDED GWO SEARCH
                 * -------------------------------------------------
                 */

                candidate = createCandidate(
                        wolf,
                        alpha,
                        beta,
                        delta,
                        a
                );
            }


            /*
             * -----------------------------------------------------
             * BLOAT CONTROL
             * -----------------------------------------------------
             *
             * Do not insert an oversized candidate.
             *
             * If the mutation produced an invalid/too-large suite,
             * preserve the original wolf instead.
             */

            if (isTooLong(candidate)) {

                candidate = wolf.clone();
            }


            newGeneration.add(candidate);
        }


        /*
         * Replace the old generation.
         *
         * Fitness is deliberately NOT calculated here.
         *
         * generateSolution() will perform the single evaluation
         * pass after evolve().
         */

        population = newGeneration;


        /*
         * Move to the next generation.
         */
        currentIteration++;
    }


    /**
     * Handles populations smaller than three wolves.
     *
     * The best available individual is preserved and the remaining
     * individuals are generated through mutation.
     */
    private void evolveSmallPopulation() {

        List<TestSuiteChromosome> newGeneration =
                new ArrayList<>(population.size());


        /*
         * Preserve the best available solution.
         */
        newGeneration.add(
                population.get(0).clone()
        );


        /*
         * Generate the remaining individuals.
         */
        int parentIndex = 1;

        while (!isNextPopulationFull(newGeneration)) {

            TestSuiteChromosome parent =
                    population.get(
                            parentIndex % population.size()
                    );

            parentIndex++;


            TestSuiteChromosome candidate =
                    parent.clone();


            candidate.mutate();

            notifyMutation(candidate);


            if (isTooLong(candidate)) {

                candidate = parent.clone();
            }


            newGeneration.add(candidate);
        }


        population = newGeneration;
    }


    /**
     * Creates a discrete GWO-guided candidate.
     *
     * The original continuous GWO uses:
     *
     *     X1 = Xa - Aa * |Ca * Xa - X|
     *     X2 = Xb - Ab * |Cb * Xb - X|
     *     X3 = Xd - Ad * |Cd * Xd - X|
     *
     *     X(t+1) = (X1 + X2 + X3) / 3
     *
     * TestSuiteChromosome does not have a continuous vector space,
     * therefore literal vector arithmetic is not possible.
     *
     * This implementation provides a discrete approximation:
     *
     *   1. Generate independent A and C coefficients for Alpha,
     *      Beta and Delta.
     *
     *   2. Determine whether each leader is in exploitation mode
     *      using |A| < 1.
     *
     *   3. Starting from the current wolf, allow Delta, Beta and
     *      Alpha to influence the mutation base.
     *
     *   4. Alpha has the final priority.
     *
     *   5. Apply exactly one mutation.
     *
     * This should therefore be described as a
     * "discrete hierarchical approximation of GWO leader guidance",
     * rather than literal continuous GWO vector averaging.
     */
    private TestSuiteChromosome createCandidate(
            TestSuiteChromosome wolf,
            TestSuiteChromosome alpha,
            TestSuiteChromosome beta,
            TestSuiteChromosome delta,
            double a) {


        /*
         * ---------------------------------------------------------
         * INDEPENDENT GWO COEFFICIENTS
         * ---------------------------------------------------------
         *
         * Each leader receives independent A and C values.
         */

        double Aa =
                2.0 * a * nextDouble() - a;

        double Ab =
                2.0 * a * nextDouble() - a;

        double Ad =
                2.0 * a * nextDouble() - a;


        double Ca =
                2.0 * nextDouble();

        double Cb =
                2.0 * nextDouble();

        double Cd =
                2.0 * nextDouble();


        /*
         * ---------------------------------------------------------
         * EXPLOITATION TEST
         * ---------------------------------------------------------
         *
         * In classical GWO:
         *
         * |A| < 1 -> exploitation
         * |A| >= 1 -> exploration
         */

        boolean exploitAlpha =
                Math.abs(Aa) < 1.0;

        boolean exploitBeta =
                Math.abs(Ab) < 1.0;

        boolean exploitDelta =
                Math.abs(Ad) < 1.0;


        /*
         * ---------------------------------------------------------
         * FULL EXPLORATION
         * ---------------------------------------------------------
         *
         * If no leader is in exploitation mode, mutate the current
         * wolf directly.
         */

        if (!exploitAlpha
                && !exploitBeta
                && !exploitDelta) {

            TestSuiteChromosome candidate =
                    wolf.clone();

            candidate.mutate();

            notifyMutation(candidate);

            return candidate;
        }


        /*
         * ---------------------------------------------------------
         * HIERARCHICAL LEADER GUIDANCE
         * ---------------------------------------------------------
         *
         * Start from the current wolf.
         *
         * Apply leaders from weakest to strongest:
         *
         *     Delta -> Beta -> Alpha
         *
         * Therefore Alpha has the final decision if its influence
         * is activated.
         */

        TestSuiteChromosome base =
                wolf.clone();


        /*
         * Delta influence.
         */
        if (exploitDelta
                && nextDouble() < Cd / 2.0) {

            base = delta.clone();
        }


        /*
         * Beta influence.
         */
        if (exploitBeta
                && nextDouble() < Cb / 2.0) {

            base = beta.clone();
        }


        /*
         * Alpha influence.
         */
        if (exploitAlpha
                && nextDouble() < Ca / 2.0) {

            base = alpha.clone();
        }


        /*
         * ---------------------------------------------------------
         * SINGLE MUTATION
         * ---------------------------------------------------------
         *
         * Exactly one mutation is applied.
         *
         * This is important because TestSuiteChromosome mutation
         * can be computationally expensive.
         */

        base.mutate();

        notifyMutation(base);


        return base;
    }


    /**
     * Calculates the GWO parameter a.
     *
     * Classical GWO:
     *
     *     a : 2 -> 0
     *
     * This implementation uses the current generation progress.
     *
     * The value only controls GWO exploration/exploitation.
     * It does NOT control EvoSuite's stopping condition.
     */
    private double calculateA() {

        double horizon =
                DEFAULT_GENERATION_HORIZON;


        /*
         * If the search uses MAXGENERATIONS, use the configured
         * generation budget.
         */

        if (Properties.STOPPING_CONDITION
                == Properties.StoppingCondition.MAXGENERATIONS) {

            horizon =
                    Math.max(
                            1.0,
                            Properties.SEARCH_BUDGET
                    );
        }


        /*
         * Calculate search progress.
         */

        double progress =
                Math.min(
                        1.0,
                        (double) currentIteration
                                / horizon
                );


        /*
         * Linear decrease:
         *
         *     2 -> 0
         */

        return Math.max(
                0.0,
                2.0 * (1.0 - progress)
        );
    }


    /**
     * Initializes the GWO population.
     */
    @Override
    public void initializePopulation() {

        notifySearchStarted();

        currentIteration = 0;

        gwoDebugCount = 0;


        /*
         * Generate initial population.
         */

        generateInitialPopulation(
                Properties.POPULATION
        );


        /*
         * Initial fitness evaluation and sorting.
         *
         * After this point the population is sorted and therefore:
         *
         *     population[0] = Alpha
         *     population[1] = Beta
         *     population[2] = Delta
         */

        calculateFitnessAndSortPopulation();


        notifyIteration();
    }


    /**
     * Main EvoSuite search loop.
     */
    @Override
    public void generateSolution() {

        /*
         * ---------------------------------------------------------
         * SECONDARY OBJECTIVE
         * ---------------------------------------------------------
         *
         * Keep the same EvoSuite behaviour used by the other GA
         * implementations.
         */

        if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
                || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {

            disableFirstSecondaryCriterion();
        }


        /*
         * ---------------------------------------------------------
         * INITIALIZATION
         * ---------------------------------------------------------
         */

        if (population.isEmpty()) {

            initializePopulation();
        }


        /*
         * ---------------------------------------------------------
         * FITNESS TRACKING
         * ---------------------------------------------------------
         */

        int starvationCounter = 0;


        boolean maximizing =
                getFitnessFunction().isMaximizationFunction();


        double bestFitness =
                maximizing
                        ? Double.NEGATIVE_INFINITY
                        : Double.POSITIVE_INFINITY;


        double lastBestFitness =
                bestFitness;


        /*
         * ---------------------------------------------------------
         * MAIN GWO LOOP
         * ---------------------------------------------------------
         */

        while (!isFinished()) {

            /*
             * 1. Generate next generation.
             *
             * evolve() does NOT evaluate fitness.
             */
            evolve();


            /*
             * 2. Evaluate every new individual and sort.
             *
             * This is the main fitness evaluation for the generation.
             */
            calculateFitnessAndSortPopulation();


            /*
             * 3. Local search.
             *
             * Local search evaluates the modified individuals itself.
             */
            applyLocalSearch();


            /*
             * 4. Local search can change fitness ordering.
             *
             * We only sort here instead of calling
             * calculateFitnessAndSortPopulation() again.
             *
             * This avoids another complete fitness evaluation.
             */
            sortPopulation();


            /*
             * 5. Read current best solution.
             */

            double newFitness =
                    getBestIndividual().getFitness();


            /*
             * -----------------------------------------------------
             * BEST FITNESS REGRESSION CHECK
             * -----------------------------------------------------
             *
             * The current population is allowed to contain worse
             * individuals, but Alpha should remain protected through
             * elitism.
             *
             * Therefore a regression warning is useful for debugging
             * the GWO implementation.
             */

            boolean regressed =
                    maximizing
                            ? newFitness < bestFitness
                            : newFitness > bestFitness;


            if (regressed
                    && bestFitness !=
                    (maximizing
                            ? Double.NEGATIVE_INFINITY
                            : Double.POSITIVE_INFINITY)) {

                LoggingUtils.getEvoLogger().warn(
                        "GWO: best fitness regressed from "
                                + bestFitness
                                + " to "
                                + newFitness
                                + " at iteration "
                                + currentIteration
                );
            }


            /*
             * Update global best fitness.
             */

            bestFitness =
                    newFitness;


            /*
             * -----------------------------------------------------
             * STARVATION DETECTION
             * -----------------------------------------------------
             */

            if (Double.compare(
                    bestFitness,
                    lastBestFitness
            ) == 0) {

                starvationCounter++;

            } else {

                starvationCounter = 0;

                lastBestFitness =
                        bestFitness;
            }


            /*
             * -----------------------------------------------------
             * SECONDARY OBJECTIVE
             * -----------------------------------------------------
             */

            updateSecondaryCriterion(
                    starvationCounter
            );


            /*
             * -----------------------------------------------------
             * ITERATION NOTIFICATION
             * -----------------------------------------------------
             */

            notifyIteration();


            /*
             * -----------------------------------------------------
             * DEBUG
             * -----------------------------------------------------
             */

            if (currentIteration <= 5
                    || currentIteration % 20 == 0) {

                LoggingUtils.getEvoLogger().info(
                        "GWO iteration="
                                + currentIteration
                                + " bestFitness="
                                + newFitness
                                + " population="
                                + population.size()
                                + " a="
                                + calculateA()
                );
            }
        }


        /*
         * ---------------------------------------------------------
         * ARCHIVE
         * ---------------------------------------------------------
         *
         * Restore the best solution recorded by EvoSuite's archive.
         */

        updateBestIndividualFromArchive();


        /*
         * Search finished.
         */

        notifySearchFinished();
    }
}