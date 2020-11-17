package gui.components;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.Dimension;
import swing.SwingUtils;
import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Collections;

public class BiomeUnit extends JPanel {
    private final boolean cords;
    private final boolean radius;
    private boolean button = false;
    public JTextField xCord;
    public JTextField zCord;
    public SelectionBox<Biome> biomeSelector;
    public JTextField radiusField;

    public SelectionBox<Dimension> dimensionSelector;
    public JButton locateButton;

    public BiomeUnit() {
        this(true, false);
    }

    public BiomeUnit(boolean cords, boolean radius) {
        this.cords = cords;
        this.radius = radius;

        if (cords) {
            this.xCord = new JTextField();
            this.zCord = new JTextField();

            SwingUtils.setPrompt("X", this.xCord);
            SwingUtils.setPrompt("Z", this.zCord);

            this.add(this.xCord);
            this.add(this.zCord);
        }

        this.biomeSelector = new SelectionBox<>(Biome::getName, Biome.REGISTRY.values());

        this.biomeSelector.setPreferredSize(new java.awt.Dimension(150, 25));
        this.biomeSelector.setSelectedIndex(-1);
        this.biomeSelector.setToolTipText("biome");

        this.add(this.biomeSelector);
        if (radius) {
            this.radiusField = new JTextField();

            SwingUtils.setPrompt("radius", this.radiusField);
            this.radiusField.setText("1");
            this.radiusField.setToolTipText("radius");

            this.add(this.radiusField);
        }
    }

    public BiomeUnit(SelectionBox<Dimension> dimensionSelector, JButton locateButton) {
        this(true, false);

        this.dimensionSelector = dimensionSelector;
        this.locateButton = locateButton;
        this.button = true;
        this.add(this.dimensionSelector);
        this.add(this.locateButton);
    }

    public void addButtonListener(ActionListener listener) {
        if (this.button) this.locateButton.addActionListener(listener);
    }

    public void reset() {
        if (this.cords) {
            this.xCord.setText("");
            SwingUtils.setPrompt("X", this.xCord);
            this.zCord.setText("");
            SwingUtils.setPrompt("Z", this.zCord);
        }
        if (this.radius)
            this.radiusField.setText("1");
        this.biomeSelector.setSelectedIndex(-1);
    }

    public boolean doesNotMatch(BiomeSource biomeSource) {
        return !this.matches(biomeSource);
    }

    public boolean matches(BiomeSource biomeSource) {
        if (this.biomeSelector.getSelectedIndex() == -1) return true;

        if (this.radius && this.cords) {
            JRand rand = new JRand(biomeSource.getWorldSeed());

            try {
                if (Integer.parseInt(this.radiusField.getText().trim()) < 1) this.radiusField.setText("1");
            } catch (NumberFormatException e) {
                this.radiusField.setText("1");
            }

            try {
                return biomeSource.locateBiome(Integer.parseInt(this.xCord.getText().trim()), 0, Integer.parseInt(this.zCord.getText().trim()),
                        Integer.parseInt(this.radiusField.getText()), Collections.singletonList(this.biomeSelector.getSelected()), rand) != null;
            } catch (NumberFormatException e) {
                return true;
            }
        } else if (this.cords) {
            try {
                return biomeSource.getBiome(Integer.parseInt(this.xCord.getText().trim()), 0, Integer.parseInt(this.zCord.getText().trim()))
                        == this.biomeSelector.getSelected();
            } catch (NumberFormatException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean shouldBeChecked() {
        return this.biomeSelector.getSelectedIndex() != -1;
    }

    public boolean shouldBeChecked(Dimension dimension) {
        return this.shouldBeChecked() && this.biomeSelector.getSelected().getDimension() == dimension;
    }
}
