<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="申购编号">
              <a-input placeholder="请输入申购编号" v-model="queryParam.orderNo"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="申购科室名称">
               <a-select
                showSearch
                :departId="departValue"
                :defaultActiveFirstOption="false"
                :allowClear="true"
                :showArrow="true"
                :filterOption="false"
                @search="departHandleSearch"
                @change="departHandleChange"
                @focus="departHandleSearch"
                :notFoundContent="notFoundContent"
                v-model="queryParam.departId"
                placeholder="请选择科室"
              >
                <a-select-option v-for="d in departData" :key="d.value">{{d.text}}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col  :md="6" :sm="8">
            <a-form-item label="申购日期">
              <a-range-picker @change="rejectedDateChange" v-model="queryParam.queryDate"/>
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :md="6" :sm="8">
              <a-form-item label="审核状态">
                <a-select v-model="queryParam.auditStatus" placeholder="请选择审核状态">
                  <a-select-option value="1">待审核</a-select-option>
                  <a-select-option value="2">审核通过</a-select-option>
                  <a-select-option value="3">已驳回</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->
    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="batchAduit('1')" type="primary" icon="plus">批量审核</a-button>
      <a-divider type="vertical" />
      <a-button @click="batchAduit('2')" type="primary" icon="plus">合并审核</a-button>
      <a-divider type="vertical" />
      <a-button @click="batchAduit('3')" type="primary" icon="plus">批量拒绝</a-button>

      <!--<a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="onClearSelected"><a-icon type="delete"/>删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>-->
    </div>


    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :customRow="onClickRow"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
         @change="handleTableChange">
        <span slot="action" slot-scope="text, record">
          <a v-if="record.auditStatus=='1'" @click="handleEdit(record)">审核</a>&nbsp;&nbsp;&nbsp;
          <a href="javascript:;" @click="handleDetail(record)">详情</a>
        </span>
      </a-table>
    </div>
    <pd-purchase-examine-modal ref="modalForm" @ok="modalFormOk"></pd-purchase-examine-modal>
  </a-card>
</template>

