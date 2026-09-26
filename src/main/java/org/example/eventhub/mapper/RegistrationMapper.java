package org.example.eventhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.eventhub.domain.Registration;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Update("UPDATE registration SET status = 'CANCELLED', cancelled_at = NOW(), version = version + 1 " +
            "WHERE id = #{id} AND status = 'REGISTERED'")
    int cancelRegistration(@Param("id") Long id);

    @Update("UPDATE registration SET status = 'CHECKED_IN', checked_in_at = NOW(), version = version + 1 " +
            "WHERE id = #{id} AND status = 'REGISTERED'")
    int checkInRegistration(@Param("id") Long id);
}