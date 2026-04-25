# Vulkan

## notes

example initialization for initialization structs...
```java
var appInfo = VkApplicationInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationName(appShortName)
                    .applicationVersion(1)
                    .pEngineName(appShortName)
                    .engineVersion(0)
                    .apiVersion(VK_API_VERSION_1_3);
var inst = VkInstanceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pNext(extension)   // e.g. debugger, ...
                    .pApplicationInfo(appInfo)   // VkApplicationInfo
                    .ppEnabledLayerNames(requiredLayers) // PointerBuffer
                    .ppEnabledExtensionNames(requiredExtensions); // PointerBuffer
```

stack scope isn't awful.

We want to...
- arrange comprehensive teardown for all this stuff
- enable any extensions that the user expects enabled
  - so we need to determine whether the configurator overrides that for us

- discover all our physical devices and pick the nice one
- discover all our compute queues and get hold of one