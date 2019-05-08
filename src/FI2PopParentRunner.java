import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import fi2pop.FI2PopGeneticAlgorithm;
import shared.Chromosome;
import shared.RepeatedLevelSlicesLibrary;
import shared.SlicesLibrary;
import shared.UniqueLevelSlicesLibrary;
import shared.evaluator.ParentEvaluator;

public class FI2PopParentRunner {
    private static void deleteDirectory(File directoryToBeDeleted) {
	File[] allContents = directoryToBeDeleted.listFiles();
	if (allContents != null) {
	    for (File file : allContents) {
		deleteDirectory(file);
	    }
	}
	directoryToBeDeleted.delete();
    }
    
    private static void appendInfo(String path, int iteration, FI2PopGeneticAlgorithm gen) throws FileNotFoundException {
	double[] stats = gen.getStatistics();
	PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path + "result.txt"), true));
	String result = "";
	for(double v:stats) {
	    result += v + ", ";
	}
	result = result.substring(0, result.length() - 1);
	pw.println("Batch number " + iteration + ": " + result);
	pw.close();
    }
    
    private static HashMap<String, String> readParameters(String filename) throws IOException {
	List<String> lines = Files.readAllLines(Paths.get("", filename));
	HashMap<String, String> parameters = new HashMap<String, String>();
	for(int i=0; i<lines.size(); i++) {
	    if(lines.get(i).trim().length() == 0) {
		continue;
	    }
	    String[] parts = lines.get(i).split("=");
	    parameters.put(parts[0].trim(), parts[1].trim());
	}
	return parameters;
    }
    
    public static void main(String[] args) {
	HashMap<String, String> parameters = null;
	try {
	    parameters = readParameters("FI2PopParameters.txt");
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	SlicesLibrary lib = new RepeatedLevelSlicesLibrary();
	if(parameters.get("slicesType").toLowerCase().contains("unique")) {
	    lib = new UniqueLevelSlicesLibrary();
	}
	File directory = new File(parameters.get("levelFolder"));
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith("txt");
            }
        });
        try{
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }});
            for (File f : files) {
                String[] lines = Files.readAllLines(f.toPath()).toArray(new String[0]);
                lib.addLevel(lines);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Random rnd = new Random(Integer.parseInt(parameters.get("seed")));
        int appendingSize = Integer.parseInt(parameters.get("appendingSize"));
        int chromosomeLength = Integer.parseInt(parameters.get("chromosomeLength"));
        int popSize = Integer.parseInt(parameters.get("populationSize"));
        double crossover = Double.parseDouble(parameters.get("crossover"));
        double mutation = Double.parseDouble(parameters.get("mutation"));
        int elitism = Integer.parseInt(parameters.get("elitism"));
        System.out.println("Initialize FI2Pop");
        FI2PopGeneticAlgorithm gen = new FI2PopGeneticAlgorithm(lib, rnd, popSize, chromosomeLength, appendingSize, crossover, mutation, elitism);
        ParentEvaluator parent = new ParentEvaluator(parameters.get("inputFolder"), parameters.get("outputFolder"));
        System.out.println("First Batch of Chromosomes");
        gen.randomChromosomesInitialize();
        Chromosome[] chromosomes = gen.getPopulation();
        int iteration = 0;
        int maxIterations = -1;
        if(args.length > 0) {
            maxIterations = Integer.parseInt(args[0]);
        }
        while(true) {
            try {
        	System.out.println("Writing in Files for Children");
        	String[] levels = new String[chromosomes.length];
        	for(int i=0; i<chromosomes.length; i++) {
        	    levels[i] = chromosomes[i].getAge() + ",";
        	    levels[i] += chromosomes[i].getGenes() + "\n" + chromosomes[i].toString() + "\n";
        	}
		parent.writeChromosomes(levels);
		System.out.println("Waiting for children to finish");
		while(!parent.checkChromosomes(chromosomes.length)) {
		    Thread.sleep(500);
		}
		Thread.sleep(1000);
		System.out.println("Reading and assigning children results");
		String[] values = parent.assignChromosomes(chromosomes.length);
		for(int i=0; i<chromosomes.length; i++) {
		    chromosomes[i].childEvaluationInitialization(values[i]);
		}
		parent.clearOutputFiles(chromosomes.length);
		System.out.println("Writing results");
		File f = new File(parameters.get("resultFolder") + iteration + "/");
		f.mkdir();
		gen.writePopulation(parameters.get("resultFolder") + iteration + "/");
		appendInfo(parameters.get("resultFolder"), iteration, gen);
		deleteDirectory(new File(parameters.get("resultFolder") + (iteration - 1) + "/"));
		if(maxIterations > 0 && iteration >= maxIterations) {
		    break;
		}
		System.out.println("Generate Next Population");
		gen.getNextGeneration();
		chromosomes = gen.getPopulation();
		iteration += 1;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
        }
    }
}
