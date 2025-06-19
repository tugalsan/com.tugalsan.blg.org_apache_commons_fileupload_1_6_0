package com.tugalsan.blg.semaphore;

import com.tugalsan.api.function.client.maythrowexceptions.unchecked.TGS_FuncMTU_OutTyped_In1;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.thread.server.async.await.TS_ThreadAsyncAwait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncWait;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import java.time.Duration;
import java.util.stream.IntStream;

//https://www.javatpoint.com/java-semaphore
public class Main {

    private static final TS_Log d = TS_Log.of(Main.class);

    //cd C:\me\codes\com.tugalsan\blg\com.tugalsan.blg.semaphore
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.blg.semaphore-1.0-SNAPSHOT-jar-with-dependencies.jar
    //java -jar target/com.tugalsan.blg.semaphore-1.0-SNAPSHOT-jar-with-dependencies.jar
    public static void main(String... s) {
        var threadKiller = TS_ThreadSyncTrigger.of("main");//not used on this project
        var threadRateLimit = 2;
        var threadUntil = Duration.ofMinutes(5);//AN EXXECCESSIVE AMOUNT
        var await = TS_ThreadAsyncAwait.callParallelRateLimited(threadKiller.newChild(d.className), threadRateLimit, threadUntil,
                Caller.of("#1"), Caller.of("#2"),
                Caller.of("#3"), Caller.of("#4")
        );
        if (await.timeout()) {
            d.ce("main", "timeout");
            return;
        }
        if (await.hasError()) {
            await.exceptions.forEach(e -> d.ct("main.error", e));
        }
        await.resultsForSuccessfulOnes.forEach(result -> d.cr("main", "result", result));
    }

    public static class Caller implements TGS_FuncMTU_OutTyped_In1<TGS_UnionExcuse<Void>, TS_ThreadSyncTrigger> {

        private static final TS_Log d = TS_Log.of(Caller.class);

        @Override
        public TGS_UnionExcuse<Void> call(TS_ThreadSyncTrigger threadKiller) {
            act(threadKiller);
            return Void();
        }

        private Caller(String threadName) {
            this.threadName = threadName;
        }
        final private String threadName;

        public static Caller of(String threadName) {
            return new Caller(threadName);
        }

        private void act(TS_ThreadSyncTrigger threadKiller) {
            IntStream.range(0, 5).sequential().forEach(i -> {
                if (threadKiller.hasTriggered()) {
                    return;
                }
                d.cr("act", "%s: running...".formatted(threadName));
                TS_ThreadSyncWait.of(threadName, threadKiller, WORK_LOAD);
            });
        }
        final private static Duration WORK_LOAD = Duration.ofSeconds(1);//A SMALL AMOUNT
    }
}
