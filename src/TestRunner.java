import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import engine.core.MarioGame;
import shared.*;

public class TestRunner {
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
	
	Chromosome c = new Chromosome(rnd, sl, 14, 3);
	c.stringInitialize("3468,3183,727,3134,2208,467,1407,1005,3504,334,603,667,2483,457");
	MarioGame game = new MarioGame();
	game.runGame(new agents.robinBaumgarten.Agent(), c.toString(), 20, 0, true);
    }
}
