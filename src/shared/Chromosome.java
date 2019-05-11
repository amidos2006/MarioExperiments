package shared;

import java.util.HashMap;
import java.util.Random;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

public class Chromosome implements Comparable<Chromosome>{
    protected Random _rnd;
    protected int[] _genes;
    protected int _appendingSize;
    protected SlicesLibrary _library;
    protected double _constraints;
    protected double _fitness;
    protected int[] _dimensions;
    private int _age;
    
    public Chromosome(Random rnd, SlicesLibrary lib, int levelWidth, int appendingSize) {
	this._rnd = rnd;
	this._library = lib;
	this._genes = new int[levelWidth];
	this._appendingSize = appendingSize;
	this._constraints = 0;
	this._fitness = 0;
	this._dimensions = null;
	this._age = 0;
    }
    
    public void randomInitialization() {
	for (int i = 0; i < this._genes.length; i++) {
	    this._genes[i] = this._rnd.nextInt(this._library.getNumberOfSlices());
	}
    }
    
    public void stringInitialize(String level) {
	String[] parts = level.split(",");
	this._age = Integer.parseInt(parts[0]);
	for (int i = 0; i < this._genes.length; i++) {
	    this._genes[i] = Integer.parseInt(parts[i + 1]);
	}
    }
    
    public void childEvaluationInitialization(String values) {
	String[] parts = values.split(",");
	this._age = Integer.parseInt(parts[0]);
	this._constraints = Double.parseDouble(parts[1]);
	this._fitness = Double.parseDouble(parts[2]);
	this._dimensions = new int[parts.length - 3];
	for(int i=3; i<parts.length; i++) {
	    this._dimensions[i-3] = Integer.parseInt(parts[i]);
	}
    }
    
    public Chromosome clone() {
	Chromosome chromosome = new Chromosome(this._rnd, this._library, this._genes.length, this._appendingSize);
	for(int i=0; i<this._genes.length; i++) {
	    chromosome._genes[i] = this._genes[i];
	}
	return chromosome;
    }
    
    protected MarioResult[] runAlgorithms(MarioGame[] games, MarioAgent[] agents, int maxTime) {
	MarioResult[] results = new MarioResult[agents.length];
	for(int i=0; i<agents.length; i++) {
	    results[i] = games[i].runGame(agents[i], this.toString(), maxTime);
	}
	return results;
    }
    
    private void calculateConstraints(MarioResult[] runs) {
	double tempConst = runs[0].getCompletionPercentage();
	if(runs.length > 1) {
	    tempConst = runs[1].getCompletionPercentage() - tempConst;
	    if(runs[1].getGameStatus() == GameStatus.WIN && runs[2].getGameStatus() == GameStatus.LOSE) {
		tempConst = 1;
	    }
	}
	if(this._age > 0) {
	    this._constraints = Math.min(this._constraints, tempConst);
	}
	else {
	    this._constraints = tempConst;
	}
    }
    
    private void calculateFitnessEntropy() {
	HashMap<Character, Integer> stats = new HashMap<>();
	int[] horDerivative = new int[2];
	for (int i = 0; i < this._genes.length; i++) {
	    String slice = this._library.getSlice(this._genes[i]);
	    for (int j = 0; j < slice.length(); j++) {
		if(i > 0) {
		    String prevSlice = this._library.getSlice(this._genes[i - 1]);
		    if(slice.charAt(j) != prevSlice.charAt(j)) {
			horDerivative[1] += 1;
		    }
		    else {
			horDerivative[0] += 1;
		    }
		}
		Character c = slice.charAt(j);
		if(!stats.containsKey(c)) {
		    stats.put(c, 0);
		}
		stats.put(c, stats.get(c) + 1);
	    }
	}
	
	double entropy = 0;
	for(Character key:stats.keySet()) {
	    double prob = (1.0 * stats.get(key)) / (1.0*this._genes.length * this._library.getSlice(0).length());
	    if(prob > 0) {
		entropy += -prob * Math.log10(prob);
	    }
	}
	double derEntropy = 0;
	for(int i=0; i<horDerivative.length; i++) {
	    double prob = (1.0 * horDerivative[i]) / (1.0 * (this._genes.length - 1) * this._library.getSlice(0).length());
	    if(prob > 0) {
		derEntropy += - prob * Math.log10(prob);
	    }
	}
	
	this._fitness = ((1 - entropy) + 3 * (1 - derEntropy)) / 4.0;
    }
    
