import java.io.*;
import java.util.*;

public class Main {
    private static final String CONFIG_FILE = "config.txt";
    private static final String STATS_FILE = "stats.txt";

    private static GameConfig config = new GameConfig();
    private static Scanner scanner = new Scanner(System.in);
    private static GameBoard board;

    public static void main(String[] args) {
        loadConfig();
        gameMenu();
    }

    private static void gameMenu() {
        boolean run = true;
        while (run) {
            System.out.println("\nГоловне меню:");
            System.out.println("1. Грати");
            System.out.println("2. Налаштування");
            System.out.println("3. Статистика");
            System.out.println("4. Вихід");
            String input = scanner.nextLine();

            switch (input) {
                case "1": startGame(); break;
                case "2": changeSettings(); break;
                case "3": showStats(); break;
                case "4": run = false; break;
                default: System.out.println("Невірний вибір.");
            }
        }
    }

    private static void changeSettings() {
        System.out.println("Оберiть розмiр гри: 3 (1), 5 (2), 7 (3), 9 (4)");
        switch (scanner.nextLine()) {
            case "1": config.size = 3; break;
            case "2": config.size = 5; break;
            case "3": config.size = 7; break;
            case "4": config.size = 9; break;
            default: System.out.println("Невірний вибір.");
        }
        System.out.print("Iм'я гравця X: ");
        config.playerX = scanner.nextLine();
        System.out.print("Iм'я гравця O: ");
        config.playerO = scanner.nextLine();
        saveConfig();
    }

    private static void startGame() {
        board = new GameBoard(config.size);
        char player = 'X';
        int moves = 0;

        while (true) {
            board.print();
            if (!playerMove(player)) continue;
            moves++;
            if (board.checkWin(player)) {
                board.print();
                System.out.println("Переможець: " + (player == 'X' ? config.playerX : config.playerO));
                saveStat(new GameStat(new Date(), (player == 'X' ? config.playerX : config.playerO), config.size));
                return;
            }
            if (moves == config.size * config.size) {
                System.out.println("Нiчия");
                saveStat(new GameStat(new Date(), "Нічия", config.size));
                return;
            }
            player = (player == 'X') ? 'O' : 'X';
        }
    }

    private static boolean playerMove(char player) {
        System.out.println("Хiд: " + (player == 'X' ? config.playerX : config.playerO) + " (" + player + ")");
        System.out.print("Введiть рядок (1-" + config.size + "): ");
        int row = getIntInput();
        System.out.print("Введiть стовпець (1-" + config.size + "): ");
        int col = getIntInput();

        if (!board.setMove(row, col, player)) {
            System.out.println("Неправильний хiд.");
            return false;
        }
        return true;
    }

    private static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    private static void saveConfig() {
        try {
            FileWriter w = new FileWriter(CONFIG_FILE);
            w.write(config.size + "\n" + config.playerX + "\n" + config.playerO + "\n");
            w.close();
        } catch (IOException e) {
            System.out.println("Помилка збереження конфігурації.");
        }
    }

    private static void loadConfig() {
        try {
            BufferedReader r = new BufferedReader(new FileReader(CONFIG_FILE));
            config.size = Integer.parseInt(r.readLine());
            config.playerX = r.readLine();
            config.playerO = r.readLine();
            r.close();
        } catch (Exception ignored) {
        }
    }

    private static void saveStat(GameStat stat) {
        try {
            FileWriter w = new FileWriter(STATS_FILE, true);
            w.write(stat.date + " | " + stat.winner + " | " + stat.size + "x" + stat.size + "\n");
            w.close();
        } catch (IOException e) {
            System.out.println("Не вдалося зберегти статистику.");
        }
    }

    private static void showStats() {
        try {
            BufferedReader r = new BufferedReader(new FileReader(STATS_FILE));
            String line;
            System.out.println("Історія ігор:");
            while ((line = r.readLine()) != null) System.out.println(line);
            r.close();
        } catch (IOException e) {
            System.out.println("Немає статистики.");
        }
    }
}

class GameConfig {
    public int size = 3;
    public String playerX = "Гравець X";
    public String playerO = "Гравець O";
}

class GameStat {
    public Date date;
    public String winner;
    public int size;

    public GameStat(Date date, String winner, int size) {
        this.date = date;
        this.winner = winner;
        this.size = size;
    }
}

class GameBoard {
    private char[][] board;
    private int size;

    public GameBoard(int size) {
        this.size = size;
        board = new char[size * 2 - 1][size * 2 - 1];
        initBoard();
    }

    private void initBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = (i % 2 == 0 && j % 2 == 0) ? ' ' : (i % 2 == 0 ? '|' : (j % 2 == 0 ? '-' : '+'));
            }
        }
    }

    public void print() {
        for (char[] row : board) {
            for (char cell : row) System.out.print(cell + " ");
            System.out.println();
        }
    }

    public boolean setMove(int row, int col, char player) {
        if (row < 1 || row > size || col < 1 || col > size || board[(row - 1) * 2][(col - 1) * 2] != ' ') return false;
        board[(row - 1) * 2][(col - 1) * 2] = player;
        return true;
    }

    public boolean checkWin(char player) {
        for (int i = 0; i < size; i++) {
            boolean row = true, col = true;
            for (int j = 0; j < size; j++) {
                if (board[i * 2][j * 2] != player) row = false;
                if (board[j * 2][i * 2] != player) col = false;
            }
            if (row || col) return true;
        }
        boolean diag1 = true, diag2 = true;
        for (int i = 0; i < size; i++) {
            if (board[i * 2][i * 2] != player) diag1 = false;
            if (board[i * 2][(size - 1 - i) * 2] != player) diag2 = false;
        }
        return diag1 || diag2;
    }
}
