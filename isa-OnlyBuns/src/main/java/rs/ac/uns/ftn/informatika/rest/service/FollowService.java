package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.rest.domain.Follow;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.FollowRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserAccountRepository userAccountRepository;


    private final UserAccountService userAccountService;

    public FollowService(FollowRepository followRepository, UserAccountService userAccountService, UserAccountRepository userAccountRepository) {
        this.followRepository = followRepository;
        this.userAccountService = userAccountService;
        this.userAccountRepository = userAccountRepository;
    }



    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            throw new IllegalArgumentException("FollowerId i FollowingId ne smeju biti null!");
        }
        // Zaključaj korisnika koji se prati
        userAccountRepository.findByIdWithLock(followingId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isFollowing(followerId, followingId)) {
            throw new IllegalStateException("Already following this user");
        }

        Follow follow = new Follow(followerId, followingId);
        Follow savedFollow = followRepository.save(follow);

        userAccountRepository.incrementFollowersCount(followingId);

        return savedFollow;
    }


    public void unfollow(Long followerId, Long followingId) {
        if (!isFollowing(followerId, followingId)) {
            throw new IllegalStateException("Not following this user");
        }
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);

        // DEKREMENTUJ broj pratilaca korisnika koji se prati (followingId)
        userAccountRepository.decrementFollowersCount(followingId);
    }

    public List<UserAccount> getFollowers(Long userId) {
        // Pratioci su oni koji prate userId, tj. svi followerId-ovi gde je followingId = userId
        return followRepository.findByFollowingId(userId).stream()
                .map(follow -> userAccountService.findById(follow.getFollowerId()))
                .collect(Collectors.toList());
    }

    public List<UserAccount> getFollowing(Long userId) {
        // Lista korisnika koje userId prati, tj. followingId-ovi za koje followerId = userId
        return followRepository.findByFollowerId(userId).stream()
                .map(follow -> userAccountService.findById(follow.getFollowingId()))
                .collect(Collectors.toList());
    }
}
