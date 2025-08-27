package com.mcp.service;

import com.mcp.dto.SampleDto;
import com.mcp.mapper.SampleMapper;
import com.mcp.response.ResponseObject;
import com.mcp.vo.SampleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;


@Service
@RequiredArgsConstructor
public class SampleMyBatisService {
    private final SampleMapper sampleMapper;

    
    @Tool(description = "새로운 게시글을 생성합니다. 파라미터: - id: 게시글 고유 식별자 (필수) - content: 게시글 본문 내용 (필수) - post: 게시 상태 플래그 (1=게시됨, 0=임시 저장) 예: post=1이면 즉시 게시글이 등록됩니다.")
    public ResponseObject post(SampleVo vo) {
        SampleDto sampleDto = SampleDto.builder()
            .id(vo.getId())
            .content(vo.getContent())
            .post(vo.getPost())
            .build();

        sampleMapper.insert(sampleDto);

        ResponseObject responseObject = new ResponseObject();
        responseObject.putResult(true);
        return responseObject;
    }

    
    @Tool(description = "저장된 게시글을 조회합니다. 옵션: - post=1: 게시된 글만 조회 - post=0: 임시 저장된 글만 조회 파라미터를 주지 않으면 모든 게시글을 반환합니다.")
    public ResponseObject get() {
        List<SampleDto> selectAll = sampleMapper.selectAll();

        ResponseObject responseObject = new ResponseObject();
        responseObject.putResult(selectAll);
        return responseObject;
    }

    
    @Tool(description = "주어진 ID에 해당하는 게시글을 삭제합니다. 삭제 시 게시 상태(게시/임시)에 관계없이 해당 글이 완전히 제거됩니다. 파라미터: - id: 삭제할 게시글의 고유 식별자 (필수)")
    public ResponseObject delete(String id) {
        int selectAll = sampleMapper.delete(id);

        ResponseObject responseObject = new ResponseObject();
        responseObject.putResult(selectAll);
        return responseObject;
    }
}