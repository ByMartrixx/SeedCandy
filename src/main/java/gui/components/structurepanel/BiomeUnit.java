package gui.components.structurepanel;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import swing.SwingUtils;
import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;

public class BiomeUnit extends JPanel {

    public final JTextField xCord;
    public final JTextField zCord;
    public final SelectionBox<Biome> biomeSelector;

    public BiomeUnit() {
        this.xCord = new JTextField();
        this.zCord = new JTextField();
        this.biomeSelector = new SelectionBox<>(Biome::getName, Biome.REGISTRY.values());

        SwingUtils.setPrompt("X", this.xCord);
        SwingUtils.setPrompt("Z", this.zCord);
        this.biomeSelector.setPreferredSize(new Dimension(150, 25));

        this.add(this.xCord);
        this.add(this.zCord);
        this.add(this.biomeSelector);
    }

    public boolean matches(OverworldBiomeSource biomeSource) {
        try {
            return biomeSource.getBiome(Integer.parseInt(this.xCord.getText().trim()), 0, Integer.parseInt(this.zCord.getText().trim()))
                    == this.biomeSelector.getSelected();
        } catch (NumberFormatException exception) {
            return true;
        }
    }
}
