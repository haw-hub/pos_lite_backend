package com.pos.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputSafetyTest {
    @Test
    void allowsMyanmarPlainText() {
        assertEquals("ရွှေမြန်မာ စတိုး", InputSafety.plainText(" ရွှေမြန်မာ စတိုး ", "Shop name", 255, true));
    }

    @Test
    void rejectsHtmlAndScriptProtocol() {
        assertThrows(IllegalArgumentException.class,
                () -> InputSafety.plainText("<script>alert(1)</script>", "Product name", 255, true));
        assertThrows(IllegalArgumentException.class,
                () -> InputSafety.plainText("javascript:alert(1)", "Product name", 255, true));
    }

    @Test
    void restrictsUsernameCharacters() {
        assertEquals("shop.admin-1", InputSafety.username("Shop.Admin-1"));
        assertThrows(IllegalArgumentException.class, () -> InputSafety.username("<admin>"));
    }
}
