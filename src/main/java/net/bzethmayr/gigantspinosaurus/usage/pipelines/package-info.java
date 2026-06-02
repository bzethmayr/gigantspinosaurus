/**
 * GPU-accelerated reduction pipelines.
 *
 * @implSpec these implementations call into Vulkan code
 * and therefore must not be exposed to mutation testing,
 * at risk of JVM crash, system crash, or hardware damage.
 */
package net.bzethmayr.gigantspinosaurus.usage.pipelines;