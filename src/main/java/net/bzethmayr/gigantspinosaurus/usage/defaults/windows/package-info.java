/**
 * Windows 11 reference permanent keypair implementations.
 *
 * <p>{@link net.bzethmayr.gigantspinosaurus.usage.defaults.windows.WindowsCredentialSignatory}
 * stores an Ed25519 private key seed in the Windows Credential Manager
 * ({@code Advapi32.dll} / {@code CredWriteW} / {@code CredReadW}) under the
 * target names {@code gigantspinosaurus/mar/ed25519/priv} and
 * {@code gigantspinosaurus/mar/ed25519/pub}, following the MAR naming convention.
 *
 * <p>On first invocation a new Ed25519 keypair is generated and persisted;
 * subsequent invocations reload the same keypair from the credential store.
 */
@HigherLevel
package net.bzethmayr.gigantspinosaurus.usage.defaults.windows;

import net.zethmayr.fungu.core.declarations.HigherLevel;
