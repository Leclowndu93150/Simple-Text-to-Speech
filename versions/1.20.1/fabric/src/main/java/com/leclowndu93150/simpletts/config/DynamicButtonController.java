package com.leclowndu93150.simpletts.config;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicButtonController implements Controller<Boolean> {

    private final Option<Boolean> option;
    private final Supplier<Component> textSupplier;
    private final Consumer<Option<Boolean>> action;

    public DynamicButtonController(Option<Boolean> option, Supplier<Component> textSupplier, Consumer<Option<Boolean>> action) {
        this.option = option;
        this.textSupplier = textSupplier;
        this.action = action;
    }

    @Override
    public Option<Boolean> option() {
        return option;
    }

    @Override
    public Component formatValue() {
        return textSupplier.get();
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new DynamicButtonWidget(this, screen, widgetDimension);
    }

    public static class DynamicButtonWidget extends ControllerWidget<DynamicButtonController> {

        public DynamicButtonWidget(DynamicButtonController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && isAvailable() && button == 0) {
                playDownSound();
                control.action.accept(control.option);
                return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!focused) return false;
            if (keyCode == 257 || keyCode == 32 || keyCode == 335) {
                playDownSound();
                control.action.accept(control.option);
                return true;
            }
            return false;
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public boolean canReset() {
            return false;
        }
    }
}