    private void calculateDimensions(MarioResult run, int doNothingFallKills) {
	this._dimensions = new int[12];
	this._dimensions[0] = run.getNumJumps() >= 1? 1:0;
	this._dimensions[1] = (run.getMaxJumpAirTime() > 0 && run.getMaxJumpAirTime() <= 10.0)? 1:0;
	this._dimensions[2] = run.getMaxJumpAirTime() >= 12.0? 1:0;
	this._dimensions[3] = (run.getMaxXJump() > 0 && run.getMaxXJump() <= 40.0)? 1:0;
	this._dimensions[4] = run.getMaxXJump() >= 120.0? 1:0;
	this._dimensions[5] = run.getKillsByStomp() >= 1? 1:0;
	this._dimensions[6] = run.getKillsByShell() >= 1? 1:0;
	this._dimensions[7] = doNothingFallKills >= 1? 1:0;
	this._dimensions[8] = run.getMarioMode() >= 1? 1:0;
	this._dimensions[9] = run.getCurrentCoins() >= 1? 1:0;
	this._dimensions[10] = run.getNumBumpBrick() >= 1? 1:0;
	this._dimensions[11] = run.getNumBumpQuestionBlock() >= 1? 1:0;
    }
    
    public void calculateResults(MarioGame game, MarioAgent agent, int maxTime) {
	this.calculateResults(new MarioGame[] {game}, new MarioAgent[] {agent}, maxTime);
    }
    
    public void calculateResults(MarioGame[] games, MarioAgent agent, int maxTime) {
	MarioAgent[] agents = new MarioAgent[games.length];
	for(int i=0; i<agents.length; i++) {
	    agents[i] = agent;
	}
	this.calculateResults(games, agents, maxTime);
    }
    
    public void calculateResults(MarioGame game, MarioAgent[] agents, int maxTime) {
	MarioGame[] games = new MarioGame[agents.length];
	for(int i=0; i<games.length; i++) {
	    games[i] = game;
	}
	this.calculateResults(games, agents, maxTime);
    }
    
    public void calculateResults(MarioGame[] games, MarioAgent[] agents, int maxTime) {
	MarioResult[] runs = this.runAlgorithms(games, agents, maxTime);
	this.calculateConstraints(runs);
	this._age += 1;
	MarioGame game = new MarioGame();
	int fallKills = game.runGame(new agents.doNothing.Agent(), this.toString(), maxTime).getKillsByFall();
	this.calculateDimensions(runs[0], fallKills);
	if(this._constraints >= 1) {
	    this.calculateFitnessEntropy();
	}
	else {
	    this._fitness = 0;
	}
    }
    
    public int getAge() {
	return this._age;
    }
    
    public double getConstraints() {
	return this._constraints;
    }
    
    public double getFitness() {
	return this._fitness;
    }
    
    public int[] getDimensions() {
	return this._dimensions;
    }
    
    public Chromosome mutate() {
	Chromosome mutated = this.clone();
	mutated._genes[mutated._rnd.nextInt(mutated._genes.length)] = mutated._rnd
		.nextInt(mutated._library.getNumberOfSlices());
	return mutated;
    }
    
    public Chromosome crossover(Chromosome c) {
	Chromosome child = this.clone();
	int index1 = child._rnd.nextInt(child._genes.length);
	int index2 = child._rnd.nextInt(child._genes.length);
	if (index1 > index2) {
	    int temp = index2;
	    index2 = index1;
	    index1 = temp;
	}
	for (int i = index1; i < index2 + 1; i++) {
	    child._genes[i] = c._genes[i];
	}
	return child;
    }
    
    public String getGenes() {
	String result = "" + this._genes[0];
	for (int i = 1; i < this._genes.length; i++) {
	    result += "," + this._genes[i];
	}
	return result;
    }

    public String toString() {
	String level = "";
	int height = this._library.getSlice(this._genes[0]).length();
	for (int i = 0; i < height; i++) {
	    String appendingChar = "-";
	    if (i == height - 1 || i == height - 2) {
		appendingChar = "X";
	    }
	    for (int k = 0; k < this._appendingSize; k++) {
		level += appendingChar;
	    }
	    for (int j = 0; j < this._genes.length; j++) {
		level += this._library.getSlice(this._genes[j]).charAt(i);
	    }
	    for (int k = 0; k < this._appendingSize; k++) {
		level += appendingChar;
	    }
	    level += "\n";
	}

	return level;
    }

    @Override
    public int compareTo(Chromosome o) {
	if (this._constraints == 1) {
	    return (int) Math.signum(this._fitness - o._fitness);
	}
	return (int) Math.signum(this._constraints - o._constraints);
    }
}
