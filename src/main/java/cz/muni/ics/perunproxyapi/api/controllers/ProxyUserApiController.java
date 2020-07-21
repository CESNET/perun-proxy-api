package cz.muni.ics.perunproxyapi.api.controllers;

import cz.muni.ics.perunproxyapi.facade.ProxyFacadeImpl;
import cz.muni.ics.perunproxyapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/proxyuser")
public class ProxyUserApiController {

    @Autowired
    private ProxyFacadeImpl facade;

    @RequestMapping(value = "/findByIdentifiers",method = RequestMethod.GET)
    public User findByIdentifiers(@RequestParam(value = "IdPIdentifier") String IdPIdentifier,
                                  @RequestParam(value = "identifiers") List<String> identifiers) {
        // Should be automatically converted to JSON - needs to be tested
        return facade.findByIdentifiers(IdPIdentifier, identifiers);
    }
}
