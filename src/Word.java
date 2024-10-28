import java.io.Serializable;

public class Word implements Serializable, Comparable<Word> {

    private static final long serialVersionUID = 1L;
    
    public String surface;
    public short id;
    public short cost;

    public Word(String surface, int id, int cost) {
        this.surface = surface;
        this.id = (short) id;
        this.cost = (short) cost;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public int compareTo(Word word) {
        if (cost != word.cost) {
            return cost - word.cost;
        }
        return surface.compareTo(word.surface);
    }

    @Override
    public String toString() {
        return id + "," + cost + "," + surface;
    }

}
