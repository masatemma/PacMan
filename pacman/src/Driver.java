package src;

import src.matachi.mapeditor.editor.*;
import src.matachi.mapeditor.grid.GridModel;
import src.utility.GameCallback;
import src.utility.PropertiesLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Driver {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/test5.properties";

    /**
     * Starting point
     * @param args the command line arguments
     */

    public static void main(String args[]) {
        int nbHorzCells = 20;
        int nbVertCells = 11;
        String propertiesPath = DEFAULT_PROPERTIES_PATH;
        List<Tile> tiles = TileManager.getTilesFromFolder("data/");
        GridModel model = new GridModel(nbHorzCells,nbVertCells,tiles.get(0).getCharacter());
        LoadAndSaveFiles SL = new LoadAndSaveFiles();
        File file = null;
        Checker checker = new Checker(model);
        if(args.length>0){
            file = new File(args[0].toString());
        }
        if(file == null) {
            Controller controller = new Controller(file,model);
        }
        else if(file.isFile()) {
            SL.loadChosenFile(model, file);
            Controller controller = new Controller(file,model);
        }
        else if(file.isDirectory()&&file.listFiles().length==1){
            ArrayList<File> listOfValidMaps = checker.gameChecking(file.getName(), SL.getGameCheckWriter());
            if(listOfValidMaps.size() > 0){
                final Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
                GameCallback gameCallback = new GameCallback();
                if(SL.loadChosenFile(model,file.listFiles()[0])){
                    new Game(gameCallback, properties,model);
                    Controller controller = new Controller(file.listFiles()[0],model);
                }
            }
        }
        else if(file.isDirectory()){
            ArrayList<File> listOfValidMaps = checker.gameChecking(file.getName(), SL.getGameCheckWriter());
            if(listOfValidMaps.size() > 0){
                for(File f: listOfValidMaps){
                    final Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
                    GameCallback gameCallback = new GameCallback();
                    if(SL.loadChosenFile(model,f)){
                        new Game(gameCallback, properties,model);
                    }
                }
                Controller controller = new Controller(null,new GridModel(nbHorzCells,nbVertCells,tiles.get(0).getCharacter()));
            }
        }
        SL.getLevelCheckWriter().close();
        SL.getGameCheckWriter().close();
    }
}
