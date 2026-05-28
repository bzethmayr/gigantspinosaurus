package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static net.zethmayr.fungu.PredicateFactory.anyOf;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public final class VulkanCommon {
    private VulkanCommon() {
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

    public static ByteBuffer javaBuffer(final int capacity) {
        return ByteBuffer.allocateDirect(capacity)
                .order(ByteOrder.LITTLE_ENDIAN);
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

    static PointerBuffer asciiNamesFrom(final MemoryStack stack, final List<String> names) {
        PointerBuffer namesBuf = null;
        if (!names.isEmpty()) {
            namesBuf = stack.mallocPointer(names.size());
            names.stream().map(stack::ASCII).forEach(namesBuf::put);
        }
        return namesBuf;
    }

    static PointerBuffer asciiNamesFlippedFrom(final MemoryStack stack, final List<String> names) {
        PointerBuffer namesBuf = null;
        if (!names.isEmpty()) {
            namesBuf = stack.mallocPointer(names.size());
            names.stream().map(stack::ASCII).forEach(namesBuf::put);
            namesBuf.flip();
        }
        return namesBuf;
    }

    static PointerBuffer utf8NamesFlippedFrom(final MemoryStack stack, final List<String> names) {
        PointerBuffer namesBuf = null;
        if (!names.isEmpty()) {
            final int numNames = names.size();
            namesBuf = stack.mallocPointer(numNames);
            for (int i = 0; i < numNames; i++) {
                namesBuf.put(i, stack.UTF8(names.get(i)));
            }
            namesBuf.flip();
        }
        return namesBuf;
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

    static int indexOfMaxScorePassing(final String nonePassing, final int... scores) {
        int maxScore = Integer.MIN_VALUE;
        final int size = scores.length;
        int maxAt = 0;
        for (int i = 0; i < size; i++) {
            if (scores[i] > maxScore) {
                maxAt = i;
                maxScore = scores[i];
            }
        }
        if (maxScore < 0) {
            throw becauseImpossible(nonePassing);
        }
        return maxAt;
    }
}
