package fi2pop;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import shared.Chromosome;
import shared.SlicesLibrary;

public class FI2PopGeneticAlgorithm {
    private Chromosome[] _population;
    private int _populationSize;
    private int _chromosomeLength;
    private int _appendingSize;
    private double _crossover;
    private double _mutation;
    private int _elitism;
    private Random _rnd;
    private SlicesLibrary _lib;


    public FI2PopGeneticAlgorithm(SlicesLibrary lib, Random rnd, int populationSize, int chromosomeLength, int appendingSize, double crossover,
							double mutation, int elitism) {
	this._lib = lib;
	this._populationSize = populationSize;
	this._chromosomeLength = chromosomeLength;
	this._appendingSize = appendingSize;
	this._crossover = crossover;
	this._mutation = mutation;
	this._elitism = elitism;
	this._rnd = rnd;
	this._population = new Chromosome[0];
    }
    
    public Chromosome[] getPopulation() {
	return this._population;
    }
    
    public void randomChromosomesInitialize() {
	this._population = new Chromosome[this._populationSize];
	for(int i=0; i<this._population.length; i++) {
	    this._population[i] = new Chromosome(this._rnd, this._lib, this._chromosomeLength, this._appendingSize);
	    this._population[i].randomInitialization();
	}
    }
    
    private Chromosome rankSelection(Chromosome[] pop) {
	double[] ranks = new double[pop.length];
	ranks[0] = 1;
	for(int i=1; i<pop.length; i++) {
	    ranks[i] = ranks[i-1] + i + 1;
	}
	for(int i=0; i<pop.length; i++) {
	    ranks[i] /= ranks[ranks.length - 1];
	}
	double randValue = this._rnd.nextDouble();
	for(int i=0; i<ranks.length; i++){
	    if(randValue <= ranks[i]) {
		return pop[i];
	    }
	}
	return pop[pop.length - 1];
    }
    
    private Chromosome[][] getFeasibleInfeasible(boolean descending){
	ArrayList<Chromosome> feasible = new ArrayList<Chromosome>();
	ArrayList<Chromosome> infeasible = new ArrayList<Chromosome>();
	for(int i=0; i<this._population.length; i++) {
	    if(this._population[i].getConstraints() < 1) {
		infeasible.add(this._population[i]);
	    }
	    else {
		feasible.add(this._population[i]);
	    }
	}
	if(feasible.size() > 0) {
	    Collections.sort(feasible);
	    if (descending) {
		Collections.reverse(feasible);
	    }
	}
	if(infeasible.size() > 0) {
	    Collections.sort(infeasible);
	    if (descending) {
		Collections.reverse(infeasible);
	    }
	}
	return new Chromosome[][] {feasible.toArray(new Chromosome[0]), infeasible.toArray(new Chromosome[0]) };
    }
    
    public void getNextGeneration() {
	Chromosome[][] feasibleInfeasible = this.getFeasibleInfeasible(false);
	Chromosome[] newPopulation = new Chromosome[this._populationSize];
	for (int i = 0; i < this._populationSize - this._elitism; i++) {
	    Chromosome[] usedPopulation = feasibleInfeasible[1];
	    if (this._rnd.nextDouble() < (double) (feasibleInfeasible[0].length) / this._populationSize) {
		usedPopulation = feasibleInfeasible[0];
	    }
	    Chromosome parent1 = this.rankSelection(usedPopulation);
	    Chromosome child = (Chromosome)parent1.clone();
	    if (this._rnd.nextDouble() < this._crossover) {
		Chromosome parent2 = this.rankSelection(usedPopulation);
		child = (Chromosome)parent1.crossover(parent2);
		if (this._rnd.nextDouble() < this._mutation) {
		    child = (Chromosome)child.mutate();
		}
	    } else {
		child = (Chromosome)child.mutate();
	    }
	    newPopulation[i] = child;
	}
	for (int i = 0; i < this._elitism; i++) {
	    if (i < feasibleInfeasible[0].length) {
		newPopulation[newPopulation.length - 1 - i] = feasibleInfeasible[0][feasibleInfeasible[0].length - 1 - i];
	    } else {
		newPopulation[newPopulation.length - 1 - i] = feasibleInfeasible[1][feasibleInfeasible[1].length - 1 - (i - feasibleInfeasible[0].length)];
	    }
	}
	this._population = newPopulation;
    }
    
    public void writePopulation(String path) throws FileNotFoundException, UnsupportedEncodingException {
	Chromosome[][] pop = this.getFeasibleInfeasible(true);
	Chromosome[] feasible = pop[0];
	Chromosome[] infeasible = pop[1];
	int index = 0;
	PrintWriter resultWriter = new PrintWriter(path + "result.txt", "UTF-8");
	for (Chromosome ch : feasible) {
	    PrintWriter writer = new PrintWriter(path + index + ".txt", "UTF-8");
	    writer.println("Genes: " + ch.getGenes());
	    writer.println("Fitness: " + ch.getFitness());
	    writer.println("Constraints: " + ch.getConstraints());
	    writer.println("Age: " + ch.getAge());
	    writer.println("Level:\n" + ch.toString());
	    writer.close();
	    resultWriter.println("Chromosome " + index + ": " + ch.getConstraints() + ", " + ch.getFitness());
	    index += 1;
	}
	for (Chromosome ch : infeasible) {
	    PrintWriter writer = new PrintWriter(path + index + ".txt", "UTF-8");
	    writer.println("Genes: " + ch.getGenes());
	    writer.println("Fitness: " + ch.getFitness());
	    writer.println("Constraints: " + ch.getConstraints());
	    writer.println("Age: " + ch.getAge());
	    writer.println("Level:\n" + ch.toString());
	    writer.close();
	    resultWriter.println("Chromosome " + index + ": " + ch.getConstraints() + ", " + ch.getFitness());
	    index += 1;
	}
	resultWriter.close();
    }
    
    public double[] getStatistics() {
	int numFeasible = 0;
	int numInfeasible = 0;
	double maxFitness = 0;
	double avgFitness = 0;
	double minFitness = 0;
	double maxConstraints = 0;
	double avgConstraints = 0;
	double minConstraints = 0;
	
	Chromosome[][] pop = this.getFeasibleInfeasible(true);
	Chromosome[] feasible = pop[0];
	Chromosome[] infeasible = pop[1];
	
	numFeasible = feasible.length;
	for(Chromosome c:feasible) {
	    avgFitness += c.getFitness();
	}
	if(numFeasible > 0) {
	    maxFitness = feasible[0].getFitness();
	    minFitness = feasible[numFeasible - 1].getFitness();
	    avgFitness /= numFeasible;
	}
	
	numInfeasible = infeasible.length;
	for(Chromosome c:feasible) {
	    avgConstraints += c.getConstraints();
	}
	if(numInfeasible > 0) {
	    maxConstraints = infeasible[0].getConstraints();
	    minConstraints = infeasible[numInfeasible - 1].getConstraints();
	    avgConstraints /= numInfeasible;
	}
	
	return new double[] {numFeasible, maxFitness, avgFitness, minFitness, numInfeasible, maxConstraints, avgConstraints, minConstraints};
    }
}
