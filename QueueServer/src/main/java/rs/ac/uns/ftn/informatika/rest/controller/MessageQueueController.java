package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rs.ac.uns.ftn.informatika.rest.service.DirectMessageQueueService;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/queues")
public class MessageQueueController {

    private final DirectMessageQueueService queueService;

    public MessageQueueController(DirectMessageQueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/{queueName}/send")
    public ResponseEntity<String> sendMessage(@PathVariable String queueName, @RequestBody Object message) {
        queueService.sendMessage(queueName, message);
        return ResponseEntity.ok("Message successfully sent to queue '" + queueName + "'.");
    }

    // Endpoint za primanje poruka sa Long Polling-om
    @GetMapping("/{queueName}/receive")
    public DeferredResult<ResponseEntity<Object>> receiveMessage(@PathVariable String queueName) {
        // timeout 30 sekundi
        DeferredResult<ResponseEntity<Object>> deferredResult = new DeferredResult<>(30000L);

        ForkJoinPool.commonPool().submit(() -> {
            Object message = null;
            long startTime = System.currentTimeMillis();
            long timeout = 29000L;

            // Cekamo dok poruka ne stigne ili dok ne istekne timeout
            while (message == null && (System.currentTimeMillis() - startTime) < timeout) {
                message = queueService.receiveMessage(queueName);
                if (message == null) {
                    try {
                        // Kratka pauza pre ponovne provere
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (message != null) {
                deferredResult.setResult(ResponseEntity.ok(message));
            } else {
                deferredResult.setResult(ResponseEntity.noContent().build()); // Nema sadrzaja nakon timeouta
            }
        });

        return deferredResult;
    }
}