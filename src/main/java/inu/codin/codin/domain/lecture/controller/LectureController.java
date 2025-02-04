package inu.codin.codin.domain.lecture.controller;

import inu.codin.codin.common.Department;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.dto.Option;
import inu.codin.codin.domain.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의실 정보 API")
public class LectureController {

    private final LectureService lectureService;

    @Operation(
            summary = "학과명 및 과목/교수 정렬 페이지",
            description = "학과명과 과목/교수 라디오 토클을 통해 정렬한 리스트 반환<br>"+
                    "department : COMPUTER_SCI, INFO_COMM, EMBEDDED, OTHER(교양 및 프로젝트 실습 강의) <br>"+
                    "option : LEC(과목명) , PROF(교수명)"
    )
    @GetMapping("/list")
    public ResponseEntity<SingleResponse<?>> sortListOfLectures(@RequestParam("department") Department department,
                                                           @RequestParam("option") Option option,
                                                              @RequestParam("page") int page){

        return ResponseEntity.ok()
                .body(new SingleResponse<>(200,
                        department.getDescription()+" 강의들 "+option.getDescription()+"순으로 정렬 반환",
                        lectureService.sortListOfLectures(department, option, page)));
    }

    @Operation(
            summary = "교수명, 과목명, 과목코드 검색",
            description = "keyword 입력을 통해 (교수명, 과목명, 과목코드) 중 일치하는 결과 반환"
    )
    @GetMapping("/search")
    public ResponseEntity<SingleResponse<?>> searchLectures(@RequestParam("keyword") String keyword,
                                                            @RequestParam("page") int page){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, keyword+" 의 검색 결과 반환", lectureService.searchLectures(keyword, page)));
    }

    @Operation(summary = "강의실 정보 반환")
    @GetMapping("/{lectureId}")
    public ResponseEntity<?> getLectureDetails(@PathVariable("lectureId") String lectureId){
        lectureService.getLectureDetails(lectureId);
        return null;
    }

    @Operation(
            summary = "오늘의 강의 현황",
            description = "당일의 요일에 따라 층마다 호실에서의 수업 내용 반환"
    )
    @GetMapping("/rooms/empty")
    public ResponseEntity<?> statusOfEmptyRoom(@RequestParam("floor") @Max(5) @Min(1) int floor){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, floor + "층의 강의실 현황 반환", lectureService.statusOfEmptyRoom(floor)));
    }


}
