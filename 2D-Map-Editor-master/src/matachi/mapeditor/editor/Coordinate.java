package matachi.mapeditor.editor;

public class Coordinate {
    String tile;
    int x, y;
    Coordinate(int x, int y, String tile){
        this.tile = tile;
        this.x = x;
        this.y = y;
    }
}
