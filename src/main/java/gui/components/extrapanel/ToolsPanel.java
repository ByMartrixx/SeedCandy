package gui.components.extrapanel;

import data.Strings;
import gui.MoveButtonPanel;
import gui.SeedCandy;
import gui.components.BiomeUnit;
import gui.components.TextBlock;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.seed.WorldSeed;
import swing.components.ButtonSet;
import swing.components.GridPanel;
import swing.components.SelectionBox;
import util.HashCoding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

public class ToolsPanel extends JPanel {
    public HashCoding hash;
    public JProgressBar progressBar;
    public TextBlock inputText;
    public TextBlock outputText;

    public ToolsPanel() { // TODO: Version selector
        // HashCoding
        HashCoding.init();
        this.hash = new HashCoding();

        // Initialize
        this.progressBar = new JProgressBar(0, 1);
        this.inputText = new TextBlock(true);
        this.outputText = new TextBlock(false);
        JPanel selectionPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Biome Selection
        GridPanel<BiomeUnit> biomeSelectorPanel = new GridPanel<>(1, 7, () -> new BiomeUnit(true, true));
        SelectionBox<Biome> spawnBiomeSelector = new SelectionBox<>(Biome::getName, Biome.REGISTRY.values());

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
        ButtonSet<JButton> buttonSet = new ButtonSet<>(JButton::new,
                "to String", "to int",
                "get ShadowSeed", "get Spawn Biome",
                "check Biomes", "copy Output",
                "clear Output","clear"
        );
        MoveButtonPanel<Tabs> moveButtonPanel = new MoveButtonPanel<Tabs>(Tabs::getName) {
            @Override
            protected Tabs[] getValues() {
                return Tabs.values();
            }

            @Override
            protected ActionListener buttonListener() {
                return button -> {
                    switch (this.tabSelector.getSelected()) {
                        case WORLD_SEED:
                            SeedCandy.INSTANCE.worldPanel.inputText.setText(ToolsPanel.this.outputText.getText());
                            SeedCandy.INSTANCE.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.worldPanel);
                            break;
                        case LOCATOR:
                            SeedCandy.INSTANCE.extraPanel.locatorPanel.inputText.setText(ToolsPanel.this.outputText.getText());
                            SeedCandy.INSTANCE.extraPanel.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.extraPanel.locatorPanel);
                            break;
                    }
                };
            }
        };

        // Setup
        spawnBiomeSelector.setSelectedIndex(-1);
        spawnBiomeSelector.setToolTipText("spawn Biome");
        buttonSet.addListeners(
                stringButton -> {
                    this.reset();
                    this.progressBar.setMaximum(Strings.countLines(this.inputText.getText()));

                    AtomicInteger progress = new AtomicInteger(0);
                    SeedCandy.POOL.execute(Strings.splitLines(this.inputText.getText()), line -> {
                        try {
                            int seed = Integer.parseInt(line);
                            String seedStr = this.hash.getStrFromHash(seed);
                            SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%d\n -> %s", seed, seedStr)));
                        } catch (NumberFormatException e) {
                            SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%s\n -> can't be converted", line)));
                        }
                        this.progressBar.setValue(progress.incrementAndGet());
                    });
                },
                intButton -> {
                    this.reset();
                    this.progressBar.setMaximum(Strings.countLines(this.inputText.getText()));

                    AtomicInteger progress = new AtomicInteger(0);
                    SeedCandy.POOL.execute(Strings.splitLines(this.inputText.getText()), line -> {
                        SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%d", line.hashCode())));
                        this.progressBar.setValue(progress.incrementAndGet());
                    });
                },
                shadowButton -> {
                    this.reset();

                    this.progressBar.setMaximum(Strings.splitToLongs(this.inputText.getText()).length);
                    AtomicInteger progress = new AtomicInteger(0);
                    SeedCandy.POOL.execute(Strings.splitToLongs(this.inputText.getText()), seed -> {
                        SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%d", WorldSeed.getShadowSeed(seed))));
                        this.progressBar.setMaximum(progress.incrementAndGet());
                    });
                },
                spawnBiomeButton -> {
                    this.reset();
                    this.progressBar.setMaximum(Strings.countLines(this.inputText.getText()));

                    AtomicInteger progress = new AtomicInteger(0);
                    SeedCandy.POOL.execute(Strings.splitToLongs(this.inputText.getText()), seed -> {
                        OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                        Biome spawnBiome = biomeSource.getBiome(biomeSource.getSpawnPoint().getX(), 0, biomeSource.getSpawnPoint().getZ());

                        SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("(%d) %s", seed, spawnBiome.getName())));
                        this.progressBar.setValue(progress.incrementAndGet());
                    });
                },
                biomeButton -> {
                    this.reset();

                    this.progressBar.setMaximum(Strings.countLines(this.inputText.getText()));
                    AtomicInteger progress = new AtomicInteger(0);

                    boolean shouldCheckSpawn = spawnBiomeSelector.getSelectedIndex() != -1 && spawnBiomeSelector.getSelected().getDimension() == Dimension.OVERWORLD;
                    boolean shouldCheckBiomes = false;
                    for (int i = 0; i < 5; ++i) {
                        if (biomeSelectorPanel.getComponent(0, i).shouldBeChecked(Dimension.OVERWORLD)) {
                            shouldCheckBiomes = true;
                            break;
                        }
                    }

                    boolean finalShouldCheckBiomes = shouldCheckBiomes;
                    SeedCandy.POOL.execute(Strings.splitLines(this.inputText.getText()), line -> {
                        boolean matches = true;
                        OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, Long.parseLong(line));

                        if (shouldCheckSpawn && spawnBiomeSelector.getSelected() != biomeSource.getBiome(biomeSource.getSpawnPoint().getX(), 0, biomeSource.getSpawnPoint().getZ()))
                            matches = false;

                        if (matches && finalShouldCheckBiomes)
                            for (int i = 0; i < 7; ++i) {
                                if (!biomeSelectorPanel.getComponent(0, i).shouldBeChecked(Dimension.OVERWORLD)) continue;
                                if (biomeSelectorPanel.getComponent(0, i).doesNotMatch(biomeSource)) {
                                    matches = false;
                                    break;
                                }
                            }

                        boolean finalMatches = matches;
                        SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%s\n -> %b", line, finalMatches)));
                        this.progressBar.setValue(progress.incrementAndGet());
                    });
                },
                copyButton -> Strings.clipboard(this.outputText.getText()),
                clearOutputButton -> this.reset(),
                clearButton -> {
                    this.reset();
                    this.inputText.setText("");
                }
        );

        // Visual setup
        this.setLayout(new BorderLayout());
        this.setName("Tools");
        buttonSet.addAll(buttonPanel);
        selectionPanel.add(moveButtonPanel, BorderLayout.NORTH);
        selectionPanel.add(buttonPanel, BorderLayout.CENTER);
        selectionPanel.add(this.progressBar, BorderLayout.SOUTH);
        leftPanel.add(selectionPanel, BorderLayout.NORTH);
        leftPanel.add(biomeSelectorPanel, BorderLayout.CENTER);
        leftPanel.add(spawnBiomeSelector, BorderLayout.SOUTH);
        this.add(leftPanel, BorderLayout.WEST);
        this.add(this.inputText, BorderLayout.CENTER);
        this.add(this.outputText, BorderLayout.EAST);
    }

    private void reset() {
        this.outputText.setText("");
        this.progressBar.setMaximum(1);
        this.progressBar.setValue(0);
    }

    private enum Tabs implements MoveButtonPanel.Tab {
        WORLD_SEED("world_seed"),
        LOCATOR("locator");

        Tabs(String name) {
            this.name = name;
        }

        private final String name;

        public String getName() {
            return this.name;
        }
    }
}
