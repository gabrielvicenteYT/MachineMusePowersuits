package net.machinemuse.powersuits.client.gui.tinker.clickable;

import net.machinemuse.numina.general.MuseMathUtils;
import net.machinemuse.numina.utils.math.Colour;
import net.machinemuse.numina.utils.math.geometry.DrawableMuseRect;
import net.machinemuse.numina.utils.math.geometry.MusePoint2D;
import net.machinemuse.numina.utils.render.MuseRenderer;
import net.minecraftforge.common.util.Constants;

/**
 * Author: MachineMuse (Claire Semple)
 * Created: 7:08 AM, 06/05/13
 *
 * Ported to Java by lehjr on 10/19/16.
 */
public class ClickableSlider extends Clickable {
    double valueInternal = 0;
    MusePoint2D pos;
    double width;
    String name;
    DrawableMuseRect insideRect;
    DrawableMuseRect outsideRect;
    final int cornersize = 3;

    public ClickableSlider(MusePoint2D pos, double width, String name) {
        this.pos = pos;
        this.width = width;
        this.name = name;
        this.position = pos;
        this.insideRect = new DrawableMuseRect(position.getX() - width / 2.0 - cornersize, position.getY() + 8, 0, position.getY() + 16, Colour.LIGHTBLUE, Colour.ORANGE);
        this.outsideRect = new DrawableMuseRect(position.getX() - width / 2.0 - cornersize, position.getY() + 8, position.getX() + width / 2.0 + cornersize, position.getY() + 16, Colour.LIGHTBLUE, Colour.DARKBLUE);
    }

    public String name() {
        return this.name;
    }

    @Override
    public void draw() {
        MuseRenderer.drawCenteredString(name, position.getX(), position.getY());
        this.insideRect.setRight(position.getX() + width * (getValue() - 0.5) + cornersize);
        this.outsideRect.draw();
        this.insideRect.draw();
    }

    @Override
    public boolean hitBox(double x, double y) {
        return Math.abs(position.getX() - x) < width / 2 &&
                Math.abs(position.getY() + 12 - y) < 4;
    }

    public double getValue() {
        return valueInternal;
    }

    public void setValueByX(double x) {
        valueInternal = MuseMathUtils.clampDouble((x - pos.getX()) / width + 0.5, 0, 1);
    }

    public void setValue(double v) {
        valueInternal = v;
    }
}
