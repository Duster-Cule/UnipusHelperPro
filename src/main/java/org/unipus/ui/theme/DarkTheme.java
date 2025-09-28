package org.unipus.ui.theme;

/* (っ*´Д`)っ 小代码要被看光啦 */

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.plaf.FontUIResource;
import javax.swing.UIDefaults;

public class DarkTheme extends FlatDarkLaf {
    @Override
    public String getName() {
        return "IntelliJ Dark";
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults defaults = super.getDefaults();
        defaults.put("Component.arc", 8);
        defaults.put("Button.arc", 8);
        defaults.put("TextComponent.arc", 8);
        defaults.put("TitlePane.background", defaults.get("Panel.background"));
        defaults.put("TitlePane.foreground", defaults.get("Label.foreground"));
        defaults.put("Button.background", "#2d2d2d");
        defaults.put("Button.foreground", "#e6e6e6");
        defaults.put("Panel.background", "#232323");
        defaults.put("Label.foreground", "#e6e6e6");
        defaults.put("font", new FontUIResource("JetBrains Mono", 0, 14));
        return defaults;
    }
}
