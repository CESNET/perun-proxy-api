package cz.muni.ics.perunproxyapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * AUP object model.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode
public class Aup {

    public static final String SIGNED_ON = "signed_on";

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Getter
    private String version;
    @Getter
    private String date;
    @Getter
    private String link;
    @Getter
    private String text;

    @JsonProperty(SIGNED_ON)
    private String signedOn = null;

    public Aup() {
    }

    public Aup(String version, String date, String link, String text, String signedOn) {
        this.setVersion(version);
        this.setDate(date);
        this.setLink(link);
        this.setText(text);
        this.setSignedOn(signedOn);
    }



    public void setVersion(String version) {
        if (Strings.isNullOrEmpty(version)) {
            throw new IllegalArgumentException("version cannot be null or empty");
        }

        this.version = version;
    }



    @JsonIgnore
    public LocalDate getDateAsLocalDate() {
        return LocalDate.parse(date, format);
    }

    public void setDate(String date) {
        if (Strings.isNullOrEmpty(date)) {
            throw new IllegalArgumentException("date cannot be null or empty");
        }

        this.date = date;
    }



    public void setLink(String link) {
        if (Strings.isNullOrEmpty(link)) {
            throw new IllegalArgumentException("link cannot be null or empty");
        }

        this.link = link;
    }



    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("version cannot be null");
        }

        this.text = text;
    }



    public void setSignedOn(String signedOn) {
        this.signedOn = signedOn;
    }
}

