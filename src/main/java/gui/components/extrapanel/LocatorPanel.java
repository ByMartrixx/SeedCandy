package gui.components.extrapanel;

import data.Strings;
import gui.SeedCandy;
import gui.components.BiomeUnit;
import gui.components.TextBlock;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.EndBiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.BPos;
import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class LocatorPanel extends JPanel {
    public TextBlock inputText;
    public TextBlock outputText;
    public JProgressBar progressBar;

    public LocatorPanel() { // TODO: Version selector
        // Initialize
        this.inputText = new TextBlock(true);
        this.outputText = new TextBlock(false);
        this.progressBar = new JProgressBar(0, 1);
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Biome locator
        JButton locateBiomeButton = new JButton("locate");
        SelectionBox<Dimension> biomeDimensionSelector = new SelectionBox<>(Dimension::toString, Dimension.values());
        biomeDimensionSelector.setToolTipText("dimension");
        BiomeUnit biomeUnit = new BiomeUnit(biomeDimensionSelector, locateBiomeButton);
        biomeUnit.addButtonListener(button -> {
            this.reset();
            this.progressBar.setMaximum(Strings.countLines(this.inputText.getText()));

            AtomicInteger progress = new AtomicInteger(0);
            SeedCandy.POOL.execute(Strings.splitToLongs(this.inputText.getText()), seed -> {
                Biome biomeToSearch = biomeUnit.biomeSelector.getSelected();
                Dimension dimension = biomeUnit.dimensionSelector.getSelected();
                int centerX = !biomeUnit.xCord.getText().equals("") && !biomeUnit.xCord.getText().equals("X") ? Integer.parseInt(biomeUnit.xCord.getText().trim()) : 0;
                int centerZ = !biomeUnit.zCord.getText().equals("") && !biomeUnit.zCord.getText().equals("Z") ? Integer.parseInt(biomeUnit.zCord.getText().trim()) : 0;

                JRand rand = new JRand(seed);
                BiomeSource biomeSource;

                if (biomeToSearch.getDimension() != dimension) {
                    SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%s can't be found\n at %s", biomeToSearch.getName(), dimension.toString())));
                    this.progressBar.setValue(progress.incrementAndGet());
                    return;
                }

                switch (dimension) {
                    case OVERWORLD:
                    default:
                        biomeSource = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
                        break;
                    case NETHER:
                        biomeSource = new NetherBiomeSource(MCVersion.v1_16_2, seed);
                        break;
                    case END:
                        biomeSource = new EndBiomeSource(MCVersion.v1_16_2, seed);
                        break;
                }

                BPos biomePos = biomeSource.locateNearestBiome(centerX, 0, centerZ, 32768,
                        Collections.singleton(biomeToSearch), rand);
                if (biomePos != null)
                    SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%d\n -> (%d, %d)", seed, biomePos.getX(), biomePos.getZ())));
                else
                    SwingUtilities.invokeLater(() -> this.outputText.addEntry(String.format("%d\n -> no %s found", seed, biomeToSearch.getName())));
                this.progressBar.setValue(progress.incrementAndGet());
            });
        });

        // Structure locator
        // TODO

        // Visual setup
        this.setLayout(new BorderLayout());
        this.setName("Locator");
        leftPanel.add(this.progressBar, BorderLayout.NORTH);
        leftPanel.add(biomeUnit, BorderLayout.CENTER);
        this.add(leftPanel, BorderLayout.WEST);
        this.add(this.inputText, BorderLayout.CENTER);
        this.add(this.outputText, BorderLayout.EAST);
    }

    private void reset() {
        this.outputText.setText("");
        this.progressBar.setMaximum(1);
        this.progressBar.setValue(0);
    }
}
