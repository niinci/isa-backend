package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.domain.Follow;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.service.FollowService;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // Provera da li pratim korisnika
    @GetMapping("/isFollowing")
    public ResponseEntity<Boolean> isFollowing(@RequestParam Long followerId, @RequestParam Long followingId) {
        boolean result = followService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(result);
    }

    // Praćenje korisnika
    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestParam Long followerId, @RequestParam Long followingId) {
        try {
            Follow follow = followService.follow(followerId, followingId);
            return ResponseEntity.ok(follow);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Prestani da pratiš korisnika
    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        try {
            followService.unfollow(followerId, followingId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserAccount>> getFollowers(@RequestParam Long userId) {
        List<UserAccount> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserAccount>> getFollowing(@RequestParam Long userId) {
        List<UserAccount> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }

}
