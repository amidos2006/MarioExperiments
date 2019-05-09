import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import engine.core.MarioGame;
import shared.*;

public class TestHuman {
    public static void main(String[] args) {
	Random rnd = new Random();
	SlicesLibrary sl = new RepeatedLevelSlicesLibrary();
        File directory = new File("levels/");
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
                sl.addLevel(lines);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
	Chromosome c = new Chromosome(rnd, sl, 12, 2);
	c.stringInitialize("2517,440,161,989,974,1002,1012,2860,548,1951,88,2250");
	MarioGame game = new MarioGame();
	game.playGame(c.toString(), 20);
    }
}
