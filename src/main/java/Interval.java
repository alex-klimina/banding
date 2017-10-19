public class Interval {
    private String name;
    private int startIndex;
    private int endIndex;

    public Interval(String name, int startIndex, int endIndex) {
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Interval(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Interval{" +
                "name='" + name + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getLength() {
        return endIndex - startIndex;
    }
}
