/*
 * MIT License
 *
 * Copyright (c) 2025 KeyHub Projects
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;
import keyhub.distributedtransactionkit.core.transaction.composite.SequencedTransaction;
import keyhub.distributedtransactionkit.starter.component.AfterTransactionEventHandler;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class SequencedFrameworkTransactionTest {

    @Nested
    class Sequenced_정상트랜잭션{
        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        @Test
        void 정상_트랜잭션_동작() throws KhTransactionException {
            SequencedTransaction utd = SequencedFrameworkTransaction.of(
                            single("1"), single("2"), single("3")
                    );
            var result = utd.resolve();
            assertNotNull(result);
            log.info(result.toString());
            SequencedMap<TransactionId, KhTransaction.Result<?>> result2 = result.get();
            assertNotNull(result2);
            log.info(result2.toString());
            assertEquals(3, result2.size());
            assertEquals("1", result2.pollFirstEntry().getValue().get());
            assertEquals("2", result2.pollFirstEntry().getValue().get());
            assertEquals("3", result2.pollFirstEntry().getValue().get());
        }
    }

    @Nested
    class Sequenced_callback사례{
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private CallbackService callbackService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public CallbackService callbackService() {
                return new CallbackService();
            }
        }

        public static class CallbackService {
            @Transactional
            public SequencedMap<TransactionId, KhTransaction.Result<?>> callbackSample() {
                var result = SequencedFrameworkTransaction.of(
                        single("1"), single("2"), single("3"),
                        single("I will invoke callback1!").setCallback(single("callback1")),
                        single("I will invoke callback2!").setCallback(single("callback2"))
                ).resolve();
                SequencedFrameworkTransaction.of(
                        single("5"), single("6"),
                        single("I will invoke callback3!").setCallback(single("callback3")),
                        single("I will invoke callback4!").setCallback(single("callback4")),
                        single("7"), single("8")
                ).resolve();
                return result.get();
            }
        }

        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        @Test
        void callback_트랜잭션_동작() {
            SequencedMap<TransactionId, KhTransaction.Result<?>> result = callbackService.callbackSample();

            assertNotNull(result);
            assertEquals(5, result.size());
            log.info(result.toString());
            assertEquals("1", result.pollFirstEntry().getValue().get());
            assertEquals("2", result.pollFirstEntry().getValue().get());
            assertEquals("3", result.pollFirstEntry().getValue().get());
            verify(frameworkTransactionContext, times(0)).compensate();
            verify(frameworkTransactionContext, times(1)).callback();
            verify(afterTransactionEventHandler, times(4)).handleResolveEvent(any(AfterTransactionEvent.class));
        }
    }

    @Nested
    class composite_보상사례{
        @Autowired
        private AfterTransactionEventHandler afterTransactionEventHandler;
        @Autowired
        private FrameworkTransactionContext frameworkTransactionContext;
        @Autowired
        private CompensationService compensationService;

        @TestConfiguration
        static class TestConfig {
            @Bean
            @Primary // 기존 빈 대신 사용
            public AfterTransactionEventHandler afterTransactionEventHandler() {
                return spy(new AfterTransactionEventHandler());
            }
            @Bean
            @Primary // 기존 빈 대신 사용
            public FrameworkTransactionContext frameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
                return spy(new FrameworkTransactionContext(applicationEventPublisher));
            }

            @Bean
            public CompensationService compensationService() {
                return new CompensationService();
            }
        }

        public static class CompensationService {
            @Transactional
            public void compensateSample() throws KhTransactionException {
                var failTransaction = failSingle("I will be fail!");
                SequencedFrameworkTransaction.of(
                        single("1"), single("2"),
                        single("I will compensate1!").setCompensation(single("compensation1")).setCallback(single("no callback1")),
                        single("3"),
                        single("4").setCallback(single("no callback2")),
                        single("I will compensate2!").setCompensation(single("compensation2"))
                    ).setCallback(single("no callback3"))
                    .resolve();
                single("I will compensate3!").setCompensation(single("compensation3")).resolve();
                failTransaction.resolve();
                SequencedFrameworkTransaction.of(
                        single("no1"), single("no2"),
                        single("no3").setCompensation(single("no compensation1")).setCallback(single("no callback4"))
                    ).resolve();
            }
        }

        static KhTransaction single(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                return message;
            });
        }

        static KhTransaction failSingle(String message){
            return SingleFrameworkTransaction.of(()->{
                log.info(message);
                throw new RuntimeException("fail");
            });
        }

        @Test
        void 보상_트랜잭션_동작() {
            assertThrows(KhTransactionRuntimeException.class, ()-> compensationService.compensateSample());

            verify(frameworkTransactionContext, times(1)).compensate();
            verify(frameworkTransactionContext, times(0)).callback();
            verify(afterTransactionEventHandler, times(3)).handleResolveEvent(any(AfterTransactionEvent.class));
        }
    }
}