package inu.codin.codin.domain.post.domain.scrap.repository;

import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, ObjectId> {

    List<ScrapEntity> findByPostId(ObjectId postId);

    long countByPostId(ObjectId postId);

    void deleteByPostIdAndUserId(ObjectId postId, ObjectId userId);

    boolean existsByPostIdAndUserId(ObjectId postId, ObjectId userId);
}
