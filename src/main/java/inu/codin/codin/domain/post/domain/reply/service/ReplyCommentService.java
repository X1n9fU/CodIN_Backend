package inu.codin.codin.domain.post.domain.reply.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.dto.response.UserDto;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisLikeService;
import inu.codin.codin.infra.redis.service.RedisService;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyCommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final UserRepository userRepository;

    private final LikeService likeService;
    private final RedisService redisService;
    private final RedisLikeService redisLikeService;
    private final S3Service s3Service;

    // 대댓글 추가
    public void addReply(String id, ReplyCreateRequestDTO requestDTO) {
        log.info("대댓글 추가 요청 - commentId: {}, content: {}, anonymous: {}",
                id, requestDTO.getContent(), requestDTO.isAnonymous());

        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();

        ReplyCommentEntity reply = ReplyCommentEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .content(requestDTO.getContent())
                .anonymous(requestDTO.isAnonymous())
                .build();

        replyCommentRepository.save(reply);

        // 댓글 수 증가 (대댓글도 댓글 수에 포함)
        log.info("대댓글 추가전, commentCount: {}", post.getCommentCount());
        post.updateCommentCount(post.getCommentCount() + 1);
        redisService.applyBestScore(1, post.get_id());
        postRepository.save(post);
        log.info("대댓글 추가후, commentCount: {}", post.getCommentCount());

        log.info("대댓글 추가 완료 - replyId: {}, postId: {}, commentCount: {}",
                reply.get_id(), post.get_id(), post.getCommentCount());

    }

    // 대댓글 삭제 (Soft Delete)
    public void softDeleteReply(String replyId) {
        log.info("대댓글 삭제 요청 - replyId: {}", replyId);

        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(new ObjectId(replyId))
                .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
        SecurityUtils.validateUser(reply.getUserId());
        // 대댓글 삭제
        reply.delete();
        replyCommentRepository.save(reply);

//        // 댓글 수 감소 (대댓글도 댓글 수에서 감소)
//        CommentEntity comment = commentRepository.findByIdAndNotDeleted(reply.getCommentId())
//                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
//
//        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
//                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
//        post.updateCommentCount(post.getCommentCount() - 1);
//        postRepository.save(post);

        log.info("대댓글 성공적 삭제  replyId: {}", replyId);
    }

    // 특정 댓글의 대댓글 조회
    public List<CommentResponseDTO> getRepliesByCommentId(ObjectId commentId) {

        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentId(commentId);

        Map<ObjectId, UserDto> userMap = userRepository.findAllById(
                replies.stream()
                        .filter(replyCommentEntity -> !replyCommentEntity.isAnonymous())
                        .map(ReplyCommentEntity::getUserId).distinct().toList()
        ).stream()
                .collect(Collectors.toMap(
                        UserEntity::get_id,
                        user -> new UserDto(user.getNickname(), user.getProfileImageUrl())));

        String defaultImageUrl = s3Service.getDefaultProfileImageUrl();

        return replies.stream()
                .map(reply -> {
                    String nickname = reply.isAnonymous() ? "익명" : userMap.get(reply.getUserId()).nickname();
                    String userImageUrl = reply.isAnonymous() ? defaultImageUrl: userMap.get(reply.getUserId()).imageUrl();
                    boolean isDeleted = reply.getDeletedAt() != null;
                    return new CommentResponseDTO(
                            reply.get_id().toString(),
                            reply.getUserId().toString(),
                            reply.getContent(),
                            nickname,
                            userImageUrl,
                            reply.isAnonymous(),
                            List.of(), //대댓글은 대댓글이 없음
                            likeService.getLikeCount(LikeType.valueOf("REPLY"), reply.get_id()), // 대댓글 좋아요 수
                            isDeleted,
                            reply.getCreatedAt(),
                            getUserInfoAboutPost(reply.get_id())

                    );
                }).toList();
    }
    public CommentResponseDTO.UserInfo getUserInfoAboutPost(ObjectId replyId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return CommentResponseDTO.UserInfo.builder()
                .isLike(redisLikeService.isReplyLiked(replyId, userId))
                .build();
    }


    public void updateReply(String id, @Valid ReplyUpdateRequestDTO requestDTO) {
        log.info("대댓글 수정 요청 - replyId: {}, newContent: {}", id, requestDTO.getContent());

        ObjectId replyId = new ObjectId(id);
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(replyId)
                .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));

        reply.updateReply(requestDTO.getContent());
        replyCommentRepository.save(reply);

        log.info("대댓글 수정 완료 - replyId: {}", replyId);

    }
}
