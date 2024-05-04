package xyz.mashtoolz.config;

import org.joml.Vector2i;

public class Category_XPCalc {
    public boolean enabled = true;
    public Vector2i position = new Vector2i(5, 99);

    public static Category_XPCalc getDefault() {
        return new Category_XPCalc();
    }

}

