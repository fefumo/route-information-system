package se.ifmo.route_information_system.aop;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class L2CacheStatsAspect {

    private final EntityManagerFactory emf;

    @Value("${app.cache.stats.enabled:false}")
    private boolean enabled;

    @Around("execution(* se.ifmo.route_information_system.service..*(..))")
    public Object logCacheStats(ProceedingJoinPoint pjp) throws Throwable {
        if (!enabled) {
            return pjp.proceed();
        }

        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();

        long hitsBefore = stats.getSecondLevelCacheHitCount();
        long missesBefore = stats.getSecondLevelCacheMissCount();
        long putsBefore = stats.getSecondLevelCachePutCount();

        Object result = pjp.proceed();

        long hitsAfter = stats.getSecondLevelCacheHitCount();
        long missesAfter = stats.getSecondLevelCacheMissCount();
        long putsAfter = stats.getSecondLevelCachePutCount();

        log.info("L2 cache [{}]: hits +{}, misses +{}, puts +{}",
                pjp.getSignature(),
                hitsAfter - hitsBefore,
                missesAfter - missesBefore,
                putsAfter - putsBefore);

        return result;
    }
}
