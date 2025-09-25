package org.unipus.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.plaf.FontUIResource;
import javax.swing.UIDefaults;

public class LightTheme extends FlatLightLaf {
    @Override
    public String getName() {
        return "IntelliJ Light";
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults defaults = super.getDefaults();
        defaults.put("Component.arc", 8);
        defaults.put("Button.arc", 8);
        defaults.put("TextComponent.arc", 8);
        defaults.put("TitlePane.background", defaults.get("Panel.background"));
        defaults.put("TitlePane.foreground", defaults.get("Label.foreground"));
        defaults.put("Button.background", "#e6e6e6");
        defaults.put("Button.foreground", "#222222");
        defaults.put("Panel.background", "#f5f5f5");
        defaults.put("Label.foreground", "#222222");
        defaults.put("font", new FontUIResource("JetBrains Mono", 0, 14));
        return defaults;
    }
}
