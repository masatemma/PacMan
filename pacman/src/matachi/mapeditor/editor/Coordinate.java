package src.matachi.mapeditor.editor;

public class Coordinate {
    private String tile;
    private int x, y;
    public Coordinate(int x, int y, String tile){
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY(){
        return y;
    }

    public String getTile(){
        return this.tile;
    }
}
