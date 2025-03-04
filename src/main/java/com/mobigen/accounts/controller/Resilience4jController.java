package com.mobigen.accounts.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RefreshScope
@Slf4j
@Tag(name = "CircuitBreaker check for test", description = "CircuitBreaker check for test")
@RestController
@RequestMapping(path = "/api/circuit", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class Resilience4jController {

    /*
        1. Bulkhead – /api/circuit/bulkhead
        설정 요약
            애노테이션: @Bulkhead(name = "bulkhead", type = Bulkhead.Type.SEMAPHORE)
            Bulkhead는 동시 호출 수를 제한하여, 시스템의 다른 부분이 과부하되지 않도록 격리하는 패턴입니다.
            Bulkhead 설정은 별도의 yml 설정이 없으면 기본값(예: 최대 동시 호출 수 등)이 적용됩니다.
        테스트 방법
            여러 동시 요청을 보내 Bulkhead 제한을 확인합니다.
            예를 들어, Apache Bench나 JMeter를 사용해 동시 호출 수를 늘려 테스트합니다.
            동시 요청 수가 Bulkhead 제한보다 많을 때 일부 요청이 거부되거나 대기 상태가 될 수 있습니다.
            로깅이나 모니터링(예: actuator 엔드포인트)을 통해 Bulkhead 상태를 확인합니다.
        예상 결과
            정상 호출의 경우 응답 "OK"를 받습니다.
            Bulkhead 제한에 도달하면, 일부 요청이 거부되거나 대기 상태에 빠지며, 예외 또는 fallback이 발생할 수 있습니다.
            Bulkhead 관련 설정이 없다면 기본 설정값으로 동작하므로, 동시 요청 수를 과도하게 높이지 않으면 대부분 "OK" 응답을 받습니다.
     */
    @Operation(summary = "Bulkhead 테스트", description = 
    "ab -n 10 -c 5 -v 3 http://localhost:8080/api/circuit/bulkhead"
    + "\n-n 10 : 총 10개의 요청"
    + "\n-c 5 : 동시 요청 5개"
    + "\n예상 결과"
    + "\nBulkhead의 동시 처리 가능 요청 수 제한이 설정되지 않음 → 기본값 적용"
    + "\n너무 많은 동시 요청이 들어오면 일부 요청이 BulkheadFullException으로 실패할 가능성 있음."
    )
    @GetMapping("/bulkhead")
    @Bulkhead(name = "bulkhead", type = Bulkhead.Type.SEMAPHORE)
    public String getBulkhead() {
        return "OK";
    }

    /*
    2. Circuit Breaker – /api/circuit/circuit
    설정 요약
        애노테이션: @CircuitBreaker(name = "circuitBreaker")
        yml 설정에서 기본 설정은 다음과 같습니다.
            slidingWindowSize: 10 – 최근 10번의 호출 결과를 기준으로 상태를 평가.
            failureRateThreshold: 50 – 50% 이상의 실패율이면 서킷을 Open 상태로 전환.
            waitDurationInOpenState: 10000 (10초) – Open 상태 유지 후 Half-Open 상태로 전환 시도.
            permittedNumberOfCallsInHalfOpenState: 2 – Half-Open 상태에서 2번의 호출 허용.
    테스트 방법
    정상 동작 확인:
        URL: GET /api/circuit/circuit?isError=false
        예상 결과: 
            응답 "OK".
    에러 발생:
        URL: GET /api/circuit/circuit?isError=true
        예상 결과:
            첫 호출 시 예외 발생 (내부적으로 RuntimeException 던짐).
            여러 번 연속 호출하여 실패율이 50%를 넘으면 서킷 브레이커가 Open 상태가 되어 이후 호출은 바로 예외를 발생시킵니다.
    서킷 복구 테스트:
        에러가 연속되어 서킷이 Open 상태가 된 후 10초가 지나면, 허용된 2번의 Half-Open 호출이 진행됩니다.
        Half-Open 상태에서 정상 호출이 지속되면 서킷이 닫히고 정상 동작합니다.
        예상 결과
            정상 호출:
                "OK" 반환.
            에러 발생 시:
                처음 몇 번은 실제 호출에서 예외 발생.
                일정 실패율 초과 후: 서킷이 Open 상태가 되어 바로 예외 반환 (fallback이 없으므로 오류 응답 또는 HTTP 500).
                10초 후, 일부 호출이 Half-Open 상태에서 테스트되고, 정상이면 서킷 닫힘.
     */
    @Operation(summary = "Circuit Breaker 테스트", description = 
    "for i in {1..10}; do curl \"http://localhost:8080/api/circuit/circuit?isError=true\"; done"
    + "\n10번 연속 오류 발생 시도"
    + "\nfailureRateThreshold: 50 → 10개 중 5개 이상 실패하면 Open 상태가 됨"
    + "\ncurl \"http://localhost:8080/api/circuit/circuit?isError=false\""
    + "\n이후 정상 요청이 차단되는지 확인"
    + "\nwaitDurationInOpenState: 10000 → 10초 후 Half-Open 상태로 복구되는지 확인"
    + "\n예상 결과"
    + "\n처음에는 500 Internal Server Error"
    + "\n실패율이 50%를 넘으면 CircuitBreakerOpenException 발생"
    + "\n10초 후 Half-Open 상태에서 2개만 허용 (permittedNumberOfCallsInHalfOpenState: 2)"
    + "\n성공하면 다시 Close 상태로 복구"
    )
    @GetMapping("/circuit")
    @CircuitBreaker(name = "circuitBreaker")
    public String getCircuitBreaker(boolean isError) {
        if (isError) {
            throw new RuntimeException("예기치 않은 에러 발생");
        }
        return "OK";
    }

    /*
    설정 요약
        애노테이션: @RateLimiter(name = "rateLimiter")
        yml 설정에서 기본 리미터 설정은 다음과 같습니다.
            timeoutDuration: 1000 (1초) – 리미터가 토큰을 기다리는 최대 시간.
            limitRefreshPeriod: 5000 (5초) – 토큰이 새로 공급되는 주기.
            limitForPeriod: 1 – 해당 주기마다 최대 1개의 호출 허용.
    테스트 방법
        단순 호출 테스트:
            URL: GET /api/circuit/rate-limiter-default
            한 번 호출하면 정상적으로 "OK" 반환.
        연속 호출 테스트:
            5초 내에 두 번 이상 호출하면 두 번째 호출은 리미터에 걸려 예외가 발생할 가능성이 큽니다.
            예를 들어, 아래와 같이 2회 연속 호출하면:
                curl http://localhost:8080/api/circuit/rate-limiter-default
                curl http://localhost:8080/api/circuit/rate-limiter-default
            첫 번째 호출은 "OK".
            두 번째 호출은 RateLimiter 제한에 의해 예외(보통 RequestNotPermitted 예외)가 발생하고, 클라이언트에는 오류 응답이 전달될 수 있습니다.
            예상 결과
            호출 간격이 5초 이상이면 모두 "OK".
            5초 이내에 여러 요청 시 두 번째 요청부터는 오류(예: HTTP 429 또는 지정된 예외 메시지)가 발생함.
     */
    @Operation(summary = "Rate Limiter 테스트", description = 
    "for i in {1..5}; do curl \"http://localhost:8080/api/circuit/rate-limiter-default\"; done"
    + "\nlimitForPeriod: 1 → 5초 동안 1번만 성공, 이후 RateLimiterException 발생"
    + "\n예상 결과"
    + "\n첫 번째 요청은 200 OK"
    + "\n나머지 요청은 429 Too Many Requests"
    )
    @GetMapping("/rate-limiter-default")
    @RateLimiter(name = "rateLimiter")
    public String getRateLimiterDefault() {
        return "OK";
    }

    /*
    설정 요약
        애노테이션: @RateLimiter(name = "backendB", fallbackMethod = "fallback")
        이 엔드포인트는 리미터 이름 "backendB"를 사용하며, 실패 시 fallback 메서드를 호출합니다.
        yml 설정에서 기본 리미터 설정이 적용되지 않는다면(예: 별도 인스턴스 설정이 없다면) 기본값이 사용될 수 있습니다.
        Fallback 메서드: 예외 발생 시 호출되어 "error: " + exception.getMessage()를 반환.
    테스트 방법
    단순 호출 테스트:
        URL: GET /api/circuit/rate-limiter-instance
        단일 호출이면 정상 "OK" 반환.
        연속 호출 테스트:
            빠르게 2번 이상 호출하여 리미터 제한에 걸리도록 합니다.
            예를 들어, 연속 호출 시 두 번째 호출은 리미터 제한으로 인한 예외 발생 → fallback 메서드가 호출되어 오류 메시지 반환.
            예상 결과
                첫 번째 호출: "OK".
                두 번째 및 이후 호출(제한 주기 내):
                Fallback이 동작하여 "error: Request not permitted" 또는 유사한 예외 메시지를 포함한 문자열 반환.
                실제 예외 메시지는 내부 구현과 리미터 설정에 따라 다를 수 있습니다.
     */
    @Operation(summary = "Rate Limiter + Fallback", description = 
    "for i in {1..5}; do curl \"http://localhost:8080/api/circuit/rate-limiter-instance\"; done"
    + "\n예상 결과"
    + "\n첫 번째 요청은 200 OK"
    + "\n이후 요청은 fallback 메서드가 실행되어 \"error: RateLimiter has been exceeded\" 반환"
    )
    @GetMapping("/rate-limiter-instance")
    @RateLimiter(name = "backendB", fallbackMethod = "fallback")
    public String getRateLimiterInstance() {
        return "OK";
    }

    private static String fallback(Exception exception) {
        return "error: " + exception.getMessage();
    }

    /*
    설정 요약
        애노테이션: @TimeLimiter(name = "timeLimiter")
        이 설정은 요청의 실행 시간이 설정된 제한 시간(기본은 10초 등)보다 길어지면 타임아웃 예외를 발생시킵니다.
        Controller 메서드에서는 Thread.sleep(10000)를 사용해 10초 동안 지연 후 "OK"를 반환하도록 작성되어 있습니다.
    테스트 방법
    단순 호출 테스트:
        URL: GET /api/circuit/time-limiter
        비동기 방식(CompletableFuture)으로 처리되므로, 타임아웃 설정에 따라 결과가 달라질 수 있습니다.
        TimeLimiter 설정이 별도로 yml에 정의되어 있지 않다면 기본값이 적용되며, yml의 타임리미터 설정을 확인할 필요가 있음.
        테스트 시, 10초 이상 지연되므로 타임아웃이 발생할 가능성이 높습니다.
        호출 결과로 TimeoutException이 발생하면, 클라이언트는 오류 응답(예: HTTP 503 또는 500)을 받게 됩니다.
    예상 결과
        예상 시나리오 1:
            TimeLimiter의 제한 시간보다 지연 시간이 더 길 경우, 타임아웃이 발생하여 클라이언트에 예외(예: TimeoutException) 혹은 fallback 메서드가 있다면 fallback 결과가 반환됩니다.
        예상 시나리오 2:
            제한 시간이 10초 이상으로 설정되어 있다면, "OK"를 반환할 수 있습니다.
            일반적으로, 타임리미터는 타임아웃이 발생하면 클라이언트에 비동기 오류 응답을 보내므로, 실제 테스트 시 TimeoutException(또는 관련 오류 메시지)를 확인할 수 있습니다.
     */
    @Operation(summary = "Time Limiter 테스트", description = 
    "curl \"http://localhost:8080/api/circuit/time-limiter\""
    + "\n설정된 TimeLimiter가 없으므로 기본값 (1초) 적용"
    + "\n요청이 10초 동안 실행되므로 타임아웃 발생"
    + "\n예상 결과"
    + "\nTimeoutException 발생 → 500 Internal Server Error"
    )
    @GetMapping("/time-limiter")
    @TimeLimiter(name = "timeLimiter")
    public CompletableFuture<String> getPingPongTimeLimiter() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("error", e);
                Thread.currentThread().interrupt();
            }
            return "OK";
        });
    }

    /*
    설정 요약
        애노테이션: @CircuitBreaker(name = "retry")
        (여기서 CircuitBreaker를 통해서 retry가 동작하도록 설정한 것으로 보임)
        yml의 retry 기본 설정:
            maxAttempts: 3 – 최대 3번의 재시도.
            waitDuration: 500ms – 재시도 간 500ms 대기.
            enableExponentialBackoff: true, exponentialBackoffMultiplier: 2 – 지수적 백오프 적용.
            ignoreExceptions: [NullPointerException] – NullPointerException 발생 시 재시도하지 않음.
            retryExceptions: [TimeoutException] – TimeoutException은 재시도 대상.
    테스트 방법
    정상 호출 테스트:
        URL: GET /api/circuit/retry?isError=false
        예상 결과: "OK" 반환.
    실패 호출 테스트:
        URL: GET /api/circuit/retry?isError=true
        첫 호출 시 RuntimeException 발생.
        설정에 따라 최대 3번까지 재시도하므로, 로그를 확인하면 최대 3번의 시도가 발생함.
        재시도 후에도 계속 에러가 발생하면 최종적으로 예외가 전파되어 HTTP 500 등의 오류 응답이 전달됩니다.
        예상 결과
            정상 호출: "OK".
        에러 발생 시:
            최대 3회 재시도 후 최종 호출에서도 예외 발생 시, 클라이언트는 오류 응답(예: HTTP 500) 또는 해당 예외 메시지를 받음.
            재시도 로직이 내부적으로 수행됨을 로그에서 확인할 수 있습니다.
     */
    @Operation(summary = "Retry 테스트", description = 
    "curl \"http://localhost:8080/api/circuit/retry?isError=true\""
    + "\n예상 결과"
    + "\n기본적으로 재시도 설정이 Retry가 아니라 CircuitBreaker로 되어 있음 → Retry가 작동하지 않음"
    + "\n재시도를 적용하려면 @Retry(name = \"retry\") 어노테이션 추가 필요"
    )
    @GetMapping("/retry")
    @CircuitBreaker(name = "retry")
    public String getRetry(boolean isError) {
        if (isError) {
            throw new RuntimeException("예기치 않은 에러 발생");
        }
        return "OK";
    }
}
