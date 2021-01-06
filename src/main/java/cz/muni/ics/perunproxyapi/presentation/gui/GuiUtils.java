package cz.muni.ics.perunproxyapi.presentation.gui;

import org.springframework.web.servlet.ModelAndView;

public class GuiUtils {

    public static final String HEADER = "header_path";
    public static final String FOOTER = "footer_path";
    public static final String LANGUAGE_BAR_ENABLED = "language_bar_enabled";

    public static ModelAndView addCommonGuiOptions(ModelAndView mav, GuiProperties guiProperties) {
        mav.addObject(HEADER, guiProperties.getHeaderPath());
        mav.addObject(FOOTER, guiProperties.getFooterPath());
        mav.addObject(LANGUAGE_BAR_ENABLED, guiProperties.isLanguageBarEnabled());
        return mav;
    }

}
