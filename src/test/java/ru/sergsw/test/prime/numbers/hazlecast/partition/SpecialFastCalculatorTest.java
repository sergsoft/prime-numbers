package ru.sergsw.test.prime.numbers.hazlecast.partition;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.IExecutorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SpecialFastCalculatorTest {
    @Mock
    private SmartContext smartContext;
    @Mock
    private IExecutorService executorService;
    @Mock
    private Member member1;
    @Mock
    private Member member2;
    private SpecialFastCalculator sut = new SpecialFastCalculator();

    @Test
    void checkNums() {
        Set<Member> members = newHashSet(member1, member2);
        doReturn(members).when(smartContext).getOtherMembers();
        doReturn(executorService).when(smartContext).getExecutorService();

        Map<Member, Future<int[]>> futureMap = new HashMap<>();
        futureMap.put(member1, CompletableFuture.completedFuture(new int[]{3, 5, 9, 13, 21}));
        futureMap.put(member2, CompletableFuture.completedFuture(new int[]{3, 5, 6, 13, 18}));
        doReturn(futureMap).when(executorService).submitToMembers(new PrimeNumCheck(3, 30), members);

        int cnt = sut.checkNums(smartContext, 3, 30);
        Assertions.assertEquals(3, cnt);
    }
}