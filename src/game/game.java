package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class game extends JApplet {
  private static final Color BG_COLOR = new Color(0xbbada0);
  private static final String FONT_NAME = "Arial";
  private static final int tiles_SIZE = 64;
  private static final int ptiles_MARGIN = 16;

  private tiles[] ptiles;
  boolean win = false;
  boolean lose = false;
  int score = 0;

  public void start() {
    setFocusable(true);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          resetGame();
        }
        if (!canMove()) {
          lose = true;
        }

        if (!win && !lose) {
          switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
              left();
              break;
            case KeyEvent.VK_RIGHT:
              right();
              break;
            case KeyEvent.VK_DOWN:
              down();
              break;
            case KeyEvent.VK_UP:
              up();
              break;
          }
        }

        if (!win && !canMove()) {
          lose = true;
        }

        repaint();
      }
    });
    resetGame();
  }

  public void resetGame() {
    score = 0;
    win = false;
    lose = false;
    ptiles = new tiles[4 * 4];
    for (int i = 0; i < ptiles.length; i++) {
      ptiles[i] = new tiles();
    }
    addtiles();
    addtiles();
  }

  public void left() {
    boolean needAddtiles = false;
    for (int i = 0; i < 4; i++) {
      tiles[] line = getLine(i);
      tiles[] merged = mergeLine(moveLine(line));
      setLine(i, merged);
      if (!needAddtiles && !compare(line, merged)) {
        needAddtiles = true;
      }
    }

    if (needAddtiles) {
      addtiles();
    }
  }

  public void right() {
    ptiles = rotate(180);
    left();
    ptiles = rotate(180);
  }

  public void up() {
    ptiles = rotate(270);
    left();
    ptiles = rotate(90);
  }

  public void down() {
    ptiles = rotate(90);
    left();
    ptiles = rotate(270);
  }

  private tiles tilesAt(int x, int y) {
    return ptiles[x + y * 4];
  }

  private void addtiles() {
    List<tiles> list = availableSpace();
    if (!availableSpace().isEmpty()) {
      int index = (int) (Math.random() * list.size()) % list.size();
      tiles emptyTime = list.get(index);
      emptyTime.value = Math.random() < 0.9 ? 2 : 4;
    }
  }

  private List<tiles> availableSpace() {
    final List<tiles> list = new ArrayList<tiles>(16);
    for (tiles t : ptiles) {
      if (t.isEmpty()) {
        list.add(t);
      }
    }
    return list;
  }

  private boolean isFull() {
    return availableSpace().size() == 0;
  }

  boolean canMove() {
    if (!isFull()) {
      return true;
    }
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        tiles t = tilesAt(x, y);
        if ((x < 3 && t.value == tilesAt(x + 1, y).value)
          || ((y < 3) && t.value == tilesAt(x, y + 1).value)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean compare(tiles[] line1, tiles[] line2) {
    if (line1 == line2) {
      return true;
    } else if (line1.length != line2.length) {
      return false;
    }

    for (int i = 0; i < line1.length; i++) {
      if (line1[i].value != line2[i].value) {
        return false;
      }
    }
    return true;
  }

  private tiles[] rotate(int angle) {
    tiles[] newptiles = new tiles[4 * 4];
    int offsetX = 3, offsetY = 3;
    if (angle == 90) {
      offsetY = 0;
    } else if (angle == 270) {
      offsetX = 0;
    }

    double rad = Math.toRadians(angle);
    int cos = (int) Math.cos(rad);
    int sin = (int) Math.sin(rad);
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        int newX = (x * cos) - (y * sin) + offsetX;
        int newY = (x * sin) + (y * cos) + offsetY;
        newptiles[(newX) + (newY) * 4] = tilesAt(x, y);
      }
    }
    return newptiles;
  }

  private tiles[] moveLine(tiles[] oldLine) {
    LinkedList<tiles> l = new LinkedList<tiles>();
    for (int i = 0; i < 4; i++) {
      if (!oldLine[i].isEmpty())
        l.addLast(oldLine[i]);
    }
    if (l.size() == 0) {
      return oldLine;
    } else {
      tiles[] newLine = new tiles[4];
      ensureSize(l, 4);
      for (int i = 0; i < 4; i++) {
        newLine[i] = l.removeFirst();
      }
      return newLine;
    }
  }

  private tiles[] mergeLine(tiles[] oldLine) {
    LinkedList<tiles> list = new LinkedList<tiles>();
    for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
      int num = oldLine[i].value;
      if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
        num *= 2;
        score += num;
        int ourTarget = 2048;
        if (num == ourTarget) {
          win = true;
        }
        i++;
      }
      list.add(new tiles(num));
    }
    if (list.size() == 0) {
      return oldLine;
    } else {
      ensureSize(list, 4);
      return list.toArray(new tiles[4]);
    }
  }

  private static void ensureSize(java.util.List<tiles> l, int s) {
    while (l.size() != s) {
      l.add(new tiles());
    }
  }

  private tiles[] getLine(int index) {
    tiles[] result = new tiles[4];
    for (int i = 0; i < 4; i++) {
      result[i] = tilesAt(i, index);
    }
    return result;
  }

  private void setLine(int index, tiles[] re) {
    System.arraycopy(re, 0, ptiles, index * 4, 4);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(BG_COLOR);
    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
    for (int y = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++) {
        drawtiles(g, ptiles[x + y * 4], x, y);
      }
    }
  }

  private void drawtiles(Graphics g2, tiles tiles, int x, int y) {
    Graphics2D g = ((Graphics2D) g2);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    int value = tiles.value;
    int xOffset = offsetCoors(x);
    int yOffset = offsetCoors(y);
    g.setColor(tiles.getBackground());
    g.fillRoundRect(xOffset, yOffset, tiles_SIZE, tiles_SIZE, 14, 14);
    g.setColor(tiles.getForeground());
    final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
    final Font font = new Font(FONT_NAME, Font.BOLD, size);
    g.setFont(font);

    String s = String.valueOf(value);
    final FontMetrics fm = getFontMetrics(font);

    final int w = fm.stringWidth(s);
    final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

    if (value != 0)
      g.drawString(s, xOffset + (tiles_SIZE - w) / 2, yOffset + tiles_SIZE - (tiles_SIZE - h) / 2 - 2);

    if (win || lose) {
      g.setColor(new Color(255, 255, 255, 30));
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(new Color(78, 139, 202));
      g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
      if (win) {
        g.drawString("You won!", 68, 150);
      }
      if (lose) {
        g.drawString("You lose!", 68, 150);
      }
      if (win || lose) {
        g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
        g.setColor(new Color(128, 128, 128, 128));
        g.drawString("Press ESC to play again", 80, getHeight() - 40);
      }
    }
    g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
    g.drawString("Score: " + score, 200, 365);

  }

  private static int offsetCoors(int arg) {
    return arg * (ptiles_MARGIN + tiles_SIZE) + ptiles_MARGIN;
  }

  static class tiles {
    int value;

    public tiles() {
      this(0);
    }

    public tiles(int num) {
      value = num;
    }

    public boolean isEmpty() {
      return value == 0;
    }

    public Color getForeground() {
      return value < 16 ? new Color(0x776e65) :  new Color(0xf9f6f2);
    }

    public Color getBackground() {
      switch (value) {
        case 2:    return new Color(0xeee4da);
        case 4:    return new Color(0xede0c8);
        case 8:    return new Color(0xf2b179);
        case 16:   return new Color(0xf59563);
        case 32:   return new Color(0xf67c5f);
        case 64:   return new Color(0xf65e3b);
        case 128:  return new Color(0xedcf72);
        case 256:  return new Color(0xedcc61);
        case 512:  return new Color(0xedc850);
        case 1024: return new Color(0xedc53f);
        case 2048: return new Color(0xedc22e);
      }
      return new Color(0xcdc1b4);
    }
  }

  public static void init(String[] args) {
    JFrame game = new JFrame();
    game.setTitle("2048");
    game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    game.setSize(340, 400);
    game.setResizable(false);

    game.add(new game());

    game.setLocationRelativeTo(null);
    game.setVisible(true);
  }
}
