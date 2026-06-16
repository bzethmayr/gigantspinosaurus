package net.bzethmayr.gigantspinosaurus.usage.defaults.windows;

import com.sun.jna.Structure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowsCredentialSignatoryTest {

    @Test
    void credentialTargets_areCorrectConstants() {
        assertEquals("gigantspinosaurus/mar/ed25519/priv", WindowsCredentialSignatory.CRED_TARGET_PRIV);
        assertEquals("gigantspinosaurus/mar/ed25519/pub", WindowsCredentialSignatory.CRED_TARGET_PUB);
    }

    @Test
    void credentialStruct_hasCorrectFieldOrder() {
        final var ann = WindowsCredentialSignatory.CREDENTIAL.class
                .getAnnotation(Structure.FieldOrder.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"Flags", "Type", "TargetName", "Comment", "LastWritten",
                "CredentialBlobSize", "CredentialBlob", "Persist", "AttributeCount",
                "Attributes", "TargetAlias", "UserName"}, ann.value());
    }

    @Test
    void credentialHasZeroArgConstructor() {
        assertDoesNotThrow((org.junit.jupiter.api.function.Executable) WindowsCredentialSignatory.CREDENTIAL::new);
    }

    @Test
    void credentialHasPointerConstructor() {
        assertDoesNotThrow((org.junit.jupiter.api.function.Executable) () -> new WindowsCredentialSignatory.CREDENTIAL(null));
    }

    @Test
    void filetimeStruct_hasCorrectFieldOrder() {
        final var ft = new WindowsCredentialSignatory.FILETIME();
        assertEquals(List.of("dwLowDateTime", "dwHighDateTime"), ft.getFieldOrder());
    }

    @Test
    void filetimeHasZeroArgConstructor() {
        assertDoesNotThrow(WindowsCredentialSignatory.FILETIME::new);
    }
}
