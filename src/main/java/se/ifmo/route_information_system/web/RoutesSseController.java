package se.ifmo.route_information_system.web;

import org.springframework.http.MediaType;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import se.ifmo.route_information_system.events.RouteChangedEvent;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class RoutesSseController {

    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @GetMapping(path = "/sse/routes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        // 0L = no server-side timeout; let proxy/browser manage it
        SseEmitter emitter = new SseEmitter(0L);
        clients.add(emitter);

        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError((e) -> clients.remove(emitter));

        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRouteChanged(RouteChangedEvent event) {
        clients.forEach(em -> {
            try {
                em.send(SseEmitter.event().data(event));
            } catch (IOException e) {
                em.complete();
                clients.remove(em);
            }
        });
    }
}
