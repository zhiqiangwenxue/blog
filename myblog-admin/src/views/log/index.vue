<template>
  <div class="app-container">

    <el-table v-loading="listLoading" :default-sort = "{prop: 'id', order: 'descending'}" :data="list" border fit highlight-current-row style="width: 100%" @sort-change="sortChange">
      <el-table-column align="center" label="ID" prop="id" width="150" sortable/>

      <el-table-column align="center" label="IP地址" min-width="100" prop="ip"/>

      <el-table-column align="center" label="所在城市" min-width="100" prop="city"/>

      <el-table-column align="center" label="访问链接" min-width="100">
        <template slot-scope="scope">
          <a :href="scope.row.url" style="color: #337ab7;" target="_blank">{{ scope.row.url }}</a>
        </template>
      </el-table-column>
      <el-table-column align="center" label="来源" min-width="100" prop="referer"/>
      <el-table-column :formatter="formatTime" width="150" align="center" sortable="custom" label="访问时间" prop="createTime"/>
      <el-table-column align="center" label="接口响应时长（单位：毫秒）" min-width="100" prop="duration"/>
      <el-table-column align="center" label="访问类型" min-width="100" prop="type"/>
      <el-table-column align="center" label="执行的方法" min-width="100" prop="method"/>
      <el-table-column align="center" label="请求是否正常" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isNormal===false? 'danger' : 'Info'">{{ scope.row.isNormal===false?'异常':'正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="浏览器" min-width="100" prop="browserName"/>
      <el-table-column align="center" label="操作系统" min-width="100" prop="operatingSystemName"/>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.current" :limit.sync="listQuery.size" @pagination="handleSizeChange" />

  </div>
</template>

<script>

import { getLogList } from '@/api/log'
import Pagination from '@/components/Pagination'
import { parseTime } from '@/utils'
export default {
  name: 'LinkList',
  components: { Pagination },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      addOrUpdateVisible: false,
      listQuery: {
        current: 1,
        size: 10,
        ascs: undefined,
        descs: undefined
      }
    }
  },
  // created() {
  //   this.getList()
  // },
  methods: {
    getList() {
      this.listLoading = true
      getLogList(this.listQuery).then(response => {
        if (response.data) {
          this.list = response.data.records
          this.total = response.data.total
        }
        this.listLoading = false
      })
    },

    // 格式化时间
    formatTime(row, column, cellValue) {
      return parseTime(cellValue)
    },

    // 分页
    handleSizeChange(val) {
      console.log(val)
      this.listQuery.size = val.limit
      this.getList()
    },
    // 排序
    sortChange(data) {
      if (data.order === 'ascending') {
        this.listQuery.descs = undefined
        this.listQuery.ascs = data.prop.replace(/([A-Z])/g, '_$1').toLowerCase()
      } else {
        this.listQuery.ascs = undefined
        this.listQuery.descs = data.prop.replace(/([A-Z])/g, '_$1').toLowerCase()
      }
      this.getList()
    }
  }
}
</script>
<style scoped>
.filter-container{
  float: right;
  margin-bottom: 15px;
}
</style>

