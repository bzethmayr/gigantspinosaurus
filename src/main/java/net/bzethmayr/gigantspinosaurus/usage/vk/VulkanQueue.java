package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.vulkan.VkQueue;

record VulkanQueue(int familyIndex, VkQueue vkQueue) {
}
