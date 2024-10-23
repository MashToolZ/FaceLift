package xyz.mashtoolz.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.Text;
import xyz.mashtoolz.mixins.ScreenAccessor;

import java.util.ArrayList;
import java.util.List;

public class DropDownMenu {

	private final List<ButtonWidget> BUTTONS = new ArrayList<>();

	private final Screen screen;
	private final Text text;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final boolean inverted;
	private final ButtonWidget btn;

	private boolean active = false;

	public DropDownMenu(Screen screen, String text, int x, int y, int width, int height, boolean inverted) {
		this.screen = screen;
		this.text = Text.literal(text);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.inverted = inverted;
		this.btn = ButtonWidget.builder(this.text, button -> {
			active = !active;
			BUTTONS.forEach(b -> b.visible = active);
		}).position(x, y).size(width, height).build();
	}

	public void addButton(String text, PressAction onPress) {
		var btn = ButtonWidget.builder(Text.literal(text), onPress)
				.position(x, y + ((inverted ? -1 : 1) * (BUTTONS.size() + 1) * height))
				.size(width, height)
				.build();
		btn.visible = active;
		BUTTONS.add(btn);
		((ScreenAccessor) screen).invokeAddDrawableChild(btn);
	}

	public Text getText() {
		return text;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ButtonWidget getButton() {
		return btn;
	}
}
