package shared;

import java.util.ArrayList;

public class RepeatedLevelSlicesLibrary extends SlicesLibrary {
    private ArrayList<String> slices;

    public RepeatedLevelSlicesLibrary() {
	super();
	this.slices = new ArrayList<String>();
    }

    @Override
    public void addLevel(String[] lines) {
	for (int i = 0; i < lines[0].length(); i++) {
	    String slice = "";
	    for (int j = 0; j < lines.length; j++) {
		slice += lines[j].charAt(i);
	    }
	    String[] expand = this.decodeLine(slice);
	    for(String l:expand) {
		this.slices.add(l);
	    }
	}
	this.arrayedSlices = this.slices.toArray(new String[0]);
    }
}
