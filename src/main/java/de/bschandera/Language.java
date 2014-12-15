package de.bschandera;

import java.math.BigDecimal;

public class Language {

    private final String name;
    private final BigDecimal bytes;

    public Language(String name, BigDecimal bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    /**
     * Number of bytes that are written in this specific language for a given repository.
     *
     * @return
     */
    public BigDecimal getBytes() {
        return bytes;
    }
}
