package net.bzethmayr.gigantspinosaurus.util;

import net.zethmayr.fungu.test.ExampleCheckedException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClosingChainTest {

    @Test
    void initial_givenResource_whenClose_closesResource() {
        final AutoCloseable res = mock();
        ClosingChain underTest = new ClosingChain(res);

        underTest.close();

        assertDoesNotThrow(() -> verify(res).close());
    }

    private void setUpCloseThrows(final AutoCloseable res) {
        assertDoesNotThrow(() ->
                doThrow(new ExampleCheckedException("close")).when(res).close());
    }

    @Test
    void initial_givenResource_whenCloseThrows_throwsWrapped() {
        final AutoCloseable res = mock();
        ClosingChain underTest = new ClosingChain(res);
        setUpCloseThrows(res);

        final RuntimeException thrown = assertThrows(RuntimeException.class, underTest::close);

        assertThat(thrown.getCause(), instanceOf(ExampleCheckedException.class));
        assertDoesNotThrow(() -> verify(res).close());
    }

    @Test
    void link_givenResource_whenClose_closesSecondThenFirst() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.link(second);

        underTest.close();

        final InOrder order = inOrder(second, first);
        assertDoesNotThrow(() -> {
            order.verify(second).close();
            order.verify(first).close();
        });
    }

    @Test
    void link_givenResource_whenFirstCloseThrows_hasClosedSecond() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.link(second);
        setUpCloseThrows(first);

        final RuntimeException thrown = assertThrows(RuntimeException.class, underTest::close);

        assertThat(thrown.getCause(), instanceOf(ExampleCheckedException.class));
        final InOrder order = inOrder(second, first);
        assertDoesNotThrow(() -> {
            order.verify(second).close();
            order.verify(first).close();
        });
    }

    @Test
    void link_givenResource_whenSecondCloseThrows_stillClosesFirst() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.link(second);
        setUpCloseThrows(second);

        final RuntimeException thrown = assertThrows(RuntimeException.class, underTest::close);

        assertThat(thrown.getCause(), instanceOf(ExampleCheckedException.class));
        final InOrder order = inOrder(second, first);
        assertDoesNotThrow(() -> {
            order.verify(second).close();
            order.verify(first).close();
        });
    }

    @Test
    void link_givenResource_whenBothCloseThrow_stillClosesBoth() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.link(second);
        setUpCloseThrows(first);
        setUpCloseThrows(second);

        final RuntimeException thrown = assertThrows(RuntimeException.class, underTest::close);

        assertThat(thrown.getCause(),
                instanceOf(ExampleCheckedException.class));
        final InOrder order = inOrder(second, first);
        assertDoesNotThrow(() -> {
            order.verify(second).close();
            order.verify(first).close();
        });
    }

    @Test
    void swap_givenResource_whenClose_closesFirstThenSecond() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.swap(second);

        underTest.close();

        final InOrder order = inOrder(first, second);
        assertDoesNotThrow(() -> {
            order.verify(first).close();
            order.verify(second).close();
        });
    }

    @Test
    void swap_givenResource_whenBothCloseThrow_stillClosesFirstThenSecond() {
        final AutoCloseable first = mock();
        ClosingChain underTest = new ClosingChain(first);
        final AutoCloseable second = mock();
        underTest = underTest.swap(second);
        setUpCloseThrows(first);
        setUpCloseThrows(second);

        final RuntimeException thrown = assertThrows(RuntimeException.class, underTest::close);

        assertThat(thrown.getCause().getCause(),
                instanceOf(ExampleCheckedException.class));
        final InOrder order = inOrder(first, second);
        assertDoesNotThrow(() -> {
            order.verify(first).close();
            order.verify(second).close();
        });
    }

}