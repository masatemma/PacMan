package src.matachi.mapeditor.editor;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import src.matachi.mapeditor.grid.Grid;
import src.matachi.mapeditor.grid.GridModel;
import src.matachi.mapeditor.grid.GridView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.List;
public class LoadAndSaveFiles {
    private PrintWriter levelCheckWriter;
    private PrintWriter gameCheckWriter;

    public LoadAndSaveFiles() {
        try {
            this.levelCheckWriter = new PrintWriter("levelCheckingLog.txt", "UTF-8");
            this.gameCheckWriter = new PrintWriter("gameCheckingLog.txt", "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public PrintWriter getLevelCheckWriter() {
        return this.levelCheckWriter;
    }

    public PrintWriter getGameCheckWriter() {
        return this.gameCheckWriter;
    }

    public JFileChooser saveFile(GridModel model) {
        int height = model.getHeight();
        int width = model.getWidth();
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "xml files", "xml");
        chooser.setFileFilter(filter);
        File workingDirectory = new File(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(workingDirectory);

        int returnVal = chooser.showSaveDialog(null);
        Document doc = null;
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                Element level = new Element("level");
                doc = new Document(level);
                doc.setRootElement(level);

                Element size = new Element("size");
                size.addContent(new Element("width").setText(width + ""));
                size.addContent(new Element("height").setText(height + ""));
                doc.getRootElement().addContent(size);

                for (int y = 0; y < height; y++) {
                    Element row = new Element("row");
                    for (int x = 0; x < width; x++) {
                        char tileChar = model.getTile(x, y);
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
                        System.out.println(type);
                        Element e = new Element("cell");
                        row.addContent(e.setText(type));
                    }
                    doc.getRootElement().addContent(row);
                }
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput
                        .output(doc, new FileWriter(chooser.getSelectedFile()));
                Checker checker = new Checker(model);
                checker.levelChecking(doc, chooser.getSelectedFile().getName(), levelCheckWriter);
            }
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(null, "Invalid file!", "error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
        }
        return chooser;
    }

    public JFileChooser loadFile(GridModel model) {
        int height = model.getHeight();
        ;
        int width = model.getWidth();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        SAXBuilder builder = new SAXBuilder();
        try {

            File selectedFile;
            BufferedReader in;
            FileReader reader = null;
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
            int returnVal = chooser.showOpenDialog(null);
            Document document;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                if (selectedFile.isDirectory()) {
                    return chooser;
                } else if (selectedFile == null) {
                    return null;
                } else if (selectedFile.canRead() && selectedFile.exists()) {
                    document = (Document) builder.build(selectedFile);

                    Element rootNode = document.getRootElement();

                    List sizeList = rootNode.getChildren("size");
                    Element sizeElem = (Element) sizeList.get(0);
                    height = Integer.parseInt(sizeElem
                            .getChildText("height"));
                    width = Integer
                            .parseInt(sizeElem.getChildText("width"));
                    model.map = model.createEmptyMap(width, height);
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
                    Checker checker = new Checker(model);
                    checker.levelChecking(document, chooser.getSelectedFile().getName(), levelCheckWriter);
                }
            }
            return chooser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chooser;
    }

    public boolean loadChosenFile(GridModel model, File selectedFile) {
        int height = model.getHeight();
        int width = model.getWidth();
        SAXBuilder builder = new SAXBuilder();
        boolean validMap = true;
        try {
            BufferedReader in;
            FileReader reader = null;
            File workingDirectory = new File(System.getProperty("user.dir"));

            Document document;
            if (selectedFile.canRead() && selectedFile.exists()) {
                document = (Document) builder.build(selectedFile);

                Element rootNode = document.getRootElement();

                List sizeList = rootNode.getChildren("size");
                Element sizeElem = (Element) sizeList.get(0);
                height = Integer.parseInt(sizeElem
                        .getChildText("height"));
                width = Integer
                        .parseInt(sizeElem.getChildText("width"));
                model.map = model.createEmptyMap(width, height);
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
                Checker checker = new Checker(model);
                validMap = checker.levelChecking(document, selectedFile.getName(), levelCheckWriter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return validMap;
    }
}


