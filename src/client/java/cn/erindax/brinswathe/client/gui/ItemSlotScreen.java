package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.BuySlots;
import cn.erindax.brinswathe.client.PersonalSettingsClient;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ItemSlotScreen extends Screen {
    private static final int ROW_WIDTH = 280;
    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SLOT_WIDTH = 72;
    private static final int ICON_SIZE = 16;
    private static final int GAP = 4;
    private static final int TEXT_WIDTH = 300;
    private static final int SEARCH_WIDTH = 200;
    private static final int FOOTER_HEIGHT = 36;
    private static final int NOTICE_HEIGHT = 12;
    private static final int TITLE_COLOR = 0xFFD54F;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final List<ResourceLocation> COMMON = List.of(
        ResourceLocation.fromNamespaceAndPath("wathe", "key"),
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key"),
        ResourceLocation.fromNamespaceAndPath("wathe", "lockpick"),
        ResourceLocation.fromNamespaceAndPath("wathe", "knife"),
        ResourceLocation.fromNamespaceAndPath("wathe", "revolver")
    );
    private static final List<Integer> SLOTS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    private final Screen parent;
    private final List<Item> items = new ArrayList<>();
    private List<FormattedCharSequence> hint = List.of();
    private String filter = "";
    @Nullable
    private RowList list;

    public ItemSlotScreen(Screen parent) {
        super(Component.translatable("screen.brinswathe.item_slots"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.items.isEmpty()) this.collectItems();
        double scroll = this.list == null ? 0.0 : this.list.getScrollAmount();
        int center = this.width / 2;
        this.hint = this.font.split(
            Component.translatable("gui.brinswathe.item_slots.hint"),
            Math.max(120, Math.min(TEXT_WIDTH, this.width - 20))
        );
        int searchTop = 26 + this.hint.size() * 11;
        EditBox search = new EditBox(
            this.font,
            center - SEARCH_WIDTH / 2,
            searchTop,
            SEARCH_WIDTH,
            18,
            Component.translatable("gui.brinswathe.item_slots.search")
        );
        search.setHint(Component.translatable("gui.brinswathe.item_slots.search").withStyle(ChatFormatting.GRAY));
        search.setValue(this.filter);
        search.setResponder(value -> {
            this.filter = value;
            this.refreshRows();
            if (this.list != null) this.list.setClampedScrollAmount(0.0);
        });
        this.addRenderableWidget(search);
        int footer = PersonalSettingsClient.serverSupportsItemSlots() ? FOOTER_HEIGHT : FOOTER_HEIGHT + NOTICE_HEIGHT;
        this.list = this.addRenderableWidget(new RowList(searchTop + 24, footer));
        this.refreshRows();
        this.list.setClampedScrollAmount(scroll);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
            .bounds(center - 100, this.height - FOOTER_HEIGHT / 2 - BUTTON_HEIGHT / 2, 200, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, center, 10, TITLE_COLOR);
        int y = 24;
        for (FormattedCharSequence line : this.hint) {
            graphics.drawCenteredString(this.font, line, center, y, TEXT_COLOR);
            y += 11;
        }
        if (!PersonalSettingsClient.serverSupportsItemSlots()) {
            graphics.drawCenteredString(
                this.font,
                Component.translatable("gui.brinswathe.buy_slot.server_missing").withStyle(ChatFormatting.GRAY),
                center,
                this.height - FOOTER_HEIGHT - NOTICE_HEIGHT + 2,
                TEXT_COLOR
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void collectItems() {
        Set<Item> found = new LinkedHashSet<>();
        for (ResourceLocation id : COMMON) found.add(BuiltInRegistries.ITEM.get(id));
        for (ResourceLocation id : PersonalSettingsClient.itemSlotIds()) found.add(BuiltInRegistries.ITEM.get(id));
        if (this.minecraft.player != null) {
            for (int slot = 0; slot < BuySlots.HOTBAR_SIZE; slot++) {
                found.add(this.minecraft.player.getInventory().getItem(slot).getItem());
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BlockItem) continue;
            if (ResourceLocation.DEFAULT_NAMESPACE.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) continue;
            found.add(item);
        }
        found.remove(Items.AIR);
        this.items.addAll(found);
    }

    private void refreshRows() {
        if (this.list == null) return;
        String needle = this.filter.trim().toLowerCase(Locale.ROOT);
        List<ItemRow> rows = new ArrayList<>();
        for (Item item : this.items) {
            ItemRow row = new ItemRow(item);
            if (needle.isEmpty()
                || BuiltInRegistries.ITEM.getKey(item).toString().contains(needle)
                || row.name.getString().toLowerCase(Locale.ROOT).contains(needle)) {
                rows.add(row);
            }
        }
        this.list.replace(rows);
    }

    private static Component slotLabel(int slot) {
        return slot == 0
            ? Component.translatable("gui.brinswathe.item_slots.default")
            : Component.translatable("gui.brinswathe.item_slots.slot", slot).withStyle(ChatFormatting.YELLOW);
    }

    private final class ItemRow extends ContainerObjectSelectionList.Entry<ItemRow> {
        private final ItemStack icon;
        private final Component name;
        private final CycleButton<Integer> button;

        ItemRow(Item item) {
            this.icon = new ItemStack(item);
            this.name = this.icon.getHoverName();
            this.button = CycleButton.<Integer>builder(ItemSlotScreen::slotLabel)
                .withValues(SLOTS)
                .withInitialValue(PersonalSettingsClient.itemSlot(item))
                .withTooltip(value -> Tooltip.create(Component.translatable("gui.brinswathe.item_slots.tip")))
                .displayOnlyValue()
                .create(0, 0, SLOT_WIDTH, BUTTON_HEIGHT, this.name,
                    (button, value) -> PersonalSettingsClient.setItemSlot(item, value));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - SLOT_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            graphics.renderItem(this.icon, left, top + (height - ICON_SIZE) / 2);
            int textLeft = left + ICON_SIZE + GAP;
            List<FormattedCharSequence> lines = ItemSlotScreen.this.font.split(
                this.name,
                Math.max(1, width - SLOT_WIDTH - ICON_SIZE - GAP * 2)
            );
            if (!lines.isEmpty()) {
                graphics.drawString(ItemSlotScreen.this.font, lines.get(0), textLeft, top + (height - 8) / 2, TEXT_COLOR);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }

    private final class RowList extends ContainerObjectSelectionList<ItemRow> {
        RowList(int top, int footer) {
            super(
                ItemSlotScreen.this.minecraft,
                ItemSlotScreen.this.width,
                ItemSlotScreen.this.height - top - footer,
                top,
                ROW_HEIGHT
            );
        }

        void replace(List<ItemRow> rows) {
            this.replaceEntries(rows);
            this.setClampedScrollAmount(this.getScrollAmount());
        }

        @Override
        public int getRowWidth() {
            return Math.min(ROW_WIDTH, ItemSlotScreen.this.width - 40);
        }

        @Override
        protected int getScrollbarPosition() {
            return (ItemSlotScreen.this.width + this.getRowWidth()) / 2 + 6;
        }
    }
}
