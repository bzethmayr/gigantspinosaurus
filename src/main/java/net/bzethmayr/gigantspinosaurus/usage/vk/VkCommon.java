package net.bzethmayr.gigantspinosaurus.usage.vk;

import java.util.Locale;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

final class VkCommon {
    private VkCommon() {
        throw becauseStaticsOnly();
    }

    static final String PORTABILITY_EXTENSION = "VK_KHR_portability_enumeration";
    static final String VALIDATION_LAYER = "VK_LAYER_KHRONOS_validation";

    enum OSType {WINDOWS, MACOS, LINUX, OTHER}

    static OSType getOS() {
        OSType result;
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            result = OSType.MACOS;
        } else if (os.contains("win")) {
            result = OSType.WINDOWS;
        } else if (os.contains("nux")) {
            result = OSType.LINUX;
        } else {
            result = OSType.OTHER;
        }

        return result;
    }

    static class VulkanUsageException extends RuntimeException {
        public VulkanUsageException(int code, String context) {
            super("Vulkan %s at %s".formatted(code, context));
        }
    }

    static void checkVk(int invokedResult, String context) {
        if (invokedResult != VK_SUCCESS) {
            throw new VulkanUsageException(invokedResult, context);
        }
    }
}
