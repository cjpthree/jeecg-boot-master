<template>
  <j-modal
    :visible="visible"
    :width="popModal.width"
    :maskClosable="disableSubmit"
    :title="popModal.title"
    :lockScroll="popModal.lockScroll"
    :fullscreen="popModal.fullscreen"
    :switchFullscreen="popModal.switchFullscreen"
    @cancel="handleCancel"
  >


    <a-spin :spinning="confirmLoading">
      <div style="background:#ECECEC; padding:20px">
      <a-card title="" style="margin-bottom: 10px;">
        <a-form :form="form">

          <a-row class="form-row" :gutter="{ xs: 8, sm: 16, md: 24, lg: 32 }">
            <a-col :lg="12">
              <a-form-item label="检验项目类型" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <j-dict-select-tag-expand  :disabled="disableSubmit" :trigger-change="true" dictCode="inspection_item_type" v-decorator="['itemType',validatorRules.itemType]"  placeholder="请选择检验项目类型"/>
              </a-form-item>
            </a-col>
            <a-col :lg="12">
              <a-form-item label="关联病人信息" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-input :disabled="true" v-decorator="[ 'refName']" placeholder="请关联病人信息"></a-input>
                <a-button @click="choice"  v-show="!disableSubmit"  type="primary">关联病人信息</a-button>
              </a-form-item>
              <a-form-item v-show="false" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-input  v-decorator="['refId']"/>
              </a-form-item>
            </a-col>
          </a-row>

        </a-form>
      </a-card>

      <!-- 检验项目包区域 -->
      <a-card style="margin-bottom: 10px;" v-show="showPackageCard">
        <a-tabs v-model="activeKey">
          <a-tab-pane tab="检验项目包明细" :key="refKeys[0]" :forceRender="true">
            <div style="margin-bottom: 8px;" v-show="showPackageBtn">
              <a-button type="primary" icon="plus" @click="choosePackageList">选择检验项目</a-button>
              <a-popconfirm style="margin-left: 8px"
                            :title="`确定要删除吗?`"
                            @confirm="handleConfirmDelete">
                <a-button type="primary" icon="minus">删除</a-button>
                <span class="gap"></span>
              </a-popconfirm>
            </div>
            <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
              <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ pdPackageTable.selectedRowKeys.length }}</a>项
              <a style="margin-left: 24px" @click="onClearSelected">清空</a>
            </div>
            <a-table
              v-show="showPackageTable"
              ref="table"
              bordered
              rowKey="id"
              :pagination="false"
              :columns="pdPackageTable.columns"
              :dataSource="pdPackageTable.dataSource"
              :loading="pdPackageTable.loading"
              :customRow="onClickRow"
              :rowSelection="{selectedRowKeys: pdPackageTable.selectedRowKeys,onChange: onSelectChange}"
            >
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </a-card>

        <!-- 产品列表区域 -->
        <a-card style="margin-bottom: 10px;">
          <a-tabs v-model="activeKey">
            <a-tab-pane tab="产品明细" :key="refKeys[0]" :forceRender="true">
              <a-form v-show="!disableSubmit">
                <a-row>
                  <a-col :md="6" :sm="8">
                    <a-form-item label="产品编号" :labelCol="labelCol" :wrapperCol="wrapperCol">
                      <a-input ref="productNumberInput" v-focus placeholder="请输入产品编号" v-model="queryParam.productNumber" @keyup.enter.native="searchQuery(0)"></a-input>
                    </a-form-item>
                  </a-col>
                  <a-col :md="6" :sm="8">
                    <a-form-item label="二级条码" :labelCol="labelCol" :wrapperCol="wrapperCol">
                      <a-input ref="productBarCodeInput" placeholder="请输入二级条码" v-model="queryParam.productBarCode" @keyup.enter.native="searchQuery(1)"></a-input>
                    </a-form-item>
                  </a-col>
                  <a-col :md="12" :sm="8">
                    <a-form-item label="" :labelCol="labelCol" :wrapperCol="wrapperCol" style="text-align: left;padding-left: 15px;">
                      提示：按<span style="color: red">Ctrl+Alt</span>键快速定位到产品扫码输入框
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form>

              <div style="margin-bottom: 8px;" v-show="!disableSubmit">
                <a-button type="primary" icon="plus" @click="chooseProductList">选择产品</a-button>
                <a-popconfirm style="margin-left: 8px"
                              :title="`确定要删除吗?`"
                              @confirm="stockHandleConfirmDelete">
                  <a-button type="primary" icon="minus">删除</a-button>
                  <span class="gap"></span>
                </a-popconfirm>
              </div>

              <j-editable-table
                bordered
                :ref="refKeys[0]"
                :loading="exInspectionItemsUseDetailTable.loading"
                :columns="exInspectionItemsUseDetailTable.columns"
                :dataSource="exInspectionItemsUseDetailTable.dataSource"
                :maxHeight="650"
                :rowNumber="true"
                :rowSelection="true"
                :actionButton="false"
                :disabled="disableSubmit"
                @valueChange="valueChange"
                style="text-overflow: ellipsis;"
              >
              </j-editable-table>

            </a-tab-pane>
          </a-tabs>
        </a-card>

        <a-card style="">
          <a-form :form="form">
            <a-row>
              <a-col :span="12">
                <a-form-item label="备注" :labelCol="labelCol2" :wrapperCol="wrapperCol2" style="text-align: left">
                  <a-textarea :disabled="disableSubmit" v-decorator="[ 'remarks', validatorRules.remarks]" placeholder="请输入备注"></a-textarea>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

      </div>
    </a-spin>

    <template slot="footer">
      <a-button @click="close" style="margin-right: 15px;" v-show="disableSubmit">关  闭</a-button>
      <a-popconfirm title="确定放弃编辑？" @confirm="handleCancel" v-show="!disableSubmit" okText="确定" cancelText="取消">
        <a-button style="margin-right: 15px;">取  消</a-button>
      </a-popconfirm>
      <a-button @click="handleOk" v-show="!disableSubmit" type="primary" :loading="confirmLoading" style="margin-right: 15px;">提  交</a-button>
    </template>

    <ex-inspection-items-add-modal ref="exInspectionItemsAddModal" @ok="modalFormOk"></ex-inspection-items-add-modal>
    <ex-choose-package-exInspection-items-list-model ref="exChoosePackageExInspectionItemsListModel" @ok="returnPackageRecordData" ></ex-choose-package-exInspection-items-list-model>
    <pd-choose-product-stock-list-model ref="pdChooseProductStockListModel" @ok="returnProductStockData" ></pd-choose-product-stock-list-model>
  </j-modal>
