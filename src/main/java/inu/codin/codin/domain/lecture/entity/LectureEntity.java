package inu.codin.codin.domain.lecture.entity;

import inu.codin.codin.common.Department;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LectureEntity {

    private ObjectId _id;
    private String lectureNm;
    private String professor;
    private Department department; //OTHERS : 교양
    private int grade; //0 : 전학년


}
