package cz.muni.ics.perunproxyapi.presentation.rest.localization;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@Configuration
@Component("messageSource")
public class CustomMessageSource extends AbstractMessageSource {

    @Value("${localization.path}")
    private String externalMessagesPath;

    private static final String MESSAGES_EXTERNAL = "messages_external";
    private static final String MESSAGES = "messages";

    @SneakyThrows
    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        File file = new File(externalMessagesPath);
        URL[] urls = {file.toURI().toURL()};
        ClassLoader loader = new URLClassLoader(urls);
        ResourceBundle externalResourceBundle = ResourceBundle.getBundle(MESSAGES_EXTERNAL, locale, loader);

        try {
            return new MessageFormat(externalResourceBundle.getString(key), locale);
        } catch (Exception ex) {
            // just ignore this exception and skip to the second resource bundle
        }

        ResourceBundle internalResourceBundle = ResourceBundle.getBundle(MESSAGES, locale);
        return new MessageFormat(internalResourceBundle.getString(key), locale);
    }

}
