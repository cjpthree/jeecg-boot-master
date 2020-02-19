package org.jeecg.modules.pd.controller;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.constant.PdConstant;
import org.jeecg.modules.pd.entity.PdProductStock;
import org.jeecg.modules.pd.entity.PdProductStockTotal;
import org.jeecg.modules.pd.entity.PdPurchaseOrder;
import org.jeecg.modules.pd.entity.PdStockRecordDetail;
import org.jeecg.modules.pd.service.IPdProductStockService;
import org.jeecg.modules.pd.service.IPdProductStockTotalService;
import org.jeecg.modules.pd.service.IPdStockRecordDetailService;
import org.jeecg.modules.pd.vo.PdProductStockTotalPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;

 /**
 * @Description: 库存总表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@RestController
@RequestMapping("/pd/pdProductStockTotal")
@Slf4j
public class PdProductStockTotalController {
	 @Autowired
	 private IPdProductStockTotalService pdProductStockTotalService;
	 @Autowired
	 private IPdProductStockService pdProductStockService;
	 @Autowired
	 private IPdStockRecordDetailService pdStockRecordDetailService;
	 /**
	  * 分页列表查询
	  *
	  * @param pdProductStockTotal
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @GetMapping(value = "/list")
	 public Result<?> queryPageList(PdProductStockTotal pdProductStockTotal,
									@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
									@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
									HttpServletRequest req) {

         Page<PdProductStockTotalPage> page = new Page<PdProductStockTotalPage>(pageNo, pageSize);
         page = pdProductStockTotalService.selectList(page,pdProductStockTotal);
		//计算总库存数量，近效期数量，过期数量等值；
         //**************************
         List<PdProductStockTotalPage> aList = pdProductStockTotalService.findListForQuery(pdProductStockTotal);
         Double gCount=0.00;//过期数量
         Double jCount=0.00;//近效期数量
         Double isLcount=0.00;//久存产品数量
         Double pCount=0.00;//总数量
         Double limtCount=0.00;//超出库房上下限产品数量
         if(aList!=null&&!aList.isEmpty()){
             for (PdProductStockTotal p : aList) {
                 if(PdConstant.PD_STATE_2.equals(p.getExpire())){
                     gCount++;
                 }else if(PdConstant.PD_STATE_1.equals(p.getExpire())){
                     jCount++;
                 }
                 if(PdConstant.IS_LONG_1.equals(p.getIsLong())){
                     isLcount++;
                 }
                 if(ObjectUtils.isNotEmpty(p.getLimitUp()) && ObjectUtils.isEmpty(p.getLimitDown())){
                     Double up=p.getLimitUp();
                     if(p.getStockNum()>up)limtCount++;
                 }
                 if(ObjectUtils.isNotEmpty(p.getLimitDown())&&ObjectUtils.isEmpty(p.getLimitUp())){
                     Double down =  p.getLimitDown();
                     if(p.getStockNum()<down)limtCount++;
                 }
                 if(ObjectUtils.isNotEmpty(p.getLimitDown())&&ObjectUtils.isNotEmpty(p.getLimitUp())){
                     Double up= p.getLimitUp();
                     Double down =p.getLimitDown();
                     if(p.getStockNum()>up||p.getStockNum()<down){
                         limtCount++;
                     }
                 }
                 pCount+=p.getStockNum();
             }
         }
		Map map=new HashMap();
		 map.put("records",page);
		 map.put("gCount",gCount);//过期数量
		 map.put("jCount",jCount);//近效期数量
		 map.put("isLcount",isLcount);//久存产品数量
		 map.put("pCount",pCount);//总数量
		 map.put("limtCount",limtCount);//超出库房上下限产品数量
		 return Result.ok(map);
	 }
	 /**
	  * 设置库存上下线数值
	  *
	  * @param pdProductStockTotalPage
	  * @return
	  */
	 @PutMapping(value = "/updateProductStock")
	 public Result<?> updateProductStock(@RequestBody PdProductStockTotalPage pdProductStockTotalPage) {
		 String ids = pdProductStockTotalPage.getIds();
		 Double upNum=pdProductStockTotalPage.getLimitUp();
		 Double downNum=pdProductStockTotalPage.getLimitDown();
		 List arr = Arrays.asList(ids.split(","));
		 for (int i = 0; i < arr.size(); i++) {
			 String id=(String)arr.get(i);
			 PdProductStockTotal stockTotal = new PdProductStockTotal();
			 stockTotal.setId(id);
			 if (ObjectUtils.isNotEmpty(downNum)) {
				 stockTotal.setLimitDown(downNum);
			 }
			 if (ObjectUtils.isNotEmpty(upNum)) {
				 stockTotal.setLimitUp(upNum);
			 }
			pdProductStockTotalService.updateProductStock(stockTotal);
		 }
		 return Result.ok("设置成功!");
	 }


	

	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		PdProductStockTotal pdProductStockTotal = pdProductStockTotalService.getById(id);
		if(pdProductStockTotal==null) {
			return Result.error("未找到对应数据");
		}
		return Result.ok(pdProductStockTotal);

	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryPdProductStockByMainId")
	public Result<?> queryPdProductStockListByMainId(@RequestParam(name="id",required=true) String id) {
		List<PdProductStock> pdProductStockList = pdProductStockService.selectByMainId(id);
		return Result.ok(pdProductStockList);
	}



	 /**
	  * 分页查询库存明细信息  mcb  --20200217   用于库存管理查询库存明细信息
	  *
	  * @param stockTotalPage
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @GetMapping(value = "/chooseProductStockList")
	 public Result<?> chooseProductStockList(PdProductStockTotalPage stockTotalPage,
									@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
									@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
									HttpServletRequest req) {

		 Page<PdProductStock> page = new Page<PdProductStock>(pageNo, pageSize);
		 page = pdProductStockService.selectList(page,stockTotalPage);
		 //计算总库存数量，近效期数量，过期数量等值；
		 //**************************
		 List<PdProductStock> aList = pdProductStockService.findListForQuery(stockTotalPage);
		 Double gCount=0.00;//过期数量
		 Double jCount=0.00;//近效期数量
		 Double pCount=0.00;//总数量
		 if(aList!=null&&!aList.isEmpty()){
			 for (PdProductStock p : aList) {
				 if(PdConstant.PD_STATE_2.equals(p.getPdState())){
					 gCount++;
				 }else if(PdConstant.PD_STATE_1.equals(p.getPdState())){
					 jCount++;
				 }
				 pCount+=p.getStockNum();
			 }
		 }
		 Map map=new HashMap();
		 map.put("records",page);
		 map.put("gCount",gCount);//过期数量
		 map.put("jCount",jCount);//近效期数量
		 map.put("pCount",pCount);//总数量
		 return Result.ok(map);
	 }




	 /**
	  * 查询出入库明细  mcb  --20200217 用于库存管理查询出入库明细
	  * @param stockTotalPage
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @GetMapping(value = "/stockInAndOutRecordDetailQuery")
	 public Result<?> stockInAndOutRecordDetailQuery(PdProductStockTotalPage stockTotalPage,
											 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
											 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
											 HttpServletRequest req) {

		 Page<PdStockRecordDetail> page = new Page<PdStockRecordDetail>(pageNo, pageSize);
		 //page = pdStockRecordDetailService.stockInAndOutRecordDetailQueryForPage(page,stockTotalPage);
		 Double productTotNum = 0.00;//入库总数量
		 Double productOutTotNum = 0.00;//出库总数量
		 BigDecimal inPrice = new BigDecimal(0);//入库总金额
		 BigDecimal outPrice = new BigDecimal(0);//出库总金额

		 List<PdStockRecordDetail> list =null;// pdStockRecordDetailService.stockInAndOutRecordDetailQuery(stockTotalPage);
		 if(list != null && list.size() > 0){
			/* for (PdStockRecordDetail item : list) {
					 if(PdConstant.STOCK_RECORD_TYPE_IN.equals(item.getRecodeType())){
					 productTotNum += item.getProductNum();
					 if(item.getInPrice() != null){
						 inPrice = inPrice.add(item.getInPrice());
					 }
				 }else if(PdConstant.STOCK_RECORD_TYPE_OUT.equals(item.getRecodeType())){
					 productOutTotNum += item.getProductNum();
					 if(item.getOutPrice() != null){
						 outPrice = outPrice.add(item.getOutPrice());
					 }
				 }
			 }*/
		 }

		 Map map=new HashMap();
		 map.put("page",page);
		 map.put("productTotNum",productTotNum);//入库总数量
		 map.put("productOutTotNum",productOutTotNum);//出库总数量
		 map.put("inPrice",inPrice);//入库总金额
		 map.put("outPrice",outPrice);//出库总金额
		 return Result.ok(map);
	 }


	 /**
	  * 分页查询库存明细信息  mcb  --20200217   用于统计查询库存明细信息
	  *
	  * @param stockTotalPage
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @GetMapping(value = "/queryList")
	 public Result<?> queryList(PdProductStockTotalPage stockTotalPage,
											 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
											 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
											 HttpServletRequest req) {
		 Page<PdProductStock> page = new Page<PdProductStock>(pageNo, pageSize);
		 page = pdProductStockService.selectList(page,stockTotalPage);
		 return Result.ok(page);
	 }
    /**
    * 导出excel
    *
    * @param request
    * @param pdProductStockTotal
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdProductStockTotal pdProductStockTotal) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<PdProductStockTotal> queryWrapper = QueryGenerator.initQueryWrapper(pdProductStockTotal, request.getParameterMap());
      LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //Step.2 获取导出数据
      List<PdProductStockTotal> queryList = pdProductStockTotalService.list(queryWrapper);
      // 过滤选中数据
      String selections = request.getParameter("selections");
      List<PdProductStockTotal> pdProductStockTotalList = new ArrayList<PdProductStockTotal>();
      if(oConvertUtils.isEmpty(selections)) {
          pdProductStockTotalList = queryList;
      }else {
          List<String> selectionList = Arrays.asList(selections.split(","));
          pdProductStockTotalList = queryList.stream().filter(item -> selectionList.contains(item.getId())).collect(Collectors.toList());
      }

      // Step.3 组装pageList
      List<PdProductStockTotalPage> pageList = new ArrayList<PdProductStockTotalPage>();
      for (PdProductStockTotal main : pdProductStockTotalList) {
          PdProductStockTotalPage vo = new PdProductStockTotalPage();
          BeanUtils.copyProperties(main, vo);
          List<PdProductStock> pdProductStockList = pdProductStockService.selectByMainId(main.getId());
          vo.setPdProductStockList(pdProductStockList);
          pageList.add(vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      mv.addObject(NormalExcelConstants.FILE_NAME, "库存总表列表");
      mv.addObject(NormalExcelConstants.CLASS, PdProductStockTotalPage.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("库存总表数据", "导出人:"+sysUser.getRealname(), "库存总表"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
    }

}