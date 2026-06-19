package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.*;
import static org.junit.jupiter.api.Assertions.*;

class VulkanCommonTest {

    @Test
    void constructor_throwsStaticsOnly() throws Exception {
        final var ctor = VulkanCommon.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertThrows(Throwable.class, ctor::newInstance);
    }

    @Test
    void getOS_onWindows_returnsWindows() {
        final var original = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");
            assertEquals(OSType.WINDOWS, getOS());
        } finally {
            System.setProperty("os.name", original);
        }
    }

    @Test
    void getOS_onMac_returnsMacos() {
        final var original = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Mac OS X");
            assertEquals(OSType.MACOS, getOS());
        } finally {
            System.setProperty("os.name", original);
        }
    }

    @Test
    void getOS_onLinux_returnsLinux() {
        final var original = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");
            assertEquals(OSType.LINUX, getOS());
        } finally {
            System.setProperty("os.name", original);
        }
    }

    @Test
    void getOS_onOther_returnsOther() {
        final var original = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "SunOS");
            assertEquals(OSType.OTHER, getOS());
        } finally {
            System.setProperty("os.name", original);
        }
    }

    @Test
    void checkVk_success_doesNotThrow() {
        assertDoesNotThrow(() -> checkVk(0, "test"));
    }

    @Test
    void checkVk_failure_throwsVulkanUsageException() {
        final var ex = assertThrows(VulkanUsageException.class, () -> checkVk(-1, "createBuffer"));
        assertTrue(ex.getMessage().contains("-1"));
        assertTrue(ex.getMessage().contains("createBuffer"));
    }

    @Test
    void indexOfMaxScorePassing_returnsIndexOfMax() {
        assertEquals(2, indexOfMaxScorePassing("nope", -1, 0, 5));
        assertEquals(0, indexOfMaxScorePassing("nope", 10));
        assertEquals(2, indexOfMaxScorePassing("nope", -5, -1, 3));
    }

    @Test
    void indexOfMaxScorePassing_allNegative_throws() {
        assertThrows(IllegalStateException.class,
                () -> indexOfMaxScorePassing("all negative", -1, -5, -3));
    }

    @Test
    void optionalAny_noPredicates_returnsAlwaysTrue() {
        final Predicate<String> underTest = optionalAny();
        assertTrue(underTest.test("anything"));
        assertTrue(underTest.test(""));
    }

    @Test
    void optionalAny_withPredicates_returnsCombined() {
        final Predicate<String> underTest = optionalAny(s -> s.startsWith("a"), s -> s.startsWith("b"));
        assertTrue(underTest.test("alpha"));
        assertTrue(underTest.test("beta"));
        assertFalse(underTest.test("gamma"));
    }

    @Test
    void filteredList_noFilters_returnsEmptyList() {
        final List<String> result = filteredList(3);
        assertTrue(result.isEmpty());
    }

    @Test
    void filteredList_withFilters_returnsEmptyList() {
        final List<String> result = filteredList(3, s -> true);
        assertTrue(result.isEmpty());
    }
}
