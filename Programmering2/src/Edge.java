public class Edge<T> {

    private T destination;
    private String name;
    private int weight;

    public Edge(T destination, String name, int weight){
        this.destination = destination;
        this.name = name;
        this.weight = weight;
    }

    public T getDestination(){
        return destination;
    }
    public int getWeight(){
        return weight;
    }
    public void setWeight(int weight){
        if(weight < 0){
            throw new IllegalArgumentException("Edge, setWeight: Negative weight value");
        }
        else{
            this.weight = weight;
        }
    }
    public String getName(){
        return name;
    }
    public String toString(){
        return "till " + destination + " med " + name + " tar " + weight;
    }
}