package com.viveek.aiclass.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailUtilsTest {

    @Test
    void constructor_ThrowsException() {
        assertThatThrownBy(() -> {
            // Use reflection to invoke the private constructor
            java.lang.reflect.Constructor<EmailUtils> constructor = EmailUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void normalizeEmail_ConvertsToLowerCase() {
        String email = "User@Example.COM";
        
        String result = EmailUtils.normalizeEmail(email);
        
        assertThat(result).isEqualTo("user@example.com");
    }

    @Test
    void normalizeEmail_TrimsWhitespace() {
        String email = "  user@example.com  ";
        
        String result = EmailUtils.normalizeEmail(email);
        
        assertThat(result).isEqualTo("user@example.com");
    }

    @Test
    void normalizeEmail_HandlesNull() {
        String result = EmailUtils.normalizeEmail(null);
        
        assertThat(result).isNull();
    }

    @Test
    void normalizeEmail_CombinesLowerCaseAndTrim() {
        String email = "  User@EXAMPLE.com  ";
        
        String result = EmailUtils.normalizeEmail(email);
        
        assertThat(result).isEqualTo("user@example.com");
    }

    @Test
    void isValidEmail_ValidEmail() {
        assertThat(EmailUtils.isValidEmail("user@example.com")).isTrue();
        assertThat(EmailUtils.isValidEmail("test.user@domain.co.uk")).isTrue();
        assertThat(EmailUtils.isValidEmail("User@Example.COM")).isTrue();
    }

    @Test
    void isValidEmail_InvalidEmail() {
        assertThat(EmailUtils.isValidEmail(null)).isFalse();
        assertThat(EmailUtils.isValidEmail("")).isFalse();
        assertThat(EmailUtils.isValidEmail("   ")).isFalse();
        assertThat(EmailUtils.isValidEmail("@example.com")).isFalse();
        assertThat(EmailUtils.isValidEmail("user@")).isFalse();
        assertThat(EmailUtils.isValidEmail("userexample.com")).isFalse();
    }

    @Test
    void isValidEmail_EdgeCases() {
        assertThat(EmailUtils.isValidEmail("a@b.c")).isTrue();
        assertThat(EmailUtils.isValidEmail("  user@example.com  ")).isTrue();
    }
}


