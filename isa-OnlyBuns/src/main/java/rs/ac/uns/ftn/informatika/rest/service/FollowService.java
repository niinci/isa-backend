package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.rest.domain.Follow;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.FollowRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserAccountServiceImpl userAccountServiceImpl;

    public FollowService(FollowRepository followRepository, UserAccountServiceImpl userAccountServiceImpl) {
        this.followRepository = followRepository;
        this.userAccountServiceImpl = userAccountServiceImpl;
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public Follow follow(Long followerId, Long followingId) {
        if (isFollowing(followerId, followingId)) {
            throw new IllegalStateException("Already following this user");
        }
        Follow follow = new Follow(followerId, followingId);
        return followRepository.save(follow);
    }

    public void unfollow(Long followerId, Long followingId) {
        if (!isFollowing(followerId, followingId)) {
            throw new IllegalStateException("Not following this user");
        }
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    public List<UserAccount> getFollowers(Long userId) {
        // Pratioci su oni koji prate userId, tj. svi followerId-ovi gde je followingId = userId
        return followRepository.findByFollowingId(userId).stream()
                .map(follow -> userAccountServiceImpl.findById(follow.getFollowerId()))
                .collect(Collectors.toList());
    }

    public List<UserAccount> getFollowing(Long userId) {
        // Lista korisnika koje userId prati, tj. followingId-ovi za koje followerId = userId
        return followRepository.findByFollowerId(userId).stream()
                .map(follow -> userAccountServiceImpl.findById(follow.getFollowingId()))
                .collect(Collectors.toList());
    }
}
