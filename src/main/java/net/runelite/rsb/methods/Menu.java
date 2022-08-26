package net.runelite.rsb.methods;

import net.runelite.api.ItemComposition;
import net.runelite.api.MenuEntry;
import net.runelite.cache.definitions.ItemDefinition;
import net.runelite.rsb.wrappers.RSItem;
import net.runelite.client.ui.FontManager;

import java.awt.*;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Context menu related operations.
 */

@Slf4j
public class Menu extends MethodProvider {
    private static final Pattern HTML_TAG = Pattern
            .compile("(^[^<]+>|<[^>]+>|<[^>]+$)");

    protected static final int TOP_OF_MENU_BAR = 18;
    protected static final int MENU_ENTRY_LENGTH = 15;
    protected static final int MENU_SIDE_BORDER = 7;
    protected static final int MAX_DISPLAYABLE_ENTRIES = 32;

    private boolean DEBUG = false;

    public void enableDebug(boolean v) {
        DEBUG = v;
    }

    protected Menu(final MethodContext ctx) {
        super(ctx);
    }


    /**
     * Clicks the menu target. Will left-click if the menu item is the first,
     * otherwise open menu and click the target.
     *
     * @param action The action (or action substring) to click.
     * @return <code>true</code> if the menu item was clicked; otherwise
     * <code>false</code>.
     */
    public boolean doAction(String action) {
        return doAction(action, null);
    }

    /**
     * Clicks the menu target. Will left-click if the menu item is the first,
     * otherwise open menu and click the target.
     *
     * @param action The action (or action substring) to click.
     * @param target The target (or target substring) of the action to click.
     * @return <code>true</code> if the menu item was clicked; otherwise
     * <code>false</code>.
     */
    public boolean doAction(final String action, String target) {
        int idx = getIndex(action, target);
        if (idx == -1) {
            while (isOpen()) {
                methods.mouse.moveRandomly(750);
                sleep(random(100, 500));
            }
            return false;
        }

        if (!isOpen()) {
            if (idx == -1 || idx > MAX_DISPLAYABLE_ENTRIES) {
                return false;
            }

            if (idx == 0) {
                methods.mouse.click(true);
                return true;
            }

            methods.mouse.click(false);
            sleep(random(150,300));
        }

        return clickIndex(idx);
    }

