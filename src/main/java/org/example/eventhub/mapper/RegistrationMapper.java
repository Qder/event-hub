package org.example.eventhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.dto.RegistrationQueryRequest;
import org.example.eventhub.vo.EventStatisticsVO;
import org.example.eventhub.vo.RegistrationDetailVO;
import org.example.eventhub.vo.StudentRegistrationVO;

import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Update("UPDATE registration SET status = 'CANCELLED', cancelled_at = NOW(), version = version + 1 " +
            "WHERE id = #{id} AND status = 'REGISTERED'")
    int cancelRegistration(@Param("id") Long id);

    @Update("UPDATE registration SET status = 'CHECKED_IN', checked_in_at = NOW(), version = version + 1 " +
            "WHERE id = #{id} AND status = 'REGISTERED'")
    int checkInRegistration(@Param("id") Long id);

    /**
     * 分页查询活动报名名单（JOIN student）
     */
    IPage<RegistrationDetailVO> selectRegistrationDetails(
            IPage<RegistrationDetailVO> page,
            @Param("eventId") Long eventId,
            @Param("req") RegistrationQueryRequest request);

    /**
     * 查询学生的报名历史（JOIN event）
     */
    List<StudentRegistrationVO> selectStudentRegistrationHistory(
            @Param("studentId") Long studentId);

    /**
     * 查询活动统计
     */
    EventStatisticsVO selectEventStatistics(@Param("eventId") Long eventId);
}