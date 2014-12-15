package de.bschandera;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class Repository {

    private List<Language> languages;
    private final String id;

    public Repository(String id, List<Language> languages) {
        this.id = id;
        this.languages = new ArrayList<>();
        for (Language language : languages) {
            this.languages.add(language);
        }
    }

    public List<Language> getLanguages() {
        return Lists.newArrayList(languages);
    }

    public String getId() {
        return id;
    }
}
