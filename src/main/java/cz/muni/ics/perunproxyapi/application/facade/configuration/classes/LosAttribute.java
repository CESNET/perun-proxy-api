package cz.muni.ics.perunproxyapi.application.facade.configuration.classes;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LosAttribute {

    @JsonAlias({"sourceAttrName", "source_attr_name"})
    private String sourceAttrName;
    @JsonAlias({"isMultiLanguage", "is_multi_language"})
    private boolean isMultiLanguage;
    @JsonAlias({"isUrl", "is_url"})
    private boolean isUrl = false;
    @JsonAlias({"urlSourceAttr", "url_source_attr"})
    private String urlSourceAttr = null;

}
