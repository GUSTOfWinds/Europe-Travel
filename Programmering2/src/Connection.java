public class Connection {
    private String from;
    private String to;
    private String name;
    private int weight;

    public Connection(String from, String to, String name, int weight){
        this.from = from;
        this.to = to;
        this.name = name;
        this.weight = weight;
    }
    public String getFrom(){
        return from;
    }
    public String getTo(){
        return to;
    }
    public String getName(){
        return name;
    }
    public int getWeight(){
        return weight;
    }
}