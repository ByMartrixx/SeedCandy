package gui.components.generatorpanel;

import data.Strings;
import gui.SeedCandy;
import swing.SwingUtils;
import swing.components.ButtonSet;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GenPanel extends JPanel {
    public JProgressBar progressBar;
    public JTextField nField;
    public JTextField wordField;
    public HashCoding hashCoding;

    public GenPanel() {
        HashCoding.init();
        this.hashCoding = new HashCoding();
        this.setLayout(new BorderLayout());

        JPanel selectionPanel = new JPanel();
        progressBar = new JProgressBar(0, 1);
        nField = new JTextField();
        wordField = new JTextField();

        nField.setPreferredSize(new Dimension(50, 25));
        SwingUtils.setPrompt("number", nField);
        nField.setText("100");
        wordField.setPreferredSize(new Dimension(50, 25));
        SwingUtils.setPrompt("word", wordField);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
        ButtonSet<JButton> buttonSet = new ButtonSet<>(JButton::new,
                "generate", "move",
                "copy text", "to letters"
        );

        buttonSet.addListeners(
                generateButton -> {
                    reset();

                    Random random = new Random();

                    int n = Integer.parseInt(nField.getText());
                    progressBar.setMaximum(n);
                    AtomicInteger progress = new AtomicInteger(0);

                    if (Boolean.parseBoolean(wordField.getText())) {
                        SeedCandy.POOL.execute(random.ints(n), seed -> {
                            progressBar.setValue(progress.incrementAndGet());
                            SwingUtilities.invokeLater(() -> addSeed(seed));
                        });
                    } else {
                        SeedCandy.POOL.execute(random.longs(n), seed -> {
                            progressBar.setValue(progress.incrementAndGet());
                            SwingUtilities.invokeLater(() -> addSeed(seed));
                        });
                    }
                },
                moveButton -> {
                    SeedCandy.INSTANCE.worldPanel.inputText.setText(getText());
                    SeedCandy.INSTANCE.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.worldPanel);
                },
                copyButton -> Strings.clipboard(getText()),
                lettersButton -> {
                    resetOutput();
                    progressBar.setMaximum(Strings.countLines(getInputText()));

                    AtomicInteger progress = new AtomicInteger(0);
                    SeedCandy.POOL.execute(Strings.splitLines(getInputText()), line -> {
                        progressBar.setValue(progress.incrementAndGet());
                        try {
                            int seedAsHash = Integer.parseInt(line);
                            String seedAsStr = hashCoding.getStrFromHash(seedAsHash);
                            SwingUtilities.invokeLater(() -> SeedCandy.INSTANCE.extraPanel.outputText.addEntry(
                                    String.format("%d\n -> %s", seedAsHash, seedAsStr)));
                        } catch (NumberFormatException e) {
                            SwingUtilities.invokeLater(() -> SeedCandy.INSTANCE.extraPanel.outputText.addEntry(
                                    String.format("%s\n -> cannot be converted", line)));
                        }
                    });
                }
        );

        buttonSet.addAll(buttonPanel);
        selectionPanel.add(nField);
        selectionPanel.add(wordField);
        selectionPanel.add(progressBar);
        this.add(selectionPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
    }

    private void reset() {
        SeedCandy.INSTANCE.extraPanel.inputText.setText("");
        resetOutput();
    }

    private void resetOutput() {
        progressBar.setValue(0);
        progressBar.setMaximum(1);
        SeedCandy.INSTANCE.extraPanel.outputText.setText("");
    }

    private void addSeed(long seed) {
        SeedCandy.INSTANCE.extraPanel.inputText.addEntry(String.format("%d", seed));
    }

    private String getText() {
        return !SeedCandy.INSTANCE.extraPanel.outputText.getText().equals("") ? SeedCandy.INSTANCE.extraPanel.outputText.getText() : getInputText();
    }

    private String getInputText() {
        return SeedCandy.INSTANCE.extraPanel.inputText.getText();
    }

    private static class HashCoding {
        private boolean simpleSeed = true;
        private static final int minChar = 65;
        private static final int middleMinChar = 91;
        private static final int middleMaxChar = 96;
        private static final int maxChar = 122;
        private static final int maxStrLength = 12;
        private static long[] minHashValues;
        private static long[] maxHashValues;

        static void init() {
            HashCoding.minHashValues = new long[13];
            for (int exponent = 0; exponent <= 12; ++exponent)
                HashCoding.minHashValues[exponent] = ((long) Math.pow(31, exponent)) / 30L * minChar;
            HashCoding.maxHashValues = new long[13];
            for (int exponent = 0; exponent <= 12; ++exponent)
                HashCoding.maxHashValues[exponent] = ((long) Math.pow(31, exponent)) / 30L * maxChar;
        }

        public String getStrFromHash(int hash) { // I don't have any clue on how this works
            long hash1 = ((long) (Integer) hash);
            long num = 4294967296L;
            String result = "";

            for (int length = 0; length <= maxStrLength; ++length) {
                long maxHashValue = HashCoding.maxHashValues[length];

                if (hash1 <= maxHashValue) {
                    long minHashValue = HashCoding.minHashValues[length];

                    while (hash1 < minHashValue)
                        hash1 += num;

                    for (; hash1 <= maxHashValue; hash1 += num) {
                        this.simpleSeed = true;
                        result = this.getStrFromLongHash(hash1, new char[length], length - 1);
                        if (!result.equals("")) return result;
                    }
                }
            }
            return result;
        }

        private String getStrFromLongHash(long hash, char[] chars, int charPos) {
            if (hash <= maxChar) {
                if (hash < minChar || hash >= middleMinChar && hash <= middleMaxChar)
                    return "";
                char ch1 = (char) hash;
                chars[charPos] = ch1;
                for (char ch2 : chars) {
                    if ((ch2 < 'A' || ch2 > 'Z') && (ch2 < 'a' || ch2 > 'z')) {
                        this.simpleSeed = false;
                        break;
                    }
                }
                if (this.simpleSeed)
                    return new String(chars);
            }
            else {
                char ch = (char) ((Long) hash % new Long(31L));
                while (ch < 'A')
                    ch += '\u001F';
                for (; ch <= 'z' && (charPos != chars.length - 1 || (ch < '[' || ch > '`')); ch += '\u001F') { // I think this loop isn't needed
                    chars[charPos] = ch;
                    return this.getStrFromLongHash( (hash - (long) ch) / 31L, chars, charPos - 1);
                }
            }
            return "";
        }
    }
}
