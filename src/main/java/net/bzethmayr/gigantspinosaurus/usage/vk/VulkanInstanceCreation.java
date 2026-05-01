package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Predicate;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VkCommon.OSType.MACOS;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VkCommon.*;
import static org.lwjgl.vulkan.KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR;
import static org.lwjgl.vulkan.VK10.*;

class VulkanInstanceCreation {
    static VkApplicationInfo appInfo(final MemoryStack stack, final String app, final String engine) {
        final ByteBuffer appName = stack.UTF8(app);
        final ByteBuffer engineName = stack.UTF8(engine);
        return VkApplicationInfo.calloc(stack)
                .sType$Default()
                .pApplicationName(appName)
                .applicationVersion(1)
                .pEngineName(engineName)
                .engineVersion(1)
                .apiVersion(VK_API_VERSION_1_0);
    }

    @SafeVarargs
    static List<String> allLayerNames(final MemoryStack stack, final Predicate<String>... anyOf) {
        final IntBuffer layerBuf = stack.callocInt(1);
        vkEnumerateInstanceLayerProperties(layerBuf, null);
        final int numLayers = layerBuf.get(0);
        final VkLayerProperties.Buffer propsBuf = VkLayerProperties.calloc(numLayers, stack);
        vkEnumerateInstanceLayerProperties(layerBuf, propsBuf);
        final List<String> layers = filteredList(numLayers, anyOf);
        final Predicate<String> filter = optionalAny(anyOf);
        for (int i = 0; i < numLayers; i++) {
            final VkLayerProperties props = propsBuf.get(i);
            final String layerName = props.layerNameString();
            if (filter.test(layerName)) {
                layers.add(layerName);
            }
        }
        return layers;
    }

    @SafeVarargs
    static List<String> allExtensionNames(final MemoryStack stack, final Predicate<String>... anyOf) {
        final IntBuffer extensionBuf = stack.callocInt(1);
        vkEnumerateInstanceExtensionProperties((String) null, extensionBuf, null);
        final int numExtensions = extensionBuf.get(0);
        final VkExtensionProperties.Buffer propsBuf = VkExtensionProperties.calloc(numExtensions, stack);
        vkEnumerateInstanceExtensionProperties((String) null, extensionBuf, propsBuf);
        final List<String> extensions = filteredList(numExtensions, anyOf);
        final Predicate<String> filter = optionalAny(anyOf);
        for (int i = 0; i < numExtensions; i++) {
            final VkExtensionProperties props = propsBuf.get(i);
            final String extensionName = props.extensionNameString();
            if (filter.test(extensionName)) {
                extensions.add(extensionName);
            }
        }
        return extensions;
    }

    private static PointerBuffer layerNamesFrom(final MemoryStack stack, final List<String> names) {
        PointerBuffer namesBuf = null;
        if (!names.isEmpty()) {
            final int numNames = names.size();
            namesBuf = stack.mallocPointer(numNames);
            for (int i = 0; i < numNames; i++) {
                namesBuf.put(i, stack.ASCII(names.get(i)));
            }
        }
        return namesBuf;
    }

    private static PointerBuffer extensionNamesFrom(final MemoryStack stack, final List<String> names) {
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

    static VkInstanceCreateInfo instanceCreateInfo(
            final MemoryStack stack,
            final VkApplicationInfo appInfo,
            final List<String> layerNames,
            final List<String> extensionNames,
            final long... next
    ) {
        VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack)
                .sType$Default()
                .pApplicationInfo(appInfo)
                .ppEnabledLayerNames(layerNamesFrom(stack, layerNames))
                .ppEnabledExtensionNames(extensionNamesFrom(stack, extensionNames));
        for (long extension : next) {
            info = info.pNext(extension);
        }
        if (extensionNames.contains(PORTABILITY_EXTENSION)
                && getOS() == MACOS
        ) {
            info.flags(VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
        }
        return info;
    }

}
