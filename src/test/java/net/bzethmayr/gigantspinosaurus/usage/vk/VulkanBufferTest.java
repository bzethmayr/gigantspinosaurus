package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class VulkanBufferTest {

    @Test
    void uploadThenDownload_returnsUploadedData() throws Exception {
        try (final VulkanRoot root = new VulkanRoot()) {
            final GpuBuffer.BufferDesc desc = new GpuBuffer.BufferDesc(
                    1024, GpuBuffer.BufferUsage.STORAGE, GpuBuffer.MemoryHint.CPU_VISIBLE);
            try (final GpuBuffer buffer = root.createBuffer(desc)) {
                final byte[] expected = {0x01, 0x02, 0x03, 0x04};
                final ByteBuffer upload = ByteBuffer.allocateDirect(4);
                upload.put(expected);
                upload.flip();

                buffer.upload(0, upload);

                final ByteBuffer download = ByteBuffer.allocateDirect(4);
                buffer.download(0, download);
                download.flip();

                final byte[] actual = new byte[4];
                download.get(actual);

                assertArrayEquals(expected, actual);
            }
        }
    }

    @Test
    void uploadToOffsetThenDownloadAtSameOffset_returnsUploadedData() throws Exception {
        try (final VulkanRoot root = new VulkanRoot()) {
            final GpuBuffer.BufferDesc desc = new GpuBuffer.BufferDesc(
                    1024, GpuBuffer.BufferUsage.STORAGE, GpuBuffer.MemoryHint.CPU_VISIBLE);
            try (final GpuBuffer buffer = root.createBuffer(desc)) {
                final byte[] expected = {0x10, 0x20, 0x30};
                final ByteBuffer upload = ByteBuffer.allocateDirect(3);
                upload.put(expected);
                upload.flip();

                buffer.upload(256, upload);

                final ByteBuffer download = ByteBuffer.allocateDirect(3);
                buffer.download(256, download);
                download.flip();

                final byte[] actual = new byte[3];
                download.get(actual);

                assertArrayEquals(expected, actual);
            }
        }
    }
}
