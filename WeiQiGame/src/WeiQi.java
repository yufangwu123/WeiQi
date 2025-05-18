import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.Queue;


/**
 * @author yufangwu
 */
public class WeiQi extends JFrame {
  // 棋盘大小（格数）
  private static final int BOARD_SIZE = 18;
  // 每个格子的大小
  private static final int GRID_SIZE = 55;
  // 保存鼠标点击的点
  private Point clickPoint;
  boolean flag = true;
  private static char[][] QI = new char[19][19];
  private static List<List<Integer>> LOCAL = new ArrayList<>();
  private static int RANGE = 19;
  int judge = 1;
  private static Map<String, Boolean> isFlightMap = new HashMap<>();

  static {
    for (int i = 0; i < BOARD_SIZE + 1; i++) {
      for (int j = 0; j < BOARD_SIZE + 1; j++) {
        QI[i][j] = 'S';
        LOCAL.add(Arrays.asList(GRID_SIZE * i, GRID_SIZE * j));
      }
    }
  }

  public WeiQi() {
    setTitle("围棋棋盘");
    setBackground(new Color(228, 185, 150));
    getContentPane().setVisible(false);
    setSize(GRID_SIZE * BOARD_SIZE + 55, GRID_SIZE * BOARD_SIZE + 55);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);


    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        clickPoint = e.getPoint(); // 获取点击点
        repaint(); // 触发重绘事件
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    if (judge == 1) {
      //不重新刷新
      super.paint(g);
      for (int i = 0; i < BOARD_SIZE; i++) {
        g.drawLine(55, 55 + i * GRID_SIZE, 55 + GRID_SIZE * (BOARD_SIZE - 1), 55 + i * GRID_SIZE);
        g.drawLine(55 + i * GRID_SIZE, 55, 55 + i * GRID_SIZE, 55 + GRID_SIZE * (BOARD_SIZE - 1));
      }
      judge++;
    }
    if (clickPoint != null) {
      // 画一个黑色的圆，半径为10
      List<Integer> range = findRange(clickPoint.x, clickPoint.y, LOCAL, RANGE);
      if (!range.isEmpty()) {
        g.setColor(flag ? Color.BLACK : Color.WHITE);
        int x = (range.get(0) / GRID_SIZE);
        int y = (range.get(1) / GRID_SIZE);
        if (QI[x][y] != 'S') {
          return;
        }

        QI[x][y] = flag ? 'B' : 'W';
        //劫争


        boolean canPush = canPush(QI, x, y);
        if (!canPush) {
          QI[x][y] = 'S';
          return;
        }
        List<List<Integer>> disappear = disappear(QI, x, y);
        g.fillOval(range.get(0) - 20, range.get(1) - 20, 40, 40);
        for (List<Integer> list : disappear) {
          int x1 = list.get(0);
          int y1 = list.get(1);
          QI[x1][y1] = 'S';
          g.setColor(new Color(228, 185, 150));
          g.fillOval(x1 * GRID_SIZE - 20, y1 * GRID_SIZE - 20, 40, 40);
          g.setColor(Color.BLACK);
          g.drawLine(Math.max(x1 * GRID_SIZE - 20, 0), y1 * GRID_SIZE, Math.min(x1 * GRID_SIZE + 20, 55 + GRID_SIZE * (BOARD_SIZE - 1)), y1 * GRID_SIZE);
          g.drawLine(x1 * GRID_SIZE, Math.max(y1 * GRID_SIZE - 20, 0), x1 * GRID_SIZE, Math.min(y1 * GRID_SIZE + 20, 55 + GRID_SIZE * (BOARD_SIZE - 1)));
        }
        flag = !flag;
      }
    }

  }

  public static void main(String[] args) {
    WeiQi weiQi = new WeiQi();
  }

  public List<Integer> findRange(int x, int y, List<List<Integer>> board, int range) {
    for (List<Integer> list : board) {
      int x1 = list.get(0);
      int y1 = list.get(1);
      if (x1 - range < x && x1 + range > x && y1 - range < y && y1 + range > y) {
        return list;
      }
    }
    return new ArrayList<>();
  }

  public List<List<Integer>> disappear(char[][] board, int x, int y) {
    List<List<Integer>> result = new ArrayList<>();
    List<List<Integer>> list = new ArrayList<>();
    Queue<List<Integer>> queue = new ArrayDeque<>();
    boolean[][] flag = new boolean[board.length][board.length];
    if (x - 1 >= 0) {
      if (board[x - 1][y] != 'S' && board[x - 1][y] != board[x][y]) {
        queue.offer(Arrays.asList(x - 1, y));
        result.add(Arrays.asList(x - 1, y));
        flag[x - 1][y] = true;
      }
    }
    if (x + 1 <= RANGE - 1) {
      if (board[x + 1][y] != 'S' && board[x + 1][y] != board[x][y]) {
        queue.offer(Arrays.asList(x + 1, y));
        result.add(Arrays.asList(x + 1, y));
        flag[x + 1][y] = true;
      }
    }
    if (y - 1 >= 0) {
      if (board[x][y - 1] != 'S' && board[x][y - 1] != board[x][y]) {
        queue.offer(Arrays.asList(x, y - 1));
        result.add(Arrays.asList(x, y - 1));
        flag[x][y - 1] = true;
      }
    }
    if (y + 1 <= RANGE - 1) {
      if (board[x][y + 1] != 'S' && board[x][y + 1] != board[x][y]) {
        queue.offer(Arrays.asList(x, y + 1));
        result.add(Arrays.asList(x, y + 1));
        flag[x][y + 1] = true;
      }
    }
    for(List<Integer> one:result){
      list.addAll(oneDisappear(board,one.get(0),one.get(1)));
    };
    return list;
  }


  public boolean canPush(char[][] board, int x, int y) {
    Queue<List<Integer>> queue = new ArrayDeque<>();
    boolean[][] flag = new boolean[board.length][board.length];
    List<List<Integer>> result = new ArrayList<>();
    boolean up = false;
    boolean down = false;
    boolean right = false;
    boolean left = false;

    if (x - 1 >= 0) {
      if (board[x - 1][y] != 'S' && board[x - 1][y] != board[x][y]) {
        result.add(Arrays.asList(x - 1, y));
      } else if (board[x - 1][y] == 'S') {
        left =  true;
      } else {
        queue.offer(Arrays.asList(x - 1, y));
        flag[x - 1][y] = true;
      }
    }
    if (x + 1 <= RANGE - 1) {
      if (board[x + 1][y] != 'S' && board[x + 1][y] != board[x][y]) {
        result.add(Arrays.asList(x + 1, y));
      } else if (board[x + 1][y] == 'S') {
        right =  true;
      } else {
        queue.offer(Arrays.asList(x + 1, y));
        flag[x + 1][y] = true;
      }
    }
    if (y - 1 >= 0) {
      if (board[x][y - 1] != 'S' && board[x][y - 1] != board[x][y]) {
        result.add(Arrays.asList(x, y - 1));
      } else if (board[x][y - 1] == 'S') {
        down =  true;
      } else {
        queue.offer(Arrays.asList(x, y - 1));
        flag[x][y - 1] = true;
      }
    }
    if (y + 1 <= RANGE - 1) {
      if (board[x][y + 1] != 'S' && board[x][y + 1] != board[x][y]) {
        result.add(Arrays.asList(x, y + 1));
      } else if (board[x][y + 1] == 'S') {
        up =  true;
      } else {
        queue.offer(Arrays.asList(x, y + 1));
        flag[x][y + 1] = true;
      }
    }
    //自己的棋有气则可以落
    while (!queue.isEmpty()) {
      List<Integer> poll = queue.poll();
      int x1 = poll.get(0);
      int y1 = poll.get(1);
      if (x1 - 1 >= 0) {
        if (board[x1 - 1][y1] != 'S' && board[x1 - 1][y1] == board[x1][y1] && !flag[x1 - 1][y1]) {
          queue.offer(Arrays.asList(x1 - 1, y1));
          flag[x1 - 1][y1] = true;
        } else if (board[x1 - 1][y1] == 'S') {
          left =  true;
        }
      }
      if (x1 + 1 <= RANGE - 1) {
        if (board[x1 + 1][y1] != 'S' && board[x1 + 1][y1] == board[x1][y1] && !flag[x1 + 1][y1]) {
          queue.offer(Arrays.asList(x1 + 1, y1));
          flag[x1 + 1][y1] = true;
        } else if (board[x1 + 1][y1] == 'S') {
          right =  true;
        }
      }
      if (y1 - 1 >= 0) {
        if (board[x1][y1 - 1] != 'S' && board[x1][y1 - 1] == board[x1][y1] && !flag[x1][y1 - 1]) {
          queue.offer(Arrays.asList(x1, y1 - 1));
          flag[x1][y1 - 1] = true;
        } else if (board[x1][y1 - 1] == 'S') {
          down =  true;
        }
      }
      if (y1 + 1 <= RANGE - 1) {
        if (board[x1][y1 + 1] != 'S' && board[x1][y1 + 1] == board[x1][y1] && !flag[x1][y1 + 1]) {
          queue.offer(Arrays.asList(x1, y1 + 1));
          flag[x1][y1 + 1] = true;
        } else if (board[x1][y1 + 1] == 'S') {
          up =  true;
        }
      }
    }
    int count = 0;
    Map<Integer, String> map = new HashMap<>(4);
    for (List<Integer> list : result) {
      boolean oneJudge = oneJudge(board, list.get(0), list.get(1));
      if (!oneJudge) {
        count++;
        map.put(Integer.valueOf(count), list.get(0) + "-" + list.get(1));
      }
    }
    if (count == 1) {
      String xy = map.get(count);
      if (isFlightMap.getOrDefault(xy + "," + x + "-" + y, false)) {
        return false;
      }
      isFlightMap.clear();
      isFlightMap.put(x + "-" + y + "," + xy, true);
    }else{
      isFlightMap.clear();
    }

    return count != 0 ||up || down || left || right;
  }


  public  boolean oneJudge(char[][] board, int x, int y) {
    Queue<List<Integer>> queueAgainst = new ArrayDeque<>();
    boolean[][] flagAgainst = new boolean[board.length][board.length];
    queueAgainst.offer(Arrays.asList(x, y));
    // 相反的棋有气则不能落子
    while (!queueAgainst.isEmpty()) {
      List<Integer> poll = queueAgainst.poll();
      int x1 = poll.get(0);
      int y1 = poll.get(1);
      if (x1 - 1 >= 0) {
        if (board[x1 - 1][y1] != 'S' && board[x1 - 1][y1] == board[x1][y1] && !flagAgainst[x1 - 1][y1]) {
          queueAgainst.offer(Arrays.asList(x1 - 1, y1));
          flagAgainst[x1 - 1][y1] = true;
        } else if (board[x1 - 1][y1] == 'S') {
          return true;
        }
      }
      if (x1 + 1 <= RANGE - 1) {
        if (board[x1 + 1][y1] != 'S' && board[x1 + 1][y1] == board[x1][y1] && !flagAgainst[x1 + 1][y1]) {
          queueAgainst.offer(Arrays.asList(x1 + 1, y1));
          flagAgainst[x1 + 1][y1] = true;
        } else if (board[x1 + 1][y1] == 'S') {
          return true;
        }
      }
      if (y1 - 1 >= 0) {
        if (board[x1][y1 - 1] != 'S' && board[x1][y1 - 1] == board[x1][y1] && !flagAgainst[x1][y1 - 1]) {
          queueAgainst.offer(Arrays.asList(x1, y1 - 1));
          flagAgainst[x1][y1 - 1] = true;
        } else if (board[x1][y1 - 1] == 'S') {
          return true;
        }
      }
      if (y1 + 1 <= RANGE - 1) {
        if (board[x1][y1 + 1] != 'S' && board[x1][y1 + 1] == board[x1][y1] && !flagAgainst[x1][y1 + 1]) {
          queueAgainst.offer(Arrays.asList(x1, y1 + 1));
          flagAgainst[x1][y1 + 1] = true;
        } else if (board[x1][y1 + 1] == 'S') {
          return true;
        }
      }
    }
    return false;
  }

  public  List<List<Integer>> oneDisappear(char[][] board, int x, int y) {
    Queue<List<Integer>> queueAgainst = new ArrayDeque<>();
    boolean[][] flagAgainst = new boolean[board.length][board.length];
    List<List<Integer>> result = new ArrayList<>();
    result.add(Arrays.asList(x, y));
    queueAgainst.offer(Arrays.asList(x, y));
    // 相反的棋有气则不能落子
    while (!queueAgainst.isEmpty()) {
      List<Integer> poll = queueAgainst.poll();
      int x1 = poll.get(0);
      int y1 = poll.get(1);
      if (x1 - 1 >= 0) {
        if (board[x1 - 1][y1] != 'S' && board[x1 - 1][y1] == board[x1][y1] && !flagAgainst[x1 - 1][y1]) {
          queueAgainst.offer(Arrays.asList(x1 - 1, y1));
          result.add(Arrays.asList(x1 - 1, y1));
          flagAgainst[x1 - 1][y1] = true;
        } else if (board[x1 - 1][y1] == 'S') {
          return new ArrayList<List<Integer>>();
        }
      }
      if (x1 + 1 <= RANGE - 1) {
        if (board[x1 + 1][y1] != 'S' && board[x1 + 1][y1] == board[x1][y1] && !flagAgainst[x1 + 1][y1]) {
          queueAgainst.offer(Arrays.asList(x1 + 1, y1));
          result.add(Arrays.asList(x1 + 1, y1));
          flagAgainst[x1 + 1][y1] = true;
       } else if (board[x1 + 1][y1] == 'S') {
          return new ArrayList<List<Integer>>();
        }
      }
      if (y1 - 1 >= 0) {
        if (board[x1][y1 - 1] != 'S' && board[x1][y1 - 1] == board[x1][y1] && !flagAgainst[x1][y1 - 1]) {
          queueAgainst.offer(Arrays.asList(x1, y1 - 1));
          result.add(Arrays.asList(x1, y1 - 1));
          flagAgainst[x1][y1 - 1]=true;
        } else if (board[x1][y1 - 1] == 'S') {
          return new ArrayList<List<Integer>>();
        }
      }
      if (y1 + 1 <= RANGE - 1) {
        if (board[x1][y1 + 1] != 'S' && board[x1][y1 + 1] == board[x1][y1] && !flagAgainst[x1][y1 + 1]) {
          result.add(Arrays.asList(x1, y1 + 1));
          queueAgainst.offer(Arrays.asList(x1, y1 + 1));
          flagAgainst[x1][y1 + 1] = true;
        } else if (board[x1][y1 + 1] == 'S') {
          return new ArrayList<List<Integer>>();
        }
      }
    }
    return result;
  }

}
