package cz.muni.ics.perunproxyapi.presentation.gui.controllers;

import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.presentation.gui.GuiProperties;
import cz.muni.ics.perunproxyapi.presentation.gui.GuiUtils;
import cz.muni.ics.perunproxyapi.presentation.rest.controllers.ControllerUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.GUI;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.IDP_IDENTIFIER;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.NO_AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.RP_IDENTIFIER;

@Controller
@RequestMapping(value = NO_AUTH_PATH)
@Slf4j
public class StatisticsController {

    public static final String STATS_VIEW = "stats_view";

    @NonNull private final GuiFacade facade;
    @NonNull private final GuiProperties guiProperties;

    @Autowired
    public StatisticsController(@NonNull GuiFacade facade, @NonNull GuiProperties guiProperties) {
        this.facade = facade;
        this.guiProperties = guiProperties;
    }

    @GetMapping(GUI + "/statistics")
    public ModelAndView displayStatistics() {
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        facade.getAllStatistics();
        //TODO: process
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + "/statistics/rp")
    public ModelAndView displayStatisticsForRps() {
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        facade.getStatisticsForRps();
        //TODO: process
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + "/statistics/idp")
    public ModelAndView displayStatisticsForIdps() {
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        facade.getStatisticsForIdPs();
        //TODO: process
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + "/statistics/rp/{rp-identifier}")
    public ModelAndView displayStatisticsForRp(@NonNull @PathVariable(RP_IDENTIFIER) String rpIdentifier)
            throws EntityNotFoundException, InvalidRequestParameterException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("RP identifier cannot be empty");
        }
        String identifierDecoded = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        facade.getStatisticsForRp(identifierDecoded);
        //TODO: process
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + "/statistics/idp/{idp-identifier}")
    public ModelAndView displayStatisticsForIdp(@NonNull @PathVariable(IDP_IDENTIFIER) String idpIdentifier)
            throws EntityNotFoundException, InvalidRequestParameterException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        String identifierDecoded = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        facade.getStatisticsForIdp(identifierDecoded);
        //TODO: process
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

}
