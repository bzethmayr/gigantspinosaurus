package net.bzethmayr.gigantspinosaurus.usage.defaults.windows;

import net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment;
import net.bzethmayr.gigantspinosaurus.usage.defaults.desktop.DesktopOrientation;
import net.bzethmayr.gigantspinosaurus.usage.defaults.desktop.DesktopPosition;

import static net.bzethmayr.gigantspinosaurus.usage.defaults.DefaultEnvironments.partialEnvironment;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseFactory;

public final class WindowsEnvironments {
    private WindowsEnvironments() {
        throw becauseFactory();
    }

    public static BindsEnvironment windowsPermanentEnvironment() {
        return partialEnvironment()
                .withPosition(new DesktopPosition())
                .withOrientation(new DesktopOrientation())
                .withSignatory(new WindowsCredentialSignatory());
    }
}
