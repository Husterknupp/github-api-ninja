package de.bschandera.githubapininja;

import net.sf.qualitycheck.Check;

import java.math.BigDecimal;

public class Language {
    private final String name;
    private final BigDecimal bytes;

    /**
     * @param name  must not be an empty String.
     * @param bytes must not be a negative BigDecimal.
     */
    public Language(String name, BigDecimal bytes) {
        Check.notEmpty(name, "name");
        Check.notNegative(bytes.longValue(), "bytes");
        this.name = name;
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    /**
     * Number of bytes that are written in this specific language. This number only makes sense in a context of a repository.
     *
     * @return
     */
    public BigDecimal getBytes() {
        return bytes;
    }

    /**
     * @param o given object to compare this to.
     * @return If and only if this.name and this.bytes equal the given ones.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Language language = (Language) o;

        if (!bytes.equals(language.bytes)) return false;
        if (!name.equals(language.name)) return false;

        return true;
    }

    /**
     * Takes into account name and bytes.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + bytes.hashCode();
        return result;
    }

    /**
     * @return something like {@code Language{name='Java', bytes=12345}}
     */
    @Override
    public String toString() {
        return "Language{" +
                "name='" + name + '\'' +
                ", bytes=" + bytes +
                '}';
    }
}
