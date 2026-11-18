package com.example.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shop.entity.CaptchaRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface CaptchaRecordMapper extends BaseMapper<CaptchaRecord> {
    
    @Select("SELECT COUNT(*) FROM captcha_record WHERE ip = #{ip} AND create_time > #{startTime}")
    int countByIpAfterTime(@Param("ip") String ip, @Param("startTime") LocalDateTime startTime);
    
    @Update("UPDATE captcha_record SET retry_count = retry_count + 1 WHERE captcha_id = #{captchaId}")
    int incrementRetryCount(@Param("captchaId") String captchaId);
}
