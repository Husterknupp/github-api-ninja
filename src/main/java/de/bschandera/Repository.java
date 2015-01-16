package de.bschandera;

import com.google.common.collect.Lists;
import net.sf.qualitycheck.Check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Repository {
    private final String id;
    private String languageURL;
    private List<Language> languages;

    public Repository(String id, String languageURL) {
        this(id, languageURL, Collections.<Language>emptyList());
    }

    public Repository(String id, Collection<Language> languages) {
        this(id, "{repoName}/languages", languages);
    }

    public Repository(String id, String languageURL, Collection<Language> languages) {
        Check.notNull(id, "id");
        Check.notEmpty(languageURL, "languageURL");

        this.id = id;
        this.languageURL = languageURL;
        this.languages = new ArrayList<>(languages);
    }

    public void setLanguages(Collection<Language> languages) {
        Check.noNullElements(languages);
        this.languages = new ArrayList<>(languages);
    }

    public List<Language> getLanguages() {
        return Lists.newArrayList(languages);
    }

    public String getId() {
        return id;
    }

    public String getLanguagesURL() {
        return languageURL;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "id='" + id + '\'' +
                ", languageURL='" + languageURL + '\'' +
                ", languages=" + languages +
                '}';
    }
}
