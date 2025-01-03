package inu.codin.codin.domain.post.domain.scrap.repository;

import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, ObjectId> {

    List<ScrapEntity> findByPostIdAndDeletedAtIsNull(ObjectId postId);

    long countByPostIdAndDeletedAtIsNull(ObjectId postId);

    ScrapEntity findByPostIdAndUserId(ObjectId postId, ObjectId userId);

    Page<ScrapEntity> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(ObjectId userId, PageRequest pageRequest);
}
