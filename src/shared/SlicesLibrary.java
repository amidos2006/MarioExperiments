package shared;

import java.util.ArrayList;

public abstract class SlicesLibrary {
    private final static char[] enemies = new char[] { 'G', 'R', 'K', 'Y' };
    private final static char[] question = new char[] { '@', '!' };
    private final static char[] blocks = new char[] { 'B', 'C' };

    protected String[] arrayedSlices;

    public SlicesLibrary() {
	this.arrayedSlices = new String[0];
    }

    protected String[] decodeLine(String line) {
	ArrayList<String> result = new ArrayList<String>();
	ArrayList<String> queue = new ArrayList<String>();
	queue.add(line);

	while (queue.size() > 0) {
	    String l = queue.remove(0);
	    int index = l.indexOf('E');
	    if (index >= 0) {
		for (int i = 0; i < enemies.length; i++) {
		    String temp = l.substring(0, index) + enemies[i] + l.substring(index + 1);
		    queue.add(temp);
		}
	    } else {
		index = l.indexOf('?');
		if (index > 0) {
		    for (int i = 0; i < question.length; i++) {
			String temp = l.substring(0, index) + question[i] + l.substring(index + 1);
			queue.add(temp);
		    }
		} else {
		    index = l.indexOf('S');
		    if (index > 0) {
			for (int i = 0; i < blocks.length; i++) {
			    String temp = l.substring(0, index) + blocks[i] + l.substring(index + 1);
			    queue.add(temp);
			}
		    } else {
			result.add(l);
		    }
		}
	    }
	}

	return result.toArray(new String[0]);
    }

    public void addLevel(String level) {
	String[] lines = level.split("\n");
	this.addLevel(lines);
    }

    public int getNumberOfSlices() {
	return this.arrayedSlices.length;
    }

    public String getSlice(int index) {
	return this.arrayedSlices[index];
    }
    
    public abstract void addLevel(String[] lines);
}
