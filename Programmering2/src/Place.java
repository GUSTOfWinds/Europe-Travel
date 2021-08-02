import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Place extends Circle {

    private static final double RADIUS = 10;
    private String name;
    private boolean selected;
    private double x;
    private double y;

    public Place(String name, double x, double y){
        super(x, y, RADIUS, Color.BLUE);
        this.name = name;
        this.y = y;
        this.x = x;
    }

    public void setSelected(boolean parameter){
        selected = parameter;
        if(selected){
            setFill(Color.RED);
        }else{
            setFill(Color.BLUE);
        }
    }

    public String getName(){
        return name;
    }
    public boolean isSelected(){
        return selected;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
}