<script>
  import { JeecgListMixin,handleEdit} from '@/mixins/JeecgListMixin'
  import { deleteAction,httpAction,getAction } from '@/api/manage'
  import { filterObj } from '@/utils/util';
  import PdPurchaseExamineModal from './modules/PdPurchaseExamineModal'
  import JDictSelectTag from '@/components/dict/JDictSelectTag.vue'
  import {initDictOptions, filterMultiDictText} from '@/components/dict/JDictSelectUtil'

  let timeout;
  let currentValue;

  function fetch(value, callback,url) {
    if (timeout) {
      clearTimeout(timeout);
      timeout = null;
    }
    currentValue = value;

    function fake() {
      getAction(url,{departName:value}).then((res)=>{
        if (!res.success) {
          this.cmsFailed(res.message);
        }
        if (currentValue === value) {
          const result = res.result;
          const data = [];
          result.forEach(r => {
            data.push({
              value: r.id,
              text: r.departName,
            });
          });
          callback(data);
        }
      })
    }
    timeout = setTimeout(fake, 0);
  }
  export default {
    name: "PdPurchaseExamineList",
    mixins:[JeecgListMixin],
    components: {
      JDictSelectTag,
      PdPurchaseExamineModal
    },
    data () {
      return {
        description: '申购订单主表管理页面',
        departData: [],
        departValue: undefined,
        notFoundContent:"未找到内容",
        // 表头
        columns: [
          {
            title: '序号',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
          },
          {
            title:'申购编号',
            align:"center",
            dataIndex: 'orderNo'
          },
          {
            title:'申购人名称',
            align:"center",
            dataIndex: 'purchaseName'
          },
          {
            title:'申购日期',
            align:"center",
            dataIndex: 'orderDate',
            customRender:function (text) {
              return !text?"":(text.length>10?text.substr(0,10):text)
            }
          },
          {
            title:'申购科室',
            align:"center",
            dataIndex: 'deptName'

          },
          {
            title:'审核状态',
            align:"center",
            dataIndex: 'auditStatus',
            customRender:(text)=>{
              if(!text){
                return ''
              }else{
                return filterMultiDictText(this.dictOptions['auditStatus'], text+"")
              }
            }
          },
          {
            title:'申购总数量',
            align:"center",
            dataIndex: 'totalNum'
          },
          {
            title:'申购总金额',
            align:"center",
            dataIndex: 'totalPrice'
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
        url: {
          list: "/pd/pdPurchaseOrder/auditList",
          edit:"/pd/pdPurchaseOrderMerge/edit",
          delete: "/pd/pdPurchaseOrder/delete",
          deleteBatch: "/pd/pdPurchaseOrder/deleteBatch",
          queryDepart: "/pd/pdDepart/queryListTree",
        },
        dictOptions:{
          auditStatus:[],
         },
      }
    },
    computed: {

    },
    methods: {

      //科室查询start
      departHandleSearch(value) {
        fetch(value, data => (this.departData = data),this.url.queryDepart);
      },
      departHandleChange(value) {
        this.departValue = value;
        fetch(value, data => (this.departData = data),this.url.queryDepart);
      },
      //科室查询end


      rejectedDateChange (value, dateString) {
        this.queryParam.queryDateStart=dateString[0];
        this.queryParam.queryDateEnd=dateString[1];
      },

      getQueryParams() {
        //获取查询条件
        let sqp = {}
        if(this.superQueryParams){
          sqp['superQueryParams']=encodeURI(this.superQueryParams)
        }
        var param = Object.assign(sqp, this.queryParam, this.isorter ,this.filters);
        param.field = this.getQueryField();
        param.pageNo = this.ipagination.current;
        param.pageSize = this.ipagination.pageSize;
        delete param.queryDate; //范围参数不传递后台，传后台会报错
        return filterObj(param);
      },
         //批量审核
      batchAduit(oprtSource) {
        if (this.selectionRows.length <= 0) {
          this.$message.warning('请先选择申购单！');
          return;
        }else{
          var newOrderNos = "";
          var formData={};
          var orderNos="";
          for (let a = 0; a < this.selectionRows.length; a++) {
            let auditStatus= this.selectionRows[a].auditStatus;
            if(auditStatus!='1'){
              orderNos+=this.selectionRows[a].orderNo + ",";
            }else{
              newOrderNos += this.selectionRows[a].orderNo + ",";
            }
          }
          if(orderNos != ""){
            this.$message.warning("采购编号["+orderNos.substring(0,orderNos.length-1)+"]已提交过审核！")
            return
          }
          var msgName="合并并提交";
          formData.auditStatus="2";
          formData.submitStatus="2";
          formData.orderNos=newOrderNos;
          if(oprtSource=='1'){
            formData.auditStatus="2";//2:批量审核通过操作
            msgName="审核通过";
          }else if(oprtSource=='3'){
            formData.auditStatus="3";//3:批量审核拒绝操作
            msgName="拒绝";
            formData.submitStatus="1";
          }

          formData.oprtSource=oprtSource;
          var that = this;
          this.$confirm({
            title: "审批提醒",
            content: "确认是否"+msgName+"选择的订单吗?",
            onOk: function () {
              that.loading = true;
              httpAction(that.url.edit, formData, "put").then((res) => {
                if (res.success) {
                  that.$message.success(res.message);
                  that.loadData();
                  that.onClearSelected();
                  that.$emit('ok');
                } else {
                  that.$message.warning(res.message);
                }
              }).finally(() => {
                that.confirmLoading = false;
                that.close();
              })
            }
          });
        }
      },
      initDictConfig(){ //静态字典值加载
        initDictOptions('audit_status').then((res) => {
          if (res.success) {
            this.$set(this.dictOptions, 'auditStatus', res.result)
          }
        })
      },
      onSelectChange (selectedRowKeys) {
        this.selectedRowKeys = selectedRowKeys;
      },

     /* onSelect(record, selected){
        if(selected == true ){
          this.selectionRows.push(record);
        }else {
          this.selectionRows.forEach(function(item,index,arr){
            if(item.id == record.id) {
              arr.splice(index, 1);
            }
          })
        }
        //--update-end----author:kangxiaolin---date:20190921------for:系统发送通知 用户多选失败 #513----
      },*/
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
              let index = this.selectedRowKeys.indexOf(recordId);
              if(index>=0){
                this.selectedRowKeys.splice(index, 1);
                this.selectionRows.splice(index, 1);
              }else{
                this.selectedRowKeys.push(recordId);
                this.selectionRows.push(record);
              }
            }
          }
        }
      }
    }
  }
       
    }
  }
</script>
<style scoped>
</style>