package matachi.mapeditor.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.io.PrintWriter;


import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import matachi.mapeditor.grid.Camera;
import matachi.mapeditor.grid.Grid;
import matachi.mapeditor.grid.GridCamera;
import matachi.mapeditor.grid.GridModel;
import matachi.mapeditor.grid.GridView;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Controller of the application.
 *
 * @author Daniel "MaTachi" Jonsson
 * @version 1
 * @since v0.0.5
 *
 */
public class Controller implements ActionListener, GUIInformation {

	/**
	 * The model of the map editor.
	 */
	private Grid model;

	private Tile selectedTile;
	private Camera camera;

	private List<Tile> tiles;

	private GridView grid;
	private View view;

	private int gridWith = Constants.MAP_WIDTH;
	private int gridHeight = Constants.MAP_HEIGHT;

	/**
	 * Construct the controller.
	 */
	public Controller() {
		init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);

	}

	public void init(int width, int height) {
		this.tiles = TileManager.getTilesFromFolder("data/");
		this.model = new GridModel(width, height, tiles.get(0).getCharacter());
		this.camera = new GridCamera(model, Constants.GRID_WIDTH,
				Constants.GRID_HEIGHT);

		grid = new GridView(this, camera, tiles); // Every tile is
		// 30x30 pixels

		this.view = new View(this, camera, grid, tiles);
	}

	/**
	 * Different commands that comes from the view.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		for (Tile t : tiles) {
			if (e.getActionCommand().equals(
					Character.toString(t.getCharacter()))) {
				selectedTile = t;
				break;
			}
		}
		if (e.getActionCommand().equals("flipGrid")) {
			// view.flipGrid();
		} else if (e.getActionCommand().equals("save")) {
			//checking level
			saveFile();
		} else if (e.getActionCommand().equals("load")) {
			//checking level
			loadFile();
		} else if (e.getActionCommand().equals("update")) {
			updateGrid(gridWith, gridHeight);
		}
	}

	public void updateGrid(int width, int height) {
		view.close();
		init(width, height);
		view.setSize(width, height);
	}

	DocumentListener updateSizeFields = new DocumentListener() {

		public void changedUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void removeUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void insertUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}
	};

	private void saveFile() {

		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"xml files", "xml");
		chooser.setFileFilter(filter);
		File workingDirectory = new File(System.getProperty("user.dir"));
		chooser.setCurrentDirectory(workingDirectory);

		int returnVal = chooser.showSaveDialog(null);
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				Element level = new Element("level");
				Document doc = new Document(level);
				doc.setRootElement(level);
				Element size = new Element("size");
				int height = model.getHeight();
				int width = model.getWidth();
				size.addContent(new Element("width").setText(width + ""));
				size.addContent(new Element("height").setText(height + ""));
				doc.getRootElement().addContent(size);

				for (int y = 0; y < height; y++) {
					Element row = new Element("row");
					for (int x = 0; x < width; x++) {
						char tileChar = model.getTile(x,y);
						String type = "PathTile";
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

						Element e = new Element("cell");
						row.addContent(e.setText(type));
					}
					//need for checking the content of the file
					doc.getRootElement().addContent(row);

				}
				XMLOutputter xmlOutput = new XMLOutputter();
				xmlOutput.setFormat(Format.getPrettyFormat());
				xmlOutput
						.output(doc, new FileWriter(chooser.getSelectedFile()));
				//To get the name of the file: chooser.getSelectedFile().getName()
				//Level Checking
				levelChecking(doc, chooser.getSelectedFile().getName());
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid file!", "error",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
		}
	}

	public void loadFile() {
		SAXBuilder builder = new SAXBuilder();
		try {
			JFileChooser chooser = new JFileChooser();
			File selectedFile;
			BufferedReader in;
			FileReader reader = null;
			File workingDirectory = new File(System.getProperty("user.dir"));
			chooser.setCurrentDirectory(workingDirectory);

			int returnVal = chooser.showOpenDialog(null);
			Document document;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile = chooser.getSelectedFile();
				if (selectedFile.canRead() && selectedFile.exists()) {
					document = (Document) builder.build(selectedFile);

					Element rootNode = document.getRootElement();

					List sizeList = rootNode.getChildren("size");
					Element sizeElem = (Element) sizeList.get(0);
					int height = Integer.parseInt(sizeElem
							.getChildText("height"));
					int width = Integer
							.parseInt(sizeElem.getChildText("width"));
					updateGrid(width, height);

					List rows = rootNode.getChildren("row");
					for (int y = 0; y < rows.size(); y++) {
						Element cellsElem = (Element) rows.get(y);
						List cells = cellsElem.getChildren("cell");

						for (int x = 0; x < cells.size(); x++) {
							Element cell = (Element) cells.get(x);
							String cellValue = cell.getText();
							char tileNr = 'a';
							if (cellValue.equals("PathTile"))
								tileNr = 'a';
							else if (cellValue.equals("WallTile"))
								tileNr = 'b';
							else if (cellValue.equals("PillTile"))
								tileNr = 'c';
							else if (cellValue.equals("GoldTile"))
								tileNr = 'd';
							else if (cellValue.equals("IceTile"))
								tileNr = 'e';
							else if (cellValue.equals("PacTile"))
								tileNr = 'f';
							else if (cellValue.equals("TrollTile"))
								tileNr = 'g';
							else if (cellValue.equals("TX5Tile"))
								tileNr = 'h';
							else if (cellValue.equals("PortalWhiteTile"))
								tileNr = 'i';
							else if (cellValue.equals("PortalYellowTile"))
								tileNr = 'j';
							else if (cellValue.equals("PortalDarkGoldTile"))
								tileNr = 'k';
							else if (cellValue.equals("PortalDarkGrayTile"))
								tileNr = 'l';
							else
								tileNr = '0';

							model.setTile(x, y, tileNr);
						}
					}
					levelChecking(document, chooser.getSelectedFile().getName());
					String mapString = model.getMapAsString();
					grid.redrawGrid();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void levelChecking(Document doc, String filename){
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
		String logFilePath = "Log.txt";
		try{
			PrintWriter writer = new PrintWriter(logFilePath, "UTF-8");
			for(String tile: mapCords.keySet()){

				if (tile.equals("PillTile")){
					pillGold += mapCords.get(tile).size();
				}
				else if (tile.equals("GoldTile")){
					pillGold += mapCords.get(tile).size();
				}
				else if (tile.equals("PacTile")){
					if(mapCords.get(tile).size() < 1){
						writer.printf("%s - no start for PacMan\n", filename);
						canCheckGoldAcess = false;
					}
					else if(mapCords.get(tile).size() > 1){
						writer.printf("%s – more than one start for Pacman:", filename);
						writeCoordinates(mapCords.get(tile), writer);
						canCheckGoldAcess = false;
					}
				}
				else if (tile.equals("PortalWhiteTile") || tile.equals("PortalYellowTile") ||
						tile.equals("PortalDarkGoldTile") || tile.equals("PortalDarkGrayTile")){
					if(mapCords.get(tile).size() > 0 && mapCords.get(tile).size() != 2){
						switch (tile){
							case "PortalWhiteTile":
								writer.printf("%s – Portal %s count is not 2:", filename, "White");
								break;
							case "PortalYellowTile":
								writer.printf("%s – Portal %s count is not 2:", filename, "Yellow");
								break;
							case "PortalDarkGoldTile":
								writer.printf("%s – Portal %s count is not 2:", filename, "Dark Gold");
								break;
							case "PortalDarkGrayTile":
								writer.printf("%s – Portal %s count is not 2:", filename, "Dark Gray");
								break;

						}
						writeCoordinates(mapCords.get(tile), writer);
						canCheckGoldAcess = false;
					}
				}
			}
			if(pillGold < 2){
				writer.printf("%s - less than 2 Gold and Pill", filename);
				canCheckGoldAcess = false;
			}


			if(canCheckGoldAcess){
				HashMap<String, ArrayList<Coordinate>> nonAccessible = breadthSearch(mapCords);
				for(String tile: nonAccessible.keySet()){
					if(nonAccessible.get(tile).size() > 0){
						switch(tile){
							case "GoldTile":
								writer.printf("%s - Gold not accessible:", filename);
								break;
							case "PillTile":
								writer.printf("%s - Pill not accessible:", filename);
								break;
						}
						writeCoordinates(nonAccessible.get(tile), writer);
					}
				}
			}



			writer.close();
		}
		catch (IOException e) {
			System.out.println("An error occurred while writing to the file.");
			e.printStackTrace();
		}

	}
	void gameChecking(String folderName){
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		String duplicateLine = "multiple maps at same level: ";
		String noMapLine = "-no maps found";
		String logFilePath = "Log.txt";
		HashMap<Integer,ArrayList<String>> fileNames = new HashMap<Integer,ArrayList<String>>();
		String buffer = "";
		if (listOfFiles.length > 1){
			//more than 1 file
			//adding all file names to a hashmap
			for (int i = 0;i < listOfFiles.length;i++){
				if (listOfFiles[i].isFile()){
					int index = Integer.parseInt(listOfFiles[i].getName().split("\\D")[0]);
					fileNames.get(index).add(listOfFiles[i].getName());
				}
			}
			//checking any keys have more than one value
			for (HashMap.Entry<Integer,ArrayList<String>> entry : fileNames.entrySet()) {
				int key = entry.getKey();
				ArrayList<String> value = entry.getValue();
				//if a key has more than one filename stored
				if (value.size() > 1){
					for (int i = 0;i < value.size();i++){
						buffer = String.join(buffer,value.get(i),";");
					}
					//writing to log file
					try {
						PrintWriter writer = new PrintWriter(logFilePath, "UTF-8");
						writer.printf(String.join(duplicateLine,buffer));
					}catch (IOException e){
						System.out.println("An error occurred while writing to the file.");
						e.printStackTrace();
					}
					//resetting buffer
					buffer = "";
				}
			}

		}else{
			// number of files < 1
			try {
				PrintWriter writer = new PrintWriter(logFilePath, "UTF-8");
				writer.printf(noMapLine);
			}catch (IOException e){
				System.out.println("An error occurred while writing to the file.");
				e.printStackTrace();
			}
		}


	}

	void writeCoordinates(ArrayList<Coordinate> tileCords, PrintWriter writer){
		int count = 1;

		// Write coordinates into log file
		for(Coordinate cord: tileCords){
			if(tileCords.size() > count){
				writer.printf(" (%d, %d);", cord.x + 1, cord.y + 1);
				count++;
			}
			else if(tileCords.size() == count){
				writer.printf(" (%d, %d)", cord.x + 1, cord.y + 1);
			}

		}
		writer.printf("\n");
	}

	HashMap<String, ArrayList<Coordinate>> breadthSearch(HashMap<String, ArrayList<Coordinate>> mapCords){
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

		visited[pacCord.y][pacCord.x] = true;

		//up, down, left, right
		int[] dirX = {0, 0, -1, 1};
		int[] dirY = {-1, 1, 0, 0};

		while(!queue.isEmpty()) {
			Coordinate curCord = queue.poll();

			for (int i = 0; i < 4; i++) { // visit all four directions
				int newDirX = curCord.x + dirX[i];
				int newDirY = curCord.y + dirY[i];

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
					if(visited[cord.y][cord.x] != true){
						nonAccessible.get(tile).add(cord);
					}
				}
			}
		}

		return nonAccessible;

	}

	 Coordinate getAnotherPortal (HashMap<String, ArrayList<Coordinate>> mapCords, Coordinate portalA){

		// Add coordinate of each portals as staryting points
		for(String tile: mapCords.keySet()){
			if(tile.equals(portalA.tile)){
				for(Coordinate cord: mapCords.get(portalA.tile)){
					if(cord.x != portalA.x || cord.y != portalA.y){
						return cord;
					}
				}
			}
		}
		return null;
	}

	String getTileName(char tileChar){
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
	char[][] getCharArray(){
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



	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getSelectedTile() {
		return selectedTile;
	}
}
