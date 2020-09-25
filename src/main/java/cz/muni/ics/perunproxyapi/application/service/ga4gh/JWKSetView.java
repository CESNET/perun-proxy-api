package cz.muni.ics.perunproxyapi.application.service.ga4gh;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

@Component("jwkSet")
@Slf4j
public class JWKSetView extends AbstractView {
    public static final String VIEWNAME = "jwkSet";

    public JWKSetView() {
    }

    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        Map<String, JWK> keys = (Map)model.get("keys");
        JWKSet jwkSet = new JWKSet(new ArrayList(keys.values()));

        try {
            Writer out = response.getWriter();
            out.write(jwkSet.toString());
        } catch (IOException var7) {
            log.error("IOException in JWKSetView.java: ", var7);
        }

    }
}
