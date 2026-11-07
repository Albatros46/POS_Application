package com.sakcadag.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-with-at-least-32-bytes-0001";

    @Test
    void secretShorterThan32BytesIsRejected() {
        String thirtyOneBytes = "0123456789abcdefghijklmnopqrstu";

        assertThatThrownBy(() -> new JwtService(thirtyOneBytes, 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("app.jwt.secret must be at least 32 bytes long");
    }

    @Test
    void secretLengthIsMeasuredInUtf8Bytes() {
        // 15 characters of 2 bytes each is 30 bytes, which is still below the minimum.
        String fifteenTwoByteChars = "é".repeat(15);

        assertThatThrownBy(() -> new JwtService(fifteenTwoByteChars, 60))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretOf32BytesIsAccepted() {
        String thirtyTwoBytes = "0123456789abcdefghijklmnopqrstuv";

        assertThatCode(() -> new JwtService(thirtyTwoBytes, 60)).doesNotThrowAnyException();
    }

    @Test
    void validTokenRoundTripReturnsSubject() {
        JwtService service = new JwtService(SECRET, 60);

        String token = service.generateToken("alice");

        assertThat(service.parseUsername(token)).contains("alice");
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String token = new JwtService(SECRET, 60).generateToken("alice");

        assertThat(new JwtService(SECRET + "-other", 60).parseUsername(token)).isEmpty();
    }
}
