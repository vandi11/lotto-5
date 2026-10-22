package hu.vandi.lotto;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Desktop five-number lottery simulator built with Swing. */
public final class DesktopLottoApp {
    private static final int MAX_PICKS = 5;
    private static final Color BACKGROUND = new Color(245, 248, 239);
    private static final Color CARD = Color.WHITE;
    private static final Color GREEN = new Color(103, 145, 33);
    private static final Color DARK_GREEN = new Color(49, 78, 18);
    private static final Color MUTED = new Color(103, 111, 93);

    private final List<JCheckBox> numberBoxes = new ArrayList<>(90);
    private final JButton drawButton = new DrawButton("Sorsol");
    private final JLabel selectionCount = new JLabel("Kiválasztva: 0 / 5");
    private final JLabel drawnNumbers = new JLabel("—", SwingConstants.CENTER);
    private final JLabel hitCount = new JLabel("Válasszon öt számot, majd indítsa a sorsolást.");

    private DesktopLottoApp() {
        createAndShowWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new DesktopLottoApp();
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing's cross-platform look and feel remains available as a fallback.
        }
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("Ötöslottó – sorsolás");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 720));
        frame.setSize(960, 820);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(buildContent(frame));
        frame.setVisible(true);
    }

    private JPanel buildContent(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildPickerCard());
        content.add(Box.createVerticalStrut(16));
        content.add(buildResultCard());
        root.add(content, BorderLayout.CENTER);
        root.add(buildActions(frame), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("Ötöslottó");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        JLabel subtitle = new JLabel("Válasszon ki öt számot 1 és 90 között.");
        subtitle.setForeground(new Color(239, 247, 222));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(title);
        labels.add(Box.createVerticalStrut(4));
        labels.add(subtitle);
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private JPanel buildPickerCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 14));

        JPanel cardHeading = new JPanel(new BorderLayout());
        cardHeading.setOpaque(false);
        JLabel heading = new JLabel("Saját számok");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        heading.setForeground(DARK_GREEN);
        selectionCount.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        selectionCount.setForeground(GREEN);
        cardHeading.add(heading, BorderLayout.WEST);
        cardHeading.add(selectionCount, BorderLayout.EAST);
        card.add(cardHeading, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(9, 10, 7, 7));
        grid.setOpaque(false);
        for (int number = 1; number <= 90; number++) {
            JCheckBox box = new JCheckBox(Integer.toString(number));
            box.setHorizontalAlignment(SwingConstants.CENTER);
            box.setOpaque(true);
            box.setBackground(BACKGROUND);
            box.setForeground(DARK_GREEN);
            box.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            box.setMargin(new Insets(5, 4, 5, 4));
            box.setFocusPainted(false);
            box.addActionListener(event -> selectionChanged());
            numberBoxes.add(box);
            grid.add(box);
        }
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 9));

        JLabel heading = new JLabel("Sorsolás eredménye");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        heading.setForeground(DARK_GREEN);
        drawnNumbers.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        drawnNumbers.setForeground(GREEN);
        hitCount.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        hitCount.setForeground(MUTED);

        card.add(heading, BorderLayout.NORTH);
        card.add(drawnNumbers, BorderLayout.CENTER);
        card.add(hitCount, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildActions(JFrame frame) {
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);

        JLabel hint = new JLabel("Öt szám megjelölése után a sorsolás indítható.");
        hint.setForeground(MUTED);
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        actions.add(hint, BorderLayout.WEST);

        drawButton.setEnabled(false);
        drawButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        drawButton.setFocusPainted(false);
        drawButton.setBorder(BorderFactory.createEmptyBorder(11, 24, 11, 24));
        drawButton.addActionListener(event -> draw());

        JButton closeButton = new JButton("Bezár");
        closeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        closeButton.addActionListener(event -> frame.dispose());

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(drawButton);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(closeButton);
        actions.add(buttons, BorderLayout.EAST);
        return actions;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 205)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    /** Paints enabled and disabled states directly so native look-and-feels cannot hide the label. */
    private static final class DrawButton extends JButton {
        private DrawButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean enabled = isEnabled();
            boolean pressed = getModel().isPressed() && getModel().isArmed();
            boolean hovered = getModel().isRollover();
            Color background = !enabled ? new Color(224, 228, 216)
                    : pressed ? new Color(65, 101, 23)
                    : hovered ? new Color(84, 125, 25) : GREEN;
            Color foreground = enabled ? Color.WHITE : new Color(137, 143, 126);

            g.setColor(background);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.setFont(getFont());
            FontMetrics metrics = g.getFontMetrics();
            g.setColor(foreground);
            int textX = (getWidth() - metrics.stringWidth(getText())) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(getText(), textX, textY);
            g.dispose();
        }
    }

    private void selectionChanged() {
        int selected = selectedNumbers().size();
        selectionCount.setText("Kiválasztva: " + selected + " / " + MAX_PICKS);
        boolean full = selected == MAX_PICKS;
        for (JCheckBox box : numberBoxes) {
            box.setEnabled(!full || box.isSelected());
        }
        drawButton.setEnabled(full);
        if (!full) {
            drawnNumbers.setText("—");
            hitCount.setText("Válasszon öt számot, majd indítsa a sorsolást.");
        }
    }

    private Set<Integer> selectedNumbers() {
        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < numberBoxes.size(); i++) {
            if (numberBoxes.get(i).isSelected()) selected.add(i + 1);
        }
        return selected;
    }

    private void draw() {
        List<Integer> numbers = new ArrayList<>(90);
        for (int i = 1; i <= 90; i++) numbers.add(i);
        Collections.shuffle(numbers);
        List<Integer> result = numbers.subList(0, MAX_PICKS).stream().sorted().toList();

        Set<Integer> matched = new HashSet<>(selectedNumbers());
        matched.retainAll(result);
        drawnNumbers.setText(result.stream().map(String::valueOf).reduce((a, b) -> a + "     " + b).orElse(""));
        int hits = matched.size();
        hitCount.setText("Eltalált számok: " + hits + " találat");
    }
}
