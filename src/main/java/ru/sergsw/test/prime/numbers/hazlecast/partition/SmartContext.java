package ru.sergsw.test.prime.numbers.hazlecast.partition;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import ru.sergsw.test.prime.numbers.calculators.Context;
import ru.sergsw.test.prime.numbers.hazlecast.HazelcastGlobalContext;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public interface SmartContext extends Context {
    AtomicReference<SmartContext> SMART_CONTEXT = new AtomicReference<>();

    static SmartContext get() {
        SmartContext context = SMART_CONTEXT.get();
        if( context == null) {
            context = new SmartContextImpl(HazelcastGlobalContext.getHazelcastInstance());
            context.warmup(10_000);
            SMART_CONTEXT.set(context);
        }
        return context;
    }

    Set<Member> getOtherMembers();
    IExecutorService getExecutorService();

    int addValues(int[] vals);

    class SmartContextImpl implements SmartContext {
        private final SortedSet<Integer> sharedContext = new ConcurrentSkipListSet<>();
        private final Set<Member> otherMembers;
        private final IExecutorService executorService;

        public SmartContextImpl(HazelcastInstance hazelcast) {
            executorService = hazelcast.getExecutorService("calculator");
            otherMembers = hazelcast.getCluster().getMembers().stream()
                    .filter(member -> !member.localMember())
                    .collect(Collectors.toSet());
        }

        @Override
        public Set<Member> getOtherMembers() {
            return otherMembers;
        }

        @Override
        public IExecutorService getExecutorService() {
            return executorService;
        }

        @Override
        public int addValues(int[] vals) {
            for (int val : vals) {
                addValue(val);
            }
            return vals.length;
        }

        @Override
        public long calcSize() {
            return sharedContext.size();
        }

        @Override
        public SortedSet<Integer> getSimpleNums() {
            return sharedContext;
        }

        @Override
        public void addValue(int val) {
            sharedContext.add(val);
        }

        @Override
        public void flush() {

        }
    }
}
