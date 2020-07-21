package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.impl.ProxyuserFacadeImpl;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/proxyuser")
public class ProxyUserApiController {

    private final ProxyuserFacadeImpl facade;

    @Autowired
    public ProxyUserApiController(ProxyuserFacadeImpl facade) {
        this.facade = facade;
    }

    @RequestMapping(value = "/findByIdentifiers",method = RequestMethod.GET)
    public User findByIdentifiers(@RequestParam(value = "IdPIdentifier") String IdPIdentifier,
                                  @RequestParam(value = "identifiers") List<String> identifiers) {
        // Should be automatically converted to JSON - needs to be tested
        return facade.findByIdentifiers(IdPIdentifier, identifiers);
    }
}
