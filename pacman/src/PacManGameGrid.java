// PacGrid.java
package src;

import ch.aplu.jgamegrid.*;

import src.matachi.mapeditor.editor.Tile;
import src.matachi.mapeditor.editor.TileManager;
import src.matachi.mapeditor.grid.GridModel;
import src.matachi.mapeditor.editor.LoadAndSaveFiles;

import java.io.File;
import java.util.List;

public class PacManGameGrid
{
  private int nbHorzCells;
  private int nbVertCells;
  private int[][] mazeArray;

  public PacManGameGrid(int nbHorzCells, int nbVertCells,GridModel model)
  {
    List<Tile> tiles = TileManager.getTilesFromFolder("data/");
    this.nbHorzCells = nbHorzCells;
    this.nbVertCells = nbVertCells;
    mazeArray = new int[nbVertCells][nbHorzCells];
    String maze = model.getMapAsString();


    // Copy structure into integer array
    for (int i = 0; i < nbVertCells; i++)
    {
      for (int k = 0; k < nbHorzCells; k++) {
          int value = toInt(maze.charAt(nbHorzCells * i + k));
          mazeArray[i][k] = value;

      }
    }
  }

  public int getCell(Location location)
  {
    return mazeArray[location.y][location.x];
  }
  private int toInt(char c)
  {
    if (c == 'b')
      return 0;
    if (c == 'c')
      return 1;
    if (c == 'a')
      return 2;
    if (c == 'd')
      return 3;
    if (c == 'e')
      return 4;
    if (c == 'f')
      return 5;
    if (c == 'g')
      return 6;
    if (c == 'h')
      return 7;
    if (c == 'i')
      return 8;
    if (c == 'j')
      return 9;
    if (c == 'k')
      return 10;
    if (c == 'l')
      return 11;
    return -1;
  }
}
