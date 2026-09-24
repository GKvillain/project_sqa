package org.apache.commons.math3.genetics;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

public class ElitisticListPopulationTest {

    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        public DummyChromosome(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double fitness() {
            return this.fitness;
        }
    }

    // Tests constructor with negative elitism rate (Defects4J Math-35 defect)
    @Test(expected = OutOfRangeException.class)
    public void testConstructor_negativeElitismRate_throwsOutOfRangeException() {
        new ElitisticListPopulation(100, -0.25);
    }

    // Tests constructor with elitism rate greater than 1 (Defects4J Math-35 defect)
    @Test(expected = OutOfRangeException.class)
    public void testConstructor_elitismRateGreaterThanOne_throwsOutOfRangeException() {
        new ElitisticListPopulation(100, 1.25);
    }

    // Tests constructor with list and negative elitism rate (Defects4J Math-35 defect)
    @Test(expected = OutOfRangeException.class)
    public void testConstructorWithList_negativeElitismRate_throwsOutOfRangeException() {
        List<Chromosome> chromosomes = new ArrayList<Chromosome>();
        chromosomes.add(new DummyChromosome(1.0));
        new ElitisticListPopulation(chromosomes, 100, -0.1);
    }

    // Tests constructor with list and elitism rate greater than 1 (Defects4J Math-35 defect)
    @Test(expected = OutOfRangeException.class)
    public void testConstructorWithList_elitismRateGreaterThanOne_throwsOutOfRangeException() {
        List<Chromosome> chromosomes = new ArrayList<Chromosome>();
        chromosomes.add(new DummyChromosome(1.0));
        new ElitisticListPopulation(chromosomes, 100, 1.5);
    }

    // Tests constructor with lower boundary elitism rate 0.0
    @Test
    public void testConstructor_lowerBoundaryElitismRate_success() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.0);
        Assert.assertEquals(0.0, pop.getElitismRate(), 1e-6);
    }

    // Tests constructor with upper boundary elitism rate 1.0
    @Test
    public void testConstructor_upperBoundaryElitismRate_success() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 1.0);
        Assert.assertEquals(1.0, pop.getElitismRate(), 1e-6);
    }

    // Tests constructor with list and valid elitism rate
    @Test
    public void testConstructorWithList_validElitismRate_success() {
        List<Chromosome> chromosomes = new ArrayList<Chromosome>();
        chromosomes.add(new DummyChromosome(1.0));
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomes, 10, 0.5);
        Assert.assertEquals(0.5, pop.getElitismRate(), 1e-6);
        Assert.assertEquals(1, pop.getPopulationSize());
    }

    // Tests setElitismRate with negative rate
    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRate_negativeRate_throwsOutOfRangeException() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.5);
        pop.setElitismRate(-0.01);
    }

    // Tests setElitismRate with rate greater than 1
    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRate_rateGreaterThanOne_throwsOutOfRangeException() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.5);
        pop.setElitismRate(1.01);
    }

    // Tests setElitismRate with valid boundary and middle values
    @Test
    public void testSetElitismRate_validValues_updatesElitismRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.5);
        pop.setElitismRate(0.0);
        Assert.assertEquals(0.0, pop.getElitismRate(), 1e-6);

        pop.setElitismRate(1.0);
        Assert.assertEquals(1.0, pop.getElitismRate(), 1e-6);

        pop.setElitismRate(0.25);
        Assert.assertEquals(0.25, pop.getElitismRate(), 1e-6);
    }

    // Tests nextGeneration behavior copying expected top chromosomes
    @Test
    public void testNextGeneration_standardRate_copiesBestChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        for (int i = 0; i < 10; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population nextGen = pop.nextGeneration();
        Assert.assertEquals(5, nextGen.getPopulationSize());

        List<Chromosome> nextChromosomes = ((ElitisticListPopulation) nextGen).getChromosomes();
        for (int i = 0; i < nextChromosomes.size(); i++) {
            Assert.assertEquals(5.0 + i, nextChromosomes.get(i).getFitness(), 1e-6);
        }
    }

    // Tests nextGeneration with elitism rate 0.0 (no chromosomes copied)
    @Test
    public void testNextGeneration_zeroElitismRate_copiesNoChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.0);
        for (int i = 0; i < 5; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population nextGen = pop.nextGeneration();
        Assert.assertEquals(0, nextGen.getPopulationSize());
    }

    // Tests nextGeneration with elitism rate 1.0 (all chromosomes copied)
    @Test
    public void testNextGeneration_fullElitismRate_copiesAllChromosomes() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 1.0);
        for (int i = 0; i < 5; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population nextGen = pop.nextGeneration();
        Assert.assertEquals(5, nextGen.getPopulationSize());
    }
}