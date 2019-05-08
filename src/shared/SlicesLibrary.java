package shared;

public abstract class SlicesLibrary {
    private char[] forbiddenSymbols = new char[] {'M', 'F'};
    protected String[] arrayedSlices;
    
    public SlicesLibrary() {
	this.arrayedSlices = new String[0];
    }
    
    protected boolean canBeAdded(String line) {
	for(char s:forbiddenSymbols) {
	    if(line.contains("" + s)) {
		return false;
	    }
	}
	return true;
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
