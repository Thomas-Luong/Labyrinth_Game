import com.wwu.graphics.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;

// Extended GameGraphics that adds debug & sound switches, player info panels, and composite image support
public class CustomGameGraphics extends GameGraphics {

    private CustomBoardGraphicsInf customBoardGraphicsInf;
    private JCheckBox debugSwitch;
    private JCheckBox soundSwitch;
    private JPanel[] playerInfo;
    private JLabel[] playerInfoLabel;
    private JLabel foeAwakeText;
    private Field tileArrayField;
    private Field imageHashField;
    private Map<ExtraGraphicImageTypes, BufferedImage> extraImagesMap;
    private JSONObject config;

    public CustomGameGraphics(CustomBoardGraphicsInf customBoardGraphicsInf) {
        super(customBoardGraphicsInf);
        this.customBoardGraphicsInf = customBoardGraphicsInf;
        readConfig();
        reflectSuper();
        initializeExtraGraphics();
        initializeCustomComponents();
    }

    // Returns true if the debug switch is checked
    public boolean isDebug() {
        return debugSwitch.isSelected();
    }

    // Returns true if the sound switch is checked
    public boolean isSoundEnabled() {
        return soundSwitch.isSelected();
    }

    // Sets the visibility of the foe awake text
    public void setFoeAwake(boolean awake) {
        foeAwakeText.setVisible(awake);
    }

    // Sets the visibility of the given player's info panel
    public void setPlayerInfoVisible(int playerNumber, boolean visible) {
        int i = playerNumber - 1;
        playerInfo[i].setVisible(visible);
    }

    // Show/hide the treasure icon for the given player
    public void setPlayerHasTreasure(int playerNumber, boolean hasTreasure) {
        int i = playerNumber - 1;
        playerInfoLabel[i].setIcon(new ImageIcon(
            hasTreasure
            ? extraImagesMap.get(playerNumber == 1 ? ExtraGraphicImageTypes.HERO1_TREASURE : ExtraGraphicImageTypes.HERO2_TREASURE)
            : getImagesMap().get(playerNumber == 1 ? GraphicImageTypes.HERO1 : GraphicImageTypes.HERO2)));
    }

    // Updates the given player's moves & health in the info panel
    public void updatePlayerInfo(int playerNumber, int curMoves, int totMoves, int health) {
        int i = playerNumber - 1;
        playerInfoLabel[i].setText(makeLabelText(playerNumber, curMoves, totMoves, health));
    }

    // Sets the active player
    public void setActivePlayer(int playerNumber) {
        int i = playerNumber - 1;
        playerInfo[i].setBackground(Color.DARK_GRAY);
        playerInfo[(i + 1) % 2].setBackground(Color.BLACK);
    }

