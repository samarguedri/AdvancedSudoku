package advancedsudoku;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class AdvancedSudoku {
    private static final int GRID_SIZE = 9;
    private static int[][] grid;
    private static JTextField[][] textFields;
    private static JFrame frame;
    private static Timer timer;
    private static int timeElapsed; // Temps écoulé en secondes
    private static JLabel timerLabel;
    private static int score = 0; // Initialisation du score
    private static JLabel scoreLabel;
    private static String currentLevel = "Facile"; // Niveau de difficulté actuel
    private static String userName = "Joueur"; // Nom de l'utilisateur

    public static void main(String[] args) {
        // Demander le nom de l'utilisateur
        userName = JOptionPane.showInputDialog(null, "Entrez votre nom :", "Bienvenue dans Sudoku Avancé", JOptionPane.QUESTION_MESSAGE);

        if (userName == null || userName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nom non valide. Le jeu sera fermé.");
            System.exit(0);
        }

        // Enregistrer le joueur dans la base de données
        DatabaseManager.savePlayer(userName);

        // Afficher une boîte de dialogue pour choisir le niveau
        String[] levels = {"Facile", "Moyen", "Difficile"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choisissez le niveau de difficulté",
                "Niveau de difficulté",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, levels, levels[0]);

        if (choice != -1) {
            currentLevel = levels[choice];
            initializeUI();
        } else {
            JOptionPane.showMessageDialog(null, "Aucun niveau sélectionné. Le jeu sera fermé.");
            System.exit(0);
        }
    }

    private static void initializeUI() {
        frame = new JFrame("Sudoku Avancé - Joueur : " + userName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        // Barre de menu
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Jeu");
        JMenuItem newGameItem = new JMenuItem("Nouvelle partie");
        JMenuItem exitItem = new JMenuItem("Quitter");

        gameMenu.add(newGameItem);
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);

        // Afficher le nom de l'utilisateur dans la barre de menu
        JLabel userLabel = new JLabel("Utilisateur : " + userName);
        menuBar.add(Box.createHorizontalGlue()); // Aligner à droite
        menuBar.add(userLabel);

        frame.setJMenuBar(menuBar);

        // Panneau principal
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        textFields = new JTextField[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField textField = new JTextField();
                textField.setHorizontalAlignment(JTextField.CENTER);
                textField.setFont(new Font("Arial", Font.BOLD, 18));
                textFields[row][col] = textField;
                gridPanel.add(textField);
            }
        }

        // Panneau du bas (boutons et labels)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton checkButton = new JButton("Vérifier");
        JButton resetButton = new JButton("Réinitialiser"); // Nouveau bouton Réinitialiser

        checkButton.addActionListener(e -> validateSolution());
        resetButton.addActionListener(e -> resetGame()); // Action pour réinitialiser la partie

        timerLabel = new JLabel("Temps écoulé : 0s", JLabel.CENTER);
        scoreLabel = new JLabel("Score : 0", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        bottomPanel.add(checkButton, BorderLayout.CENTER);
        bottomPanel.add(resetButton, BorderLayout.EAST); // Ajout du bouton Réinitialiser
        bottomPanel.add(timerLabel, BorderLayout.NORTH);
        bottomPanel.add(scoreLabel, BorderLayout.SOUTH);

        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        startNewGame();
        frame.setVisible(true);
    }

    private static void startNewGame() {
        int cellsToFill;
        switch (currentLevel) {
            case "Moyen":
                cellsToFill = 35;
                break;
            case "Difficile":
                cellsToFill = 25;
                break;
            default:
                cellsToFill = 45;
        }

        grid = generateRandomSudoku(cellsToFill);
        resetGrid();
        startTimer();
        score = 0;
        scoreLabel.setText("Score : " + score);
    }

    private static void resetGame() {
        stopTimer(); // Arrêter le chronomètre
        score = 0; // Réinitialiser le score
        scoreLabel.setText("Score : " + score); // Afficher le score réinitialisé
        DatabaseManager.updateScore(userName, score); // Mettre à jour le score réinitialisé dans la base de données
        startNewGame(); // Recommencer une nouvelle partie
    }

    private static void resetGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = grid[row][col];
                JTextField textField = textFields[row][col];
                if (value != 0) {
                    textField.setText(String.valueOf(value));
                    textField.setEditable(false);
                    textField.setBackground(Color.LIGHT_GRAY);
                } else {
                    textField.setText("");
                    textField.setEditable(true);
                    textField.setBackground(Color.WHITE);
                }
            }
        }
    }

    private static void validateSolution() {
        boolean hasErrors = false;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField textField = textFields[row][col];
                String text = textField.getText();

                if (!text.isEmpty()) {
                    try {
                        int value = Integer.parseInt(text);

                        if (!isSafe(row, col, value, grid)) {
                            textField.setBackground(Color.RED);
                            score -= 5;
                            hasErrors = true;
                        } else {
                            textField.setBackground(Color.WHITE);
                            score += 10;
                        }
                    } catch (NumberFormatException ex) {
                        textField.setBackground(Color.RED);
                        hasErrors = true;
                        score -= 5;
                    }
                }
            }
        }

        scoreLabel.setText("Score : " + score);

        // Mise à jour du score dans la base de données après chaque modification
        DatabaseManager.updateScore(userName, score); // Ajouter cette ligne

        if (!hasErrors) {
            stopTimer();
            JOptionPane.showMessageDialog(frame, "Félicitations ! Vous avez résolu le Sudoku !");

            // Enregistrer le score final dans la base de données à la fin du jeu
            DatabaseManager.updateScore(userName, score);
        }
    }

    private static void startTimer() {
        timeElapsed = 0;
        timerLabel.setText("Temps écoulé : " + timeElapsed + "s");

        timer = new Timer(1000, e -> {
            timeElapsed++;
            timerLabel.setText("Temps écoulé : " + timeElapsed + "s");
        });
        timer.start();
    }

    private static void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private static int[][] generateRandomSudoku(int cellsToFill) {
        int[][] newGrid = new int[GRID_SIZE][GRID_SIZE];
        Random rand = new Random();
        for (int i = 0; i < cellsToFill; i++) {
            int row = rand.nextInt(GRID_SIZE);
            int col = rand.nextInt(GRID_SIZE);
            int num = rand.nextInt(9) + 1;
            if (newGrid[row][col] == 0 && isSafe(row, col, num, newGrid)) {
                newGrid[row][col] = num;
            } else {
                i--;
            }
        }
        return newGrid;
    }

    private static boolean isSafe(int row, int col, int value, int[][] grid) {
        for (int c = 0; c < GRID_SIZE; c++) {
            if (grid[row][c] == value) {
                return false;
            }
        }
        for (int r = 0; r < GRID_SIZE; r++) {
            if (grid[r][col] == value) {
                return false;
            }
        }
        int startRow = row - row % 3, startCol = col - col % 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (grid[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }
}

