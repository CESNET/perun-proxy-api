package cz.muni.ics.perunproxyapi.application.facade.configuration.classes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListOfServicesDisplayedAttribute {

    @JsonAlias({"sourceAttrName", "source_attr_name"})
    @NonNull private String sourceAttrName;
    @JsonAlias({"isMultiLanguage", "is_multi_language"})
    private boolean isMultiLanguage = false;
    @JsonAlias({"urlSourceAttr", "url_source_attr"})
    private String urlSourceAttr = null;
    @JsonAlias({"localizationKey", "localization_key"})
    @NonNull private String localizationKey;

}
