package com.wooteco.nolto;

import com.wooteco.nolto.auth.domain.SocialType;
import com.wooteco.nolto.feed.domain.Comment;
import com.wooteco.nolto.feed.domain.Feed;
import com.wooteco.nolto.feed.domain.Like;
import com.wooteco.nolto.feed.domain.Step;
import com.wooteco.nolto.feed.domain.repository.CommentRepository;
import com.wooteco.nolto.feed.domain.repository.FeedRepository;
import com.wooteco.nolto.feed.domain.repository.LikeRepository;
import com.wooteco.nolto.tech.domain.Tech;
import com.wooteco.nolto.tech.domain.TechRepository;
import com.wooteco.nolto.user.domain.User;
import com.wooteco.nolto.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Profile("local")
@Transactional
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final FeedRepository feedRepository;
    private final TechRepository techRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private static final String URL = "https://github.com/woowacourse-teams/2021-nolto";
    private static final String DEFAULT_IMAGE = "https://dksykemwl00pf.cloudfront.net/amazzi.jpeg";

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("데이터가 이미 존재합니다. 로딩을 생략합니다.");
            return;
        }

        User mickey = new User("1", SocialType.GITHUB, "미키", DEFAULT_IMAGE);
        List<User> users = Arrays.asList(
                mickey,
                new User("2", SocialType.GITHUB, "아마찌", DEFAULT_IMAGE),
                new User("3", SocialType.GITHUB, "지그", DEFAULT_IMAGE),
                new User("4", SocialType.GITHUB, "포모", DEFAULT_IMAGE),
                new User("5", SocialType.GITHUB, "조엘", DEFAULT_IMAGE),
                new User("6", SocialType.GITHUB, "찰리", DEFAULT_IMAGE)
        );
        userRepository.saveAll(users);

        // 중복된 Tech 방지
        Tech tech1 = findOrCreateTech("Apollo Client");
        Tech tech2 = findOrCreateTech("WebGL");

        Feed feed1 = Feed.builder()
                .title("title1")
                .content("content1")
                .step(Step.PROGRESS)
                .isSos(true)
                .storageUrl(URL)
                .thumbnailUrl(DEFAULT_IMAGE)
                .build().writtenBy(mickey);

        Feed feed2 = Feed.builder()
                .title("title2")
                .content("content2")
                .step(Step.COMPLETE)
                .isSos(false)
                .storageUrl(URL)
                .deployedUrl(URL)
                .thumbnailUrl(DEFAULT_IMAGE)
                .build().writtenBy(mickey);

        feed1.changeTechs(Collections.singletonList(tech1));
        feed2.changeTechs(Collections.singletonList(tech2));

        Feed saveFeed1 = feedRepository.save(feed1);
        feedRepository.save(feed2);

        // 댓글 생성
        createCommentsForFeed(feed1, mickey);
        createCommentsForFeed(feed2, mickey);

        likeRepository.save(new Like(mickey, saveFeed1));
    }

    private Tech findOrCreateTech(String techName) {
        return techRepository.findByName(techName)
                .orElseGet(() -> techRepository.save(new Tech(techName)));
    }

    private void createCommentsForFeed(Feed feed, User user) {
        Comment comment1 = new Comment("첫 댓글", false).writtenBy(user, feed);
        Comment comment2 = new Comment("2등 댓글", false).writtenBy(user, feed);
        commentRepository.saveAll(Arrays.asList(comment1, comment2));

        Comment reply1 = new Comment("첫 댓글의 대댓글1", false).writtenBy(user, feed);
        comment1.addReply(reply1);
        commentRepository.save(reply1);
    }
}
