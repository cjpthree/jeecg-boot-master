package org.jeecg.modules.external.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.external.entity.ExInspectionItems;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 检查项目表
 * @Author: jiangxz
 * @Date:   2020-04-23
 * @Version: V1.0
 */
public interface ExInspectionItemsMapper extends BaseMapper<ExInspectionItems> {

    List<ExInspectionItems> selectList(ExInspectionItems exInspectionItems);

    List<String> selectListIds();
}
