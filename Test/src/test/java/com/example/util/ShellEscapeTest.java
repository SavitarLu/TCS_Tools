package com.example.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellEscapeTest {

    @Test
    void wrapsPlainArgumentInSingleQuotes() {
        assertEquals("'abc'", ShellEscape.quote("abc"));
    }

    @Test
    void keepsSpacesInsideOneArgument() {
        assertEquals("'/home/f3tst/mm/applog'", ShellEscape.quote("/home/f3tst/mm/applog"));
    }

    @Test
    void escapesEmbeddedSingleQuote() {
        assertEquals("'it'\\''s'", ShellEscape.quote("it's"));
    }

    @Test
    void neutralizesCommandSubstitution() {
        assertEquals("'$(rm -rf /)'", ShellEscape.quote("$(rm -rf /)"));
    }

    @Test
    void handlesNullAndEmpty() {
        assertEquals("''", ShellEscape.quote(null));
        assertEquals("''", ShellEscape.quote(""));
    }
}
