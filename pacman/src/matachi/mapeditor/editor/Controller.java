package src.matachi.mapeditor.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import src.matachi.mapeditor.grid.Camera;
import src.matachi.mapeditor.grid.Grid;
import src.matachi.mapeditor.grid.GridCamera;
import src.matachi.mapeditor.grid.GridModel;
import src.matachi.mapeditor.grid.GridView;

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
	public static GridModel model;

	private Tile selectedTile;
	private static Camera camera;

	private static List<Tile> tiles;

	private static GridView grid;
	private static View view;

	private int gridWith = Constants.MAP_WIDTH;
	private int gridHeight = Constants.MAP_HEIGHT;
	File f;
	/**
	 * Construct the controller.
	 */
	public Controller(File f,GridModel model) {
		this.model = model;
		LoadAndSaveFiles SL = new LoadAndSaveFiles();
		if(f!=null) {
			SL.loadChosenFile(this.model, f);
		}
		init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT,f,model);
	}

	public void init(int width, int height,File f,GridModel model) {
		this.tiles = TileManager.getTilesFromFolder("data/");
		this.camera = new GridCamera(model, Constants.GRID_WIDTH,
				Constants.GRID_HEIGHT);

		grid = new GridView(this, camera, tiles); // Every tile is
													// 30x30 pixels
		this.f = f;
		this.view = new View(this, camera, grid, tiles);
		grid.redrawGrid();

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
			LoadAndSaveFiles SL = new LoadAndSaveFiles();
			SL.saveFile(model);
		} else if (e.getActionCommand().equals("load")) {
			LoadAndSaveFiles SL = new LoadAndSaveFiles();
			SL.loadFile(model);
			grid.redrawGrid();
		} else if (e.getActionCommand().equals("update")) {
			updateGrid(gridWith, gridHeight);
		}
	}

	public void updateGrid(int width, int height) {
		view.close();
		init(width, height,f,model);
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




	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getSelectedTile() {
		return selectedTile;
	}
}
