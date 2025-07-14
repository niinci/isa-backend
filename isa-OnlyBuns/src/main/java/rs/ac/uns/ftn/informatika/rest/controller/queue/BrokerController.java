package rs.ac.uns.ftn.informatika.rest.controller.queue;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.dto.LocationMsg;
import rs.ac.uns.ftn.informatika.rest.service.queue.DirectQueueBroker;

import java.util.List;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/mq")
public class BrokerController {
    private final DirectQueueBroker broker;

    public BrokerController(DirectQueueBroker broker) {
        this.broker = broker;
    }

    @PostMapping("/{queue}")
    public void publish(@PathVariable String queue, @RequestBody LocationMsg msg) {
        broker.publish(queue, msg);
    }

    @GetMapping("/{queue}/pull")
    public LocationMsg consume(@PathVariable String queue) {
        return broker.consume(queue);
    }

    @GetMapping("/{queue}/peek")
    public List<LocationMsg> peekAll(@PathVariable String queue) {
        return broker.peekAll(queue);
    }
}
