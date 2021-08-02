import java.util.*;

public class ListGraph<T> implements Graph<T> {

    private Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    @Override
    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }
    @Override
    public void remove(T node) {
        if(!nodes.containsKey(node)){
            throw new NoSuchElementException();
        }

        Collection<Edge<T>> edgesGotten = new ArrayList<>(getEdgesFrom(node));

        for(Edge<T> edge : edgesGotten){
            disconnect(edge.getDestination(), node);
        }
        nodes.remove(node);
    }
    @Override
    public void connect(T node1, T node2, String name, int weight) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2)){
            throw new NoSuchElementException("ListGraph, connect: node1 or node2 do not exist.");
        }
        if(weight < 0){
            throw new IllegalArgumentException("ListGraph, connect: Negative weight value");
        }
        if(!(getEdgeBetween(node1, node2) == null) || !(getEdgeBetween(node2, node1) == null)){
            throw new IllegalStateException("ListGraph, connect: getEdgeBetween returned NOT[null], meaning a connection already exists");
        }

        Set<Edge<T>> se1 = nodes.get(node1);
        Edge<T> e1 = new Edge<>(node2, name, weight);
        se1.add(e1);
        Set<Edge<T>> se2 = nodes.get(node2);
        Edge<T> e2 = new Edge(node1, name, weight);
        se2.add(e2);
    }
    @Override
    public void disconnect(T node1, T node2) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2)){
            throw new NoSuchElementException();
        }
        if(getEdgeBetween(node1, node2) == null){
            throw new IllegalStateException();
        }

        Edge<T> edge1 = getEdgeBetween(node1, node2);
        Edge<T> edge2 = getEdgeBetween(node2, node1);

        nodes.get(node1).remove(edge1);
        nodes.get(node2).remove(edge2);
    }
    @Override
    public void setConnectionWeight(T node1, T node2, int weight) {
        Edge<T> edge1 = getEdgeBetween(node1, node2);
        Edge<T> edge2 = getEdgeBetween(node2, node1);
        if(edge1 == null || edge2 == null){
            throw new NoSuchElementException("ListGraph, setConnectionWeight: getEdgeBetween returned null, meaning there exists no edge to set a weight to");
        }
        if(weight < 0){
            throw new IllegalArgumentException("ListGraph, setConnectionWeight: Weight requested to be set is less than 0.");
        }

        edge1.setWeight(weight);
        edge2.setWeight(weight);
    }
    @Override
    public Set<T> getNodes() {
        Set<T> nodesGotten = new HashSet<>();
        nodesGotten = nodes.keySet();
        return nodesGotten;
    }
    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if(!nodes.containsKey(node)){
            throw new NoSuchElementException("ListGraph, getEdgesFrom: node does not exist.");
        }else{
            Collection<Edge<T>> edgesGotten = new ArrayList<>();
            edgesGotten = nodes.get(node);
            return edgesGotten;
        }
    }
    @Override
    public Edge<T> getEdgeBetween(T node1, T node2) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2)){
            throw new NoSuchElementException("ListGraph, getEdgeBetween: node1 or node2 does not exist.");
        }

        for(Edge<T> edge : getEdgesFrom(node1)){
            if(edge.getDestination() == node2){
                return edge;
            }
        }
        return null;
    }
    public String toString(){
        String string = "";
        for(T t : nodes.keySet()) {
            string += t + ": ";
            for (Edge e : nodes.get(t))
                string += e;
            string += "\n";
        }
        return string;
    }
    @Override
    public boolean pathExists(T from, T to) {
        try{
            if(getPath(from, to) == null){
                return false;
            }
        }catch (NoSuchElementException | NullPointerException e){
            return false;
        }
        return true;
    }
    @Override
    public List<Edge<T>> getPath(T from, T to) {
        Set<T> visited = new HashSet<>();
        Map<T, T> via = new HashMap<>();
        depthFirstSearch(from, null, visited, via);
        if(!visited.contains(to) || !via.containsKey(to)){
            return null;
        }else{
            return gatherPath(from, to, via);
        }
    }





    private void depthFirstSearch(T where, T fromWhere, Set<T> visited, Map<T, T> via){
        visited.add(where);
        via.put(where, fromWhere);
        for(Edge<T> e : nodes.get(where)) {
            if (!visited.contains(e.getDestination())) {
                depthFirstSearch(e.getDestination(), where, visited, via);
            }
        }
    }
    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> via){
        List<Edge<T>> path = new ArrayList<>();
        T where = to;
        while (!where.equals(from)) {
            T node = via.get(where);
            Edge<T> e = getEdgeBetween(node, where);
            path.add(e);
            where = node;
        }
        Collections.reverse(path);
        return path;
    }

    public void resetGraph(){
        nodes.clear();
    }
}