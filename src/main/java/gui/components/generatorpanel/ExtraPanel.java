package gui.components.generatorpanel;

import data.Strings;
import gui.SeedCandy;
import gui.components.TextBlock;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.mc.MCVersion;
import swing.components.ButtonSet;
import swing.components.GridPanel;
import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtraPanel extends JPanel { // TODO: Locate structure, Locate biome, Clear input/output
    public TextBlock inputText;
    public TextBlock outputText;
    public GenPanel genPanel;

    public ExtraPanel() {
        inputText = new TextBlock(true); // Center
        outputText = new TextBlock(false); // East
        JPanel inputPanel = new JPanel(new BorderLayout()); // West

        //Input panel
        genPanel = new GenPanel();
        JPanel checkPanel = new CheckPanel();

        inputPanel.add(genPanel, BorderLayout.NORTH);
        inputPanel.add(checkPanel, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.setName("Extra");

        this.add(inputPanel, BorderLayout.WEST);
        this.add(inputText, BorderLayout.CENTER);
        this.add(outputText, BorderLayout.EAST);
    }

    private class CheckPanel extends JPanel {

        private CheckPanel() {
            this.setLayout(new BorderLayout());

            SelectionBox<Biome> spawnBiomeSelector = new SelectionBox<>(Biome::getName, Biome.REGISTRY.values());
            GridPanel<BiomeUnit> biomesSelectorPanel = new GridPanel<>(1, 5, BiomeUnit::new);

            JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
            ButtonSet<JButton> buttonSet = new ButtonSet<>(JButton::new,
                    "check spawn", "check biomes"
            );

            buttonSet.addListeners(
                    spawnButton -> {
                        outputText.setText("");
                        getProgressBar().setValue(0);
                        getProgressBar().setMaximum(Strings.countLines(inputText.getText()));

                        AtomicInteger progress = new AtomicInteger(0);
                        SeedCandy.POOL.execute(Strings.splitToLongs(inputText.getText()), seed -> {
                            getProgressBar().setValue(progress.incrementAndGet());
                            OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                            boolean matches = true;

                            if (spawnBiomeSelector.getSelected() != biomeSource.getBiome(biomeSource.getSpawnPoint().getX(), 0, biomeSource.getSpawnPoint().getZ()) &&
                                    spawnBiomeSelector.getSelected() != Biome.THE_VOID) matches = false;

                            if (matches) SwingUtilities.invokeLater(() -> outputText.addEntry(String.valueOf(seed)));
                        });
                    },
                    biomesButton -> {
                        outputText.setText("");
                        getProgressBar().setValue(0);
                        getProgressBar().setMaximum(Strings.countLines(inputText.getText()));

                        AtomicInteger progress = new AtomicInteger(0);
                        SeedCandy.POOL.execute(Strings.splitToLongs(inputText.getText()), seed -> {
                            getProgressBar().setValue(progress.incrementAndGet());
                            OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                            boolean matches = true;

                            for (int i = 0; i < 5; ++i) {
                                if (!biomesSelectorPanel.getComponent(0, i).matches(biomeSource)) {
                                    matches = false;
                                    break;
                                }
                            }

                            if (matches) SwingUtilities.invokeLater(() -> outputText.addEntry(String.valueOf(seed)));
                        });
                    }
            );

            buttonSet.addAll(buttonPanel);
            this.add(spawnBiomeSelector, BorderLayout.NORTH);
            this.add(biomesSelectorPanel, BorderLayout.CENTER);
            this.add(buttonPanel, BorderLayout.SOUTH);
        }

        private JProgressBar getProgressBar() {
            return SeedCandy.INSTANCE.extraPanel.genPanel.progressBar;
        }
    }
}
