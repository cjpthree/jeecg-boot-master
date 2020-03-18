package org.jeecg.modules.pd.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.PdConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.pd.entity.PdRejected;
import org.jeecg.modules.pd.service.IPdRejectedService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.pd.util.UUIDUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: pd_rejected
 * @Author: jiangxz
 * @Date:   2020-03-16
 * @Version: V1.0
 */
@Api(tags="pd_rejected")
@RestController
@RequestMapping("/pd/pdRejected")
@Slf4j
public class PdRejectedController extends JeecgController<PdRejected, IPdRejectedService> {
	@Autowired
	private IPdRejectedService pdRejectedService;


	 /**
	  * 初始化Modal页面
	  * @param req
	  * @return
	  */
	 @GetMapping(value = "/initModal")
	 public Result<?> initModal(@RequestParam(name="id") String id, HttpServletRequest req) {
		 PdRejected pdRejected = new PdRejected();
		 //获取退货单号
		 pdRejected.setRejectedNo(UUIDUtil.generateOrderNoByType(PdConstant.ORDER_NO_FIRST_LETTER_TH));
		 return Result.ok(pdRejected);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param pdRejected
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "pd_rejected-分页列表查询")
	@ApiOperation(value="pd_rejected-分页列表查询", notes="pd_rejected-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(PdRejected pdRejected,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<PdRejected> queryWrapper = QueryGenerator.initQueryWrapper(pdRejected, req.getParameterMap());
		Page<PdRejected> page = new Page<PdRejected>(pageNo, pageSize);
		IPage<PdRejected> pageList = pdRejectedService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param pdRejected
	 * @return
	 */
	@AutoLog(value = "pd_rejected-添加")
	@ApiOperation(value="pd_rejected-添加", notes="pd_rejected-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody PdRejected pdRejected) {
		pdRejectedService.save(pdRejected);
		return Result.ok("添加成功！");
	}

	 @PostMapping(value = "/submit")
	public Result<?> submit(@RequestBody PdRejected pdRejected) {
//		 PdStockRecord pdStockRecord = new PdStockRecord();
//		 BeanUtils.copyProperties(PdStockRecord, pdStockRecord);
//		 pdStockRecordService.saveMain(pdStockRecord, PdStockRecord.getPdStockRecordDetailList(), PdConstant.RECODE_TYPE_2);
		return Result.ok("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param pdRejected
	 * @return
	 */
	@AutoLog(value = "pd_rejected-编辑")
	@ApiOperation(value="pd_rejected-编辑", notes="pd_rejected-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody PdRejected pdRejected) {
		pdRejectedService.updateById(pdRejected);
		return Result.ok("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pd_rejected-通过id删除")
	@ApiOperation(value="pd_rejected-通过id删除", notes="pd_rejected-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		pdRejectedService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "pd_rejected-批量删除")
	@ApiOperation(value="pd_rejected-批量删除", notes="pd_rejected-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.pdRejectedService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "pd_rejected-通过id查询")
	@ApiOperation(value="pd_rejected-通过id查询", notes="pd_rejected-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		PdRejected pdRejected = pdRejectedService.getById(id);
		if(pdRejected==null) {
			return Result.error("未找到对应数据");
		}
		return Result.ok(pdRejected);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param pdRejected
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdRejected pdRejected) {
        return super.exportXls(request, pdRejected, PdRejected.class, "pd_rejected");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PdRejected.class);
    }

}