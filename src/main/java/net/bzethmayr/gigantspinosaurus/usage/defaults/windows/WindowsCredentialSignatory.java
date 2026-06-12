package net.bzethmayr.gigantspinosaurus.usage.defaults.windows;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import net.bzethmayr.gigantspinosaurus.usage.defaults.PermanentSignatory;

import java.util.List;

public class WindowsCredentialSignatory extends PermanentSignatory {
    static final String CRED_TARGET_PRIV = "gigantspinosaurus/mar/ed25519/priv";
    static final String CRED_TARGET_PUB = "gigantspinosaurus/mar/ed25519/pub";
    private static final int CRED_TYPE_GENERIC = 1;
    private static final int CRED_PERSIST_LOCAL_MACHINE = 2;

    public WindowsCredentialSignatory() {
        super();
    }

    @Override
    protected byte[] loadPrivateKeyBytes() {
        return readCredential(CRED_TARGET_PRIV);
    }

    @Override
    protected byte[] loadPublicKeyBytes() {
        return readCredential(CRED_TARGET_PUB);
    }

    @Override
    protected void storePrivateKeyBytes(final byte[] privKeyBytes) {
        writeCredential(CRED_TARGET_PRIV, privKeyBytes);
    }

    @Override
    protected void storePublicKeyBytes(final byte[] pubKeyBytes) {
        writeCredential(CRED_TARGET_PUB, pubKeyBytes);
    }

    private static byte[] readCredential(final String targetName) {
        final PointerByReference pCredential = new PointerByReference();
        try {
            if (CredUtil.INSTANCE.CredReadW(new WString(targetName), CRED_TYPE_GENERIC, 0, pCredential)) {
                final CREDENTIAL cred = new CREDENTIAL(pCredential.getValue());
                cred.read();
                return cred.CredentialBlob.getByteArray(0, cred.CredentialBlobSize);
            }
            return null;
        } finally {
            if (pCredential.getValue() != null) {
                CredUtil.INSTANCE.CredFree(pCredential.getValue());
            }
        }
    }

    private static void writeCredential(final String targetName, final byte[] blob) {
        final CREDENTIAL cred = new CREDENTIAL();
        cred.Flags = 0;
        cred.Type = CRED_TYPE_GENERIC;
        cred.TargetName = targetName;
        cred.CredentialBlobSize = blob.length;
        final Memory blobMem = new Memory(blob.length);
        blobMem.write(0, blob, 0, blob.length);
        cred.CredentialBlob = blobMem;
        cred.Persist = CRED_PERSIST_LOCAL_MACHINE;
        cred.UserName = "gigantspinosaurus";
        cred.write();
        try {
            if (!CredUtil.INSTANCE.CredWriteW(cred, 0)) {
                throw new RuntimeException("CredWriteW failed: error " + Native.getLastError());
            }
        } finally {
            blobMem.clear(blob.length);
        }
    }

    interface CredUtil extends StdCallLibrary {
        CredUtil INSTANCE = Native.load("Advapi32", CredUtil.class, W32APIOptions.UNICODE_OPTIONS);

        boolean CredReadW(WString targetName, int type, int flags, PointerByReference pCredential);
        boolean CredWriteW(CREDENTIAL credential, int flags);
        void CredFree(Pointer cred);
    }

    @Structure.FieldOrder({"Flags", "Type", "TargetName", "Comment", "LastWritten", "CredentialBlobSize",
            "CredentialBlob", "Persist", "AttributeCount", "Attributes", "TargetAlias", "UserName"})
    public static class CREDENTIAL extends Structure {
        public int Flags;
        public int Type;
        public String TargetName;
        public String Comment;
        public FILETIME LastWritten;
        public int CredentialBlobSize;
        public Pointer CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public Pointer Attributes;
        public String TargetAlias;
        public String UserName;

        public CREDENTIAL() {
            super();
        }

        public CREDENTIAL(final Pointer p) {
            super(p);
        }
    }

    public static class FILETIME extends Structure {
        public int dwLowDateTime;
        public int dwHighDateTime;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("dwLowDateTime", "dwHighDateTime");
        }
    }
}
