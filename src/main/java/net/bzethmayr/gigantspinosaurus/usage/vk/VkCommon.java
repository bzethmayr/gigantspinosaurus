package net.bzethmayr.gigantspinosaurus.usage.vk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static net.zethmayr.fungu.PredicateFactory.anyOf;
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

    @SafeVarargs
    static Predicate<String> optionalAny(final Predicate<String>... anyOf) {
        return anyOf.length == 0
                ? s -> true
                : anyOf(anyOf);
    }

    @SafeVarargs
    static List<String> filteredList(final int size, final Predicate<String>... filters) {
        return filters.length == 0
                ? new ArrayList<>(size)
                : new ArrayList<>();
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
