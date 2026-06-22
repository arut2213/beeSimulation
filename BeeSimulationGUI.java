import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class BeeSimulationGUI extends JFrame {
    private Hive hive;
    private JTextArea textArea;
    private JPanel controlPanel;
    private JLabel statusLabel;

    public BeeSimulationGUI() {
        hive = new Hive();
        boolean loaded = hive.load();
        if (!loaded) {
            hive.initializeHive();
        }
        initUI();
    }

    private void initUI() {
        setTitle("СИМУЛЯЦИЯ ПЧЕЛИНОГО УЛЬЯ");
        setSize(1000, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        updateStatusLabel();

        JLabel menuTitle = new JLabel("Меню");
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 13));

        String[] menuItems = {
                "1. Сделать 1 шаг",
                "2. Сделать N шагов",
                "3. Показать всех пчёл",
                "4. Статистика улья",
                "5. Сохранить и выйти"
        };
        JComboBox<String> menuCombo = new JComboBox<>(menuItems);
        menuCombo.setMaximumSize(new Dimension(280, 32));
        menuCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(statusLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(menuTitle);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        menuPanel.add(menuCombo);
        menuPanel.add(Box.createVerticalGlue());

        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        outputPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(outputPanel, BorderLayout.CENTER);

        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        menuCombo.addActionListener(e -> {
            switch (menuCombo.getSelectedIndex()) {
                case 0:
                    showStep1Card();
                    break;
                case 1:
                    showStepNCard();
                    break;
                case 2:
                    showBeesCard();
                    break;
                case 3:
                    showStatsCard();
                    break;
                case 4:
                    showSaveCard();
                    break;
            }
        });

        showStep1Card();
    }

    private void setControl(String title, JComponent body) {
        controlPanel.removeAll();
        JLabel header = new JLabel(title);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 15f));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        controlPanel.add(header, BorderLayout.NORTH);
        controlPanel.add(body, BorderLayout.CENTER);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(220, 25));
        p.add(l);
        p.add(field);
        return p;
    }

    private JButton actionButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void showStep1Card() {
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.add(new JLabel("Выполнить один шаг симуляции: "));
        body.add(actionButton("Сделать шаг", () -> {
            hive.simulationStep();
            textArea.setText("Шаг " + hive.getCurrentStep() + " выполнен.\n");
            updateStatusLabel();
        }));
        setControl("Шаг симуляции", body);
    }

    private void showStepNCard() {
        String[] presets = { "1", "5", "10", "25", "50", "100" };
        JComboBox<String> stepsCombo = new JComboBox<>(presets);
        stepsCombo.setEditable(true);
        stepsCombo.setSelectedItem("10");

        JButton run = actionButton("Выполнить", () -> {
            String s = String.valueOf(stepsCombo.getEditor().getItem()).trim();
            int n;
            try {
                n = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                textArea.setText("Ошибка: введите целое число!\n");
                return;
            }
            if (n <= 0) {
                textArea.setText("Ошибка: число должно быть положительным!\n");
                return;
            }
            for (int i = 0; i < n; i++) {
                hive.simulationStep();
            }
            textArea.setText("Выполнено шагов: " + n + ". Текущий шаг: " + hive.getCurrentStep() + "\n");
            updateStatusLabel();
        });

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(row("Сколько шагов:", stepsCombo));
        body.add(row("", run));
        setControl("Несколько шагов симуляции", body);
    }

    private void showBeesCard() {
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.add(new JLabel("Список всех пчёл улья: "));
        body.add(actionButton("Обновить", () -> runWithRedirect(() -> hive.showAllBees())));
        setControl("Все пчёлы", body);
        runWithRedirect(() -> hive.showAllBees());
    }

    private void showStatsCard() {
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.add(new JLabel("Статистика по улью: "));
        body.add(actionButton("Обновить", () -> runWithRedirect(() -> hive.showStatistics())));
        setControl("Статистика улья", body);
        runWithRedirect(() -> hive.showStatistics());
    }

    private void showSaveCard() {
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.add(new JLabel("Сохранить состояние улья и закрыть программу: "));
        body.add(actionButton("Сохранить и выйти", () -> {
            hive.save();
            System.exit(0);
        }));
        setControl("Сохранение и выход", body);
    }

    private void updateStatusLabel() {
        double honey = Math.round(hive.getHoneyStock() * 100.0) / 100.0;
        statusLabel.setText("Шаг: " + hive.getCurrentStep() + " | Мёд: " + honey);
    }

    private void runWithRedirect(Runnable r) {
        textArea.setText("");
        PrintStream ps = new PrintStream(new TextAreaOutputStream(textArea));
        PrintStream old = System.out;
        System.setOut(ps);
        r.run();
        System.setOut(old);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BeeSimulationGUI().setVisible(true));
    }
}
