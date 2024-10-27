package xyz.mashtoolz.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

public class SearchFieldWidget extends TextFieldWidget {

    private static FaceLift INSTANCE = FaceLift.getInstance();

    private long lastClickTime = 0;
    public boolean highlighted = false;

    public SearchFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable SearchFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 250) {
            highlighted = !highlighted;
            INSTANCE.CONFIG.inventory.searchbar.highlight = highlighted;
            this.setEditableColor(highlighted ? 0xFFFF78 : 0xE0E0E0);
            FaceConfig.save();
        }
        lastClickTime = currentTime;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (this.isHovered() && button == 1)
            this.setText("");

        if (this.isFocused() && button == 0)
            this.setFocused(false);

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
