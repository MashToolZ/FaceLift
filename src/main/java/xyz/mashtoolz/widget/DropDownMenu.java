package xyz.mashtoolz.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.Text;
import xyz.mashtoolz.mixins.ScreenInterface;

public class DropDownMenu {

	private List<ButtonWidget> buttons = new ArrayList<>();

	private Screen screen;
	private Text text;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean inverted;
	private ButtonWidget btn;

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
			buttons.forEach(b -> b.visible = active);
		}).position(x, y).size(width, height).build();
	}

	public void addButton(String text, PressAction onPress, boolean bool) {
		var btn = ButtonWidget.builder(Text.literal(text), onPress)
				.position(x, y + ((inverted ? -1 : 1) * (buttons.size() + 1) * height))
				.size(width, height)
				.build();
		btn.visible = active;
		buttons.add(btn);
		((ScreenInterface) screen).invokeAddDrawableChild(btn);
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

	public int getWidth() {
		return width;
	}

	public ButtonWidget getButton() {
		return btn;
	}
}
