package cn.ussshenzhou.madparticle.designer.universal.util;

/**
 * @author USS_Shenzhou
 */
public class Border {
    private int color;
    private int thickness;

    public Border(int color,int thickness){
        this.color = color;
        this.thickness = thickness;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }
}
