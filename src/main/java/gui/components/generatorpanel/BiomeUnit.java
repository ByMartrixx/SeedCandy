package gui.components.generatorpanel;

import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import swing.SwingUtils;

import javax.swing.*;
import java.util.Collections;

public class BiomeUnit extends gui.components.structurepanel.BiomeUnit {
    public final JTextField radius;

    public BiomeUnit() {
        super();
        this.radius = new JTextField();

        SwingUtils.setPrompt("radius", this.radius);
        this.radius.setText("1");

        this.add(radius);
    }

    @Override
    public boolean matches(OverworldBiomeSource biomeSource) {
        try {
            return matches(biomeSource, Integer.parseInt(xCord.getText()), Integer.parseInt(zCord.getText()));
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    public boolean matches(OverworldBiomeSource biomeSource, int x, int z) {
        JRand rand = new JRand(biomeSource.getWorldSeed());
        if (Integer.parseInt(this.radius.getText()) < 0) this.radius.setText("1");
        return biomeSource.locateBiome(x, 0, z, Integer.parseInt(this.radius.getText()), Collections.singletonList(this.biomeSelector.getSelected()), rand) != null;
    }
}
