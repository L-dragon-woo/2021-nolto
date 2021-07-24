package com.wooteco.nolto.feed.application;

import com.wooteco.nolto.exception.ErrorType;
import com.wooteco.nolto.exception.NotFoundException;
import com.wooteco.nolto.exception.UnauthorizedException;
import com.wooteco.nolto.feed.application.searchstrategy.SearchStrategy;
import com.wooteco.nolto.feed.application.searchstrategy.SearchStrategyFactory;
import com.wooteco.nolto.feed.domain.*;
import com.wooteco.nolto.feed.domain.repository.FeedRepository;
import com.wooteco.nolto.feed.domain.repository.FeedTechRepository;
import com.wooteco.nolto.feed.ui.dto.FeedCardResponse;
import com.wooteco.nolto.feed.ui.dto.FeedRequest;
import com.wooteco.nolto.feed.ui.dto.FeedResponse;
import com.wooteco.nolto.image.application.ImageService;
import com.wooteco.nolto.tech.domain.Tech;
import com.wooteco.nolto.tech.domain.TechRepository;
import com.wooteco.nolto.user.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@AllArgsConstructor
@Service
public class FeedService {

    private final ImageService imageService;
    private final FeedRepository feedRepository;
    private final TechRepository techRepository;
    private final FeedTechRepository feedTechRepository;

    public Long create(User user, FeedRequest request) {
        String thumbnailUrl = imageService.upload(request.getThumbnailImage());

        Feed feed = request.toEntityWithThumbnailUrl(thumbnailUrl).writtenBy(user);

        List<FeedTech> feedTechs = makeTechIdToFeedTech(request, feed);
        feed.changeFeedTechs(feedTechs);

        Feed savedFeed = feedRepository.save(feed);
        return savedFeed.getId();
    }

    private List<FeedTech> makeTechIdToFeedTech(FeedRequest request, Feed feed) {
        List<Tech> allTechs = techRepository.findAllById(request.getTechs());
        return allTechs.stream()
                .map(tech -> new FeedTech(feed, tech))
                .collect(Collectors.toList());
    }

    public void update(User user, Long feedId, FeedRequest request) {
        Feed findFeed = findEntityById(feedId);

        if (findFeed.notSameAuthor(user)) {
            throw new UnauthorizedException(ErrorType.UNAUTHORIZED_UPDATE_FEED);
        }
        updateFeed(request, findFeed);
    }

    private void updateFeed(FeedRequest request, Feed findFeed) {
        findFeed.update(
                request.getTitle(),
                request.getContent(),
                Step.of(request.getStep()),
                request.isSos(),
                request.getStorageUrl(),
                request.getDeployedUrl()
        );

        String updateThumbnailUrl = imageService.update(findFeed.getThumbnailUrl(), request.getThumbnailImage());
        findFeed.changeThumbnailUrl(updateThumbnailUrl);

        feedTechRepository.deleteAll(findFeed.getFeedTechs());
        List<FeedTech> feedTechs = makeTechIdToFeedTech(request, findFeed);
        findFeed.changeFeedTechs(feedTechs);
    }

    public FeedResponse findById(User user, Long feedId) {
        Feed feed = findEntityById(feedId);
        User author = feed.getAuthor();
        boolean liked = user.isLiked(feed);
        feed.increaseView();
        return FeedResponse.of(author, feed, liked);
    }

    public Feed findEntityById(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new NotFoundException(ErrorType.FEED_NOT_FOUND));
    }

    public List<FeedCardResponse> findHotFeeds() {
        Feeds feeds = new Feeds(feedRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate")));
        return FeedCardResponse.toList(feeds.sortedByLikeCount(10));
    }

    public List<FeedCardResponse> findAll(String filter) {
        FilterStrategy strategy = FilterStrategy.of(filter);
        Feeds feeds = new Feeds(feedRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate")));
        return FeedCardResponse.toList(feeds.filter(strategy));
    }

    public void delete(User user, Long feedId) {
        Feed findFeed = findEntityById(feedId);
        if (findFeed.notSameAuthor(user)) {
            throw new UnauthorizedException(ErrorType.UNAUTHORIZED_DELETE_FEED);
        }

        feedRepository.delete(findFeed);
    }

    public List<FeedCardResponse> search(String query, String techs) {
        SearchStrategy strategy = SearchStrategyFactory.of(query, techs).findStrategy();
        Set<Feed> searchFeed = strategy.search(query, techs);
        return FeedCardResponse.toList(searchFeed);
    }
}
