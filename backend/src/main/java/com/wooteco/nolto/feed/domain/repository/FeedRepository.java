package com.wooteco.nolto.feed.domain.repository;

import com.wooteco.nolto.feed.domain.Feed;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"techStack"})
    List<Feed> findAll();
}