    /**
     * Determines if the item contains the desired action.
     *
     * @param item   The item to check.
     * @param action The item menu action to check.
     * @return <code>true</code> if the item has the action; otherwise
     * <code>false</code>.
     */
    public boolean itemHasAction(final RSItem item, final String action) {
        // Used to determine if an item is droppable/destroyable
        if (item == null) {
            return false;
        }
        ItemDefinition itemDef = item.getDefinition();
        if (itemDef != null) {
            for (String a : itemDef.getInterfaceOptions()) {
                if (a.equalsIgnoreCase(action)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Left clicks at the given index.
     *
     * @param i The index of the item.
     * @return <code>true</code> if the mouse was clicked; otherwise <code>false</code>.
     */
    private boolean clickIndex(final int i) {
        if (!isOpen()) {
            return false;
        }

        MenuEntry[] entries = getEntries();
        if (entries.length <= i) {
            return false;
        }

        if (!isCollapsed()) {
            return clickMain(i);
        }

        return false;
    }

    private boolean clickMain(final int i) {
        MenuEntry[] entries = getEntries();
        String item = (entries[i].getOption() + " " + entries[i].getTarget().replaceAll("<.*?>", ""));
        Point menu = getLocation();
        FontMetrics fm = methods.runeLite.getLoader().getGraphics().getFontMetrics(FontManager.getRunescapeBoldFont());
        int xOff = random(1, (fm.stringWidth(item) + MENU_SIDE_BORDER) - 1);
        int yOff = TOP_OF_MENU_BAR + (((MENU_ENTRY_LENGTH * i) + random(2, MENU_ENTRY_LENGTH - 2)));

        methods.mouse.move(menu.x + xOff, menu.y + yOff);
        sleep(random(50, 100));

        if (this.isOpen()) {
            methods.mouse.click(true);
            return true;
        }

        return false;
    }

    public Point getLocation() {
        return new Point(calculateX(), calculateY());
    }

    /**
     * Checks whether or not the menu is collapsed.
     *
     * @return <code>true</code> if the menu is collapsed; otherwise <code>false</code>.
     */
    public boolean isCollapsed() {
        return !methods.client.isMenuOpen();
    }

    /**
     * Checks whether or not the menu is open.
     *
     * @return <code>true</code> if the menu is open; otherwise <code>false</code>.
     */
    public boolean isOpen() {
        return methods.client.isMenuOpen();
    }

    /**
     * Strips HTML tags.
     *
     * @param input The string you want to parse.
     * @return The parsed {@code String}.
     */
    public static String stripFormatting(String input) {
        return HTML_TAG.matcher(input).replaceAll("");
    }

    /**
     * Calculates the width of the menu
     *
     * @return the menu width
     */
    protected int calculateWidth() {
        MenuEntry[] entries = getEntries();
        final int MIN_MENU_WIDTH = 102;
        FontMetrics fm = methods.runeLite.getLoader().getGraphics().getFontMetrics(FontManager.getRunescapeBoldFont());
        int longestEntry = 0;
        for (MenuEntry entry : entries) longestEntry = (fm.stringWidth(entry.getOption() + " " +
                entry.getTarget().replaceAll("<.*?>", ""))
                > longestEntry) ? fm.stringWidth(entry.getOption() + " " +
                entry.getTarget().replaceAll("<.*?>", "")) : longestEntry;
        return (longestEntry + MENU_SIDE_BORDER < MIN_MENU_WIDTH) ? MIN_MENU_WIDTH : longestEntry + MENU_SIDE_BORDER;
    }

    /**
     * Calculates the height of the menu
     *
     * @return the menu height
     */
    protected int calculateHeight() {
        MenuEntry[] entries = getEntries();
        int numberOfEntries = entries.length;
        return MENU_ENTRY_LENGTH * numberOfEntries + TOP_OF_MENU_BAR;
    }

    /**
     * Calculates the top left corner X of the menu
     *
     * @return the menu x
     */
    protected int calculateX() {
        if (isOpen()) {
            final int MIN_MENU_WIDTH = 102;
            int width = calculateWidth();
            return (width + MENU_SIDE_BORDER < MIN_MENU_WIDTH) ? (methods.virtualMouse.getClientPressX() - (MIN_MENU_WIDTH / 2)) : (methods.virtualMouse.getClientPressX() - (width / 2));
        }
        return -1;
    }

    /**
     * Calculates the top left corner Y of the menu
     *
     * @return the menu y
     */
    protected int calculateY() {
        if (isOpen()) {
            final int CANVAS_LENGTH = methods.client.getCanvasHeight();
            MenuEntry[] entries = getEntries();
            int offset = CANVAS_LENGTH - (methods.virtualMouse.getClientPressY() + calculateHeight());
            if (offset < 0 && entries.length >= MAX_DISPLAYABLE_ENTRIES) {
                return 0;
            }
            if (offset < 0) {
                return methods.virtualMouse.getClientPressY() + offset;
            }
            return methods.virtualMouse.getClientPressY();
        }
        return -1;
    }

    public MenuEntry[] getEntries() {
        // gets from runelite
        MenuEntry[] entries = methods.client.getMenuEntries();
        MenuEntry[] reversed = new MenuEntry[entries.length];
        for (int i = entries.length - 1, x = 0; i >= 0; i--, x++)
            reversed[i] = entries[x];
        return reversed;
    }

    public String[] getEntriesString() {
        MenuEntry[] entries = getEntries();
        String[] entryStrings = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            entryStrings[i] = stripFormatting(entries[i].getOption()) + " " + ((entries[i].getTarget() != null) ? stripFormatting(entries[i].getTarget()) : "");
        }
        return entryStrings;
    }

    /**
     * Returns the index in the menu for a given action. Starts at 0.
     *
     * @param action The action that you want the index of.
     * @return The index of the given target in the context menu; otherwise -1.
     */
    private int getIndex(String action) {
        // note this can return the first one, which might not be what you want
        action = action.toLowerCase();

        MenuEntry[] entries = getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                continue;
            }

            // XXX can this be null?
            String menuAction = entries[i].getOption().toLowerCase();

            if (menuAction.contains(action)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the index in the menu for a given action with a given target.
     * Starts at 0.
     *
     * @param action The action of the menu entry of which you want the index.
     * @param target The target of the menu entry of which you want the index.
     *               If target is null, operates like getIndex(String action).
     * @return The index of the given target in the context menu; otherwise -1.
     */
    private int getIndex(String action, String target) {
        if (target == null) {
            return getIndex(action);
        }

        action = action.toLowerCase();
        target = target.toLowerCase();

        MenuEntry[] entries = getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                continue;
            }

            // XXX can these be null?
            String menuAction = entries[i].getOption().toLowerCase();
            String menuTarget = entries[i].getTarget().toLowerCase();

            if (menuAction.contains(action) && menuTarget.contains(target)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks whether or not a given action (or action substring) is present in
     * the menu.
     *
     * @param action The action or action substring.
     * @return <code>true</code> if present, otherwise <code>false</code>.
     */
    public boolean contains(final String action) {
        return getIndex(action) != -1;
    }

    /**
     * Checks whether or not a given action with given target is present
     * in the menu.
     *
     * @param action The action or action substring.
     * @param target The target or target substring.
     * @return <code>true</code> if present, otherwise <code>false</code>.
     */
    public boolean contains(final String action, final String target) {
        return getIndex(action, target) != -1;
    }
}