</template>

<script>

  import Vue from 'vue'
  import { httpAction } from '@/api/manage';
  import pick from 'lodash.pick';
  import JEditableTable from '@/components/jeecg/JEditableTable'
  import { JEditableTableMixin } from '@/mixins/JEditableTableMixin'
  import JDictSelectTagExpand from "@/components/dict/JDictSelectTagExpand";
  import {initDictOptions} from '@/components/dict/JDictSelectUtil';
  import ExChoosePackageExInspectionItemsListModel from "./ExChoosePackageExInspectionItemsListModel";
  import ExInspectionItemsAddModal from "./ExInspectionItemsAddModal";
  import { randomUUID,validateDuplicateValue } from '@/utils/util';
  import {stockScanCode} from '@/utils/barcode';
  import { FormTypes,getRefPromise,validateFormAndTables } from '@/utils/JEditableTableUtil';
  import PdChooseProductStockListModel from "../../pd/modules/PdChooseProductStockListModel";

  const VALIDATE_NO_PASSED = Symbol()
  export { FormTypes, VALIDATE_NO_PASSED }

  export default {
    name: "ExInspectionItemsUseModal",
    components: {
      JDictSelectTagExpand,
      ExChoosePackageExInspectionItemsListModel,
      PdChooseProductStockListModel,
      JEditableTable,
      JEditableTableMixin,
      ExInspectionItemsAddModal,
    },
    data () {
      return {
        form: this.$form.createForm(this),
        title:"操作",
        width:800,
        visible: false,
        showPackageTable:false,
        model: {},
        pdPackageTable: {
          selectedRowKeys: [],
          selectionRows: [],
          dataSource: [],
          columns:[
            {
              title: '#',
              dataIndex: 'id',
              key:'rowIndex',
              width:60,
              align:"center",
              customRender:function (t,r,index) {
                return parseInt(index)+1;
              }
            },
            { title:'检验项目包编号', align:"center", dataIndex: 'code' },
            { title:'检验项目包名称', align:"center", dataIndex: 'name' },
            { title:'产品名称', align:"center", dataIndex: 'productName' },
            { title:'产品编号', align:"center", dataIndex: 'productNumber' },
            { title:'规格', align:"center", dataIndex: 'spec' },
            { title:'单位', align:"center", dataIndex: 'unitName' },
            { title:'规格单位', align:"center", dataIndex: 'specUnitName' },
            { title:'规格数量', align:"center", dataIndex: 'specQuantity' },
            { title:'生产厂家', align:"center", dataIndex: 'venderName' },
            { title:'供应商', align:"center", dataIndex: 'supplierName' },
            { title:'产品类型', align:"center", dataIndex: 'productFlagName' },
            { title:'用量', align:"center", dataIndex: 'count' },
          ],
        },
        // 出入库明细表(产品明细)  , type: FormTypes.hidden
        exInspectionItemsUseDetailTable: {
          loading: false,
          dataSource: [],
          columns: [
            { title: '产品名称', key: 'productName', type: FormTypes.normal,width:"220px" },
            { title: '产品编号', key: 'productNumber', width:"160px" },
            { title: '产品条码', key: 'productBarCode', type: FormTypes.input, disabled:true, width:"200px" },
            { title: '规格', key: 'spec', width:"220px" },
            { title: '批号', key: 'batchNo', width:"100px" },
            { title: '单位', key: 'unitName', width:"50px" },
            { title: '有效期', key: 'expDate', width:"100px" },
           /* { title: '入库单价', key: 'purchasePrice', width:"80px" },*/
            { title: '出库单价', key: 'sellingPrice', type: FormTypes.input, disabled:true, width:"80px" },
            {
              title: '用量', key: 'productNum', type: FormTypes.input, width:"80px",
              placeholder: '${title}', defaultValue: '1',
              validateRules: [{ required: true, message: '${title}不能为空' },{ pattern: '^-?\\d+\\.?\\d*$',message: '${title}的格式不正确' }]
            },
            /*{ title: '出库金额', key: 'outTotalPrice', type: FormTypes.input, disabled:true, width:"100px" },*/
            { title: '库存数量', key: 'stockNum', width:"80px" },
            { title: '规格单位', key: 'specUnitName', width:"80px" },
            { title: '规格数量', key: 'specQuantity', width:"80px" },
            { title: '出库货位', key: 'outHuoweiName', width:"100px" },
            { title: '产品类型', key: 'productFlagName', width:"100px" },
            /*{ title: '生产日期', key: 'produceDate',  },
            { title: '入库货位', key: 'inHuoweiCode', type: FormTypes.select, width:"150px", options: [],allowSearch:true, placeholder: '${title}' },*/

            { title: '库存明细ID', key: 'productStockId', type: FormTypes.hidden },
            { title: '产品ID', key: 'productId', type: FormTypes.hidden },
            { title: '出库货位编号', key: 'outHuoweiCode', type: FormTypes.hidden },
            { title: '供应商id', key: 'supplierId', type: FormTypes.hidden },
            { title: '规格单位ID', key: 'specUnitId', type: FormTypes.hidden },
            { title: '产品类型', key: 'productFlag', type: FormTypes.hidden },
          ]
        },
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        labelCol2: {span: 3},
        wrapperCol2: {span: 20},
        confirmLoading: false,
        disableSubmit:false,
        showPackageCard:true,
        showPackageBtn:true,
        queryParam:{},
        refKeys: ['exInspectionItemsUseDetail',],
        tableKeys:['exInspectionItemsUseDetail', ],
        activeKey: 'exInspectionItemsUseDetail',
        validatorRules: {
          refId: {rules: [
          ]},
          itemType: {rules: [{required: true, message: '请选择检验项目类型!'}]},
        },
        url: {
          add: "/external/exInspectionItemsUse/add",
          submit: "/external/exInspectionItemsUse/submit",
          edit: "/external/exInspectionItemsUse/edit",
        },
        popModal: {
          title: '这里是标题',
          visible: false,
          width: '100%',
          // width: '1200',
          style: { top: '20px' },
          switchFullscreen: true,  //缩放按钮
          lockScroll: false,
          fullscreen: true,
        },
        dictOptions:{
          itemType:[],
        },
      }
    },
    created () {
    },
    directives: {
      focus: {
        // 当被绑定的元素插入到 DOM 中时……
        inserted: function (el) {
          //全局监听键盘事件
          document.onkeydown = function (event) {
            if (event.ctrlKey && event.altKey) {
              // 按ctrl+alt  聚焦元素
              el.focus();
              return;
            }
          }
        }
      },
    },
    methods: {
      add () {
        this.edit({});
      },
      edit (record) {
        this.form.resetFields();
        this.model = Object.assign({}, record);
        this.visible = true;
        this.$nextTick(() => {
          this.form.setFieldsValue(pick(this.model,'refId','itemType','refName'))
        })
      },
      close () {
        this.$emit('close');
        this.visible = false;
        this.pdPackageTable.dataSource = [];
        this.exInspectionItemsUseDetailTable.dataSource = [];
        this.pdPackageTable.selectedRowKeys = [];
        this.pdPackageTable.selectionRows = [];
      },
      handleOk () {
        this.request(this.url.submit,"post");
      },
      /** 确定按钮点击事件 */
      request(url, method) {
        /** 触发表单验证 */
        this.getAllTable().then(tables => {
          /** 一次性验证主表和所有的次表 */
          return validateFormAndTables(this.form, tables)
        }).then(allValues => {
          if (typeof this.classifyIntoFormData !== 'function') {
            throw this.throwNotFunction('classifyIntoFormData')
          }
          let formData = this.classifyIntoFormData(allValues);

          let dataSource = this.pdPackageTable.dataSource;
          if(dataSource.length>0){
            let selectedArrays = this.pdPackageTable.selectedRowKeys;
            if(selectedArrays.length <= 0){
              this.$message.warning("请勾选检验项目中的产品");
              return;
            }else{
              formData.pdUsePackageDetails = [];
              for (let data of this.pdPackageTable.dataSource){
                formData.pdUsePackageDetails.push(data);
              }
            }
          }else{
            if(formData.exInspectionItemsUseDetails.length <= 0){
              this.$message.warning("产品数据为空，请扫码出库、选择产品");
              return;
            }
          }
          let list = formData.exInspectionItemsUseDetails;
          for (let item of list){
            console.log(item)
            if(item.productFlag == 0){
              if(Number(item.productNum) > Number(item.stockNum)){
                this.$message.error("["+item.productName+"]用量不能大于库存数量！");
                return;
              }
            }else{
              if(Number(item.productNum) > Number(item.specQuantity)){
                this.$message.error("["+item.productName+"]用量不能大于规格数量！");
                return;
              }
            }

            if(Number(item.productNum) <= 0){
              this.$message.error("["+item.productName+"]用量必须大于0！");
              return;
            }
          }
          // 发起请求
          this.confirmLoading = true
          httpAction(url, formData, method).then((res) => {
            if (res.success) {
              this.$message.success(res.message)
              this.$emit('ok');
              this.close();
            } else {
              this.$message.warning(res.message)
            }
          }).finally(() => {
            this.confirmLoading = false
          })
        }).catch(e => {
          if (e.error === VALIDATE_NO_PASSED) {
            // 如果有未通过表单验证的子表，就自动跳转到它所在的tab
            this.activeKey = e.index == null ? this.activeKey : this.refKeys[e.index]
          } else {
            console.error(e)
          }
        })
      },
      handleCancel () {
        this.close()
      },
      popupCallback(row){
        this.form.setFieldsValue(pick(row,'refId','itemType','refName'))
      },
      initDictConfig(){
        initDictOptions('inspection_item_type').then((res) => {
          if (res.success) {
            this.$set(this.dictOptions, 'itemType', res.result)
          }
        })
      },
      // 选择检验项目包
      choosePackageList() {
        this.$refs.exChoosePackageExInspectionItemsListModel.show();
      },
      //检验项目选中事件
      onSelectChange(selectedRowKeys, selectionRows) {
        this.pdPackageTable.selectedRowKeys = selectedRowKeys;
        this.pdPackageTable.selectionRows = selectionRows;
      },
      //检验项目清空选择事件
      onClearSelected() {
        this.pdPackageTable.selectedRowKeys = [];
        this.pdPackageTable.selectionRows = [];
      },
      //检验项目删除行
      handleConfirmDelete() {
        let ids = this.pdPackageTable.selectedRowKeys;
        if(ids.length > 0){
          let dataSource = this.pdPackageTable.dataSource;
          for(let index =0;index<dataSource.length;index++){
            let item = dataSource[index];
            let i = ids.indexOf(item.id);
            if(i>=0){
              delete dataSource[index];
              delete ids[i];
            }
          }
          dataSource = dataSource.filter(function (val) {
            return val
          });
          ids = ids.filter(function (val) {
            return val
          });
          this.pdPackageTable.dataSource = dataSource;
          this.pdPackageTable.selectedRowKeys = ids;
        }else{
          this.$message.error("请选择需要删除的数据！")
        }
      },
      // 选择检验项目包后返回
      returnPackageRecordData(data){
        if(data.pdPdUsePackageList.length > 0){
          let pdPdUsePackage = data.pdPdUsePackageList[0];
          let pdUsePackageDetailList = pdPdUsePackage.pdUsePackageDetailList;
          if(pdUsePackageDetailList!=null && pdUsePackageDetailList.length>0){
            this.showPackageTable = true;
            this.pdPackageTable.dataSource = [];
            for(let item of  pdUsePackageDetailList){
              let data = {
                packageId:pdPdUsePackage.id,
                code:pdPdUsePackage.code,
                name:pdPdUsePackage.name,
                id:item.productId,
                productName:item.productName,
                productNumber:item.productNumber,
                spec:item.spec,
                unitName:item.unitName,
                specUnitName:item.specUnitName,
                specQuantity:item.specQuantity,
                venderName:item.venderName,
                supplierName:item.supplierName,
                productFlagName:item.productFlagName,
                count:item.count
              }
              console.log(data);
              this.pdPackageTable.dataSource.push(data);
            }
          }

        }
      },
      /**
       * 点击行选中checkbox
       * @param record
       * @returns {{on: {click: on.click}}}
       */
      onClickRow(record) {
        return {
          on: {
            click: (e) => {
              //点击操作那一行不选中表格的checkbox
              let pathArray = e.path;
              //获取当前点击的是第几列
              let td = pathArray[0];
              let cellIndex = td.cellIndex;
              //获取tr
              let tr = pathArray[1];
              //获取一共多少列
              let lie = tr.childElementCount;
              if(lie && cellIndex){
                if(parseInt(lie)-parseInt(cellIndex)!=1){
                  //操作那一行
                  let recordId = record.id;
                  let index = this.pdPackageTable.selectedRowKeys.indexOf(recordId);
                  if(index>=0){
                    this.pdPackageTable.selectedRowKeys.splice(index, 1);
                  }else{
                    this.pdPackageTable.selectedRowKeys.push(recordId);
                  }
                }
              }
            }
          }
        }
      },
      // 扫码查询
      searchQuery(num) {
        let productNumber = this.queryParam.productNumber;
        if(!productNumber){
          //清空扫码框
          this.clearQueryParam();
          this.$message.error("请输入产品编号！");
          return;
        }

        if(num == 0){       //产品编号扫码
          // 焦点条码输入框
          this.$refs.productBarCodeInput.focus();

        }else if(num == 1){ //条码扫码
          let productBarCode = this.queryParam.productBarCode;
          if(!productBarCode){
            this.$message.error("请输入二级条码！");
            return;
          }
          //解析条码
          stockScanCode(productNumber,productBarCode).then((res) => {
            if(res.code == "200" || res.code == "203"){
              let pdProductStockList = res.result;
              if(!pdProductStockList){
                //清空扫码框
                this.clearQueryParam();
                this.$message.error("条码解析失败，请校验条码是否正确！");
                return;
              }
              if(pdProductStockList.length <= 0){
                //清空扫码框
                this.clearQueryParam();
                this.$message.error("库存中没有该产品！");
                return;
              }

              let { values } = this.$refs.exInspectionItemsUseDetail.getValuesSync({ validate: false });
              for(let pdProductStock of pdProductStockList){
                let isAddRow = true;// 是否增加一行
                // 循环表格数据
                if(values.length > 0) { //表格有数据
                  for(let item of values){
                    if(pdProductStock.id == item.productStockId){// 库存明细ID一致，就+1
                      isAddRow = false;
                      if(item.productFlag == 0){
                        if(Number(item.productNum) + 1 > Number(item.stockNum)){
                          //清空扫码框
                          this.clearQueryParam();
                          this.$message.error("["+item.productName+"]用量不能大于库存数量！");
                          return;
                        }
                      }else{
                        if(Number(item.productNum) + 1 > Number(item.specQuantity)){
                          //清空扫码框
                          this.clearQueryParam();
                          this.$message.error("["+item.productName+"]用量不能大于规格数量！");
                          return;
                        }
                      }
                      let productNum = Number(item.productNum) + 1;
                      let outTotalPrice = (Number(item.sellingPrice) * Number(productNum)).toFixed(4);

                      this.$refs.exInspectionItemsUseDetail.setValues([{rowKey: item.id, values: {
                          productNum: productNum,outTotalPrice: outTotalPrice }}]);
                      break;
                    }
                  }
                }

                if(isAddRow){
                  this.exInspectionItemsUseDetailTable.dataSource = values;
                  //条码新增一行
                  this.addrows(pdProductStock);
                }
              }
              if(res.code == "203"){ // 近效期提醒
                this.$message.error(result.message);
              }
            }else if(res.code ==="201"){
              this.$message.error(res.message);
            }else{
              this.$message.error(res.message);
            }
            //清空扫码框
            this.clearQueryParam();
          })
        }
      },
      // 选择产品 新增行
      chooseProductList() {
        this.$refs.pdChooseProductStockListModel.show({});
      },
      // 表格数据变更
      valueChange(event) {
        if(event){
          const { type, row, column, value, target } = event;
          if(type === FormTypes.input){
            if(column.key === "productNum"){
              let { values } = target.getValuesSync({ validate: false });
              for(let item of values){
                if(item.productFlag == 0){
                  if(item.id == row.id && Number(value) > Number(item.stockNum)){
                    this.$message.error("["+row.productName+"]用量不能大于库存数量！");
                    // 产品数量变更 计算每条产品的价格
                    let outTotalPrice = (Number(row.sellingPrice) * Number(item.stockNum)).toFixed(4);
                    target.setValues([{rowKey: row.id, values: { outTotalPrice: outTotalPrice, productNum: item.stockNum }}])
                    return;
                  }
                }else{
                  if(item.id == row.id && Number(value) > Number(item.specQuantity)){
                    this.$message.error("["+row.productName+"]用量不能大于规格数量！");
                    // 产品数量变更 计算每条产品的价格
                    let outTotalPrice = (Number(row.sellingPrice) * Number(item.stockNum)).toFixed(4);
                    target.setValues([{rowKey: row.id, values: { outTotalPrice: outTotalPrice, productNum: item.specQuantity }}])
                    return;
                  }
                }

              }
              // 产品数量变更 计算每条产品的价格
              let outTotalPrice = (Number(row.sellingPrice) * Number(value)).toFixed(4);
              target.setValues([{rowKey: row.id, values: { outTotalPrice: outTotalPrice }}])
            }else if(column.key == "sellingPrice"){
              let outTotalPrice = (Number(row.productNum) * Number(value)).toFixed(4);
              target.setValues([{rowKey: row.id, values: { outTotalPrice: outTotalPrice }}])
            }
          }
        }
      },
      // 选择产品后返回
      returnProductStockData(data) {
        let rows = [];
        let { values } = this.$refs.exInspectionItemsUseDetail.getValuesSync({ validate: false });
        if(values.length > 0){
          // 如果列表中有相同产品则不加行
          data.forEach((item, idx) => {
            let bool = true;
            values.forEach((value, idx) => {
              if(item.id == value.productStockId){
                bool = false;
              }
            })
            if(bool){
              rows.push(item)
            }
          })
        }else{
          rows = data;
        }
        rows.forEach((item, idx) => {
          // j-editable-table表格（可能是BUG）：values变更 不会同步变更到dataSource，新增行时需要手动赋值到dataSource
          this.exInspectionItemsUseDetailTable.dataSource = values;
          this.addrows(item);
        })
      },
      // 点“选择产品”按钮后 调用 新增一行
      addrows(row){
        console.log(row)
        let data = {
          productStockId:row.id,
          productId: row.productId,
          productName: row.productName,
          productNumber:row.number,
          productBarCode:row.productBarCode,
          spec: row.spec,
          batchNo:row.batchNo,
          unitName:row.unitName,
          expDate:row.expDate,
          sellingPrice:row.sellingPrice,
          specUnitId: row.specUnitId,
          specQuantity: row.specQuantity,
          productNum: 1,
          /*purchasePrice:row.purchasePrice,*/
          outTotalPrice:Number(!row.sellingPrice ? 0 : row.sellingPrice).toFixed(4),
          stockNum:row.stockNum,
          outHuoweiName:row.huoweiName,
          outHuoweiCode:row.huoweiCode,
          productFlag:row.productFlag,
          productFlagName:row.productFlag==0?"耗材":"试剂",
          specUnitName:row.specUnitName,
          specQuantity:row.specQuantity,
          /*inHuoweiCode:"",*/
          supplierId:row.supplierId,
          /*produceDate:row.produceDate*/
        }
        this.exInspectionItemsUseDetailTable.dataSource.push(data);
      },
      //清空扫码框
      clearQueryParam(){
        this.queryParam = {};
        this.$refs.productNumberInput.focus();
      },
      //删除行
      stockHandleConfirmDelete() {
        if(this.$refs.exInspectionItemsUseDetail.selectedRowIds.length > 0){
          this.$refs.exInspectionItemsUseDetail.removeSelectedRows();
        }else{
          this.$message.error("请选择需要删除的数据！")
        }
      },
      getAllTable() {
        let values = this.tableKeys.map(key => getRefPromise(this, key))
        return Promise.all(values)
      },
      /** 整理成formData */
      classifyIntoFormData(allValues) {
        let main = Object.assign(this.model, allValues.formValue)

        return {
          ...main, // 展开
          exInspectionItemsUseDetails: allValues.tablesValue[0].values,
        }
      },
      throwNotFunction(name) {
        return `${name} 未定义或不是一个函数`
      },
      //选择标识符
      choice() {
        this.$refs.exInspectionItemsAddModal.show();
        this.$refs.exInspectionItemsAddModal.title = "关联病人信息";
      },
      modalFormOk (formData) {
        var info = formData[0];
        this.form.setFieldsValue({refId:info.id});
        this.form.setFieldsValue({refName:info.patientName});
      },
    }
  }
</script>
