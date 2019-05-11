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
	
	Chromosome c = new Chromosome(rnd, sl, 12, 2);
	c.stringInitialize("0," + "942,2702,1043,1379,2041,1733,2872,1091,2828,2729,2053,136");
	MarioGame game = new MarioGame();
	game.runGame(new agents.robinBaumgarten.Agent(), c.toString(), 10, 0, true);
    }
}
