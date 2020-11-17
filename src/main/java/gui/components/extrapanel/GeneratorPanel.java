package gui.components.extrapanel;

import data.Strings;
import gui.MoveButtonPanel;
import gui.SeedCandy;
import gui.components.TextBlock;
import gui.components.BiomeUnit;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;
import swing.SwingUtils;
import swing.components.ButtonSet;
import swing.components.GridPanel;
import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;

public class GeneratorPanel extends JPanel {
    public JTextField nTextField;
    public JTextField convertibleTextField;
    public JProgressBar progressBar;
    public TextBlock outputText;

    public GeneratorPanel() { // TODO: Version selector
        // Initialize
        this.nTextField = new JTextField();
        this.convertibleTextField = new JTextField();
        this.progressBar = new JProgressBar(0, 1);
        this.outputText = new TextBlock(false);
        JPanel fieldsPanel = new JPanel();
        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Buttons
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel subButtonPanel = new JPanel(new GridLayout(0, 2));
        ButtonSet<JButton> buttonSet = new ButtonSet<>(JButton::new,
                "generate", "clear Output",
                "clear All", "copy Output"
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
                            SeedCandy.INSTANCE.worldPanel.inputText.setText(GeneratorPanel.this.outputText.getText());
                            SeedCandy.INSTANCE.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.worldPanel);
                            break;
                        case TOOLS:
                            SeedCandy.INSTANCE.extraPanel.toolsPanel.inputText.setText(GeneratorPanel.this.outputText.getText());
                            SeedCandy.INSTANCE.extraPanel.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.extraPanel.toolsPanel);
                            break;
                        case LOCATOR:
                            SeedCandy.INSTANCE.extraPanel.locatorPanel.inputText.setText(GeneratorPanel.this.outputText.getText());
                            SeedCandy.INSTANCE.extraPanel.tabbedPane.setSelectedComponent(SeedCandy.INSTANCE.extraPanel.locatorPanel);
                            break;
                    }
                };
            }
        };

        // Biome selection
        SelectionBox<Biome> spawnBiomeSelector = new SelectionBox<>(Biome::getName, Biome.REGISTRY.values());
        GridPanel<BiomeUnit> biomeSelectorPanel = new GridPanel<>(1, 5, () -> new BiomeUnit(true, true));

        // Slime chunk selection
        // TODO

        // Setup
        SwingUtils.setPrompt("seeds", this.nTextField);
        this.nTextField.setText("10");
        this.nTextField.setToolTipText("seeds");
        SwingUtils.setPrompt("convertible", this.convertibleTextField);
        this.convertibleTextField.setText("false");
        this.convertibleTextField.setToolTipText("convertible");
        spawnBiomeSelector.setSelectedIndex(-1);
        spawnBiomeSelector.setToolTipText("spawn Biome");
        buttonSet.addListeners(
                generateButton -> {
                    this.reset();

                    Random random = new Random();

                    int n = Integer.parseInt(this.nTextField.getText());
                    this.progressBar.setMaximum(n);

                    if (Boolean.parseBoolean(this.convertibleTextField.getText())) {
                        SeedCandy.POOL.execute(() -> { // TODO: Optimize more?
                            boolean shouldCheckSpawn = spawnBiomeSelector.getSelectedIndex() != -1 && spawnBiomeSelector.getSelected().getDimension() == Dimension.OVERWORLD;
                            boolean shouldCheckBiomes = false;
                            for (int i = 0; i < 5; ++i) {
                                if (biomeSelectorPanel.getComponent(0, i).shouldBeChecked(Dimension.OVERWORLD)) {
                                    shouldCheckBiomes = true;
                                    break;
                                }
                            }

                            int i = 0;
                            while (i < n) {
                                int seed = random.nextInt();
                                OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                                boolean matches = true;

                                if (shouldCheckSpawn && spawnBiomeSelector.getSelected() != biomeSource.getBiome(biomeSource.getSpawnPoint().getX(), 0, biomeSource.getSpawnPoint().getZ()))
                                    matches = false;

                                if (matches && shouldCheckBiomes)
                                    for (int j = 0; j < 5; ++j) {
                                        // I think saving which BiomeUnits should be checked would be faster than calling BiomeUnit#shouldBeChecked(Dimension) every time
                                        if (!biomeSelectorPanel.getComponent(0, j).shouldBeChecked(Dimension.OVERWORLD)) continue;
                                        if (biomeSelectorPanel.getComponent(0, j).doesNotMatch(biomeSource)) {
                                            matches = false;
                                            break;
                                        }
                                    }

                                if (matches) {
                                    SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.valueOf(seed)));
                                    ++i;
                                    this.progressBar.setValue(i);
                                }
                            }
                        });
                    } else {
                        SeedCandy.POOL.execute(() -> { // TODO: Optimize more?
                            boolean shouldCheckSpawn = spawnBiomeSelector.getSelectedIndex() != -1 && spawnBiomeSelector.getSelected().getDimension() == Dimension.OVERWORLD;
                            boolean shouldCheckBiomes = false;
                            for (int i = 0; i < 5; ++i) {
                                if (biomeSelectorPanel.getComponent(0, i).shouldBeChecked(Dimension.OVERWORLD)) {
                                    shouldCheckBiomes = true;
                                    break;
                                }
                            }

                            int i = 0;
                            while (i < n) {
                                long seed = random.nextLong();
                                OverworldBiomeSource biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                                boolean matches = true;

                                if (shouldCheckSpawn && spawnBiomeSelector.getSelected() != biomeSource.getBiome(biomeSource.getSpawnPoint().getX(), 0, biomeSource.getSpawnPoint().getZ()))
                                    matches = false;

                                if (matches && shouldCheckBiomes)
                                    for (int j = 0; j < 5; ++j) {
                                        // I think saving which BiomeUnits should be checked would be faster than calling BiomeUnit#shouldBeChecked(Dimension) every time
                                        if (!biomeSelectorPanel.getComponent(0, j).shouldBeChecked(Dimension.OVERWORLD)) continue;
                                        if (biomeSelectorPanel.getComponent(0, j).doesNotMatch(biomeSource)) {
                                            matches = false;
                                            break;
                                        }
                                    }

                                if (matches) {
                                    SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.valueOf(seed)));
                                    ++i;
                                    this.progressBar.setValue(i);
                                }
                            }
                        });
                    }
                },
                outputButton -> this.reset(),
                clearButton -> {
                    this.reset();
                    this.nTextField.setText("10");
                    this.convertibleTextField.setText("false");

                    spawnBiomeSelector.setSelectedIndex(-1);
                    for (int i = 0; i < 5; ++i) {
                        biomeSelectorPanel.getComponent(0, i).reset();
                    }
                },
                copyButton -> Strings.clipboard(this.outputText.getText())
        );

        // Visual setup
        this.setLayout(new BorderLayout());
        this.setName("Generator");
        fieldsPanel.add(this.nTextField);
        fieldsPanel.add(this.convertibleTextField);
        fieldsPanel.add(this.progressBar);
        inputPanel.add(fieldsPanel, BorderLayout.NORTH);
        inputPanel.add(spawnBiomeSelector, BorderLayout.CENTER);
        inputPanel.add(biomeSelectorPanel, BorderLayout.SOUTH);
        buttonSet.addAll(subButtonPanel);
        buttonPanel.add(subButtonPanel, BorderLayout.CENTER);
        buttonPanel.add(moveButtonPanel, BorderLayout.SOUTH);
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);
        this.add(leftPanel, BorderLayout.WEST);
        this.add(this.outputText, BorderLayout.CENTER);
    }

    private void reset() {
        this.outputText.setText("");
        this.progressBar.setMaximum(1);
        this.progressBar.setValue(0);
    }

    private enum Tabs implements MoveButtonPanel.Tab {
        WORLD_SEED("world seed"),
        TOOLS("tools"),
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
