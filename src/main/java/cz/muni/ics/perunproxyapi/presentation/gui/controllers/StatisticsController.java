package cz.muni.ics.perunproxyapi.presentation.gui.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics.StatisticsDTO;
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

import javax.servlet.http.HttpServletRequest;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.GUI;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.IDP_IDENTIFIER;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.NO_AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.RP_IDENTIFIER;

@Controller
@RequestMapping(value = NO_AUTH_PATH)
@Slf4j
public class StatisticsController {

    public static final String RP = "rp";
    public static final String IDP = "idp";
    public static final String STATS_PATH = "/statistics";
    public static final String RP_PATH = '/' + RP;
    public static final String IDP_PATH = '/' + IDP;

    public static final String STATS_VIEW = "statistics";
    public static final String STATS_DETAIL_VIEW = "statistics_detail";

    public static final String IDP_DATA = "idpData";
    public static final String IDP_LOGINS_CNT = "idpLoginsTotalCount";
    public static final String RP_DATA = "rpData";
    public static final String RP_LOGINS_CNT = "rpLoginsTotalCount";
    public static final String LOGINS_DATA = "loginsData";
    public static final String LABEL = "label";
    public static final String MODE = "mode";

    @NonNull private final GuiFacade facade;
    @NonNull private final GuiProperties guiProperties;

    @Autowired
    public StatisticsController(@NonNull GuiFacade facade, @NonNull GuiProperties guiProperties) {
        this.facade = facade;
        this.guiProperties = guiProperties;
    }

    @GetMapping(GUI + STATS_PATH)
    public ModelAndView displayStatistics(@NonNull HttpServletRequest req) throws JsonProcessingException {
        ModelAndView mav = new ModelAndView(STATS_VIEW);
        String url = req.getRequestURL().toString();
        StatisticsDTO data = facade.getAllStatistics(url);
        addDataToModel(mav, data);
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + STATS_PATH + RP_PATH + "/{rp-identifier}")
    public ModelAndView displayStatisticsForRp(@NonNull HttpServletRequest req,
                                               @NonNull @PathVariable(RP_IDENTIFIER) String rpIdentifier)
            throws EntityNotFoundException, InvalidRequestParameterException, JsonProcessingException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("RP identifier cannot be empty");
        }
        String identifierDecoded = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        ModelAndView mav = new ModelAndView(STATS_DETAIL_VIEW);
        String url = req.getRequestURL().toString().replaceFirst(RP_PATH + "/.*", "");
        StatisticsDTO data = facade.getStatisticsForRp(url, identifierDecoded);
        mav.addObject(MODE, RP);
        addDataToModel(mav, data);
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    @GetMapping(GUI + STATS_PATH + IDP_PATH + "/{idp-identifier}")
    public ModelAndView displayStatisticsForIdp(@NonNull HttpServletRequest req,
                                                @NonNull @PathVariable(IDP_IDENTIFIER) String idpIdentifier)
            throws EntityNotFoundException, InvalidRequestParameterException, JsonProcessingException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        String identifierDecoded = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);
        ModelAndView mav = new ModelAndView(STATS_DETAIL_VIEW);
        String url = req.getRequestURL().toString().replaceFirst(IDP_PATH + "/.*", "");
        StatisticsDTO data = facade.getStatisticsForIdp(url, identifierDecoded);
        mav.addObject(MODE, IDP);
        addDataToModel(mav, data);
        GuiUtils.addCommonGuiOptions(mav, guiProperties);
        return mav;
    }

    private void addDataToModel(ModelAndView mav, StatisticsDTO data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mav.addObject(IDP_DATA, mapper.writeValueAsString(data.getIdpData()));
        mav.addObject(IDP_LOGINS_CNT, data.getLoginsIdpTotal());
        mav.addObject(RP_DATA, mapper.writeValueAsString(data.getRpData()));
        mav.addObject(RP_LOGINS_CNT, data.getLoginsRpTotal());
        mav.addObject(LOGINS_DATA, mapper.writeValueAsString(data.getLoginsData()));
        mav.addObject(LABEL, mapper.writeValueAsString(data.getLabel()));
    }

}
