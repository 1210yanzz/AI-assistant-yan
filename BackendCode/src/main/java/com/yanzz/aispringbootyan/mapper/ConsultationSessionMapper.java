package com.yanzz.aispringbootyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {

    // 平均会话时长（分钟）：有消息时算到最后一条消息，没有消息时为 0
    @Select("""
            SELECT AVG(TIMESTAMPDIFF(MINUTE, s.started_at, COALESCE(m.last_message_at, s.started_at)))
            FROM consultation_session s
            LEFT JOIN (
                SELECT session_id, MAX(created_at) AS last_message_at
                FROM consultation_message
                GROUP BY session_id
            ) m ON m.session_id = s.id
            """)
    Double selectAvgDurationMinutes();
}
