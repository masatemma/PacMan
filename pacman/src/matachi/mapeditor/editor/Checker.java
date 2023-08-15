package src.matachi.mapeditor.editor;

import org.jdom.Document;
import org.jdom.Element;
import src.matachi.mapeditor.grid.GridModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Checker {
    private GridModel model;

    public Checker(GridModel model){
        this.model = model;

    }

    public boolean levelChecking(Document doc, String filename, PrintWriter levelCheckWriter){
        HashMap<String, ArrayList<Coordinate>> mapCords = new HashMap<>();
        Element rootNode = doc.getRootElement();
        List rows = rootNode.getChildren("row");
        boolean	canCheckGoldAcess = true;

        // Store coordinates of each tile into mapCords
        for (int y = 0; y < rows.size(); y++) {
            Element cellsElem = (Element) rows.get(y);
            List cells = cellsElem.getChildren("cell");

            for (int x = 0; x < cells.size(); x++) {
                Element cell = (Element) cells.get(x);
                String cellValue = cell.getText();
                if(mapCords.containsKey(cellValue)){
                    Coordinate newCord = new Coordinate(x, y, cellValue);
                    mapCords.get(cellValue).add(newCord);
                }
                else{
                    ArrayList<Coordinate> coordinates = new ArrayList<>();
                    Coordinate newCord = new Coordinate(x , y, cellValue);
                    coordinates.add(newCord);
                    mapCords.put(cellValue, coordinates);
                }
            }
        }

        int pillGold = 0;

        if(!mapCords.containsKey("PacTile")){
            levelCheckWriter.printf("%s - no start for PacMan\n", filename);
            canCheckGoldAcess = false;
        }

        for(String tile: mapCords.keySet()){

            if (tile.equals("PillTile")){
                pillGold += mapCords.get(tile).size();
            }
            else if (tile.equals("GoldTile")){
                pillGold += mapCords.get(tile).size();
            }
            else if (tile.equals("PacTile")){
                if(mapCords.get(tile).size() > 1){
                    levelCheckWriter.printf("%s – more than one start for Pacman:", filename);
                    writeCoordinates(mapCords.get(tile), levelCheckWriter);
                    canCheckGoldAcess = false;
                }
            }
            else if (tile.equals("PortalWhiteTile") || tile.equals("PortalYellowTile") ||
                    tile.equals("PortalDarkGoldTile") || tile.equals("PortalDarkGrayTile")){
                if(mapCords.get(tile).size() > 0 && mapCords.get(tile).size() != 2){
                    switch (tile){
                        case "PortalWhiteTile":
                            levelCheckWriter.printf("%s – Portal %s count is not 2:", filename, "White");
                            break;
                        case "PortalYellowTile":
                            levelCheckWriter.printf("%s – Portal %s count is not 2:", filename, "Yellow");
                            break;
                        case "PortalDarkGoldTile":
                            levelCheckWriter.printf("%s – Portal %s count is not 2:", filename, "Dark Gold");
                            break;
                        case "PortalDarkGrayTile":
                            levelCheckWriter.printf("%s – Portal %s count is not 2:", filename, "Dark Gray");
                            break;

                    }
                    writeCoordinates(mapCords.get(tile), levelCheckWriter);
                }
            }
        }
        if(pillGold == 0){
            levelCheckWriter.printf("%s - less than 2 Gold and Pill\n", filename);
            canCheckGoldAcess = false;
        }
        else if(pillGold == 1){
            levelCheckWriter.printf("%s - less than 2 Gold and Pill\n", filename);
        }

        if(canCheckGoldAcess){
            HashMap<String, ArrayList<Coordinate>> nonAccessible = breadthSearch(mapCords);
            for(String tile: nonAccessible.keySet()){
                if(nonAccessible.get(tile).size() > 0){
                    switch(tile){
                        case "GoldTile":
                            levelCheckWriter.printf("%s - Gold not accessible:", filename);
                            break;
                        case "PillTile":
                            levelCheckWriter.printf("%s - Pill not accessible:", filename);
                            break;
                    }
                    writeCoordinates(nonAccessible.get(tile), levelCheckWriter);
                    canCheckGoldAcess = false;
                }
            }
        }
        return canCheckGoldAcess;

    }
    public ArrayList<File> gameChecking(String folderName, PrintWriter writer){
        File folder = new File(folderName);
        ArrayList<File> listOfValidMaps = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
        String duplicateLine = folderName+" - multiple maps at same level: ";
        String noMapLine = folderName+" - no maps found";
        HashMap<Integer,ArrayList<String>> fileNames = new HashMap<>();
        String buffer = "";
        if (listOfFiles.length >= 1){
            //more than 1 file
            //adding all file names to a hashmap
            for (int i = 0;i < listOfFiles.length;i++){
                if (listOfFiles[i].isFile()){
                    int index = -1;
                    Pattern pattern = Pattern.compile("^\\d+");
                    Matcher matcher = pattern.matcher(listOfFiles[i].getName());

                    if(matcher.find()){
                        index = Integer.parseInt(matcher.group());
                        if(fileNames.containsKey(index)){
                            fileNames.get(index).add(listOfFiles[i].getName());
                        }
                        else if(!fileNames.containsKey(index)){
                            ArrayList<String> newNumFiles = new ArrayList<>();
                            newNumFiles.add(listOfFiles[i].getName());
                            fileNames.put(index, newNumFiles);
                        }
                    }
                }
            }
            if(fileNames.size() > 0){
                //checking any keys have more than one value
                for (int fileNum: fileNames.keySet()) {
                    //if a key has more than one filename stored
                    if (fileNames.get(fileNum).size() > 1){
                        buffer = String.join("; ",fileNames.get(fileNum));
                        writer.printf(duplicateLine + buffer + '\n');
                    }
                    else if(fileNames.get(fileNum).size() == 1){
                        for (int i = 0;i < listOfFiles.length;i++) {
                            if(fileNames.get(fileNum).get(0).equals(listOfFiles[i].getName())){
                                listOfValidMaps.add(listOfFiles[i]);
                                break;
                            }
                        }
                    }
                }
            }

        }

        if(listOfValidMaps.size() == 0){
            writer.printf(noMapLine);
        }

        return listOfValidMaps;
    }

    private void writeCoordinates(ArrayList<Coordinate> tileCords, PrintWriter writer){
        int count = 1;

        // Write coordinates into log file
        for(Coordinate cord: tileCords){
            if(tileCords.size() > count){
                writer.printf(" (%d, %d);", cord.getX() + 1, cord.getY() + 1);
                count++;
            }
            else if(tileCords.size() == count){
                writer.printf(" (%d, %d)", cord.getX() + 1, cord.getY() + 1);
            }

        }
        writer.printf("\n");
    }

    private HashMap<String, ArrayList<Coordinate>> breadthSearch(HashMap<String, ArrayList<Coordinate>> mapCords){
        char[][] gridArray = getCharArray();

        // boolean array to track visited cell
        boolean[][] visited = new boolean[gridArray.length][gridArray[0].length];

        // HashMap to store Gold and Pill coordinates that are not accessible to PacMan
        HashMap<String, ArrayList<Coordinate>> nonAccessible = new HashMap<>();
        nonAccessible.put("GoldTile", new ArrayList<Coordinate>());
        nonAccessible.put("PillTile", new ArrayList<Coordinate>());

        Queue<Coordinate> queue = new LinkedList<>();
        Coordinate pacCord = mapCords.get("PacTile").get(0);
        queue.add(pacCord);

        visited[pacCord.getY()][pacCord.getX()] = true;

        //up, down, left, right
        int[] dirX = {0, 0, -1, 1};
        int[] dirY = {-1, 1, 0, 0};

        while(!queue.isEmpty()) {
            Coordinate curCord = queue.poll();

            for (int i = 0; i < 4; i++) { // visit all four directions
                int newDirX = curCord.getX() + dirX[i];
                int newDirY = curCord.getY() + dirY[i];

                if (newDirX >= 0 && newDirY >= 0 && newDirY < gridArray.length && newDirX < gridArray[0].length) {
                    String tileName = getTileName(gridArray[newDirY][newDirX]);
                    if (!visited[newDirY][newDirX] && gridArray[newDirY][newDirX] != 'b') {
                        Coordinate newQueueCord = new Coordinate(newDirX, newDirY, tileName);
                        queue.add(newQueueCord);
                        visited[newDirY][newDirX] = true;
                        if(mapCords.containsKey(tileName)){
                            char tileChar = gridArray[newDirY][newDirX];
                            if(tileChar == 'i' || tileChar == 'j'||
                                    tileChar == 'k'|| tileChar == 'l'){
                                Coordinate portalB = getAnotherPortal(mapCords, newQueueCord);
                                queue.add(portalB);
                            }
                        }
                    }

                }
            }
        }
        //Check if all Gold and Pill can be visited from PacMan Starting point
        for(String tile: mapCords.keySet()){
            if(tile.equals("GoldTile") || tile.equals("PillTile")){
                ArrayList<Coordinate> tileCords = mapCords.get(tile);
                for(Coordinate cord: tileCords){
                    if(visited[cord.getY()][cord.getX()] != true){
                        nonAccessible.get(tile).add(cord);
                    }
                }
            }
        }

        return nonAccessible;

    }

    private Coordinate getAnotherPortal (HashMap<String, ArrayList<Coordinate>> mapCords, Coordinate portalA){

        // Add coordinate of each portals as staryting points
        for(String tile: mapCords.keySet()){
            if(tile.equals(portalA.getTile())){
                for(Coordinate cord: mapCords.get(portalA.getTile())){
                    if(cord.getX() != portalA.getX() || cord.getY() != portalA.getY()){
                        return cord;
                    }
                }
            }
        }
        return null;
    }

    private String getTileName(char tileChar){
        String type = "";

        if (tileChar == 'b')
            type = "WallTile";
        else if (tileChar == 'c')
            type = "PillTile";
        else if (tileChar == 'd')
            type = "GoldTile";
        else if (tileChar == 'e')
            type = "IceTile";
        else if (tileChar == 'f')
            type = "PacTile";
        else if (tileChar == 'g')
            type = "TrollTile";
        else if (tileChar == 'h')
            type = "TX5Tile";
        else if (tileChar == 'i')
            type = "PortalWhiteTile";
        else if (tileChar == 'j')
            type = "PortalYellowTile";
        else if (tileChar == 'k')
            type = "PortalDarkGoldTile";
        else if (tileChar == 'l')
            type = "PortalDarkGrayTile";

        return type;
    }
    private char[][] getCharArray(){
        int height = model.getHeight();
        int width = model.getWidth();

        char[][] charArray = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tileChar = model.getTile(x,y);
                charArray[y][x] = tileChar;
            }
        }
        return charArray;
    }
}