    // Sets the tile image for the given tile using an extended image
    public void changeTileImage(int col, int row, ExtraGraphicImageTypes imageName) {
        Object tile = getTileArray()[col][row];
        try {
            Method method = tile.getClass().getMethod("changeGraphic", Image.class);
            method.setAccessible(true);
            method.invoke(tile, extraImagesMap.get(imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        repaint();
    }

    // Reads the config.json file
    private void readConfig() {
        JSONParser parser = new JSONParser();
        try {
            config = (JSONObject) parser.parse(new FileReader("config.json"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Hacky reflection to get access to the private tile array & original image hashmap from the superclass (GameGraphics)
    private void reflectSuper() {
        Class<?> superClass = getClass().getSuperclass();
        try {
            tileArrayField = superClass.getDeclaredField("tileArray");
            tileArrayField.setAccessible(true);
            imageHashField = superClass.getDeclaredField("imageHash");
            imageHashField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Returns the tile array from GameGraphics
    private Object[][] getTileArray() {
        try {
            return (Object[][]) tileArrayField.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Returns the original image hashmap from GameGraphics
    @SuppressWarnings("unchecked") 
    private Map<GraphicImageTypes, BufferedImage> getImagesMap() {
        try {
            return (Map<GraphicImageTypes, BufferedImage>) imageHashField.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Initializes extra & composite images
    private void initializeExtraGraphics() {
        extraImagesMap = new java.util.HashMap<ExtraGraphicImageTypes, BufferedImage>();
        String imagePath = (String) config.get("ImagePath");
        Map<?,?> images = (Map<?,?>) config.get("Images");
        for (ExtraGraphicImageTypes imageName : ExtraGraphicImageTypes.values()) {
            if (!imageName.isComposite()) {
                try {
                    extraImagesMap.put(imageName, ImageIO.read(new File(imagePath + images.get(imageName.toString()))));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        initializeCompositeGraphics();
    }

    // Creates composite images from the original images
    private void initializeCompositeGraphics() {
        Map<GraphicImageTypes, BufferedImage> baseImagesMap = getImagesMap();

        BufferedImage hero1 = baseImagesMap.get(GraphicImageTypes.HERO1);
        BufferedImage hero2 = baseImagesMap.get(GraphicImageTypes.HERO2);
        BufferedImage treasure = baseImagesMap.get(GraphicImageTypes.GOAL);

        BufferedImage hero1Treasure = compositeImage(treasure, 24, 25, 16, 16, hero1, 0, 0, 40, 40);
        BufferedImage hero2Treasure = compositeImage(treasure, 24, 25, 16, 16, hero2, 0, 0, 40, 40);
        BufferedImage hero1Hero2 = compositeImage(hero1, 0, 0, 30, 30, hero2, 10, 10, 30, 30);
        BufferedImage hero2Hero1 = compositeImage(hero2, 0, 0, 30, 30, hero1, 10, 10, 30, 30);

        extraImagesMap.put(ExtraGraphicImageTypes.HERO1_TREASURE, hero1Treasure);
        extraImagesMap.put(ExtraGraphicImageTypes.HERO2_TREASURE, hero2Treasure);
        extraImagesMap.put(ExtraGraphicImageTypes.HERO1_HERO2, hero1Hero2);
        extraImagesMap.put(ExtraGraphicImageTypes.HERO2_HERO1, hero2Hero1);
    }

    // Overlays two images with the given locations and sizes
    private BufferedImage compositeImage(BufferedImage img1, int x1, int y1, int w1, int h1, BufferedImage img2, int x2, int y2, int w2, int h2) {
        int width = Math.max(img1.getWidth(), img2.getWidth());
        int height = Math.max(img1.getHeight(), img2.getHeight());

        BufferedImage composite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = composite.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img2, x2, y2, w2, h2, null);
        g.drawImage(img1, x1, y1, w1, h1, null);
        g.dispose();

        return composite;
    }

    // Initializes all the custom panel components
    private void initializeCustomComponents() {
        initializeDebugSwitch();
        initializeSoundSwitch();
        initializePlayerInfo();
        initializeFoeAwakeText();
    }

    // Initializes the debug switch
    private void initializeDebugSwitch() {
        debugSwitch = new JCheckBox("Debug");
        debugSwitch.setBounds(630, 10, 90, 20);
        debugSwitch.addItemListener(l -> customBoardGraphicsInf.debugChanged());
        add(debugSwitch);
    }

    // Initializes the sound switch
    private void initializeSoundSwitch() {
        soundSwitch = new JCheckBox("Sounds");
        soundSwitch.setBounds(630, 30, 90, 20);
        soundSwitch.setSelected(true);
        add(soundSwitch);
    }

    // Returns an HTML string with the given player's info
    private String makeLabelText(int playerNumber, int curMoves, int totMoves, int health) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append("<b>Player ");
        sb.append(playerNumber);
        sb.append("</b>");
        sb.append("<br>");
        sb.append("<font color=\"red\">");
        sb.append("&#x2764;".repeat(health));
        sb.append("</font>");
        sb.append("<font color=\"gray\">");
        sb.append("&#x2764;".repeat(3 - health));
        sb.append("</font>");
        sb.append("<br>");
        if (curMoves == 0) {
            sb.append("<font color=\"red\">");
        } else if (curMoves == totMoves) {
            sb.append("<font color=\"lime\">");
        } else {
            sb.append("<font color=\"yellow\">");
        }
        sb.append(curMoves);
        sb.append("/");
        sb.append(totMoves);
        sb.append(" moves");
        sb.append("</font>");
        sb.append("</html>");

        return sb.toString();
    }

    // Initializes the player info panels
    private void initializePlayerInfo() {
        playerInfo = new JPanel[2];
        playerInfoLabel = new JLabel[2];

        for (int i = 0; i < 2; i++) {
            int playerNumber = i + 1;

            playerInfo[i] = new JPanel();
            playerInfo[i].setBounds(600, 60 + i * (60 + 10), 150, 60);
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.WHITE));
            playerInfo[i].setBackground(Color.BLACK);
            playerInfo[i].setVisible(false);

            playerInfoLabel[i] = new JLabel();
            playerInfoLabel[i].setText(makeLabelText(playerNumber, 8, 8, 3));
            playerInfoLabel[i].setForeground(Color.WHITE);
            setPlayerHasTreasure(playerNumber, false);

            playerInfo[i].add(playerInfoLabel[i]);

            add(playerInfo[i]);
        }
    }

    // Initializes the foe awake text
    private void initializeFoeAwakeText() {
        foeAwakeText = new JLabel("The foe is awake!");
        foeAwakeText.setBounds(600, 200, 150, 20);
        foeAwakeText.setHorizontalAlignment(SwingConstants.CENTER);
        foeAwakeText.setForeground(Color.RED);
        foeAwakeText.setVisible(false);
        add(foeAwakeText);
    }
}
