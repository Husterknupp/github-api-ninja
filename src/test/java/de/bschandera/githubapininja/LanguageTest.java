package de.bschandera.githubapininja;

import de.bschandera.githubapininja.Language;
import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;
import net.sf.qualitycheck.exception.IllegalNegativeArgumentException;
import org.junit.Test;

import java.math.BigDecimal;

public class LanguageTest {

    @Test(expected = IllegalNegativeArgumentException.class)
    public void testCreateWithNegativeNumberOfBytes() {
        new Language("narf-language", BigDecimal.valueOf(-1l));
    }

    @Test(expected = IllegalEmptyArgumentException.class)
    public void testCreateWithEmptyLanguageName() {
        new Language("", BigDecimal.valueOf(100));
    }

}