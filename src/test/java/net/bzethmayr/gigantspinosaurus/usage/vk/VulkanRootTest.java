package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.bzethmayr.gigantspinosaurus.gpu.GpuProgram;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VulkanRootTest {

    @Test
    void instance_exposesInstanceToConsumer() {
        try (final VulkanRoot underTest = new VulkanRoot()) {
            assertNotNull(underTest.instance());
        }
    }

    @Test
    void computeShader_testu_transformsData() throws Exception {
        var spvBytes = VulkanRootTest.class.getResourceAsStream("/spv/testu.spv").readAllBytes();
        var spvBuffer = ByteBuffer.allocateDirect(spvBytes.length);
        spvBuffer.put(spvBytes);
        spvBuffer.flip();

        var bindings = List.of(
                new GpuProgram.ResourceBinding(0, GpuProgram.ResourceKind.STORAGE_BUFFER),
                new GpuProgram.ResourceBinding(1, GpuProgram.ResourceKind.STORAGE_BUFFER)
        );
        var desc = new GpuProgram.ProgramDesc("main", GpuProgram.ShaderStage.COMPUTE,
                spvBuffer, bindings, 8);

        try (var root = new VulkanRoot();
             var program = root.createProgram(desc)) {

            int numElements = 64;
            var inputData = new int[numElements];
            var expected = new int[numElements];
            for (int i = 0; i < numElements; i++) {
                inputData[i] = i + 1;
                expected[i] = ((i + 1) << 1) ^ 0x5A5A5A5A;
            }

            var bufferDesc = new GpuBuffer.BufferDesc(
                    (long) numElements * Integer.BYTES,
                    GpuBuffer.BufferUsage.STORAGE,
                    GpuBuffer.MemoryHint.CPU_VISIBLE);

            try (var input = root.createBuffer(bufferDesc);
                 var output = root.createBuffer(bufferDesc)) {

                var inputBuf = ByteBuffer.allocateDirect(numElements * Integer.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN);
                inputBuf.asIntBuffer().put(inputData);
                inputBuf.position(numElements * Integer.BYTES);
                inputBuf.flip();
                input.upload(0, inputBuf);

                var pushConstants = ByteBuffer.allocateDirect(8)
                        .order(ByteOrder.LITTLE_ENDIAN);
                pushConstants.putInt(0, 8).putInt(4, 8);

                root.withProgram(program, loan ->
                        loan.bindBuffer(0, input)
                                .bindBuffer(1, output)
                                .setScalars(pushConstants)
                                .dispatch(1, 1, 1));

                var result = ByteBuffer.allocateDirect(numElements * Integer.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN);
                output.download(0, result);
                result.flip();
                var actual = new int[numElements];
                result.asIntBuffer().get(actual);

                assertArrayEquals(expected, actual);
            }
        }
    }
}