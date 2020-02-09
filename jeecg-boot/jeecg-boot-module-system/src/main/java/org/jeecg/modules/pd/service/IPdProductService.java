package org.jeecg.modules.pd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.pd.entity.PdProduct;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.pd.vo.PdProductPage;

/**
 * @Description: pd_product
 * @Author: zxh
 * @Date:   2020-02-05
 * @Version: V1.0
 */
public interface IPdProductService extends IService<PdProduct> {

    Page<PdProductPage> chooseProductList(Page<PdProductPage> pageList, PdProduct pdProduct);
}